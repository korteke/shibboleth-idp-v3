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

import java.util.Arrays;
import java.util.HashSet;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import net.shibboleth.idp.cas.ticket.impl.TicketIdentifierGenerationStrategy;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.spring.IdPPropertiesApplicationContextInitializer;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.webflow.execution.RequestContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract base class for all flow action tests.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(
        locations = {
                "/spring/test-flow-beans.xml",
        },
        initializers = IdPPropertiesApplicationContextInitializer.class)
@WebAppConfiguration
@TestPropertySource(properties = {"idp.initializer.failFast = false"})
public abstract class AbstractFlowActionTest extends AbstractTestNGSpringContextTests {

    protected static final String TEST_SESSION_ID = "+TkSGIRofZyue/p8F4M7TA==";

    protected static final String TEST_PRINCIPAL_NAME = "omega";

    @Autowired
    protected TicketService ticketService;

    private TicketIdentifierGenerationStrategy serviceTicketGenerator =
            new TicketIdentifierGenerationStrategy("ST", 25);

    private TicketIdentifierGenerationStrategy proxyTicketGenerator =
            new TicketIdentifierGenerationStrategy("PT", 25);


    private TicketIdentifierGenerationStrategy proxyGrantingTicketGenerator =
            new TicketIdentifierGenerationStrategy("PGT", 50);


    protected static ProfileRequestContext getProfileContext(final RequestContext context) {
        return (ProfileRequestContext) context.getConversationScope().get(ProfileRequestContext.BINDING_KEY);
    }

    protected static IdPSession mockSession(
            final String sessionId, final boolean expiredFlag, final AuthenticationResult ... results) {
        final IdPSession mockSession = mock(IdPSession.class);
        when(mockSession.getId()).thenReturn(sessionId);
        when(mockSession.getPrincipalName()).thenReturn(TEST_PRINCIPAL_NAME);
        try {
            when(mockSession.checkTimeout()).thenReturn(expiredFlag);
        } catch (SessionException e) {
            throw new RuntimeException("Session exception", e);
        }
        when(mockSession.getAuthenticationResults()).thenReturn(new HashSet<>(Arrays.asList(results)));
        return mockSession;
    }

    protected static Instant expiry() {
        return DateTime.now().plusSeconds(30).toInstant();
    }

    protected String generateServiceTicketId() {
        return serviceTicketGenerator.generateIdentifier();
    }

    protected String generateProxyTicketId() {
        return proxyTicketGenerator.generateIdentifier();
    }

    protected String generateProxyGrantingTicketId() {
        return proxyGrantingTicketGenerator.generateIdentifier();
    }

    protected ServiceTicket createServiceTicket(final String service, final boolean renew) {
        return ticketService.createServiceTicket(generateServiceTicketId(), expiry(), TEST_SESSION_ID, service, renew);
    }

    protected ProxyTicket createProxyTicket(final ProxyGrantingTicket pgt, final String service) {
        return ticketService.createProxyTicket(generateProxyTicketId(), expiry(), pgt, service);
    }

    protected ProxyGrantingTicket createProxyGrantingTicket(final ServiceTicket st) {
        return ticketService.createProxyGrantingTicket(generateProxyGrantingTicketId(), expiry(), st);
    }

    protected ProxyGrantingTicket createProxyGrantingTicket(final ProxyTicket pt) {
        return ticketService.createProxyGrantingTicket(generateProxyGrantingTicketId(), expiry(), pt);
    }
}
