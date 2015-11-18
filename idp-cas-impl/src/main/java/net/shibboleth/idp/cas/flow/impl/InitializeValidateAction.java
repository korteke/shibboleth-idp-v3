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
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Initializes the CAS protocol interaction at the <code>/login</code> URI and returns one of the following events:
 *
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#ServiceNotSpecified ServiceNotSpecified}</li>
 *     <li>{@link ProtocolError#TicketNotSpecified TicketNotSpecified}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class InitializeValidateAction extends
        AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {
    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final ParameterMap params = springRequestContext.getRequestParameters();
        String service = params.get(ProtocolParam.Service.id());
        Event result = null;
        if (service == null) {
            service = ProtocolError.ServiceNotSpecified.getDetailCode();
            result = ProtocolError.ServiceNotSpecified.event(this);
        }
        String ticket = params.get(ProtocolParam.Ticket.id());
        if (ticket == null) {
            ticket = ProtocolError.TicketNotSpecified.getDetailCode();
            result = ProtocolError.TicketNotSpecified.event(this);
        }
        final TicketValidationRequest ticketValidationRequest = new TicketValidationRequest(service, ticket);

        final String renew = params.get(ProtocolParam.Renew.id());
        if (renew != null) {
            ticketValidationRequest.setRenew(true);
        }
        ticketValidationRequest.setPgtUrl(params.get(ProtocolParam.PgtUrl.id()));

        setCASRequest(profileRequestContext, ticketValidationRequest);

        return result;
    }
}
