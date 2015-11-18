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
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.idp.session.criterion.HttpServletRequestCriterion;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 * A profile action that populates a {@link SessionContext} with an active, valid
 * {@link IdPSession}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post As above, and the session will be bound to the client's address if the underlying
 *  {@link SessionResolver} is configured to do so.
 */
public class PopulateSessionContext extends AbstractProfileAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateSessionContext.class);
    
    /** Session resolver. */
    @NonnullAfterInit private SessionResolver sessionResolver;
    
    /** Creation/lookup function for SessionContext. */
    @Nonnull private Function<ProfileRequestContext,SessionContext> sessionContextCreationStrategy;
    
    /** Function to return {@link CriteriaSet} to give to session resolver. */
    @Nonnull private Function<ProfileRequestContext,CriteriaSet> sessionResolverCriteriaStrategy;
    
    /** Constructor. */
    public PopulateSessionContext() {
        sessionContextCreationStrategy = new ChildContextLookup<>(SessionContext.class, true);
        
        sessionResolverCriteriaStrategy = new Function<ProfileRequestContext,CriteriaSet>() {
            public CriteriaSet apply(ProfileRequestContext input) {
                return new CriteriaSet(new HttpServletRequestCriterion());
            }            
        };
    }
    
    /**
     * Set the {@link SessionResolver} to use.
     * 
     * @param resolver  session resolver to use
     */
    public void setSessionResolver(@Nonnull final SessionResolver resolver) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sessionResolver = Constraint.isNotNull(resolver, "SessionResolver cannot be null");
    }
    
    /**
     * Set the creation/lookup strategy for the SessionContext to populate.
     * 
     * @param strategy  creation/lookup strategy
     */
    public void setSessionContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext,SessionContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sessionContextCreationStrategy = Constraint.isNotNull(strategy,
                "SessionContext creation strategy cannot be null");
    }
    
    /**
     * Set the strategy for building the {@link CriteriaSet} to feed into the {@link SessionResolver}.
     * 
     * @param strategy  building strategy
     */
    public void setSessionResolverCriteriaStrategy(
            @Nonnull final Function<ProfileRequestContext,CriteriaSet> strategy) {
        sessionResolverCriteriaStrategy = Constraint.isNotNull(strategy,
                "SessionResolver CriteriaSet strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (!getActivationCondition().equals(Predicates.alwaysFalse()) && sessionResolver == null) {
            throw new ComponentInitializationException("SessionResolver cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        IdPSession session = null;
        try {
            session = sessionResolver.resolveSingle(sessionResolverCriteriaStrategy.apply(profileRequestContext));
            if (session == null) {
                log.debug("{} No session found for client", getLogPrefix());
                return;
            } else if (!session.checkTimeout()) {
                log.info("{} Session {} no longer valid due to inactivity", getLogPrefix(), session.getId());
                return;
            }
            
            final HttpServletRequest request = getHttpServletRequest();
            if (request != null && request.getRemoteAddr() != null) {
                if (!session.checkAddress(request.getRemoteAddr())) {
                    return;
                }
            } else {
                log.info("{} No servlet request or client address available, skipping address check for session {}",
                        getLogPrefix(), session.getId());
            }
            
            final SessionContext sessionCtx = sessionContextCreationStrategy.apply(profileRequestContext);
            if (sessionCtx == null) {
                log.error("{} Unable to create or locate SessionContext", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
                return;
            }
            
            sessionCtx.setIdPSession(session);
            
        } catch (final ResolverException e) {
            log.error("{} Error resolving a session for the active client", getLogPrefix(), e);
        } catch (final SessionException e) {
            log.error("{} Error during timeout or address checking for session {}",getLogPrefix(), session.getId(), e);
        }
    }

}