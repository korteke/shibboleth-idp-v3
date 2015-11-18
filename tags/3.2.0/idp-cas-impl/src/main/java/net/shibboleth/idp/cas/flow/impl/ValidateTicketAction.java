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

import net.shibboleth.idp.cas.config.impl.ConfigLookupFunction;
import net.shibboleth.idp.cas.config.impl.LoginConfiguration;
import net.shibboleth.idp.cas.config.impl.ProxyConfiguration;
import net.shibboleth.idp.cas.config.impl.ValidateConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * CAS protocol service ticket validation action emits one of the following events based on validation result:
 *
 * <ul>
 *     <li>{@link Events#ServiceTicketValidated ServiceTicketValidated}</li>
 *     <li>{@link Events#ProxyTicketValidated ProxyTicketValidated}</li>
 *     <li>{@link ProtocolError#InvalidTicketFormat InvalidTicketFormat}</li>
 *     <li>{@link ProtocolError#ServiceMismatch ServiceMismatch}</li>
 *     <li>{@link ProtocolError#TicketExpired TicketExpired}</li>
 *     <li>{@link ProtocolError#TicketRetrievalError TicketRetrievalError}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class ValidateTicketAction extends AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ValidateTicketAction.class);

    /** Profile configuration lookup function. */
    private final ConfigLookupFunction<ValidateConfiguration> configLookupFunction =
            new ConfigLookupFunction<>(ValidateConfiguration.class);

    /** Manages CAS tickets. */
    @Nonnull
    private final TicketService ticketService;


    /**
     * Creates a new instance.
     *
     * @param ticketService Ticket service component.
     */
    public ValidateTicketAction(@Nonnull TicketService ticketService) {
        this.ticketService = Constraint.isNotNull(ticketService, "TicketService cannot be null");
    }

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final ValidateConfiguration config = configLookupFunction.apply(profileRequestContext);
        if (config == null) {
            log.info("Ticket validation configuration undefined");
            return ProtocolError.IllegalState.event(this);
        }

        final TicketValidationRequest request = getCASRequest(profileRequestContext);
        final Ticket ticket;
        try {
            final String ticketId = request.getTicket();
            log.debug("Attempting to validate {}", ticketId);
            if (ticketId.startsWith(LoginConfiguration.DEFAULT_TICKET_PREFIX)) {
                ticket = ticketService.removeServiceTicket(request.getTicket());
            } else if (ticketId.startsWith(ProxyConfiguration.DEFAULT_TICKET_PREFIX)) {
                ticket = ticketService.removeProxyTicket(ticketId);
            } else {
                return ProtocolError.InvalidTicketFormat.event(this);
            }
            if (ticket != null) {
                log.debug("Found and removed {}/{} from ticket store", ticket, ticket.getSessionId());
            }
        } catch (RuntimeException e) {
            log.debug("CAS ticket retrieval failed with error: {}", e);
            return ProtocolError.TicketRetrievalError.event(this);
        }

        if (ticket == null || ticket.getExpirationInstant().isBeforeNow()) {
            return ProtocolError.TicketExpired.event(this);
        }

        if (config.getServiceComparator().compare(ticket.getService(), request.getService()) != 0) {
            log.debug("Service issued for {} does not match {}", ticket.getService(), request.getService());
            return ProtocolError.ServiceMismatch.event(this);
        }

        log.info("Successfully validated {} for {}", request.getTicket(), request.getService());
        setCASResponse(profileRequestContext, new TicketValidationResponse());
        setCASTicket(profileRequestContext, ticket);
        if (ticket instanceof ProxyTicket) {
            return Events.ProxyTicketValidated.event(this);
        }
        return Events.ServiceTicketValidated.event(this);
    }
}
