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
import net.shibboleth.idp.cas.protocol.ProxyTicketRequest;
import net.shibboleth.idp.cas.protocol.ServiceTicketRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BuildRelyingPartyContextActionTest extends AbstractFlowActionTest {

    @Autowired
    private BuildRelyingPartyContextAction action;

    @Test
    public void testExecuteFromServiceTicketRequest() {
        final String serviceURL = "https://serviceA.example.org:8443/landing";
        final RequestContext requestContext = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new ServiceTicketRequest(serviceURL), null)
                .build();
        assertNull(action.execute(requestContext));
        final Service service = action.getCASService(getProfileContext(requestContext));
        assertEquals(serviceURL, service.getName());
        assertEquals("allowedToProxy", service.getGroup());
        assertTrue(service.isAuthorizedToProxy());
    }

    @Test
    public void testExecuteFromTicketValidationRequest() {
        final String serviceURL = "http://serviceB.example.org/";
        final RequestContext requestContext = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new TicketValidationRequest(serviceURL, "ST-123"), null)
                .build();
        assertNull(action.execute(requestContext));
        final Service service = action.getCASService(getProfileContext(requestContext));
        assertEquals(serviceURL, service.getName());
        assertEquals("notAllowedToProxy", service.getGroup());
        assertFalse(service.isAuthorizedToProxy());
    }

    @Test
    public void testExecuteFromProxyTicketRequest() {
        final String serviceURL = "http://mallory.untrusted.org/";
        final RequestContext requestContext = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addProtocolContext(new ProxyTicketRequest("PGT-123", serviceURL), null)
                .build();
        assertNull(action.execute(requestContext));
        final Service service = action.getCASService(getProfileContext(requestContext));
        assertEquals(serviceURL, service.getName());
        assertEquals(BuildRelyingPartyContextAction.UNVERIFIED_GROUP, service.getGroup());
        assertFalse(service.isAuthorizedToProxy());
    }
}