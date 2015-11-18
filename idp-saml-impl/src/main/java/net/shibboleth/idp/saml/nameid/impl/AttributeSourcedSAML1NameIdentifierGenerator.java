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

package net.shibboleth.idp.saml.nameid.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.ThreadSafeAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.profile.AbstractSAML1NameIdentifierGenerator;
import org.opensaml.saml.saml1.profile.SAML1ObjectSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Generator for {@link NameIdentifier} objects based on {@link IdPAttribute} data.
 */
@ThreadSafeAfterInit
public class AttributeSourcedSAML1NameIdentifierGenerator extends AbstractSAML1NameIdentifierGenerator {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeSourcedSAML1NameIdentifierGenerator.class);

    /** Strategy function to lookup AttributeContext. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;

    /** Whether to look at filtered or unfiltered attributes. */
    private boolean useUnfilteredAttributes;

    /** Delimiter to use for scoped attribute serialization. */
    private char delimiter;

    /** Attribute(s) to use as an identifier source. */
    @Nonnull @NonnullElements private List<String> attributeSourceIds;

    /** Constructor. */
    public AttributeSourcedSAML1NameIdentifierGenerator() {
        attributeContextLookupStrategy = Functions.compose(
                new ChildContextLookup<RelyingPartyContext,AttributeContext>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        delimiter = '@';
        attributeSourceIds = Collections.emptyList();
        setDefaultIdPNameQualifierLookupStrategy(new ResponderIdLookupFunction());
        setDefaultSPNameQualifierLookupStrategy(new RelyingPartyIdLookupFunction());
        useUnfilteredAttributes = false;
    }

    /**
     * Set the lookup strategy to use to locate the {@link AttributeContext}.
     * 
     * @param strategy lookup function to use
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        attributeContextLookupStrategy = Constraint.isNotNull(strategy,
                "AttributeContext lookup strategy cannot be null");
    }

    /**
     * Set the delimiter to use for serializing scoped attribute values.
     * 
     * @param ch scope to set
     */
    public void setScopedDelimiter(final char ch) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        delimiter = ch;
    }

    /**
     * Set the attribute sources to pull from.
     * 
     * @param ids attribute IDs to pull from
     */
    public void setAttributeSourceIds(@Nonnull @NonnullElements final List<String> ids) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(ids, "Attribute ID collection cannot be null");

        attributeSourceIds = new ArrayList<>(Collections2.filter(ids, Predicates.notNull()));
    }

    /**
     * Set whether to source the input attributes from the unfiltered attribute set.
     * 
     * @param flag flag to set
     */
    public void setUseUnfilteredAttributes(final boolean flag) {
        useUnfilteredAttributes = flag;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (attributeSourceIds.isEmpty()) {
            throw new ComponentInitializationException("Attribute source ID list cannot be empty");
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected NameIdentifier doGenerate(@Nonnull final ProfileRequestContext profileRequestContext)
            throws SAMLException {

        // Check for a natively generated NameIdentifier attribute value.

        final AttributeContext attributeCtx = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeCtx == null) {
            log.warn("Unable to locate AttributeContext");
            return null;
        }

        final Map<String,IdPAttribute> attributes = useUnfilteredAttributes ? attributeCtx.getUnfilteredIdPAttributes()
                : attributeCtx.getIdPAttributes();

        for (final String sourceId : attributeSourceIds) {

            final IdPAttribute attribute = attributes.get(sourceId);
            if (attribute == null) {
                continue;
            }

            final List<IdPAttributeValue<?>> values = attribute.getValues();
            for (final IdPAttributeValue value : values) {
                if (value instanceof XMLObjectAttributeValue && value.getValue() instanceof NameIdentifier) {
                    if (SAML1ObjectSupport.areNameIdentifierFormatsEquivalent(getFormat(),
                            ((NameIdentifier) value.getValue()).getFormat())) {
                        log.info("Returning NameIdentifier from XMLObject-valued attribute {}", sourceId);
                        return (NameIdentifier) value.getValue();
                    } else {
                        log.debug("Attribute {} value was NameIdentifier, but Format did not match", sourceId);
                    }
                }
            }
        }

        // Fall into base class version which will ask us for an identifier.

        return super.doGenerate(profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override @Nullable protected String getIdentifier(@Nonnull final ProfileRequestContext profileRequestContext)
            throws SAMLException {

        final AttributeContext attributeCtx = attributeContextLookupStrategy.apply(profileRequestContext);

        final Map<String,IdPAttribute> attributes = useUnfilteredAttributes ? attributeCtx.getUnfilteredIdPAttributes()
                : attributeCtx.getIdPAttributes();

        for (final String sourceId : attributeSourceIds) {
            log.debug("Checking for source attribute {}", sourceId);

            final IdPAttribute attribute = attributes.get(sourceId);
            if (attribute == null) {
                continue;
            }

            final List<IdPAttributeValue<?>> values = attribute.getValues();
            for (final IdPAttributeValue value : values) {
                if (value instanceof ScopedStringAttributeValue) {
                    log.debug("Generating NameIdentifier from Scoped String-valued attribute {}", sourceId);
                    return ((ScopedStringAttributeValue) value).getValue() + delimiter
                            + ((ScopedStringAttributeValue) value).getScope();
                } else if (value instanceof StringAttributeValue) {
                    final String strVal = StringSupport.trimOrNull((String) value.getValue());
                    if (strVal == null) {
                        log.debug("Skipping all-whitespace string value");
                        continue;
                    }
                    log.debug("Generating NameIdentifier from String-valued attribute {}", sourceId);
                    return strVal;
                } else if (value instanceof EmptyAttributeValue) {
                    log.debug("Skipping empty value");
                    continue;
                } else {
                    log.warn("Unrecognized attribute value type: {}", value.getClass().getName());
                }
            }
        }

        log.info("Attribute sources {} did not produce a usable identifier", attributeSourceIds);
        return null;
    }

}