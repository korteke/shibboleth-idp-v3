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

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.ThreadSafeAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.profile.AbstractSAML2NameIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Generator for "persistent" Format {@link NameID} objects that provides a source/seed ID based on {@link IdPAttribute}
 * data.
 */
@ThreadSafeAfterInit
public class PersistentSAML2NameIDGenerator extends AbstractSAML2NameIDGenerator {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PersistentSAML2NameIDGenerator.class);

    /** Strategy function to lookup SubjectContext. */
    @Nonnull private Function<ProfileRequestContext, SubjectContext> subjectContextLookupStrategy;

    /** Strategy function to lookup AttributeContext. */
    @Nonnull private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;

    /** Attribute(s) to use as an identifier source. */
    @Nonnull @NonnullElements private List<String> attributeSourceIds;

    /** Generation strategy for IDs. */
    @NonnullAfterInit private PersistentIdGenerationStrategy persistentIdStrategy;

    /** Predicate to select whether to look at filtered or unfiltered attributes. */
    private boolean useUnfilteredAttributes;

    /** Constructor. */
    public PersistentSAML2NameIDGenerator() {
        setFormat(NameID.PERSISTENT);
        subjectContextLookupStrategy = new ChildContextLookup<>(SubjectContext.class);
        attributeContextLookupStrategy = Functions.compose(
                new ChildContextLookup<RelyingPartyContext, AttributeContext>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
        attributeSourceIds = Collections.emptyList();
        setDefaultIdPNameQualifierLookupStrategy(new ResponderIdLookupFunction());
        setDefaultSPNameQualifierLookupStrategy(new RelyingPartyIdLookupFunction());
        useUnfilteredAttributes = true;
    }

    /**
     * Set the lookup strategy to use to locate the {@link SubjectContext}.
     * 
     * @param strategy lookup function to use
     */
    public void setSubjectContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, SubjectContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        subjectContextLookupStrategy = Constraint.isNotNull(strategy, "SubjectContext lookup strategy cannot be null");
    }

    /**
     * Set the lookup strategy to use to locate the {@link AttributeContext}.
     * 
     * @param strategy lookup function to use
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "AttributeContext lookup strategy cannot be null");
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
     * Set the generation strategy for the persistent ID.
     * 
     * @param strategy generation strategy
     */
    public void setPersistentIdGenerator(@Nonnull final PersistentIdGenerationStrategy strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        persistentIdStrategy = Constraint.isNotNull(strategy, "PersistentIdGenerationStrategy cannot be null");
    }

    /**
     * Set whether to source the input attributes from the unfiltered set.
     * 
     * <p>Defaults to true, since the input is not directly exposed.</p>
     * 
     * @param flag flag to set
     */
    public void setUseUnfilteredAttributes(final boolean flag) {
        useUnfilteredAttributes = flag;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (persistentIdStrategy == null) {
            throw new ComponentInitializationException("PersistentIdGenerationStrategy cannot be null");
        } else if (attributeSourceIds.isEmpty()) {
            throw new ComponentInitializationException("Attribute source ID list cannot be empty");
        }
    }

    // Checkstyle: CyclomaticComplexity|MethodLength OFF
    /** {@inheritDoc} */
    @Override @Nullable protected String getIdentifier(@Nonnull final ProfileRequestContext profileRequestContext)
            throws SAMLException {

        Function<ProfileRequestContext, String> lookup = getDefaultIdPNameQualifierLookupStrategy();
        final String responderId = lookup != null ? lookup.apply(profileRequestContext) : null;
        if (responderId == null) {
            log.debug("No responder identifier, can't generate persistent ID");
            return null;
        }

        // Effective qualifier may override default in the case of an Affiliation.
        String relyingPartyId = getEffectiveSPNameQualifier(profileRequestContext);
        if (relyingPartyId == null) {
            lookup = getDefaultSPNameQualifierLookupStrategy();
            relyingPartyId = lookup != null ? lookup.apply(profileRequestContext) : null;
        }
        if (relyingPartyId == null) {
            log.debug("No relying party identifier, can't generate persistent ID");
            return null;
        }

        final SubjectContext subjectCtx = subjectContextLookupStrategy.apply(profileRequestContext);
        if (subjectCtx == null || subjectCtx.getPrincipalName() == null) {
            log.debug("No principal name, can't generate persistent ID");
            return null;
        }

        final AttributeContext attributeCtx = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeCtx == null) {
            log.debug("No attribute context, can't generate persistent ID");
            return null;
        }

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
                    log.debug("Generating persistent NameID from Scoped String-valued attribute {}", sourceId);
                    return persistentIdStrategy.generate(responderId, relyingPartyId, subjectCtx.getPrincipalName(),
                            ((ScopedStringAttributeValue) value).getValue() + '@'
                                    + ((ScopedStringAttributeValue) value).getScope());
                } else if (value instanceof StringAttributeValue) {
                    // Check for all whitespace, but don't trim the value used.
                    if (StringSupport.trimOrNull((String) value.getValue()) == null) {
                        log.debug("Skipping all-whitespace string value");
                        continue;
                    }
                    log.debug("Generating persistent NameID from String-valued attribute {}", sourceId);
                    return persistentIdStrategy.generate(responderId, relyingPartyId, subjectCtx.getPrincipalName(),
                            (String) value.getValue());
                } else {
                    log.info("Unrecognized attribute value type: {}", value.getClass().getName());
                }
            }
        }

        log.info("Attribute sources {} did not produce a usable source identifier", attributeSourceIds);
        return null;
    }
    // Checkstyle: CyclomaticComplexity|MethodLength ON

}