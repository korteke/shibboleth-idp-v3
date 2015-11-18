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

import java.net.URI;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.cas.proxy.ProxyAuthenticator;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionManager;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.idp.test.flows.AbstractFlowTest;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.joda.time.DateTime;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Tests the flow behind the <code>/serviceValidate</code> endpoint.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = {
        "/test/test-cas-beans.xml",
})
public class ServiceValidateFlowTest extends AbstractFlowTest {

    /** Flow id. */
    @Nonnull
    private static String FLOW_ID = "cas/serviceValidate";

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SessionResolver sessionResolver;

    @Autowired
    private TestProxyAuthenticator testProxyAuthenticator;

    @Test
    public void testInvalidRequestNoTicket() throws Exception {
        externalContext.getMockRequestParameterMap().put("service", "https://test.example.org/");
        overrideEndStateOutput(FLOW_ID, "ProtocolErrorView");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ProtocolErrorView");
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_REQUEST\">"));
        assertTrue(responseBody.contains("E_TICKET_NOT_SPECIFIED"));
    }

    @Test
    public void testInvalidRequestNoService() throws Exception {
        externalContext.getMockRequestParameterMap().put("ticket", "ST-123-ABC");
        overrideEndStateOutput(FLOW_ID, "ProtocolErrorView");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ProtocolErrorView");
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_REQUEST\">"));
        assertTrue(responseBody.contains("E_SERVICE_NOT_SPECIFIED"));
    }

    @Test
    public void testSuccess() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133132-ompog68ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://test.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        overrideEndStateOutput(FLOW_ID, "ValidateSuccess");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertTrue(responseBody.contains("<cas:attributes>"));
        assertTrue(responseBody.contains("<cas:uid>john</cas:uid>"));
        assertTrue(responseBody.contains("<cas:eduPersonPrincipalName>john</cas:eduPersonPrincipalName>"));
        assertTrue(responseBody.contains("<cas:mail>john@example.org</cas:mail>"));
        assertFalse(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertFalse(responseBody.contains("<cas:proxies>"));
        assertPopulatedAttributeContext((ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME));

        final IdPSession updatedSession = sessionResolver.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(session.getId())));
        assertNotNull(updatedSession);
        assertEquals(updatedSession.getSPSessions().size(), 0);
    }

    @Test
    public void testSuccessWithSLOParticipant() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133132-ompog68ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://slo.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        overrideEndStateOutput(FLOW_ID, "ValidateSuccess");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertFalse(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertFalse(responseBody.contains("<cas:proxies>"));
        assertPopulatedAttributeContext((ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME));

        final IdPSession updatedSession = sessionResolver.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(session.getId())));
        assertNotNull(updatedSession);
        assertEquals(updatedSession.getSPSessions().size(), 1);
    }

    @Test
    public void testFailureTicketExpired() throws Exception {
        externalContext.getMockRequestParameterMap().put("service", "https://test.example.org/");
        externalContext.getMockRequestParameterMap().put("ticket", "ST-123-ABC");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        final String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_TICKET\""));
        assertTrue(responseBody.contains("E_TICKET_EXPIRED"));
    }

    @Test
    public void testFailureSessionExpired() throws Exception {
        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133227-o5ly5eArKccYkb2P+80uRE7Gq9xSAqWtOg",
                DateTime.now().plusSeconds(5).toInstant(),
                "No-Such-Session-Id",
                "https://test.example.org/",
                false);

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

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133132-ompog68ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://test.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        externalContext.getMockRequestParameterMap().put("pgtUrl", "https://proxy.example.com/");

        testProxyAuthenticator.setFailureFlag(false);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertTrue(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertFalse(responseBody.contains("<cas:proxies>"));
        assertPopulatedAttributeContext((ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME));
    }

    @Test
    public void testProxyCallbackAuthnFailure() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133132-ompog68ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://test.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        externalContext.getMockRequestParameterMap().put("pgtUrl", "https://proxy.example.com/");
        overrideEndStateOutput(FLOW_ID, "ValidateSuccess");

        testProxyAuthenticator.setFailureFlag(true);

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "ProtocolErrorView");
        assertTrue(responseBody.contains("<cas:authenticationFailure code=\"INVALID_REQUEST\""));
        assertTrue(responseBody.contains("E_PROXY_CALLBACK_AUTH_FAILURE"));
    }

    @Test
    public void testSuccessWithAltUsername() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-1415133132-pnqph79ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://alt-username.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        overrideEndStateOutput(FLOW_ID, "ValidateSuccess");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john@example.org</cas:user>"));
        assertFalse(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertFalse(responseBody.contains("<cas:proxies>"));
        assertPopulatedAttributeContext((ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME));

        final IdPSession updatedSession = sessionResolver.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(session.getId())));
        assertNotNull(updatedSession);
        assertEquals(updatedSession.getSPSessions().size(), 0);
    }

    @Test
    public void testSuccessNoAttributes() throws Exception {
        final String principal = "john";
        final IdPSession session = sessionManager.createSession(principal);
        session.addAuthenticationResult(
                new AuthenticationResult("authn/Password", new UsernamePrincipal(principal)));

        final ServiceTicket ticket = ticketService.createServiceTicket(
                "ST-2718281828-ompog68ygxKyX9BPwPuw0hESQBjuA",
                DateTime.now().plusSeconds(5).toInstant(),
                session.getId(),
                "https://no-attrs.example.org/",
                false);

        externalContext.getMockRequestParameterMap().put("service", ticket.getService());
        externalContext.getMockRequestParameterMap().put("ticket", ticket.getId());
        overrideEndStateOutput(FLOW_ID, "ValidateSuccess");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final String responseBody = response.getContentAsString();
        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(outcome.getId(), "ValidateSuccess");
        assertTrue(responseBody.contains("<cas:authenticationSuccess>"));
        assertTrue(responseBody.contains("<cas:user>john</cas:user>"));
        assertFalse(responseBody.contains("<cas:attributes>"));
        assertFalse(responseBody.contains("<cas:uid>john</cas:uid>"));
        assertFalse(responseBody.contains("<cas:eduPersonPrincipalName>john</cas:eduPersonPrincipalName>"));
        assertFalse(responseBody.contains("<cas:mail>john@example.org</cas:mail>"));
        assertFalse(responseBody.contains("<cas:proxyGrantingTicket>"));
        assertFalse(responseBody.contains("<cas:proxies>"));

        final IdPSession updatedSession = sessionResolver.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(session.getId())));
        assertNotNull(updatedSession);
        assertEquals(updatedSession.getSPSessions().size(), 0);
    }


    private void assertPopulatedAttributeContext(final ProfileRequestContext prc) {
        assertNotNull(prc);
        final RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class, false);
        assertNotNull(rpc);
        final AttributeContext ac= rpc.getSubcontext(AttributeContext.class, false);
        assertNotNull(ac);
        assertFalse(ac.getUnfilteredIdPAttributes().isEmpty());
    }

    @SuppressWarnings("unused")
    private static ProxyAuthenticator<TrustEngine<X509Credential>> mockProxyAuthenticator(final Exception toBeThrown)
            throws Exception {
        final ProxyAuthenticator<TrustEngine<X509Credential>> authenticator = mock(ProxyAuthenticator.class);
        if (toBeThrown != null) {
            doThrow(toBeThrown).when(authenticator).authenticate(any(URI.class), any(TrustEngine.class));
        }
        return authenticator;
    }
}
