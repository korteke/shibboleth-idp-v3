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

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.impl.TicketIdentifierGenerationStrategy;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionManager;
import net.shibboleth.idp.test.flows.AbstractFlowTest;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the flow behind the <code>/proxyValidate</code> endpoint.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = {
        "/test/test-cas-beans.xml",
})
public class ProxyValidateFlowTest extends AbstractFlowTest {

    /** Flow id. */
    @Nonnull
    private static String FLOW_ID = "cas/proxyValidate";

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private TestProxyAuthenticator testProxyAuthenticator;

    @Test
    public void testSuccess() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ProxyTicket ticket = createProxyTicket(session.getId());

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertFalse(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertTrue(responseBody.contains("<cas:proxy>https://service.example.org/</cas:proxy>"));
    }

    @Test
    public void testFailureTicketExpired() throws Exception {
        externalContext.getMockRequestParameterMap().put("service", "https://test.example.org/");
        externalContext.getMockRequestParameterMap().put("ticket", "PT-123-ABC");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        final String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_TICKET\""));
        assertTrue(responseBody.contains("E_TICKET_EXPIRED"));
    }

    @Test
    public void testFailureSessionExpired() throws Exception {
        final ProxyTicket ticket = createProxyTicket("No-Such-SessionId");

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        final String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_TICKET\""));
        assertTrue(responseBody.contains("E_SESSION_EXPIRED"));
    }

    @Test
    public void testSuccessWithProxy() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ProxyTicket ticket = createProxyTicket(session.getId());

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        externalContext.getMockRequestParameterMap().put("pgtUrl", "https://proxy.example.com/");

        testProxyAuthenticator.setFailureFlag(false);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertTrue(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertTrue(responseBody.contains("<cas:proxy>https://service.example.org/</cas:proxy>"));
    }

    // This test must execute after testSuccessWithProxy to prevent concurrency problems
    // on shared testProxyAuthenticator component
    @Test(dependsOnMethods = "testSuccessWithProxy")
    public void testProxyCallbackAuthnFailure() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ProxyTicket ticket = createProxyTicket(session.getId());

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        externalContext.getMockRequestParameterMap().put("pgtUrl", "https://proxy.example.com/");

        testProxyAuthenticator.setFailureFlag(true);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_REQUEST\""));
        assertTrue(responseBody.contains("E_PROXY_CALLBACK_AUTH_FAILURE"));
    }

    @Test
    public void testFailureBrokenProxyChain() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ProxyTicket ticket = createProxyTicket(session.getId());

        ticketService.removeProxyGrantingTicket(ticket.getPgtId());

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_TICKET\""));
        assertTrue(responseBody.contains("E_BROKEN_PROXY_CHAIN"));
    }

    private ProxyTicket createProxyTicket(final String sessionId) {
        final ServiceTicket st = ticketService.createServiceTicket(
                new TicketIdentifierGenerationStrategy("ST", 25).generateIdentifier(),
                DateTime.now().plusSeconds(5).toInstant(),
                sessionId,
                "https://service.example.org/",
                false);
        final ProxyGrantingTicket pgt = ticketService.createProxyGrantingTicket(
                new TicketIdentifierGenerationStrategy("PGT", 50).generateIdentifier(),
                DateTime.now().plusSeconds(10).toInstant(),
                st);
        return ticketService.createProxyTicket(
                new TicketIdentifierGenerationStrategy("PT", 25).generateIdentifier(),
                DateTime.now().plusSeconds(5).toInstant(),
                pgt,
                "https://proxyA.example.org/");
    }
}
