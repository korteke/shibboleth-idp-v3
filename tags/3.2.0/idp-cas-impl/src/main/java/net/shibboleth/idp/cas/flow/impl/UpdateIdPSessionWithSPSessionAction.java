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

import net.shibboleth.idp.cas.service.Service;
import net.shibboleth.idp.cas.session.impl.CASSPSession;
import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.idp.cas.ticket.TicketContext;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Conditionally updates the {@link net.shibboleth.idp.session.IdPSession} with a {@link CASSPSession} to support SLO.
 * If the service granted access to indicates participation in SLO via {@link Service#singleLogoutParticipant},
 * then a {@link CASSPSession} is created to track the SP session in order that it may receive SLO messages upon
 * a request to the CAS <code>/logout</code> URI.
 * <p>
 * Requires the following to be available under the {@link ProfileRequestContext}:
 * <ul>
 *     <li>{@link SessionContext}</li>
 *     <li>{@link TicketContext}</li>
 *     <li>{@link net.shibboleth.idp.cas.service.ServiceContext ServiceContext}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class UpdateIdPSessionWithSPSessionAction extends AbstractCASProtocolAction {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(UpdateIdPSessionWithSPSessionAction.class);


    /** Lifetime of sessions to create. */
    @Positive
    @Duration
    private final long sessionLifetime;


    /**
     * Creates a new instance with given parameters.
     *
     * @param lifetime lifetime in milliseconds, determines upper bound for expiration of the
     * {@link CASSPSession} to be created
     */
    public UpdateIdPSessionWithSPSessionAction(@Positive @Duration final long lifetime) {
        sessionLifetime = Constraint.isGreaterThan(0, lifetime, "Lifetime must be greater than 0");
    }

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final Ticket ticket = getCASTicket(profileRequestContext);
        final Service service = getCASService(profileRequestContext);
        if (!service.isSingleLogoutParticipant()) {
            return null;
        }
        final IdPSession session = getIdPSession(profileRequestContext);
        final long now = System.currentTimeMillis();
        final SPSession sps = new CASSPSession(
                ticket.getService(),
                now,
                now + sessionLifetime,
                ticket.getId());
        log.debug("Created SP session {}", sps);
        try {
            session.addSPSession(sps);
        } catch (SessionException e) {
            throw new IllegalStateException("Failed updating IdP session with CAS SP session", e);
        }
        return null;
    }
}
