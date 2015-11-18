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

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.SessionManager;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.idp.session.context.LogoutContext;
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
 * Profile action that resolves an active session from the profile request, and destroys it,
 * populating the associated {@link SPSession} objects into a {@link LogoutContext}.
 * 
 * <p>A {@link SubjectContext} and {@link SessionContext} are also populated.</p>
 * 
 * <p>Each {@link SPSession} is also assigned a unique number and inserted into the map
 * returned by {@link LogoutContext#getKeyedSessionMap()}.</p> 
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link org.opensaml.profile.action.EventIds#INVALID_PROFILE_CTX}
 * @event {@link org.opensaml.profile.action.EventIds#IO_ERROR}
 * @post The matching {@link IdPSession} is destroyed.
 * @post If a {@link IdPSession} was found, then a {@link SubjectContext} and {@link LogoutContext} will be populated.
 */
public class ProcessLogout extends AbstractProfileAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ProcessLogout.class);
    
    /** Session resolver. */
    @NonnullAfterInit private SessionResolver sessionResolver;

    /** Session manager. */
    @NonnullAfterInit private SessionManager sessionManager;
    
    /** Creation/lookup function for SubjectContext. */
    @Nonnull private Function<ProfileRequestContext,SubjectContext> subjectContextCreationStrategy;

    /** Creation/lookup function for SessionContext. */
    @Nonnull private Function<ProfileRequestContext,SessionContext> sessionContextCreationStrategy;

    /** Creation/lookup function for LogoutContext. */
    @Nonnull private Function<ProfileRequestContext,LogoutContext> logoutContextCreationStrategy;
    
    /** Function to return {@link CriteriaSet} to give to session resolver. */
    @Nonnull private Function<ProfileRequestContext,CriteriaSet> sessionResolverCriteriaStrategy;
    
    /** Constructor. */
    public ProcessLogout() {
        subjectContextCreationStrategy = new ChildContextLookup<>(SubjectContext.class, true);
        sessionContextCreationStrategy = new ChildContextLookup<>(SessionContext.class, true);
        logoutContextCreationStrategy = new ChildContextLookup<>(LogoutContext.class, true);
        
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
     * Set the {@link SessionManager} to use.
     * 
     * @param manager  session manager to use
     */
    public void setSessionManager(@Nonnull final SessionManager manager) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sessionManager = Constraint.isNotNull(manager, "SessionManager cannot be null");
    }
    
    /**
     * Set the creation/lookup strategy for the SubjectContext to populate.
     * 
     * @param strategy  creation/lookup strategy
     */
    public void setSubjectContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext,SubjectContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        subjectContextCreationStrategy = Constraint.isNotNull(strategy,
                "SubjectContext creation strategy cannot be null");
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
     * Set the creation/lookup strategy for the LogoutContext to populate.
     * 
     * @param strategy  creation/lookup strategy
     */
    public void setLogoutContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext,LogoutContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        logoutContextCreationStrategy = Constraint.isNotNull(strategy,
                "LogoutContext creation strategy cannot be null");
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
        
        if (!getActivationCondition().equals(Predicates.alwaysFalse())) {
            if (sessionResolver == null) {
                throw new ComponentInitializationException("SessionResolver cannot be null");
            } else if (sessionManager == null) {
                throw new ComponentInitializationException("SessionManager cannot be null");
            }
        }
    }

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        try {
            final IdPSession session =
                    sessionResolver.resolveSingle(sessionResolverCriteriaStrategy.apply(profileRequestContext));
            if (session == null) {
                log.info("{} No active session found matching current request", getLogPrefix());
                return;
            }
            
            final HttpServletRequest request = getHttpServletRequest();
            if (request != null && request.getRemoteAddr() != null) {
                try {
                    if (!session.checkAddress(request.getRemoteAddr())) {
                        return;
                    }
                } catch (final SessionException e) {
                    log.error("{} Error binding session to client address", getLogPrefix(), e);
                    ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
                    return;
                } 
            } else {
                log.info("{} No servlet request or client address available, skipping address check for sessions",
                        getLogPrefix());
            }

            final SubjectContext subjectCtx = subjectContextCreationStrategy.apply(profileRequestContext);
            if (subjectCtx != null) {
                subjectCtx.setPrincipalName(session.getPrincipalName());
            }

            final SessionContext sessionCtx = sessionContextCreationStrategy.apply(profileRequestContext);
            if (sessionCtx != null) {
                sessionCtx.setIdPSession(session);
            }

            final LogoutContext logoutCtx = logoutContextCreationStrategy.apply(profileRequestContext);
            if (logoutCtx == null) {
                log.error("{} Unable to create or locate LogoutContext", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
                return;
            }
            
            int count = 1;
            for (final SPSession spSession : session.getSPSessions()) {
                logoutCtx.getSessionMap().put(spSession.getId(), spSession);
                logoutCtx.getKeyedSessionMap().put(Integer.toString(count++), spSession);
            }
                
            try {
                sessionManager.destroySession(session.getId(), true);
            } catch (final SessionException e) {
                log.error("{} Error destroying session", getLogPrefix(), e);
                ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
            }
        } catch (final ResolverException e) {
            log.error("{} Error resolving matching session(s)", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
}