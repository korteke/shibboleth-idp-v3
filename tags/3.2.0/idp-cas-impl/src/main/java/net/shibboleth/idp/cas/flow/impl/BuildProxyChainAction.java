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
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.cas.ticket.*;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that builds the chain of visited proxies for a successful proxy ticket validation event. Possible outcomes:
 *
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#BrokenProxyChain BrokenProxyChain}</li>
 *     <li>{@link ProtocolError#InvalidTicketType InvalidTicketType}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class BuildProxyChainAction
        extends AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BuildProxyChainAction.class);

    /** Manages CAS tickets. */
    @Nonnull
    private final TicketService ticketService;


    /**
     * Creates a new instance.
     *
     * @param ticketService Ticket service component.
     */
    public BuildProxyChainAction(@Nonnull TicketService ticketService) {
        this.ticketService = Constraint.isNotNull(ticketService, "TicketService cannot be null");
    }

    @Nonnull
    @Override
    protected Event doExecute(
        final @Nonnull RequestContext springRequestContext,
        final @Nonnull ProfileRequestContext profileRequestContext) {

        final TicketValidationResponse response = getCASResponse(profileRequestContext);
        final Ticket ticket = getCASTicket(profileRequestContext);
        if (!(ticket instanceof ProxyTicket)) {
            return ProtocolError.InvalidTicketType.event(this);
        }
        final ProxyTicket pt = (ProxyTicket) ticket;
        ProxyGrantingTicket pgt;
        String pgtId = pt.getPgtId();
        do {
            pgt = ticketService.fetchProxyGrantingTicket(pgtId);
            if (pgt == null) {
                log.debug("PGT {} not found", pgtId);
                return ProtocolError.BrokenProxyChain.event(this);
            }
            response.addProxy(pgt.getService());
            pgtId = pgt.getParentId();
        } while (pgtId != null);

        return null;
    }
}
