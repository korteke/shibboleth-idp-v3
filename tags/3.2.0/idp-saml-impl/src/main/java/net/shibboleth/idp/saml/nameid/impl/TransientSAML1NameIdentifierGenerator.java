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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.idp.saml.xml.SAMLConstants;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.ThreadSafeAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml1.profile.AbstractSAML1NameIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Generator for transient {@link org.opensaml.saml.saml1.core.NameIdentifier} objects.
 */
@ThreadSafeAfterInit
public class TransientSAML1NameIdentifierGenerator extends AbstractSAML1NameIdentifierGenerator {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TransientSAML1NameIdentifierGenerator.class);

    /** Strategy function to lookup SubjectContext. */
    @Nonnull private Function<ProfileRequestContext, SubjectContext> subjectContextLookupStrategy;

    /** Generator for transients. */
    @NonnullAfterInit private TransientIdGenerationStrategy transientIdGenerator;

    /** Constructor. */
    public TransientSAML1NameIdentifierGenerator() {
        setFormat(SAMLConstants.SAML1_NAMEID_TRANSIENT);
        subjectContextLookupStrategy = new ChildContextLookup<>(SubjectContext.class);
        setDefaultIdPNameQualifierLookupStrategy(new ResponderIdLookupFunction());
        setDefaultSPNameQualifierLookupStrategy(new RelyingPartyIdLookupFunction());
    }

    /**
     * Set the lookup strategy to use to locate the {@link SubjectContext}.
     * 
     * @param strategy lookup function to use
     */
    public void setSubjectContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, 
            SubjectContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        subjectContextLookupStrategy = Constraint.isNotNull(strategy, "SubjectContext lookup strategy cannot be null");
    }

    /**
     * Set the generator of transient IDs.
     * 
     * @param generator transient ID generator
     */
    public void setTransientIdGenerator(@Nonnull final TransientIdGenerationStrategy generator) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        transientIdGenerator = Constraint.isNotNull(generator, "TransientIdGenerationStrategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (transientIdGenerator == null) {
            throw new ComponentInitializationException("TransientIdGenerationStrategy cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected String getIdentifier(@Nonnull final ProfileRequestContext profileRequestContext)
            throws SAMLException {

        final Function<ProfileRequestContext, String> lookup = getDefaultSPNameQualifierLookupStrategy();
        final String relyingPartyId = lookup != null ? lookup.apply(profileRequestContext) : null;
        if (relyingPartyId == null) {
            log.debug("No relying party identifier available, can't generate transient ID");
            return null;
        }

        final SubjectContext subjectCtx = subjectContextLookupStrategy.apply(profileRequestContext);
        if (subjectCtx == null || subjectCtx.getPrincipalName() == null) {
            log.debug("No principal name available, can't generate transient ID");
            return null;
        }

        try {
            return transientIdGenerator.generate(relyingPartyId, subjectCtx.getPrincipalName());
        } catch (final SAMLException e) {
            log.debug("Exception generating transient ID", e);
            return null;
        }
    }

}