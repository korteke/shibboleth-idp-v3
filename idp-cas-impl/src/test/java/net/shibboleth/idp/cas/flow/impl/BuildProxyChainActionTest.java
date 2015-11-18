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

import net.shibboleth.idp.cas.config.impl.ProxyConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link BuildProxyChainAction}.
 *
 * @author Marvin S. Addison
 */
public class BuildProxyChainActionTest extends AbstractFlowActionTest {
    @Autowired
    private BuildProxyChainAction action;

    @Test
    public void testBuildChainLength2() throws Exception {
        final ServiceTicket st = createServiceTicket("proxyA", true);
        final ProxyGrantingTicket pgtA = createProxyGrantingTicket(st);
        final ProxyTicket ptA = createProxyTicket(pgtA, "proxiedByA");
        final ProxyGrantingTicket pgtB = createProxyGrantingTicket(ptA);
        final ProxyTicket ptB = createProxyTicket(pgtB, "proxiedByB");
        final TicketValidationRequest request = new TicketValidationRequest("proxiedByB", ptB.getId());
        final TicketValidationResponse response = new TicketValidationResponse();
        final RequestContext context = new TestContextBuilder(ProxyConfiguration.PROFILE_ID)
                .addProtocolContext(request, response)
                .addTicketContext(ptB)
                .build();
        assertNull(action.execute(context));
        assertEquals(response.getProxies().size(), 2);
        assertEquals(response.getProxies().get(0), "proxiedByA");
        assertEquals(response.getProxies().get(1), "proxyA");
    }


    @Test
    public void testBrokenProxyChain() throws Exception {
        final ServiceTicket st = createServiceTicket("proxyA", true);
        final ProxyGrantingTicket pgtA = createProxyGrantingTicket(st);
        final ProxyTicket ptA = createProxyTicket(pgtA, "proxiedByA");
        final ProxyGrantingTicket pgtB = createProxyGrantingTicket(ptA);
        final ProxyTicket ptB = createProxyTicket(pgtB, "proxiedByB");
        final TicketValidationRequest request = new TicketValidationRequest("proxiedByB", ptB.getId());
        final TicketValidationResponse response = new TicketValidationResponse();

        // Remove first proxy-granting ticket to break chain
        ticketService.removeProxyGrantingTicket(pgtA.getId());

        final RequestContext context = new TestContextBuilder(ProxyConfiguration.PROFILE_ID)
                .addProtocolContext(request, response)
                .addTicketContext(ptB)
                .build();
        assertEquals(action.execute(context).getId(), ProtocolError.BrokenProxyChain.name());
    }
}