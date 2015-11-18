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

package net.shibboleth.idp.saml.session.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.saml.session.SAML2SPSession;
import net.shibboleth.idp.session.context.LogoutPropagationContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Action that adds an inbound {@link MessageContext} and a {@link SAMLPeerEntityContext} to the
 * {@link ProfileRequestContext} based on the identity of the relying party bound to a {@link SAML2SPSession}
 * found in a {@link LogoutPropagationContext}.  
 * 
 * <p>This action mocks up a minimal amount of machinery on the inbound message side to drive a
 * SAML 2 Logout Propagation flow, which needs to issue a logout request message for the {@link SAML2SPSession}
 * it's given.</p>
 * 
 * <p>It's named generically so that if we need to expand it to support something beyond SAML 2 (some kind of
 * hack for SAML 1?) we can do that.
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 */
public class PrepareInboundMessageContext extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrepareInboundMessageContext.class);

    /** Logout propagation context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,LogoutPropagationContext> logoutPropContextLookupStrategy;

    /** The {@link SAML2SPSession} to base the inbound context on. */
    @Nullable private SAML2SPSession saml2Session;

    /** Constructor. */
    public PrepareInboundMessageContext() {
        logoutPropContextLookupStrategy = new ChildContextLookup<>(LogoutPropagationContext.class);
    }

    /**
     * Set the logout propagation context lookup strategy.
     * 
     * @param strategy lookup strategy
     */
    public void setLogoutPropagationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,LogoutPropagationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        logoutPropContextLookupStrategy =
                Constraint.isNotNull(strategy, "LogoutPropagationContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        final LogoutPropagationContext logoutPropCtx = logoutPropContextLookupStrategy.apply(profileRequestContext);
        if (logoutPropCtx == null) {
            log.debug("{} No logout propagation context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        } else if (logoutPropCtx.getSession() == null || !(logoutPropCtx.getSession() instanceof SAML2SPSession)) {
            log.debug("{} Logout propgation context did not contain a SAML2SPSession", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        saml2Session = (SAML2SPSession) logoutPropCtx.getSession();

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final MessageContext msgCtx = new MessageContext();
        profileRequestContext.setInboundMessageContext(msgCtx);

        final SAMLPeerEntityContext peerContext = msgCtx.getSubcontext(SAMLPeerEntityContext.class, true);
        peerContext.setEntityId(saml2Session.getId());

        log.debug("{} Initialized inbound message context for logout of {}", getLogPrefix(), saml2Session.getId());
    }
    
}