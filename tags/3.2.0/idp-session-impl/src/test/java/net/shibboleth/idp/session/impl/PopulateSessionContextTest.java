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

package net.shibboleth.idp.session.impl;

import javax.servlet.http.Cookie;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.net.HttpServletRequestResponseContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link PopulateSessionContext} unit test. */
public class PopulateSessionContextTest extends SessionManagerBaseTestCase {
    
    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    private PopulateSessionContext action;
    
    @BeforeMethod public void setUpAction() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        
        action = new PopulateSessionContext();
        action.setHttpServletRequest(requestProxy);
        action.setHttpServletResponse(responseProxy);
        action.setSessionResolver(sessionManager);
        action.initialize();
    }
    
    @Test public void testNoSession() {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SessionContext.class, false));
    }
    
    @Test public void testSession() throws SessionException {
        Cookie cookie = createSession("joe");
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setCookies(cookie);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, false);
        Assert.assertNotNull(sessionCtx);
        
        Assert.assertEquals(sessionCtx.getIdPSession().getPrincipalName(), "joe");
    }

    @Test public void testAddressRebind() throws SessionException {
        Cookie cookie = createSession("joe");
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setCookies(cookie);
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setRemoteAddr("::1");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, false);
        Assert.assertNotNull(sessionCtx);
        
        Assert.assertEquals(sessionCtx.getIdPSession().getPrincipalName(), "joe");
    }
    
    @Test public void testAddressMismatch() throws SessionException {
        Cookie cookie = createSession("joe");
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setCookies(cookie);
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setRemoteAddr("192.168.1.1");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SessionContext.class, false));
    }

    @Test public void testTimeout() throws SessionException, InterruptedException {
        Cookie cookie = createSession("joe");
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ((MockHttpServletRequest) HttpServletRequestResponseContext.getRequest()).setCookies(cookie);
        
        Thread.sleep(5000);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SessionContext.class, false));
    }
}