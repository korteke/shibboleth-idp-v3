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
import net.shibboleth.idp.cas.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link CheckProxyAuthorizationAction}.
 *
 * @author Marvin S. Addison
 */
public class CheckProxyAuthorizationActionTest extends AbstractFlowActionTest {

    @Autowired
    private CheckProxyAuthorizationAction action;

    @Test
    public void testProxyAuthorizationSuccess() throws Exception {
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addServiceContext(new Service("https://serviceA.example.org/", "proxying", true))
                .build();
        assertNull(action.execute(context));
    }

    @Test
    public void testProxyAuthorizationFailure() throws Exception {
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addServiceContext(new Service("https://serviceB.example.org/", "no-proxy", false))
                .build();
        assertEquals(action.execute(context).getId(), ProtocolError.ProxyNotAuthorized.name());
    }
}
