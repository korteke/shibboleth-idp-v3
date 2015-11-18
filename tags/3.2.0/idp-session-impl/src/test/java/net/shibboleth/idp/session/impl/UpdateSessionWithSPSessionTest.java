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

import java.util.Collections;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SPSessionSerializerRegistry;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.net.HttpServletRequestResponseContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageSerializer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/** {@link UpdateSessionWithSPSession} unit test. */
public class UpdateSessionWithSPSessionTest extends SessionManagerBaseTestCase {
    
    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    private UpdateSessionWithSPSession action;
    
    @BeforeMethod public void setUpAction() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        action = new UpdateSessionWithSPSession();
        action.setSessionManager(sessionManager);
    }

    /** {@inheritDoc} */
    @Override
    protected void adjustProperties() throws ComponentInitializationException {
        sessionManager.setTrackSPSessions(true);
        sessionManager.setSecondaryServiceIndex(true);
        final SPSessionSerializerRegistry registry = new SPSessionSerializerRegistry();
        registry.setMappings(
                Collections.<Class<? extends SPSession>,StorageSerializer<? extends SPSession>>singletonMap(
                        BasicSPSession.class, new BasicSPSessionSerializer(900 * 60 * 1000)));
        registry.initialize();
        sessionManager.setSPSessionSerializerRegistry(registry);
    }
    
    @Test public void testNoSession() throws ComponentInitializationException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        action.setSPSessionCreationStrategy(new NullStrategy());
        action.initialize();
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SessionContext.class));
    }
    
    @Test public void testNullSession() throws SessionException, ComponentInitializationException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        
        final IdPSession session = sessionManager.createSession("joe");
        prc.getSubcontext(SessionContext.class, true).setIdPSession(session);
        
        action.setSPSessionCreationStrategy(new NullStrategy());
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(session.getSPSession("https://sp.example.org"));
    }

    @Test public void testBasicSession() throws SessionException, ComponentInitializationException {
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        
        final IdPSession session = sessionManager.createSession("joe");
        prc.getSubcontext(SessionContext.class, true).setIdPSession(session);
        
        final long creation = System.currentTimeMillis();
        final long expiration = creation + 3600 * 60 * 1000;
        
        action.setSPSessionCreationStrategy(new DummyStrategy(creation, expiration));
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        final SPSession spSession = session.getSPSession("https://sp.example.org");
        Assert.assertNotNull(spSession);
        Assert.assertEquals(spSession.getCreationInstant(), creation);
        Assert.assertEquals(spSession.getExpirationInstant(), expiration);
    }

    /** Returns a simple example session. */
    private class DummyStrategy implements Function<ProfileRequestContext, SPSession> {

        private long creationTime;
        
        private long expirationTime;
        
        /** Constructor. */
        DummyStrategy(final long creation, final long expiration) {
            creationTime = creation;
            expirationTime = expiration;
        }
        
        /** {@inheritDoc} */
        @Override
        public SPSession apply(ProfileRequestContext input) {
            return new BasicSPSession("https://sp.example.org", creationTime, expirationTime);
        }
        
    }
    
    /** Returns nothing. */
    private class NullStrategy implements Function<ProfileRequestContext, SPSession> {
        
        /** {@inheritDoc} */
        @Override
        public SPSession apply(ProfileRequestContext input) {
            return null;
        }
        
    }
}