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
import net.shibboleth.idp.cas.protocol.ProxyTicketRequest;
import net.shibboleth.idp.cas.protocol.ProxyTicketResponse;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link GrantProxyTicketAction}.
 *
 * @author Marvin S. Addison
 */
public class GrantProxyTicketActionTest extends AbstractFlowActionTest {

    @Autowired
    private GrantProxyTicketAction action;

    @Test
    public void testGrantProxyTicketSuccess() throws Exception {
        final String service = "https://s.example.org/";
        final ProxyGrantingTicket pgt = createProxyGrantingTicket(createServiceTicket(service, false));
        final RequestContext context = new TestContextBuilder(ProxyConfiguration.PROFILE_ID)
                .addProtocolContext(new ProxyTicketRequest(pgt.getId(), service), null)
                .addTicketContext(pgt)
                .addRelyingPartyContext(service, true, new ProxyConfiguration())
                .build();
        assertNull(action.execute(context));
        final ProxyTicketResponse response = action.getCASResponse(getProfileContext(context));
        assertNotNull(response);
        assertNotNull(response.getPt());
        final ProxyTicket pt = ticketService.removeProxyTicket(response.getPt());
        assertNotNull(pt);
        assertEquals(pt.getId(), response.getPt());
        assertEquals(pt.getService(), service);
    }
}