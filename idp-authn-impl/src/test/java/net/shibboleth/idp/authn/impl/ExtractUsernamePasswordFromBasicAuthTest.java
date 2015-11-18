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

package net.shibboleth.idp.authn.impl;


import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.profile.ActionTestingSupport;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;

/** {@link ExtractUsernamePasswordFromBasicAuth} unit test. */
public class ExtractUsernamePasswordFromBasicAuthTest extends PopulateAuthenticationContextTest {
    
    private ExtractUsernamePasswordFromBasicAuth action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new ExtractUsernamePasswordFromBasicAuth();
        action.setHttpServletRequest(new MockHttpServletRequest());
        action.initialize();
    }
    
    @Test public void testNoServlet() throws Exception {
        action = new ExtractUsernamePasswordFromBasicAuth();
        action.initialize();
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testMissingIdentity() throws Exception {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testMissingIdentity2() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addHeader(HttpHeaders.AUTHORIZATION, "foo");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testInvalid() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addHeader(HttpHeaders.AUTHORIZATION, "Basic foo:bar");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }

    @Test public void testInvalid2() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addHeader(HttpHeaders.AUTHORIZATION, "Basic Zm9vOg==");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }
    
    @Test public void testValid() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addHeader(HttpHeaders.AUTHORIZATION, "Basic Zm9vOmJhcg==");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class, false);
        UsernamePasswordContext upCtx = authCtx.getSubcontext(UsernamePasswordContext.class, false);
        Assert.assertNotNull(upCtx, "No UsernamePasswordContext attached");
        Assert.assertEquals(upCtx.getUsername(), "foo");
        Assert.assertEquals(upCtx.getPassword(), "bar");
    }
}