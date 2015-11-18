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

package net.shibboleth.idp.saml.saml2.profile.impl;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.saml.session.SAML2SPSession;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.SessionManager;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.idp.session.context.LogoutContext;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.idp.session.criterion.SPSessionCriterion;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.common.profile.SAMLEventIds;
import org.opensaml.saml.ext.saml2aslo.Asynchronous;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.profile.SAML2ObjectSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;

/**
 * Profile action that processes a {@link LogoutRequest} by resolving matching sessions, and destroys them,
 * populating the associated {@link SPSession} objects (excepting the one initiating the logout) into a
 * {@link LogoutContext}.
 * 
 * <p>A {@link SubjectContext} is also populated. If and only if a single {@link IdPSession} is resolved,
 * a {@link SessionContext} is also populated.</p>
 * 
 * <p>Each {@link SPSession} is also assigned a unique number and inserted into the map
 * returned by {@link LogoutContext#getKeyedSessionMap()}.</p> 
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#IO_ERROR}
 * @event {@link SAMLEventIds#SESSION_NOT_FOUND}
 * @post The matching session(s) are destroyed.
 * @post If a {@link IdPSession} was found, then a {@link SubjectContext} and {@link LogoutContext} will be populated.
 * @post If a single {@link IdPSession} was found, then a {@link SessionContext} will be populated.
 */
public class ProcessLogoutRequest extends AbstractProfileAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ProcessLogoutRequest.class);
    
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
    
    /** Lookup strategy for {@link LogoutRequest} to process. */
    @Nonnull private Function<ProfileRequestContext,LogoutRequest> logoutRequestLookupStrategy;
    
    /** LogoutRequest to process. */
    @Nullable private LogoutRequest logoutRequest;
    
    /** Constructor. */
    public ProcessLogoutRequest() {
        subjectContextCreationStrategy = new ChildContextLookup<>(SubjectContext.class, true);
        sessionContextCreationStrategy = new ChildContextLookup<>(SessionContext.class, true);
        logoutContextCreationStrategy = new ChildContextLookup<>(LogoutContext.class, true);
        
        sessionResolverCriteriaStrategy = new Function<ProfileRequestContext,CriteriaSet>() {
            public CriteriaSet apply(ProfileRequestContext input) {
                if (logoutRequest != null && logoutRequest.getIssuer() != null && logoutRequest.getNameID() != null) {
                    return new CriteriaSet(new SPSessionCriterion(logoutRequest.getIssuer().getValue(),
                            logoutRequest.getNameID().getValue()));
                } else {
                    return new CriteriaSet();
                }
            }
        };
    
        logoutRequestLookupStrategy = Functions.compose(new MessageLookup<>(LogoutRequest.class),
                new InboundMessageContextLookup());
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
     * Set the creation/lookup strategy for the {@link SubjectContext} to populate.
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
     * Set the creation/lookup strategy for the {@link SessionContext} to populate.
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
     * Set the creation/lookup strategy for the {@link LogoutContext} to populate.
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
    
    /**
     * Set the lookup strategy for the {@link LogoutRequest} to process.
     * 
     * @param strategy  lookup strategy
     */
    public void setLogoutRequestLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,LogoutRequest> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        logoutRequestLookupStrategy = Constraint.isNotNull(strategy, "LogoutRequest lookup strategy cannot be null");
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

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        logoutRequest = logoutRequestLookupStrategy.apply(profileRequestContext);
        if (logoutRequest == null) {
            log.warn("{} No LogoutRequest found to process", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        if (log.isDebugEnabled() && logoutRequest.getExtensions() != null
                && !logoutRequest.getExtensions().getUnknownXMLObjects(Asynchronous.DEFAULT_ELEMENT_NAME).isEmpty()) {
            log.debug("{} LogoutRequest contained Asynchronous extension", getLogPrefix());
        }
        
        return true;
    }
    
// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        try {
            final Iterable<IdPSession> sessions =
                    sessionResolver.resolve(sessionResolverCriteriaStrategy.apply(profileRequestContext));
            final Iterator<IdPSession> sessionIterator = sessions.iterator();

            IdPSession single = null;
            LogoutContext logoutCtx = null;
            
            int count = 1;
            
            while (sessionIterator.hasNext()) {
                final IdPSession session = sessionIterator.next();
                
                if (!sessionMatches(session)) {
                    log.debug("{} IdP session {} does not contain a matching SP session", getLogPrefix(),
                            session.getId());
                    continue;
                }

                log.debug("{} LogoutRequest matches IdP session {}", getLogPrefix(), session.getId());
                
                if (logoutCtx == null) {
                    logoutCtx = logoutContextCreationStrategy.apply(profileRequestContext);
                    if (logoutCtx == null) {
                        log.error("{} Unable to create or locate LogoutContext", getLogPrefix());
                        ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
                        return;
                    }

                    final SubjectContext subjectCtx = subjectContextCreationStrategy.apply(profileRequestContext);
                    if (subjectCtx != null) {
                        subjectCtx.setPrincipalName(session.getPrincipalName());
                    }
                    single = session;
                } else {
                    single = null;
                }
                
                for (final SPSession spSession : session.getSPSessions()) {
                    if (!sessionMatches(spSession)) {
                        logoutCtx.getSessionMap().put(spSession.getId(), spSession);
                        logoutCtx.getKeyedSessionMap().put(Integer.toString(count++), spSession);
                    }
                }

                try {
                    sessionManager.destroySession(session.getId(), true);
                } catch (final SessionException e) {
                    log.error("{} Error destroying session {}", getLogPrefix(), session.getId(), e);
                    ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
                    return;
                }
            }
            
            if (single != null) {
                final SessionContext sessionCtx = sessionContextCreationStrategy.apply(profileRequestContext);
                if (sessionCtx != null) {
                    sessionCtx.setIdPSession(single);
                }
            } else if (logoutCtx == null) {
                log.info("{} No active session(s) found matching LogoutRequest", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, SAMLEventIds.SESSION_NOT_FOUND);
            }
            
        } catch (final ResolverException e) {
            log.error("{} Error resolving matching session(s)", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, SAMLEventIds.SESSION_NOT_FOUND);
        }
    }
// Checkstyle: CyclomaticComplexity ON

    /**
     * Check if the session contains a {@link SAML2SPSession} with the appropriate service ID and SessionIndex.
     * 
     * @param session {@link IdPSession} to check
     * 
     * @return  true iff the set of {@link SPSession}s includes one applicable to the logout request
     */
    private boolean sessionMatches(@Nonnull final IdPSession session) {
        
        for (final SPSession spSession : session.getSPSessions()) {
            if (sessionMatches(spSession)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the {@link SPSession} has the appropriate service ID and SessionIndex.
     * 
     * @param session {@link SPSession} to check
     * 
     * @return  true iff the {@link SPSession} directly matches the logout request
     */
    private boolean sessionMatches(@Nonnull final SPSession session) {
        if (session instanceof SAML2SPSession) {
            final SAML2SPSession saml2Session = (SAML2SPSession) session;
            
            if (!saml2Session.getId().equals(logoutRequest.getIssuer().getValue())) {
                return false;
            } else if (!SAML2ObjectSupport.areNameIDsEquivalent(
                    logoutRequest.getNameID(), saml2Session.getNameID())) {
                return false;
            } else if (logoutRequest.getSessionIndexes().isEmpty()) {
                return true;
            }
            
            for (final SessionIndex index : logoutRequest.getSessionIndexes()) {
                if (index.getSessionIndex() != null
                        && index.getSessionIndex().equals(saml2Session.getSessionIndex())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}