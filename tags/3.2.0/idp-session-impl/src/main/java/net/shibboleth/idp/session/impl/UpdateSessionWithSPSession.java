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

package net.shibboleth.idp.session.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.SessionManager;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 * An action that establishes a record of an {@link SPSession} in an existing {@link IdPSession} for the client.
 * 
 * <p>The {@link SPSession} to add is obtained using a strategy function injected into the action.</p>
 * 
 * <p>The existing session to modify is identified via a {@link SessionContext} attached to the
 * {@link ProfileRequestContext}.</p>
 * 
 * <p>An error interacting with the session layer will result in an {@link EventIds#IO_ERROR} event.</p>
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#IO_ERROR}
 * @post If ProfileRequestContext.getSubcontext(SessionContext.class).getIdPSession() != null and
 * a non-null SPSession is supplied by the strategy function, then the steps above are performed.
 */
public class UpdateSessionWithSPSession extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(UpdateSessionWithSPSession.class);
    
    /** A function that returns the {@link SPSession} to add. */
    @Nonnull private Function<ProfileRequestContext,SPSession> spSessionCreationStrategy;

    /** SessionManager. */
    @NonnullAfterInit private SessionManager sessionManager;
    
    /** Lookup function for SessionContext. */
    @Nonnull private Function<ProfileRequestContext,SessionContext> sessionContextLookupStrategy;
    
    /** Existing or newly created SessionContext. */
    @Nullable private SessionContext sessionCtx;
    
    /** Constructor. */
    public UpdateSessionWithSPSession() {
        sessionContextLookupStrategy = new ChildContextLookup<>(SessionContext.class);
    }
    
    /**
     * Set the creation function to use to obtain the {@link SPSession} to add.
     * 
     * @param strategy  creation function to use
     */
    public void setSPSessionCreationStrategy(@Nonnull final Function<ProfileRequestContext,SPSession> strategy) {
        spSessionCreationStrategy = Constraint.isNotNull(strategy,
                "SPSession creation strategy function cannot be null");
    }
    
    /**
     * Set the {@link SessionManager} to use.
     * 
     * @param manager  session manager to use
     */
    public void setSessionManager(@Nonnull final SessionManager manager) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sessionManager = Constraint.isNotNull(manager, "SessionManager cannot be null");
    }
    
    /**
     * Set the lookup strategy for the SessionContext to access.
     * 
     * @param strategy  lookup strategy
     */
    public void setSessionContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SessionContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sessionContextLookupStrategy = Constraint.isNotNull(strategy,
                "SessionContext lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (!getActivationCondition().equals(Predicates.alwaysFalse())) {
            if (sessionManager == null) {
                throw new ComponentInitializationException("SessionManager cannot be null");
            } else if (spSessionCreationStrategy == null) {
                throw new ComponentInitializationException("Session creation strategy cannot be null");
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (super.doPreExecute(profileRequestContext)) {
            sessionCtx = sessionContextLookupStrategy.apply(profileRequestContext);
            
            // We can only do work if a session exists.
            return sessionCtx != null && sessionCtx.getIdPSession() != null;
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final SPSession spSession = spSessionCreationStrategy.apply(profileRequestContext);
        if (spSession == null) {
            log.debug("{} SPSession was not returned, nothing to do", getLogPrefix());
            return;
        }
        
        final IdPSession idpSession = sessionCtx.getIdPSession();
        try {
            log.debug("{} Adding new SPSession for relying party {} to existing session {}", getLogPrefix(),
                    spSession.getId(), idpSession.getId());
            final SPSession old = idpSession.addSPSession(spSession);
            if (old != null) {
                log.debug("{} Older SPSession for relying party {} was replaced", getLogPrefix(), old.getId());
            }
        } catch (final SessionException e) {
            log.error("{} Error updating session {}", getLogPrefix(), idpSession.getId(), e);
            ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
        }
    }

}