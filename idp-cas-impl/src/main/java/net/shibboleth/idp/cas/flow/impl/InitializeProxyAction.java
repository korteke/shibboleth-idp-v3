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
import net.shibboleth.idp.cas.protocol.ProtocolParam;
import net.shibboleth.idp.cas.protocol.ProxyTicketRequest;
import net.shibboleth.idp.cas.protocol.ProxyTicketResponse;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.TicketContext;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Initializes the CAS protocol interaction at the <code>/proxy</code> URI and returns one of the following events:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#ServiceNotSpecified ServiceNotSpecified}</li>
 *     <li>{@link ProtocolError#TicketExpired TicketExpired}</li>
 *     <li>{@link ProtocolError#TicketNotSpecified TicketNotSpecified}</li>
 *     <li>{@link ProtocolError#TicketRetrievalError TicketRetrievalError}</li>
 * </ul>
 * <p>
 * Creates the following contexts on success:
 * <ul>
 *     <li><code>ProfileRequestContext</code> -&gt; {@link net.shibboleth.idp.cas.protocol.ProtocolContext}</li>
 *     <li><code>ProfileRequestContext</code> -&gt; <code>ProtocolContext</code> -&gt; {@link TicketContext}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class InitializeProxyAction extends AbstractCASProtocolAction<ProxyTicketRequest, ProxyTicketResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(InitializeProxyAction.class);

    /** Manages CAS tickets. */
    @Nonnull
    private final TicketService ticketService;


    public InitializeProxyAction(@Nonnull final TicketService ticketService) {
        this.ticketService = Constraint.isNotNull(ticketService, "Ticket service cannot be null.");
    }

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final ParameterMap params = springRequestContext.getRequestParameters();
        String service = params.get(ProtocolParam.TargetService.id());
        Event result = null;
        if (service == null) {
            service = ProtocolError.ServiceNotSpecified.getDetailCode();
            result = ProtocolError.ServiceNotSpecified.event(this);
        }
        String ticket = params.get(ProtocolParam.Pgt.id());
        if (ticket == null) {
            ticket = ProtocolError.TicketNotSpecified.getDetailCode();
            result = ProtocolError.TicketNotSpecified.event(this);
        }
        final ProxyTicketRequest proxyTicketRequest = new ProxyTicketRequest(ticket, service);
        setCASRequest(profileRequestContext, proxyTicketRequest);
        if (result == null) {
            try {
                log.debug("Fetching proxy-granting ticket {}", proxyTicketRequest.getPgt());
                final ProxyGrantingTicket pgt = ticketService.fetchProxyGrantingTicket(proxyTicketRequest.getPgt());
                if (pgt == null) {
                    return ProtocolError.TicketExpired.event(this);
                }
                setCASTicket(profileRequestContext, pgt);
            } catch (RuntimeException e) {
                log.error("Failed looking up " + proxyTicketRequest.getPgt(), e);
                return ProtocolError.TicketRetrievalError.event(this);
            }
        }
        return result;
    }
}
