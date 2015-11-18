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
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.Ticket;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Ensures that a service ticket validation request that specifies renew=true matches the renew flag on the ticket
 * that is presented for validation. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#TicketNotFromRenew ticketNotFromRenew}</li>
 *     <li>{@link ProtocolError#RenewIncompatibleWithProxy renewIncompatibleWithProxy}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class ValidateRenewAction extends AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ValidateRenewAction.class);


    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final TicketValidationRequest request = getCASRequest(profileRequestContext);
        final Ticket ticket = getCASTicket(profileRequestContext);
        if (ticket instanceof ServiceTicket) {
            if (request.isRenew() != ((ServiceTicket) ticket).isRenew()) {
                log.debug("Renew=true requested at validation time but ticket not issued with renew=true.");
                return ProtocolError.TicketNotFromRenew.event(this);
            }
        } else {
            // Proxy ticket validation
            if (request.isRenew()) {
                return ProtocolError.RenewIncompatibleWithProxy.event(this);
            }
        }
        return null;
    }
}
