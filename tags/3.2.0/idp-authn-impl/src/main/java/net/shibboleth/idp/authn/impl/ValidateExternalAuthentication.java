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

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.ExternalAuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that checks for an {@link ExternalAuthenticationContext} and directly produces an
 * {@link net.shibboleth.idp.authn.AuthenticationResult} or records error state based on the
 * contents.
 *  
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link AuthnEventIds#INVALID_AUTHN_CTX}
 * @event {@link AuthnEventIds#AUTHN_EXCEPTION}
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class).getAttemptedFlow() != null</pre>
 * @post If AuthenticationContext.getSubcontext(ExternalAuthenticationContext.class) != null, then
 * an {@link net.shibboleth.idp.authn.AuthenticationResult} is saved to the {@link AuthenticationContext} on a
 * successful login. On a failed login, the
 * {@link AbstractValidationAction#handleError(ProfileRequestContext, AuthenticationContext, Exception, String)}
 * method is called.
 */
public class ValidateExternalAuthentication extends AbstractValidationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ValidateExternalAuthentication.class);

    /** Context containing the result to validate. */
    @Nullable private ExternalAuthenticationContext extContext;
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        }
        
        if (authenticationContext.getAttemptedFlow() == null) {
            log.debug("{} No attempted flow within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        extContext = authenticationContext.getSubcontext(ExternalAuthenticationContext.class);
        if (extContext == null) {
            log.debug("{} No ExternalAuthenticationContext available within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_AUTHN_CTX);
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        if (extContext.getAuthnException() != null) {
            log.warn("{} External authentication produced exception", getLogPrefix(), extContext.getAuthnException());
            handleError(profileRequestContext, authenticationContext, extContext.getAuthnException(),
                    AuthnEventIds.AUTHN_EXCEPTION);
            return;
        } else if (extContext.getAuthnError() != null) {
            log.warn("{} External authentication produced error message: {}", getLogPrefix(),
                    extContext.getAuthnError());
            handleError(profileRequestContext, authenticationContext, extContext.getAuthnError(),
                    AuthnEventIds.AUTHN_EXCEPTION);
            return;
        }
        
        if (extContext.getSubject() != null) {
            log.info("{} External authentication succeeded for Subject: {}", getLogPrefix(),
                    extContext.getSubject().getPrincipals());
        } else if (extContext.getPrincipal() != null) {
            log.info("{} External authentication succeeded for Principal: {}", getLogPrefix(),
                    extContext.getPrincipal());
            extContext.setSubject(new Subject(false, Collections.singleton(extContext.getPrincipal()),
                    Collections.emptySet(), Collections.emptySet()));
        } else if (extContext.getPrincipalName() != null) {
            log.info("{} External authentication succeeded for user: {}", getLogPrefix(),
                    extContext.getPrincipalName());
            extContext.setSubject(new Subject(false,
                    Collections.singleton(new UsernamePrincipal(extContext.getPrincipalName())),
                    Collections.emptySet(), Collections.emptySet()));
        } else {
            log.info("{} External authentication failed, no user identity or error information returned",
                    getLogPrefix());
            handleError(profileRequestContext, authenticationContext, AuthnEventIds.NO_CREDENTIALS,
                    AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        
        if (extContext.doNotCache()) {
            log.debug("{} Disabling caching of authentication result", getLogPrefix());
            authenticationContext.setResultCacheable(false);
        }
        buildAuthenticationResult(profileRequestContext, authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull protected Subject populateSubject(@Nonnull final Subject subject) {
        // Override supplied Subject with our own, after transferring over any custom Principals.
        extContext.getSubject().getPrincipals().addAll(subject.getPrincipals());
        return extContext.getSubject();
    }
    
}