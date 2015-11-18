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

package net.shibboleth.idp.cas.audit.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.shibboleth.idp.cas.protocol.ProtocolContext;
import net.shibboleth.idp.cas.protocol.ProxyTicketResponse;
import net.shibboleth.idp.cas.protocol.ServiceTicketResponse;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

/**
 * Looks up the service (proxy) ticket provided in a CAS protocol request or produced in a CAS protocol response.
 *
 * @author Marvin S. Addison
 */
public class TicketLookupFunction implements Function<ProfileRequestContext, String> {
    @Nonnull
    private final Function<ProfileRequestContext, ProtocolContext> protocolContextFunction;

    public TicketLookupFunction() {
        this(new ChildContextLookup<ProfileRequestContext, ProtocolContext>(ProtocolContext.class));
    }

    public TicketLookupFunction(@Nonnull final Function<ProfileRequestContext, ProtocolContext> protocolLookup) {
        protocolContextFunction = Constraint.isNotNull(protocolLookup, "ProtocolContext lookup cannot be null");
    }

    @Nullable
    @Override
    public String apply(@Nonnull final ProfileRequestContext input) {
        final ProtocolContext protocolContext = protocolContextFunction.apply(input);
        if (protocolContext == null || protocolContext.getRequest() ==  null) {
            return null;
        }
        final Object request = protocolContext.getRequest();
        final Object response = protocolContext.getResponse();
        final String ticket;
        if (response instanceof ServiceTicketResponse) {
            ticket = ((ServiceTicketResponse) response).getTicket();
        } else if (response instanceof ProxyTicketResponse) {
            ticket = ((ProxyTicketResponse) response).getPt();
        } else if (request instanceof TicketValidationRequest) {
            ticket = ((TicketValidationRequest) request).getTicket();
        } else {
            ticket = null;
        }
        return ticket;
    }
}
