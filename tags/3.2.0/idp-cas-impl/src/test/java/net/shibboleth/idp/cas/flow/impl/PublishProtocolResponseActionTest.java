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
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link PublishProtocolMessageAction}.
 *
 * @author Marvin S. Addison
 */
public class PublishProtocolResponseActionTest extends AbstractFlowActionTest {

    @Test
    public void testPublishRequest() throws Exception {
        final PublishProtocolMessageAction action = new PublishProtocolMessageAction(true);
        action.initialize();
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new ServiceTicketRequest("A"), new ServiceTicketResponse("A", "B"))
                .build();
        action.execute(context);
        final ServiceTicketRequest request = (ServiceTicketRequest) context.getFlowScope().get(
                "serviceTicketRequest");
        assertNotNull(request);
        assertEquals(request.getService(), "A");
    }

    @Test
    public void testPublishResponse() throws Exception {
        final PublishProtocolMessageAction action = new PublishProtocolMessageAction(false);
        action.initialize();
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new ServiceTicketRequest("A"), new ServiceTicketResponse("A", "B"))
                .build();
        action.execute(context);
        final ServiceTicketResponse response = (ServiceTicketResponse) context.getFlowScope().get(
                "serviceTicketResponse");
        assertNotNull(response);
        assertEquals(response.getTicket(), "B");
    }

}