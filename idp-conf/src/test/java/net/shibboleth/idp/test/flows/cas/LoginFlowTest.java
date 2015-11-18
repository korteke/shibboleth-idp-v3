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
import javax.servlet.http.Cookie;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.cas.config.impl.LoginConfiguration;
import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.idp.session.impl.StorageBackedSessionManager;
import net.shibboleth.idp.test.flows.AbstractFlowTest;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the flow behind the <code>/login</code> endpoint.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = {
        "/test/test-cas-beans.xml"
})
public class LoginFlowTest extends AbstractFlowTest {

    /** Flow id. */
    @Nonnull
    private static String FLOW_ID = "cas/login";

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StorageBackedSessionManager sessionManager;

    @Autowired
    @Qualifier("shibboleth.RelyingPartyConfigurationResolver")
    private RelyingPartyConfigurationResolver relyingPartyConfigurationResolver;


    @BeforeMethod
    public void setUp() throws Exception {
        setPostAuthenticationFlows(Collections.<String>emptyList());
    }


    @Test
    public void testGateway() throws Exception {
        final String service = "https://gateway.example.org/";
        externalContext.getMockRequestParameterMap().put("service", service);
        externalContext.getMockRequestParameterMap().put("gateway", "true");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        assertEquals(result.getOutcome().getId(), "RedirectToService");
        assertEquals(externalContext.getExternalRedirectUrl(), service);
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
        assertPopulatedAttributeContext(prc);
    }

    @Test
    public void testLoginWithConsent() throws Exception {
        final String service = "https://start.example.org/";
        externalContext.getMockRequestParameterMap().put("service", service);
        setPostAuthenticationFlows(Collections.singletonList("attribute-release"));
        overrideEndStateOutput(FLOW_ID, "RedirectToService");

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);

        final FlowExecutionOutcome outcome = result.getOutcome();
        assertEquals(result.getOutcome().getId(), "RedirectToService");
        final String url = externalContext.getExternalRedirectUrl();
        assertTrue(url.contains("ticket=ST-"));
        final String ticketId = url.substring(url.indexOf("ticket=") + 7);
        final Ticket st = ticketService.removeServiceTicket(ticketId);
        assertNotNull(st);
        final IdPSession session = sessionManager.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(st.getSessionId())));
        assertNotNull(session);

        // Ensure we passed through the consent intercept subflow
        final ProfileRequestContext prc = (ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME);
        assertNotNull(prc);
        assertNotNull(prc.getSubcontext(SubjectContext.class));
        assertNotNull(prc.getSubcontext(ConsentContext.class, false));
    }

    @Test
    public void testLoginExistingSession() throws Exception {
        final String service = "https://existing.example.org/";
        final IdPSession existing = sessionManager.createSession("aurora");
        existing.addAuthenticationResult(new AuthenticationResult("authn/Password", new UsernamePrincipal("aurora")));
        externalContext.getMockRequestParameterMap().put("service", service);
        overrideEndStateOutput(FLOW_ID, "RedirectToService");
        request.setCookies(new Cookie("shib_idp_session", existing.getId()));
        initializeThreadLocals();

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
        assertEquals(session.getId(), existing.getId());

        final ProfileRequestContext prc = (ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME);
        assertNotNull(prc.getSubcontext(SubjectContext.class));
        assertPopulatedAttributeContext(prc);
    }

    @Test
    public void testLoginExistingSessionDoNotCache() throws Exception {
        final String service = "https://existing.example.org/";
        final IdPSession existing = sessionManager.createSession("maleficent");
        externalContext.getMockRequestParameterMap().put("service", service);
        overrideEndStateOutput(FLOW_ID, "RedirectToService");
        request.setCookies(new Cookie("shib_idp_session", existing.getId()));
        initializeThreadLocals();

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
        // Expect a new session to be created since authentication was required
        assertNotEquals(session.getId(), existing.getId());

        final ProfileRequestContext prc = (ProfileRequestContext) outcome.getOutput().get(END_STATE_OUTPUT_ATTR_NAME);
        assertNotNull(prc.getSubcontext(SubjectContext.class));
        assertPopulatedAttributeContext(prc);
    }

    @Test
    public void testErrorNoService() throws Exception {
        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);
        final String responseBody = response.getContentAsString();
        assertEquals(result.getOutcome().getId(), "AuditedErrorView");
        assertTrue(responseBody.contains("ServiceNotSpecified"));
    }

    private void setPostAuthenticationFlows(final List<String> flowIdentifiers) throws Exception {
        final ProfileRequestContext prc = new ProfileRequestContext();
        prc.setProfileId(LoginConfiguration.PROFILE_ID);
        final RelyingPartyConfiguration rpConfig = relyingPartyConfigurationResolver.resolveSingle(prc);
        if (rpConfig == null) {
            throw new IllegalStateException("Relying party configuration not found");
        }
        final LoginConfiguration loginConfiguration =
                (LoginConfiguration) rpConfig.getProfileConfiguration(LoginConfiguration.PROFILE_ID);
        if (loginConfiguration == null) {
            throw new IllegalStateException("CAS login profile configuration not found");
        }
        loginConfiguration.setPostAuthenticationFlows(flowIdentifiers);
    }

    private void assertPopulatedAttributeContext(final ProfileRequestContext prc) {
        assertNotNull(prc);
        final RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class, false);
        assertNotNull(rpc);
        final AttributeContext ac= rpc.getSubcontext(AttributeContext.class, false);
        assertNotNull(ac);
        assertFalse(ac.getUnfilteredIdPAttributes().isEmpty());
    }
}
