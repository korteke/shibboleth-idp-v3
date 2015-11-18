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
import net.shibboleth.idp.cas.config.impl.ProxyConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.ProxyTicketRequest;
import net.shibboleth.idp.cas.protocol.ProxyTicketResponse;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.joda.time.DateTime;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Generates and stores a CAS protocol proxy ticket. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#TicketCreationError TicketCreationError}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class GrantProxyTicketAction extends AbstractCASProtocolAction<ProxyTicketRequest, ProxyTicketResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(GrantProxyTicketAction.class);


    /** Profile configuration lookup function. */
    private final ConfigLookupFunction<ProxyConfiguration> configLookupFunction =
            new ConfigLookupFunction<>(ProxyConfiguration.class);

    /** Manages CAS tickets. */
    @Nonnull
    private final TicketService ticketService;


    /**
     * Creates a new instance.
     *
     * @param ticketService Ticket service component.
     */
    public GrantProxyTicketAction(final TicketService ticketService) {
        this.ticketService = Constraint.isNotNull(ticketService, "TicketService cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final ProxyTicketRequest request = getCASRequest(profileRequestContext);
        final ProxyGrantingTicket pgt = (ProxyGrantingTicket) getCASTicket(profileRequestContext);
        final ProxyConfiguration config = configLookupFunction.apply(profileRequestContext);
        if (config == null) {
            throw new IllegalStateException("Proxy ticket configuration undefined");
        }
        if (config.getSecurityConfiguration() == null || config.getSecurityConfiguration().getIdGenerator() == null) {
            throw new IllegalStateException(
                    "Invalid proxy ticket configuration: SecurityConfiguration#idGenerator undefined");
        }
        final ProxyTicket pt;
        try {
            log.debug("Granting proxy ticket for {}", request.getTargetService());
            pt = ticketService.createProxyTicket(
                    config.getSecurityConfiguration().getIdGenerator().generateIdentifier(),
                    DateTime.now().plus(config.getTicketValidityPeriod()).toInstant(),
                    pgt,
                    request.getTargetService());
        } catch (RuntimeException e) {
            log.error("Failed granting proxy ticket due to error.", e);
            return ProtocolError.TicketCreationError.event(this);
        }
        log.info("Granted proxy ticket for {}", request.getTargetService());
        setCASResponse(profileRequestContext, new ProxyTicketResponse(pt.getId()));
        return null;
    }
}
