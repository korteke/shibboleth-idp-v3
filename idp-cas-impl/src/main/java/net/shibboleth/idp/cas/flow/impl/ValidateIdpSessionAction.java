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

package net.shibboleth.idp.cas.flow.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.idp.cas.ticket.TicketContext;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * IdP session validation for back-channel ticket request and validation. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#SessionExpired SessionExpired}</li>
 *     <li>{@link ProtocolError#SessionRetrievalError SessionRetrievalError}</li>
 * </ul>
 * <p>
 * Requires a {@link TicketContext} bound to the {@link ProfileRequestContext} that is provided to the action.
 * <p>
 * On success, adds a {@link SessionContext} to the profile request context.
 *
 * @author Marvin S. Addison
 */
public class ValidateIdpSessionAction extends AbstractCASProtocolAction {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ValidateIdpSessionAction.class);

    /** Looks up IdP sessions. */
    @Nonnull
    private final SessionResolver sessionResolver;


    /**
     * Creates a new instance.
     * @param resolver IdP session resolver component.
     */
    public ValidateIdpSessionAction(@Nonnull SessionResolver resolver) {
        this.sessionResolver = Constraint.isNotNull(resolver, "Session resolver cannot be null.");
    }

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final Ticket ticket = getCASTicket(profileRequestContext);
        final String sessionId = ticket.getSessionId();
        final IdPSession session;
        try {
            log.debug("Attempting to retrieve session {}", sessionId);
            session = sessionResolver.resolveSingle(new CriteriaSet(new SessionIdCriterion(sessionId)));
        } catch (ResolverException e) {
            log.debug("IdP session retrieval failed with error: {}", e);
            return ProtocolError.SessionRetrievalError.event(this);
        }
        boolean expired = (session == null);
        if (session != null) {
            try {
                expired = !session.checkTimeout();
                log.debug("Session {} expired={}", sessionId, expired);
            } catch (SessionException e) {
                log.debug("Error performing session timeout check. Assuming session has expired.", e);
                expired = true;
            }
        }
        if (expired) {
            return ProtocolError.SessionExpired.event(this);
        }
        final SessionContext sessionContext = new SessionContext();
        sessionContext.setIdPSession(session);
        profileRequestContext.addSubcontext(sessionContext);
        return null;
    }
}
