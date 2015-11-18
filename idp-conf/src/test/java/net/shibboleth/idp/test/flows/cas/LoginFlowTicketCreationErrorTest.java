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

import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.impl.StorageBackedSessionManager;
import net.shibboleth.idp.test.flows.AbstractFlowTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;

import static org.testng.Assert.assertEquals;

/**
 * Tests error handling of ticketCreationError event in login flow.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(locations = {
        "/test/test-cas-beans.xml",
        "/test/test-cas-error-beans.xml",
})
public class LoginFlowTicketCreationErrorTest extends AbstractFlowTest {
    /** Flow id. */
    @Nonnull
    private static String FLOW_ID = "cas/login";

    @Autowired
    private StorageBackedSessionManager sessionManager;

    @Test
    public void testTicketCreationError() throws Exception {
        final String service = "https://existing.example.org/";
        final IdPSession existing = sessionManager.createSession("aurora");
        externalContext.getMockRequestParameterMap().put("service", service);
        request.setCookies(new Cookie("shib_idp_session", existing.getId()));
        initializeThreadLocals();

        final FlowExecutionResult result = flowExecutor.launchExecution(FLOW_ID, null, externalContext);
        assertEquals(result.getOutcome().getId(), "AuditedErrorView");
    }
}
