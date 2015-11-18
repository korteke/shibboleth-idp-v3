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
import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.LegacyPrincipalDecoder;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.authn.AbstractSubjectCanonicalizationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.SubjectCanonicalizationException;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.authn.principal.NameIdentifierPrincipal;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * Action to perform C14N based on the contents of the attribute-resolver.xml file, this
 * delegates the work to an {@link AttributeResolver} instance that supports the
 * {@link LegacyPrincipalDecoder} interface.
 */
public class LegacyCanonicalization extends AbstractSubjectCanonicalizationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(LegacyCanonicalization.class);
    
    /** Service used to get the resolver used to fetch attributes. */
    @Nonnull private final ReloadableService<AttributeResolver> attributeResolverService;

    /**
     * Constructor.
     * 
     * @param resolverService the service which will implement {@link LegacyPrincipalDecoder}.
     */
    public LegacyCanonicalization(@Nonnull final ReloadableService<AttributeResolver> resolverService) {
        attributeResolverService = Constraint.isNotNull(resolverService, "AttributeResolver cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final SubjectCanonicalizationContext c14nContext) {

        ServiceableComponent<AttributeResolver> component = null;
        try {
            component = attributeResolverService.getServiceableComponent();
            if (null == component) {
                log.error("{} Error resolving PrincipalConnector: Invalid Attribute resolver configuration.",
                        getLogPrefix());
                c14nContext.setException(new SubjectCanonicalizationException(
                        "Error resolving PrincipalConnectore: Invalid Attribute resolver configuration."));
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                return;
            }

            final AttributeResolver attributeResolver = component.getComponent();
            if (!(attributeResolver instanceof LegacyPrincipalDecoder)) {
                log.info("{} Attribute Resolver did not implement LegacyPrincipalDecoder.", getLogPrefix());
                c14nContext.setException(new SubjectCanonicalizationException(
                        "Attribute Resolver did not implement LegacyPrincipalDecoder."));
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                return;
            }

            LegacyPrincipalDecoder decoder = (LegacyPrincipalDecoder) attributeResolver;

            final String decodedPrincipal = decoder.canonicalize(c14nContext);
            if (null == decodedPrincipal) {
                log.info("{} Legacy Principal Decoding returned no value", getLogPrefix());
                c14nContext.setException(new SubjectCanonicalizationException(
                        "Legacy Principal Decoding returned no value"));
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_SUBJECT);
                return;
            }
            
            c14nContext.setPrincipalName(decodedPrincipal);
        } catch (ResolutionException e) {
            c14nContext.setException(e);
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.SUBJECT_C14N_ERROR);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }
    
    /**
     * A predicate that determines if this action can run or not - it does this by inspecting the attribute resolver for
     * principal connectors.
     */
    public static class ActivationCondition implements Predicate<ProfileRequestContext> {

        /** Service used to get the resolver used to fetch attributes. */
        @Nullable private final ReloadableService<AttributeResolver> attributeResolverService;

        /**
         * Constructor.
         * 
         * @param service the service we need to interrogate.
         */
        public ActivationCondition(ReloadableService<AttributeResolver> service) {
            attributeResolverService = service;
        }

        /**
         * {@inheritDoc}
         * 
         * <p>Iff there is a valid service and there are no parsing errors and the service does understand
         * principal connectors and there were some configured we will proceed.</p>
         */
        @Override public boolean apply(@Nullable final ProfileRequestContext input) {

            if (null == input) {
                return false;
            }
            
            final SubjectCanonicalizationContext c14nContext =
                    input.getSubcontext(SubjectCanonicalizationContext.class);
            if (null == c14nContext) {
                return false;
            }

            final Subject subject = c14nContext.getSubject();
            if (null == subject) {
                return false;
            }
            
            final Set<NameIDPrincipal> nameIDs = subject.getPrincipals(NameIDPrincipal.class);
            final Set<NameIdentifierPrincipal> nameIdentifiers = subject.getPrincipals(NameIdentifierPrincipal.class);
            if (1 != nameIDs.size() + nameIdentifiers.size()) {
                return false;
            }
            
            if (null == attributeResolverService) {
                return false;
            }

            ServiceableComponent<AttributeResolver> component = null;
            try {
                component = attributeResolverService.getServiceableComponent();
                if (null == component) {
                    return false;
                }

                final AttributeResolver attributeResolver = component.getComponent();
                if (!(attributeResolver instanceof LegacyPrincipalDecoder)) {
                    return false;
                }
                return ((LegacyPrincipalDecoder) attributeResolver).hasValidConnectors();
            } finally {
                if (null != component) {
                    component.unpinComponent();
                }
            }
        }
    }

}