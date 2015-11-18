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

package net.shibboleth.idp.test.flows.cas;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.idp.session.impl.StorageBackedSessionManager;
import net.shibboleth.idp.test.flows.AbstractFlowTest;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests login flow with initial-authn enabled.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = {
        "/test/test-cas-beans.xml"
})
public class LoginFlowInitialAuthnTest extends AbstractFlowTest {
    /** Flow id. */
    @Nonnull
    private static String FLOW_ID = "cas/login";

    @Autowired
    private StorageBackedSessionManager sessionManager;

    @Autowired
    private TicketService ticketService;

    @BeforeClass
    public void enableInitialAuthn() {
        System.setProperty("idp.authn.flows.initial", "Password");
    }

    @AfterClass
    public void disableInitialAuthn() {
        System.setProperty("idp.authn.flows.initial", "");
    }

    @Test
    public void testLoginStartSession() throws Exception {
        final String service = "https://start.example.org/";
        externalContext.getMockRequestParameterMap().put("service", service);
        overrideEndStateOutput(FLOW_ID, "RedirectToService");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "RedirectToService");
        final String url = externalContext.getExternalRedirectUrl();
        assertTrue(url.contains("ticket=ST-"));
        final String ticketId = url.substring(url.indexOf("ticket=") + 7);
        final Ticket st = ticketService.removeServiceTicket(ticketId);
        assertNotNull(st);
        final IdPSession session = sessionManager.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(st.getSessionId())));
        assertNotNull(session);

        final ProfileRequestContext prc = (ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME);
        assertNotNull(prc.getSubcontext(SubjectContext.class));
    }
}
