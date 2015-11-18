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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AbstractUsernamePasswordValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.LDAPResponseContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.ldaptive.Credential;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.jaas.LdapPrincipal;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that checks for a {@link UsernamePasswordContext} and directly produces an
 * {@link net.shibboleth.idp.authn.AuthenticationResult} based on that identity by authenticating against an LDAP.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#AUTHN_EXCEPTION}
 * @event {@link AuthnEventIds#ACCOUNT_WARNING}
 * @event {@link AuthnEventIds#ACCOUNT_ERROR}
 * @event {@link AuthnEventIds#INVALID_CREDENTIALS}
 * @pre <pre>
 * ProfileRequestContext.getSubcontext(AuthenticationContext.class).getAttemptedFlow() != null
 * </pre>
 * @post If AuthenticationContext.getSubcontext(UsernamePasswordContext.class) != null, then an
 *       {@link net.shibboleth.idp.authn.AuthenticationResult} is saved to the {@link AuthenticationContext} on a
 *       successful login. On a failed login, the
 *       {@link AbstractValidationAction#handleError(ProfileRequestContext, AuthenticationContext, String, String)}
 *       method is called.
 */
public class ValidateUsernamePasswordAgainstLDAP extends AbstractUsernamePasswordValidationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ValidateUsernamePasswordAgainstLDAP.class);

    /** LDAP authenticator. */
    @Nonnull private Authenticator authenticator;

    /** Attributes to return from authentication. */
    @Nullable private String[] returnAttributes;

    /** Authentication response associated with the login. */
    @Nullable private AuthenticationResponse response;

    /**
     * Returns the authenticator.
     * 
     * @return authenticator
     */
    @NonnullAfterInit public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the authenticator.
     * 
     * @param auth to authenticate with
     */
    public void setAuthenticator(@Nonnull final Authenticator auth) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        authenticator = Constraint.isNotNull(auth, "Authenticator cannot be null");
    }

    /**
     * Returns the return attributes.
     * 
     * @return attribute names
     */
    @Nullable public String[] getReturnAttributes() {
        return returnAttributes;
    }

    /**
     * Sets the return attributes.
     * 
     * @param attributes attribute names
     */
    public void setReturnAttributes(@Nullable final String... attributes) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        returnAttributes = attributes;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (authenticator == null) {
            throw new ComponentInitializationException("Authenticator cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        try {
            log.debug("{} Attempting to authenticate user {}", getLogPrefix(), getUsernamePasswordContext()
                    .getUsername());
            final AuthenticationRequest request =
                    new AuthenticationRequest(getUsernamePasswordContext().getUsername(), new Credential(
                            getUsernamePasswordContext().getPassword()), returnAttributes);
            response = authenticator.authenticate(request);
            log.trace("{} Authentication response {}", getLogPrefix(), response);
            if (response.getResult()) {
                log.info("{} Login by '{}' succeeded", getLogPrefix(), getUsernamePasswordContext().getUsername());
                authenticationContext.getSubcontext(LDAPResponseContext.class, true)
                        .setAuthenticationResponse(response);
                if (response.getAccountState() != null) {
                    final AccountState.Error error = response.getAccountState().getError();
                    handleWarning(
                            profileRequestContext,
                            authenticationContext,
                            String.format("%s:%s:%s", error != null ? error : "ACCOUNT_WARNING",
                                    response.getResultCode(), response.getMessage()), AuthnEventIds.ACCOUNT_WARNING);
                }
                buildAuthenticationResult(profileRequestContext, authenticationContext);
            } else {
                log.info("{} Login by '{}' failed", getLogPrefix(), getUsernamePasswordContext().getUsername());
                authenticationContext.getSubcontext(LDAPResponseContext.class, true)
                        .setAuthenticationResponse(response);
                if (AuthenticationResultCode.DN_RESOLUTION_FAILURE == response.getAuthenticationResultCode()
                        || AuthenticationResultCode.INVALID_CREDENTIAL == response.getAuthenticationResultCode()) {
                    handleError(profileRequestContext, authenticationContext,
                            String.format("%s:%s", response.getAuthenticationResultCode(), response.getMessage()),
                            AuthnEventIds.INVALID_CREDENTIALS);
                } else if (response.getAccountState() != null) {
                    final AccountState state = response.getAccountState();
                    handleError(profileRequestContext, authenticationContext, String.format("%s:%s:%s",
                            state.getError(), response.getResultCode(), response.getMessage()),
                            AuthnEventIds.ACCOUNT_ERROR);
                } else if (response.getResultCode() == ResultCode.INVALID_CREDENTIALS) {
                    handleError(profileRequestContext, authenticationContext,
                            String.format("%s:%s", response.getResultCode(), response.getMessage()),
                            AuthnEventIds.INVALID_CREDENTIALS);
                } else {
                    throw new LdapException(response.getMessage(), response.getResultCode(), response.getMatchedDn(),
                            response.getControls(), response.getReferralURLs(), response.getMessageId());
                }
            }
        } catch (final LdapException e) {
            log.warn("{} Login by {} produced exception", getLogPrefix(), getUsernamePasswordContext().getUsername(),
                    e);
            handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.AUTHN_EXCEPTION);
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected Subject populateSubject(@Nonnull final Subject subject) {
        subject.getPrincipals().add(
                new LdapPrincipal(getUsernamePasswordContext().getUsername(), response.getLdapEntry()));
        return super.populateSubject(subject);
    }

}
