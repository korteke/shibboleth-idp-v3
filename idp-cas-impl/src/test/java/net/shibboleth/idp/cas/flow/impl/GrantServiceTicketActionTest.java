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

import net.shibboleth.idp.cas.config.impl.LoginConfiguration;
import net.shibboleth.idp.cas.protocol.ServiceTicketRequest;
import net.shibboleth.idp.cas.protocol.ServiceTicketResponse;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link GrantServiceTicketAction}.
 *
 * @author Marvin S. Addison
 */
public class GrantServiceTicketActionTest extends AbstractFlowActionTest {

    @Autowired
    private GrantServiceTicketAction action;


    @DataProvider(name = "messages")
    public Object[][] provideMessages() {
        final ServiceTicketRequest renewedRequest = new ServiceTicketRequest("https://www.example.com/beta");
        renewedRequest.setRenew(true);
        return new Object[][] {
                { new ServiceTicketRequest("https://www.example.com/alpha") },
                { renewedRequest },
        };
    }

    @Test(dataProvider = "messages")
    public void testExecute(final ServiceTicketRequest request) throws Exception {
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(request, null)
                .addSessionContext(mockSession("1234567890", true))
                .addRelyingPartyContext(request.getService(), true, new LoginConfiguration())
                .build();
        assertNull(action.execute(context));
        final ServiceTicketResponse response = action.getCASResponse(getProfileContext(context));
        assertNotNull(response);
        assertNotNull(response.getTicket());
        assertEquals(response.getService(), request.getService());
        final ServiceTicket ticket = ticketService.removeServiceTicket(response.getTicket());
        assertNotNull(ticket);
        assertEquals(ticket.isRenew(), request.isRenew());
        assertEquals(ticket.getId(), response.getTicket());
        assertEquals(ticket.getService(), response.getService());
    }
}
