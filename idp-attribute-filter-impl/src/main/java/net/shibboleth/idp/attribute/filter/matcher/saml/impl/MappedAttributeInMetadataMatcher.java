/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.attribute.filter.matcher.saml.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.saml.attribute.mapping.AttributesMapContainer;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.navigate.AttributeConsumerServiceLookupFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Matcher that checks whether an attribute is enumerated in an SP's metadata as a required or optional attribute.
 * Also supports simple value filtering.
 * 
 * <p>This matcher compares attributes from the metadata that have been mapped into {@link IdPAttribute}s.</p> 
 */
public class MappedAttributeInMetadataMatcher extends AbstractIdentifiableInitializableComponent implements Matcher {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(MappedAttributeInMetadataMatcher.class);

    /** Whether optionally requested attributes should be matched. */
    private boolean onlyIfRequired = true;

    /** Whether to return a match if the metadata does not contain an ACS descriptor. */
    private boolean matchIfMetadataSilent;

    /** The String used to prefix log message. */
    private String logPrefix;

    /** The strategy to get the appropriate XMLObject from the context. */
    @Nonnull private Function<SAMLMetadataContext, ? extends XMLObject> objectStrategy;
    
    /** Constructor. */
    public MappedAttributeInMetadataMatcher() {
        objectStrategy = Functions.compose(new AttributeConsumerServiceLookupFunction(),
                new ChildContextLookup<SAMLMetadataContext,AttributeConsumingServiceContext>(
                        AttributeConsumingServiceContext.class));
    }

    /**
     * Gets whether optionally requested attributes should be matched.
     * 
     * @return Whether optionally requested attributes should be matched.
     */
    public boolean getOnlyIfRequired() {
        return onlyIfRequired;
    }

    /**
     * Sets whether optionally requested attributes should be matched.
     * 
     * @param flag whether optionally requested attributes should be matched
     */
    public void setOnlyIfRequired(final boolean flag) {
        onlyIfRequired = flag;
    }

    /**
     * Get the strategy to get the appropriate XMLObject from the context.
     * 
     * @return Returns the strategy.
     */
    public Function<SAMLMetadataContext, ? extends XMLObject> getXMLObjectStrategy() {
        return objectStrategy;
    }

    /**
     * Set the strategy to get the appropriate XMLObject from the context.
     * 
     * @param strategy what to set.
     */
    public void setObjectStrategy(@Nonnull final Function<SAMLMetadataContext, ? extends XMLObject> strategy) {
        objectStrategy = Constraint.isNotNull(strategy, "ObjectStrategy must be non null");
    }

    /**
     * Gets whether to matched if the metadata contains no AttributeConsumingService.
     * 
     * @return whether to match if the metadata contains no AttributeConsumingService
     */
    public boolean getMatchIfMetadataSilent() {
        return matchIfMetadataSilent;
    }

    /**
     * Sets whether to match if the metadata contains no AttributeConsumingService.
     * 
     * @param flag whether to match if the metadata contains no AttributeConsumingService
     */
    public void setMatchIfMetadataSilent(final boolean flag) {
        matchIfMetadataSilent = flag;
    }

    /**
     * Get the appropriate map of mapped attributes.
     * 
     * @param filterContext the context for the operation
     * @return the map, or null.
     */
    @Nullable protected Multimap<String, ? extends IdPAttribute> getRequestedAttributes(
            @Nonnull final AttributeFilterContext filterContext) {
        final SAMLMetadataContext metadataContext = filterContext.getRequesterMetadataContext();
        if (null == metadataContext) {
            log.warn("{} No metadata context when filtering", getLogPrefix());
            return null;
        }

        final XMLObject xmlObject = getXMLObjectStrategy().apply(metadataContext);
        if (null == xmlObject) {
            log.warn("{} No RP XML Object found when filtering", getLogPrefix());
            return null;
        }
        final List<AttributesMapContainer> containerList =
                xmlObject.getObjectMetadata().get(AttributesMapContainer.class);
        if (null == containerList || containerList.isEmpty()) {
            log.debug("{} No mapped attributes found when filtering", getLogPrefix());
            return null;
        }
        if (containerList.size() > 1) {
            log.warn("{} More than one set of mapped attributes found when filtering, please report an error",
                    getLogPrefix());
        }
        return containerList.get(0).get();
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull final IdPAttribute attribute,
            @Nonnull final AttributeFilterContext filterContext) {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        final Multimap<String, ? extends IdPAttribute> requestedAttributes = getRequestedAttributes(filterContext);

        if (null == requestedAttributes || requestedAttributes.isEmpty()) {
            if (matchIfMetadataSilent) {
                log.debug("{} The peer's metadata did not have appropriate requested attributes available"
                        + ", returning all the input values", getLogPrefix());
                return ImmutableSet.copyOf(attribute.getValues());
            } else {
                log.debug("{} The peer's metadata did not have appropriate requested attributes available"
                        + ", returning no values", getLogPrefix());
                return Collections.emptySet();
            }
        }

        final Collection<? extends IdPAttribute> requestedAttributeList = requestedAttributes.get(attribute.getId());

        if (null == requestedAttributeList) {
            log.debug("{} Attribute {} not found in metadata", getLogPrefix(), attribute.getId());
            return Collections.emptySet();
        }

        final Set<IdPAttributeValue<?>> values = new HashSet<>();

        for (final IdPAttribute requestedAttribute : requestedAttributeList) {

            if (null == requestedAttribute) {
                log.info("{} Attribute {} found in metadata but with no values that"
                        + " could be decoded, values not matched", getLogPrefix(), attribute.getId());
                continue;
            }

            if (requestedAttribute instanceof IdPRequestedAttribute
                    && !((IdPRequestedAttribute) requestedAttribute).getIsRequired() && onlyIfRequired) {
                log.debug("{} Attribute {} found in metadata, but was not required, values not matched",
                        getLogPrefix(), attribute.getId());
                continue;
            }

            values.addAll(filterValues(attribute, requestedAttribute.getValues()));
        }
        return values;
    }

    /**
     * Given an attribute and the requested values do the filtering.
     * 
     * @param attribute the attribute
     * @param requestedValues the values
     * @return the result of the filter
     */
    @Nonnull private Set<IdPAttributeValue<?>> filterValues(@Nullable final IdPAttribute attribute,
            @Nonnull @NonnullElements final List<? extends IdPAttributeValue> requestedValues) {

        if (null == requestedValues || requestedValues.isEmpty()) {
            log.debug("{} Attribute {} found in metadata and no values specified", getLogPrefix(), attribute.getId());
            return ImmutableSet.copyOf(attribute.getValues());
        }

        final ImmutableSet.Builder<IdPAttributeValue<?>> builder = ImmutableSet.builder();

        for (final IdPAttributeValue attributeValue : attribute.getValues()) {
            if (requestedValues.contains(attributeValue)) {
                builder.add(attributeValue);
            }
        }
        
        final ImmutableSet<IdPAttributeValue<?>> result = builder.build();
        log.debug("{} Values matched with metadata for Attribute {} : {}", getLogPrefix(), attribute.getId(), result);
        return result;
    }

    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Filter '<filterID>' :"
     */
    @Nonnull protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            final StringBuilder builder = new StringBuilder("Attribute Filter '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }

}