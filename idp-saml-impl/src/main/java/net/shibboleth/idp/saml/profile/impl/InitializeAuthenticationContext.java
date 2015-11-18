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

package net.shibboleth.idp.saml.profile.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * An action that creates an {@link AuthenticationContext} and attaches it to the current {@link ProfileRequestContext}.
 * 
 * <p>If the incoming message is a SAML 2.0 {@link AuthnRequest}, then basic authentication policy (IsPassive,
 * ForceAuthn) is copied into the context from the request.</p>
 * 
 * <p>If a previously populated {@link AuthenticationContext} is found, and it contains a successful
 * {@link AuthenticationResult}, that result is copied to the new context via
 * {@link AuthenticationContext#setInitialAuthenticationResult(net.shibboleth.idp.authn.AuthenticationResult)}.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post ProfileRequestContext.getSubcontext(AuthenticationContext.class, false) != true
 * @post SAML 2.0 AuthnRequest policy flags are copied to the {@link AuthenticationContext}
 */
public class InitializeAuthenticationContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(InitializeAuthenticationContext.class);

    /** Strategy used to locate the {@link AuthnRequest} to operate on, if any. */
    @Nonnull private Function<ProfileRequestContext,AuthnRequest> requestLookupStrategy;
    
    /** Incoming SAML 2.0 request, if present. */
    @Nullable private AuthnRequest authnRequest;

    /** Constructor. */
    public InitializeAuthenticationContext() {
        requestLookupStrategy =
                Functions.compose(new MessageLookup<>(AuthnRequest.class), new InboundMessageContextLookup());
    }
    
    /**
     * Set the strategy used to locate the {@link AuthnRequest} to examine, if any.
     * 
     * @param strategy strategy used to locate the {@link AuthnRequest}
     */
    public void setRequestLookupStrategy(@Nonnull final Function<ProfileRequestContext,AuthnRequest> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        requestLookupStrategy = Constraint.isNotNull(strategy, "AuthnRequest lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        authnRequest = this.requestLookupStrategy.apply(profileRequestContext);
        if (authnRequest == null) {
            log.debug("{} No inbound AuthnRequest, passive and forced flags will be off", getLogPrefix());
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final AuthenticationContext authnCtx = new AuthenticationContext();

        if (authnRequest != null) {
            authnCtx.setForceAuthn(authnRequest.isForceAuthn());
            authnCtx.setIsPassive(authnRequest.isPassive());
        }

        final AuthenticationContext initialAuthnContext =
                profileRequestContext.getSubcontext(AuthenticationContext.class);
        if (initialAuthnContext != null) {
            authnCtx.setInitialAuthenticationResult(initialAuthnContext.getAuthenticationResult());
        }
        
        profileRequestContext.addSubcontext(authnCtx, true);

        log.debug("{} Created authentication context: {}", getLogPrefix(), authnCtx);
    }
    
}