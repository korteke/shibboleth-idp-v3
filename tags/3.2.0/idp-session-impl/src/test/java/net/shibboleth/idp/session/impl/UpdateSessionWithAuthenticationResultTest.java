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

import java.util.Arrays;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.impl.DefaultAuthenticationResultSerializer;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.net.HttpServletRequestResponseContext;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageSerializer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link UpdateSessionWithAuthenticationResult} unit test. */
public class UpdateSessionWithAuthenticationResultTest extends SessionManagerBaseTestCase {
    
    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    private AuthenticationContext ac;
    
    private AuthenticationFlowDescriptor flowDescriptor;
    
    private UpdateSessionWithAuthenticationResult action;
    
    @BeforeMethod public void setUpAction() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        ac = prc.getSubcontext(AuthenticationContext.class, true);

        action = new UpdateSessionWithAuthenticationResult();
        action.setSessionManager(sessionManager);
        action.initialize();
    }
    
    /** {@inheritDoc} */
    protected void adjustProperties() throws ComponentInitializationException {
        StorageSerializer<AuthenticationResult> resultSerializer = new DefaultAuthenticationResultSerializer();
        resultSerializer.initialize();

        flowDescriptor = new AuthenticationFlowDescriptor();
        flowDescriptor.setId("test1");
        flowDescriptor.setResultSerializer(resultSerializer);
        flowDescriptor.initialize();
        sessionManager.setAuthenticationFlowDescriptors(Arrays.asList(flowDescriptor));
    }

    @Test public void testNoResult() {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SessionContext.class, false));
    }

    @Test public void testNoFlow() throws SessionException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("joe");
        ac.setAttemptedFlow(flowDescriptor);
        ac.setAuthenticationResult(new AuthenticationResult("test2", new UsernamePrincipal("joe")));
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.IO_ERROR);
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, false);
        Assert.assertNotNull(sessionCtx);
        
        Assert.assertEquals(sessionCtx.getIdPSession().getPrincipalName(), "joe");
        Assert.assertEquals(sessionCtx.getIdPSession().getAuthenticationResults().size(), 0);
        Assert.assertNotNull((((MockHttpServletResponse) HttpServletRequestResponseContext.getResponse()).getCookies()[0]));
    }

    @Test public void testNotCacheable() throws SessionException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ac.setAttemptedFlow(flowDescriptor);
        ac.setAuthenticationResult(new AuthenticationResult("test1", new UsernamePrincipal("joe")));
        ac.setResultCacheable(false);
        
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, true);
        sessionCtx.setIdPSession(sessionManager.createSession("joe"));
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(sessionCtx.getIdPSession().getAuthenticationResults().size(), 0);
    }
    
    @Test public void testNewSession() throws SessionException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("joe");
        ac.setAttemptedFlow(flowDescriptor);
        ac.setAuthenticationResult(new AuthenticationResult("test1", new UsernamePrincipal("joe")));
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, false);
        Assert.assertNotNull(sessionCtx);
        
        Assert.assertEquals(sessionCtx.getIdPSession().getPrincipalName(), "joe");
        Assert.assertSame(sessionCtx.getIdPSession().getAuthenticationResult("test1"), ac.getAuthenticationResult());
        Assert.assertNotNull((((MockHttpServletResponse) HttpServletRequestResponseContext.getResponse()).getCookies()[0]));
    }
    
    @Test public void testAddToSession() throws SessionException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ac.setAttemptedFlow(flowDescriptor);
        ac.setAuthenticationResult(new AuthenticationResult("test1", new UsernamePrincipal("joe")));
        
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, true);
        sessionCtx.setIdPSession(sessionManager.createSession("joe"));
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertSame(sessionCtx.getIdPSession().getAuthenticationResult("test1"), ac.getAuthenticationResult());
    }

    @Test public void testUpdateSessionNoResult() throws SessionException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ac.setAuthenticationResult(new AuthenticationResult("test1", new UsernamePrincipal("joe")));
        
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, true);
        sessionCtx.setIdPSession(sessionManager.createSession("joe"));
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertEquals(sessionCtx.getIdPSession().getAuthenticationResults().size(), 0);
    }
    
    @Test public void testUpdateSession() throws SessionException, ResolverException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        ac.setAuthenticationResult(new AuthenticationResult("test1", new UsernamePrincipal("joe")));
        
        SessionContext sessionCtx = prc.getSubcontext(SessionContext.class, true);
        sessionCtx.setIdPSession(sessionManager.createSession("joe"));
        sessionCtx.getIdPSession().addAuthenticationResult(ac.getAuthenticationResult());
        
        long ts = System.currentTimeMillis() + 5 * 60 * 1000;
        ac.getAuthenticationResult().setLastActivityInstant(ts);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertSame(sessionCtx.getIdPSession().getAuthenticationResult("test1"), ac.getAuthenticationResult());
        
        IdPSession session2 = sessionManager.resolveSingle(
                new CriteriaSet(new SessionIdCriterion(sessionCtx.getIdPSession().getId())));
        AuthenticationResult result = session2.getAuthenticationResult("test1");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getLastActivityInstant(), ts);
    }

}