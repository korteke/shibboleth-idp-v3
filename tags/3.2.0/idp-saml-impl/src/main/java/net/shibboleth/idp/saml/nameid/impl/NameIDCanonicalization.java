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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractSubjectCanonicalizationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.SubjectCanonicalizationException;
import net.shibboleth.idp.authn.SubjectCanonicalizationFlowDescriptor;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDCanonicalizationFlowDescriptor;
import net.shibboleth.idp.saml.nameid.NameIDDecoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.profile.SAML2ObjectSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * Action to perform subject canonicalization, transforming the input {@link javax.security.auth.Subject}
 * into a principal name by searching for one and only one {@link NameIDPrincipal} custom principal,
 * using an injected {@link NameIDDecoder} to carry out the process.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#INVALID_SUBJECT}
 * @pre <pre>
 * ProfileRequestContext.getSubcontext(SubjectCanonicalizationContext.class, false) != null
 * </pre>
 * @post <pre>
 * SubjectCanonicalizationContext.getPrincipalName() != null || SubjectCanonicalizationContext.getException() != null
 * </pre>
 */
public class NameIDCanonicalization extends AbstractSubjectCanonicalizationAction {

    /** Supplies logic for pre-execute test. */
    @Nonnull private final ActivationCondition embeddedPredicate;
    
    /** Supplies logic for decoding the {@link NameID} into a principal. */
    @NonnullAfterInit private NameIDDecoder decoder;

    /**
     * Constructor.
     * 
     */
    public NameIDCanonicalization() {
        embeddedPredicate = new ActivationCondition();
    }

    /**
     * Get the class responsible for decoding the {@link NameID#getValue()} into a principal.
     * 
     * @return the decoder
     */
    @NonnullAfterInit public NameIDDecoder getDecoder() {
        return decoder;
    }

    /**
     * Set the class responsible for decoding the {@link NameID#getValue()} into a principal.
     * 
     * @param theDecoder the decoder
     */
    @NonnullAfterInit public void setDecoder(@Nonnull NameIDDecoder theDecoder) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        decoder = Constraint.isNotNull(theDecoder, "Name ID decoder cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        if (null == decoder) {
            throw new ComponentInitializationException(getLogPrefix() + " decoder not supplied");
        }
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final SubjectCanonicalizationContext c14nContext) {

        if (!embeddedPredicate.apply(profileRequestContext, c14nContext, true)) {
            return false;
        }
        
        return super.doPreExecute(profileRequestContext, c14nContext);
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final SubjectCanonicalizationContext c14nContext) {

        final Set<NameIDPrincipal> nameIDs = c14nContext.getSubject().getPrincipals(NameIDPrincipal.class);
        final NameID nameID = nameIDs.iterator().next().getNameID();

        try {
            c14nContext.setPrincipalName(decoder.decode(c14nContext, nameID));
            if (c14nContext.getPrincipalName() == null) {
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
            }
        } catch (final NameDecoderException e) {
            c14nContext.setException(e);
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.SUBJECT_C14N_ERROR);
        }
    }

    /** A predicate that determines if this action can run or not. */
    public static class ActivationCondition implements Predicate<ProfileRequestContext> {

        /** Class logger. */
        @Nonnull private Logger log = LoggerFactory.getLogger(ActivationCondition.class);

        /** {@inheritDoc} */
        @Override public boolean apply(@Nullable final ProfileRequestContext input) {

            if (input != null) {
                final SubjectCanonicalizationContext c14nContext =
                        input.getSubcontext(SubjectCanonicalizationContext.class);
                if (c14nContext != null) {
                    return apply(input, c14nContext, false);
                }
            }

            return false;
        }

        /**
         * Check the format against the format list. If we are in the action then we log the error into the C14N context
         * and add the appropriate event to the profile request context.
         * 
         * @param format the format to check
         * @param c14nContext the current c14n context
         * 
         * @return true iff the format matches
         */
        protected boolean formatMatches(@Nonnull final String format,
                @Nonnull final SubjectCanonicalizationContext c14nContext) {

            final SubjectCanonicalizationFlowDescriptor flowDescriptor = c14nContext.getAttemptedFlow();

            log.debug("Attempting to match format '{}'", format);

            if (null == flowDescriptor) {
                log.error("Supplied Context has no active FlowDescriptor");
                return false;
            }

            if (!(flowDescriptor instanceof NameIDCanonicalizationFlowDescriptor)) {
                log.error("Flow Descriptor named {} is not appropriate for NameID canonicalization.  Use class=\"{}\"",
                        flowDescriptor.getId(), NameIDCanonicalizationFlowDescriptor.class.getCanonicalName());
                return false;
            }

            final NameIDCanonicalizationFlowDescriptor nameIDDescriptor =
                    (NameIDCanonicalizationFlowDescriptor) flowDescriptor;

            for (String testFormat : nameIDDescriptor.getFormats()) {
                if (SAML2ObjectSupport.areNameIDFormatsEquivalent(testFormat, format)) {
                    log.debug("NameIDCanonicalizationFlowDescriptor {}: format matches {}",
                            nameIDDescriptor.getId(), testFormat);
                    return true;
                }
            }

            log.debug("NameIDCanonicalizationFlowDescriptor {}: no format matches", nameIDDescriptor.getId());
            return false;
        }

        /**
         * Helper method that runs either as part of the {@link Predicate} or directly from the
         * {@link NameIDCanonicalization#doPreExecute(ProfileRequestContext, SubjectCanonicalizationContext)} method
         * above.
         * 
         * @param profileRequestContext the current profile request context
         * @param c14nContext the current c14n context
         * @param duringAction true iff the method is run from the action above
         * 
         * @return true iff the action can operate successfully on the candidate contexts
         */
        public boolean apply(@Nonnull final ProfileRequestContext profileRequestContext,
                @Nonnull final SubjectCanonicalizationContext c14nContext, final boolean duringAction) {

            Set<NameIDPrincipal> nameIDs = null;
            if (c14nContext.getSubject() != null) {
                nameIDs = c14nContext.getSubject().getPrincipals(NameIDPrincipal.class);
            }

            if (duringAction) {
                if (nameIDs == null || nameIDs.isEmpty()) {
                    c14nContext.setException(new SubjectCanonicalizationException("No NameIDPrincipals were found"));
                    ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                    return false;
                } else if (nameIDs.size() > 1) {
                    c14nContext.setException(
                            new SubjectCanonicalizationException("Multiple NameIDPrincipals were found"));
                    ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                    return false;
                } else if (!formatMatches(nameIDs.iterator().next().getNameID().getFormat(), c14nContext)) {
                    c14nContext.setException(new SubjectCanonicalizationException("Format not supported"));
                    ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                    return false;
                }
                
                return true;
            }
            
            // Not in an action, so do the same but without the context side effects
            if (nameIDs == null || nameIDs.size() != 1) {
                return false;
            }

            return formatMatches(nameIDs.iterator().next().getNameID().getFormat(), c14nContext);
        }
    }
    
}