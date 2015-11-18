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

package net.shibboleth.idp.authn.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.ExternalAuthenticationContext;
import net.shibboleth.idp.consent.context.ConsentManagementContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Implementation of the {@link ExternalAuthentication} API that handles moving information in and out
 * of request attributes.
 */
public class ExternalAuthenticationImpl extends ExternalAuthentication {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExternalAuthenticationImpl.class);
    
    /** Lookup function for relying party context. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** State of request to pull from. */
    @Nonnull private final ProfileRequestContext profileRequestContext;
    
    /** Track whether we were invoked from within another login flow. */
    private final boolean extendedFlow;

    /**
     * Constructor.
     * 
     * @param input profile request context to expose
     */
    public ExternalAuthenticationImpl(@Nonnull final ProfileRequestContext input) {
        profileRequestContext = Constraint.isNotNull(input, "ProfileRequestContext cannot be null");
        extendedFlow = false;
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }

    /**
     * Constructor.
     * 
     * @param input profile request context to expose
     * @param extended called as extended flow from another login flow
     */
    public ExternalAuthenticationImpl(@Nonnull final ProfileRequestContext input, final boolean extended) {
        profileRequestContext = Constraint.isNotNull(input, "ProfileRequestContext cannot be null");
        extendedFlow = extended;
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }

    /**
     * Set lookup strategy for relying party context.
     * 
     * @param strategy  lookup strategy
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,RelyingPartyContext> strategy) {
        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    @Override
    protected void doStart(@Nonnull final HttpServletRequest request) throws ExternalAuthenticationException {
        final AuthenticationContext authnContext = profileRequestContext.getSubcontext(AuthenticationContext.class);
        if (authnContext == null) {
            throw new ExternalAuthenticationException("No AuthenticationContext found");
        } else if (authnContext.getAttemptedFlow() == null) {
            throw new ExternalAuthenticationException("No attempted authentication flow set");
        }
        
        request.setAttribute(ProfileRequestContext.BINDING_KEY, profileRequestContext);
        request.setAttribute(EXTENDED_FLOW_PARAM, extendedFlow);
        
        request.setAttribute(PASSIVE_AUTHN_PARAM, authnContext.isPassive());
        request.setAttribute(FORCE_AUTHN_PARAM, authnContext.isForceAuthn());
                
        final Collection<Principal> principals = authnContext.getAttemptedFlow().getSupportedPrincipals();
        if (!principals.isEmpty()) {
            request.setAttribute(AUTHN_METHOD_PARAM, principals.iterator().next().getName());
        }
        
        final RelyingPartyContext rpCtx = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (rpCtx != null) {
            request.setAttribute(RELYING_PARTY_PARAM, rpCtx.getRelyingPartyId());
        }
    }

 // Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void doFinish(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
            throws ExternalAuthenticationException, IOException {
        final AuthenticationContext authnContext = profileRequestContext.getSubcontext(AuthenticationContext.class);
        if (authnContext == null) {
            throw new ExternalAuthenticationException("No AuthenticationContext found");
        }
        
        final ExternalAuthenticationContext extContext =
                authnContext.getSubcontext(ExternalAuthenticationContext.class);
        if (extContext == null) {
            throw new ExternalAuthenticationException("No ExternalAuthenticationContext found");
        } else if (extContext.getFlowExecutionUrl() == null) {
            throw new ExternalAuthenticationException("No flow execution URL found to return control");
        }
        
        Object attr = request.getAttribute(SUBJECT_KEY);
        if (attr != null && attr instanceof Subject) {
            extContext.setSubject((Subject) attr);
        } else {
            attr = request.getAttribute(PRINCIPAL_KEY);
            if (attr != null && attr instanceof Principal) {
                extContext.setPrincipal((Principal) attr);
            } else {
                attr = request.getAttribute(PRINCIPAL_NAME_KEY);
                if (attr != null && attr instanceof String) {
                    extContext.setPrincipalName((String) attr);
                }
            }
        }
        
        attr = request.getAttribute(AUTHENTICATION_INSTANT_KEY);
        if (attr != null && attr instanceof DateTime) {
            extContext.setAuthnInstant((DateTime) attr);
        }
        
        attr = request.getAttribute(AUTHENTICATION_ERROR_KEY);
        if (attr != null && attr instanceof String) {
            extContext.setAuthnError((String) attr);
        }
        
        attr = request.getAttribute(AUTHENTICATION_EXCEPTION_KEY);
        if (attr != null && attr instanceof Exception) {
            extContext.setAuthnException((Exception) attr);
        }
        
        attr = request.getAttribute(DONOTCACHE_KEY);
        if (attr != null && attr instanceof Boolean) {
            extContext.setDoNotCache((Boolean) attr);
        }
        
        attr = request.getAttribute(REVOKECONSENT_KEY);
        if (attr != null && attr instanceof Boolean && ((Boolean) attr).booleanValue()) {
            final ConsentManagementContext consentCtx =
                    getProfileRequestContext(request).getSubcontext(ConsentManagementContext.class, true);
            consentCtx.setRevokeConsent(true);
        } else {
            response.sendRedirect(extContext.getFlowExecutionUrl());
        }
    }
// Checkstyle: CyclomaticComplexity OFF

    /** {@inheritDoc} */
    @Override
    protected ProfileRequestContext getProfileRequestContext(@Nonnull final HttpServletRequest request)
            throws ExternalAuthenticationException {
        return profileRequestContext;
    }
    
    
}