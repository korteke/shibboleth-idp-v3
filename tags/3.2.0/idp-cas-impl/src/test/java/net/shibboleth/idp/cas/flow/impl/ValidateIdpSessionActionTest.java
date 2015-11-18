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
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionResolver;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ValidateIdpSessionActionTest extends AbstractFlowActionTest {

    private static final String TEST_SERVICE = "https://example.com/widget";

    private ValidateIdpSessionAction action;

    private RequestContext context;

    @BeforeTest
    public void setUp() throws Exception {
        springTestContextPrepareTestInstance();
        final ServiceTicket ticket = createServiceTicket(TEST_SERVICE, false);
        context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(TEST_SERVICE, ticket.getId()), null)
                .addTicketContext(ticket)
                .build();
    }

    @Test
    public void testSuccess() throws Exception {
        action = new ValidateIdpSessionAction(mockResolver(mockSession(TEST_SESSION_ID, true)));
        action.initialize();
        assertNull(action.execute(context));
    }

    @Test
    public void testSessionExpired() throws Exception {
        action = new ValidateIdpSessionAction(mockResolver(mockSession(TEST_SESSION_ID, false)));
        action.initialize();
        assertEquals(action.execute(context).getId(), ProtocolError.SessionExpired.name());
    }

    @Test
    public void testSessionRetrievalError() throws Exception {
        final SessionResolver throwingSessionResolver = mock(SessionResolver.class);
        when(throwingSessionResolver.resolveSingle(any(CriteriaSet.class))).thenThrow(new ResolverException("Broken"));
        action = new ValidateIdpSessionAction(throwingSessionResolver);
        action.initialize();
        assertEquals(action.execute(context).getId(), ProtocolError.SessionRetrievalError.name());
    }

    private SessionResolver mockResolver(final IdPSession session) {
        final SessionResolver mockSessionResolver = mock(SessionResolver.class);
        try {
            when(mockSessionResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(session);
        } catch (ResolverException e) {
            throw new RuntimeException("Resolver error", e);
        }
        return mockSessionResolver;
    }
}