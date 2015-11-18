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

package net.shibboleth.idp.attribute.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/** Base class for attribute definition resolver plugins. */
@ThreadSafe
public abstract class AbstractAttributeDefinition extends AbstractResolverPlugin<IdPAttribute> implements
        AttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractAttributeDefinition.class);

    /** Whether this attribute definition is only a dependency and thus its values should never be released. */
    private boolean dependencyOnly;

    /** The sourceAttributeID attributeName. */
    @Nullable private String sourceAttributeID;

    /** Attribute encoders associated with this definition. */
    @Nonnull private Set<AttributeEncoder<?>> encoders = Collections.emptySet();

    /** Localized human intelligible attribute name. */
    @Nonnull private Map<Locale, String> displayNames = Collections.emptyMap();

    /** Localized human readable description of attribute. */
    @Nonnull private Map<Locale, String> displayDescriptions = Collections.emptyMap();

    /** cache for the log prefix - to save multiple recalculations. */
    @Nullable private String logPrefix;

    /**
     * Gets whether this attribute definition is only a dependency and thus its values should never be released outside
     * the resolver.
     * 
     * @return true if this attribute is only used as a dependency, false otherwise
     */
    @Override
    public boolean isDependencyOnly() {
        return dependencyOnly;
    }

    /**
     * Sets whether this attribute definition is only a dependency and thus its values should never be released outside
     * the resolver.
     * 
     * @param isDependencyOnly whether this attribute definition is only a dependency
     */
    public void setDependencyOnly(final boolean isDependencyOnly) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        dependencyOnly = isDependencyOnly;
    }

    /**
     * Gets the localized human readable descriptions of attribute.
     * 
     * @return human readable descriptions of attribute
     */
    @Override
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayDescriptions() {
        return displayDescriptions;
    }

    /**
     * Sets the localized human readable descriptions of attribute.
     * 
     * @param descriptions localized human readable descriptions of attribute
     */
    public void setDisplayDescriptions(@Nullable @NullableElements  final Map<Locale, String> descriptions) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        HashMap<Locale, String> checkedDescriptions = new HashMap<>();
        String trimmedDescription;
        for (Entry<Locale, String> entry : descriptions.entrySet()) {
            trimmedDescription = StringSupport.trimOrNull(entry.getValue());
            if (trimmedDescription != null) {
                checkedDescriptions.put(entry.getKey(), trimmedDescription);
            }
        }

        displayDescriptions = ImmutableMap.copyOf(checkedDescriptions);
    }

    /**
     * Gets the localized human readable names of the attribute.
     * 
     * @return human readable names of the attribute
     */
    @Override
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }

    /**
     * Sets the localized human readable names of the attribute.
     * 
     * @param names localized human readable names of the attribute
     */
    public void setDisplayNames(@Nullable @NullableElements final Map<Locale, String> names) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        HashMap<Locale, String> checkedNames = new HashMap<>();
        String trimmedName;
        for (Entry<Locale, String> entry : names.entrySet()) {
            trimmedName = StringSupport.trimOrNull(entry.getValue());
            if (trimmedName != null) {
                checkedNames.put(entry.getKey(), trimmedName);
            }
        }

        displayNames = ImmutableMap.copyOf(checkedNames);
    }

    /**
     * Gets the unmodifiable encoders used to encode the values of this attribute in to protocol specific formats. The
     * returned collection is never null nor contains any null.
     * 
     * @return encoders used to encode the values of this attribute in to protocol specific formats, never null
     */
    @Override
    @Nonnull @NonnullElements @Unmodifiable public Set<AttributeEncoder<?>> getAttributeEncoders() {
        return encoders;
    }

    /**
     * Sets the encoders used to encode the values of this attribute in to protocol specific formats.
     * 
     * @param attributeEncoders encoders used to encode the values of this attribute in to protocol specific formats
     */
    public void setAttributeEncoders(@Nullable @NullableElements final Set<AttributeEncoder<?>> attributeEncoders) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Set<AttributeEncoder<?>> checkedEncoders = new HashSet<>();
        CollectionSupport.addIf(checkedEncoders, attributeEncoders, Predicates.notNull());
        encoders = ImmutableSet.copyOf(checkedEncoders);
    }

    /**
     * Gets the source attribute id.
     * 
     * @return the source attribute id
     */
    public String getSourceAttributeId() {
        return sourceAttributeID;
    }

    /**
     * Sets the source attribute id.
     * 
     * @param attributeId the source attribute id
     */
    public void setSourceAttributeId(String attributeId) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        sourceAttributeID = StringSupport.trimOrNull(attributeId);
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {

        // Set up the dependencies first. Then the initialize in the parent
        // will correctly rehash the dependencies.
        if (null != getSourceAttributeId()) {
            for (ResolverPluginDependency depends : getDependencies()) {
                depends.setDependencyAttributeId(getSourceAttributeId());
            }
        }
        super.doInitialize();

        // The Id is now definitive. Just in case it was used prior to that, reset the getPrefixCache
        logPrefix = null;
    }

    /**
     * {@inheritDoc}
     * 
     * This method delegates the actual resolution of the attribute's values to the
     * {@link #doAttributeDefinitionResolve(AttributeResolutionContext, AttributeResolverWorkContext)} method.
     * Afterwards, if null was not returned, this method will attach the registered display names, descriptions,
     * and encoders to the resultant attribute.
     */
    @Override
    @Nullable protected IdPAttribute doResolve(@Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        final IdPAttribute resolvedAttribute = doAttributeDefinitionResolve(resolutionContext, workContext);

        if (null == resolvedAttribute) {
            log.debug("{} no attribute was produced during resolution", getLogPrefix());
            return null;
        }

        if (resolvedAttribute.getValues().isEmpty()) {
            log.debug("{} produced an attribute with no values", getLogPrefix());
        } else {
            log.debug("{} produced an attribute with the following values {}", getLogPrefix(),
                    resolvedAttribute.getValues());
        }

        log.trace("{} associating the following display descriptions with the resolved attribute: {}", getLogPrefix(),
                getDisplayDescriptions());
        resolvedAttribute.setDisplayDescriptions(getDisplayDescriptions());

        log.trace("{} associating the following display names with the resolved attribute: {}", getLogPrefix(),
                getDisplayNames());
        resolvedAttribute.setDisplayNames(getDisplayNames());

        log.trace("{} associating the following encoders with the resolved attribute: {}", getLogPrefix(),
                getAttributeEncoders());
        resolvedAttribute.setEncoders(getAttributeEncoders());

        return resolvedAttribute;
    }

    /**
     * Creates and populates the values for the resolved attribute. Implementations should <strong>not</strong> set, or
     * otherwise manage, the resolved attribute's display name, description or encoders. Nor should the resultant
     * attribute be recorded in the given resolution context.
     * 
     * @param resolutionContext current attribute resolution context
     * @param workContext current resolver work context
     * 
     * @return resolved attribute or null if nothing to resolve.
     * @throws ResolutionException thrown if there is a problem resolving and creating the attribute
     */
    @Nullable protected abstract IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException;

    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Definition '<definitionID>' :"
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            StringBuilder builder = new StringBuilder("Attribute Definition '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }
    
}