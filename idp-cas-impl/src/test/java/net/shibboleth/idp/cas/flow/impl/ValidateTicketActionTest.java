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

import net.shibboleth.idp.cas.config.impl.ValidateConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit test for {@link ValidateTicketAction} class.
 *
 * @author Marvin S. Addison
 */
public class ValidateTicketActionTest extends AbstractFlowActionTest {

    private static final String TEST_SERVICE = "https://example.com/widget";

    @Test
    public void testInvalidTicketFormat() throws Exception {
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, "AB-1234-012346abcdef"), null)
                .addRelyingPartyContext(TEST_SERVICE, true, new ValidateConfiguration())
                .build();
        assertEquals(newAction(ticketService).execute(context).getId(), ProtocolError.InvalidTicketFormat.name());
    }

    @Test
    public void testServiceMismatch() throws Exception {
        final ServiceTicket ticket = createServiceTicket(TEST_SERVICE, false);
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest("mismatch", ticket.getId()), null)
                .addRelyingPartyContext(ticket.getService(), true, new ValidateConfiguration())
                .build();
        assertEquals(newAction(ticketService).execute(context).getId(), ProtocolError.ServiceMismatch.name());
    }

    @Test
    public void testTicketExpired() throws Exception {
        final ServiceTicket ticket = createServiceTicket(TEST_SERVICE, false);
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, ticket.getId()), null)
                .addRelyingPartyContext(ticket.getService(), true, new ValidateConfiguration())
                .build();
        // Remove the ticket prior to validation to simulate expiration
        ticketService.removeServiceTicket(ticket.getId());
        assertEquals(newAction(ticketService).execute(context).getId(), ProtocolError.TicketExpired.name());
    }

    @Test
    public void testTicketRetrievalError() throws Exception {
        final TicketService throwingTicketService = mock(TicketService.class);
        when(throwingTicketService.removeServiceTicket(any(String.class))).thenThrow(new RuntimeException("Broken"));
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, "ST-12345"), null)
                .addRelyingPartyContext(TEST_SERVICE, true, new ValidateConfiguration())
                .build();
        assertEquals(
                newAction(throwingTicketService).execute(context).getId(),
                ProtocolError.TicketRetrievalError.name());
    }

    @Test
    public void testServiceTicketValidateSuccess() throws Exception {
        final ServiceTicket ticket = createServiceTicket(TEST_SERVICE, false);
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, ticket.getId()), null)
                .addRelyingPartyContext(ticket.getService(), true, new ValidateConfiguration())
                .build();
        final ValidateTicketAction action = newAction(ticketService);
        assertEquals(action.execute(context).getId(), Events.ServiceTicketValidated.name());
        assertNotNull(action.getCASResponse(getProfileContext(context)));
    }

    @Test
    public void testServiceTicketValidateSuccessWithJSessionID() throws Exception {
        final ServiceTicket ticket = createServiceTicket(TEST_SERVICE + ";jsessionid=abc123", false);
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, ticket.getId()), null)
                .addRelyingPartyContext(ticket.getService(), true, new ValidateConfiguration())
                .build();
        final ValidateTicketAction action = newAction(ticketService);
        assertEquals(action.execute(context).getId(), Events.ServiceTicketValidated.name());
        assertNotNull(action.getCASResponse(getProfileContext(context)));
    }

    @Test
    public void testProxyTicketValidateSuccess() throws Exception {
        final ServiceTicket st = createServiceTicket(TEST_SERVICE, false);
        final ProxyGrantingTicket pgt = createProxyGrantingTicket(st);
        final ProxyTicket pt = createProxyTicket(pgt, "proxyA");
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest("proxyA", pt.getId()), null)
                .addRelyingPartyContext(pt.getService(), true, new ValidateConfiguration())
                .build();
        final ValidateTicketAction action = newAction(ticketService);
        assertEquals(action.execute(context).getId(), Events.ProxyTicketValidated.name());
        assertNotNull(action.getCASResponse(getProfileContext(context)));
    }

    private static ValidateTicketAction newAction(final TicketService service) {
        final ValidateTicketAction action = new ValidateTicketAction(service);
        try {
            action.initialize();
        } catch (ComponentInitializationException e) {
            throw new RuntimeException("Initialization error", e);
        }
        return action;
    }
}
