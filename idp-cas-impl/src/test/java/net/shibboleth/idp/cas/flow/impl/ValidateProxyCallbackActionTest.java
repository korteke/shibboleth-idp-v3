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

import java.net.URI;
import java.security.cert.CertificateException;

import net.shibboleth.idp.cas.config.impl.ValidateConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.cas.proxy.ProxyAuthenticator;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import org.joda.time.Instant;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit test for {@link ValidateProxyCallbackAction}.
 *
 * @author Marvin S. Addison
 */
public class ValidateProxyCallbackActionTest extends AbstractFlowActionTest {

    @Test
    public void testValidateProxySuccess() throws Exception {
        final ValidateProxyCallbackAction action = new ValidateProxyCallbackAction(
                mockProxyAuthenticator(null), ticketService);
        action.initialize();
        final RequestContext context = newRequestContext("https://test.example.org/");
        assertNull(action.execute(context));
        final TicketValidationResponse response = action.getCASResponse(getProfileContext(context));
        assertNotNull(response);
        assertNotNull(response.getPgtIou());
    }

    @Test
    public void testValidateProxyFailure() throws Exception {
        final ValidateProxyCallbackAction action = new ValidateProxyCallbackAction(
                mockProxyAuthenticator(new CertificateException()), ticketService);
        action.initialize();
        assertEquals(
                action.execute(newRequestContext("https://test.example.org/")).getId(),
                ProtocolError.ProxyCallbackAuthenticationFailure.name());
    }

    private static ProxyAuthenticator<TrustEngine<? super X509Credential>> mockProxyAuthenticator(final Exception toBeThrown)
            throws Exception {
        final ProxyAuthenticator<TrustEngine<? super X509Credential>> authenticator = mock(ProxyAuthenticator.class);
        if (toBeThrown != null) {
            doThrow(toBeThrown).when(authenticator).authenticate(any(URI.class), any(TrustEngine.class));
        }
        return authenticator;
    }

    private static RequestContext newRequestContext(final String pgtURL) {
        final String service = "https://test.example.com/";
        final String ticket = "ST-123-ABCCEF";
        final TicketValidationRequest request = new TicketValidationRequest(service, ticket);
        request.setPgtUrl(pgtURL);
        final RequestContext context = new TestContextBuilder(ValidateConfiguration.PROFILE_ID)
                .addProtocolContext(request, new TicketValidationResponse())
                .addTicketContext(new ServiceTicket(ticket, "SessionID-123", service, Instant.now(), false))
                .addRelyingPartyContext(service, true, new ValidateConfiguration())
                .build();
        return context;
    }
}
