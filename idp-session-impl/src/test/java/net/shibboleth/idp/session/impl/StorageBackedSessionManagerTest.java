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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.impl.DefaultAuthenticationResultSerializer;
import net.shibboleth.idp.authn.principal.TestPrincipal;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.IdPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.SPSessionSerializerRegistry;
import net.shibboleth.idp.session.SessionException;
import net.shibboleth.idp.session.criterion.SPSessionCriterion;
import net.shibboleth.idp.session.criterion.SessionIdCriterion;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.net.HttpServletRequestResponseContext;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.storage.StorageSerializer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** {@link StorageBackedSessionManager} unit test. */
public class StorageBackedSessionManagerTest extends SessionManagerBaseTestCase {

    private static final long sessionSlop = 60 * 5 * 1000;
    
    private Collection<AuthenticationFlowDescriptor> flowDescriptors;
    
    private SPSessionSerializerRegistry serializerRegistry;
    
    @BeforeClass public void setUp() throws ComponentInitializationException {
        serializerRegistry = new SPSessionSerializerRegistry();
        final Map<Class<? extends SPSession>,StorageSerializer<? extends SPSession>> map = new HashMap<>();
        map.put(BasicSPSession.class, new BasicSPSessionSerializer(sessionSlop));
        map.put(ExtendedSPSession.class, new ExtendedSPSessionSerializer(sessionSlop));
        serializerRegistry.setMappings(map);
        serializerRegistry.initialize();
        
        StorageSerializer<AuthenticationResult> resultSerializer = new DefaultAuthenticationResultSerializer();
        resultSerializer.initialize();
        
        AuthenticationFlowDescriptor foo = new AuthenticationFlowDescriptor();
        foo.setId("AuthenticationFlow/Foo");
        foo.setLifetime(60 * 1000);
        foo.setInactivityTimeout(60 * 1000);
        foo.setResultSerializer(resultSerializer);
        foo.initialize();
        
        AuthenticationFlowDescriptor bar = new AuthenticationFlowDescriptor();
        bar.setId("AuthenticationFlow/Bar");
        bar.setLifetime(60 * 1000);
        bar.setInactivityTimeout(60 * 1000);
        bar.setResultSerializer(resultSerializer);
        bar.initialize();
        
        flowDescriptors = Arrays.asList(foo, bar);

        super.setUp();
    }

    /** {@inheritDoc} */
    @Override
    protected void adjustProperties() {
        sessionManager.setAuthenticationFlowDescriptors(flowDescriptors);
        sessionManager.setTrackSPSessions(true);
        sessionManager.setSecondaryServiceIndex(true);
        sessionManager.setSessionSlop(sessionSlop);
        sessionManager.setSPSessionSerializerRegistry(serializerRegistry);
    }
    
    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testSimpleSession() throws ResolverException, SessionException, InterruptedException {

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), mockResponse);
        
        // Test a failed lookup.
        Assert.assertNull(sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion("test"))));

        // Username should be required.
        try {
            sessionManager.createSession(null);
            Assert.fail("A null username should not have worked");
        } catch (ConstraintViolationException e) {
            
        }
        
        // Test basic session content.
        IdPSession session = sessionManager.createSession("joe");
        Assert.assertTrue(session.getCreationInstant() <= System.currentTimeMillis());
        Assert.assertEquals(session.getCreationInstant(), session.getLastActivityInstant());
        Assert.assertEquals(session.getPrincipalName(), "joe");
        Assert.assertTrue(session.getAuthenticationResults().isEmpty());
        Assert.assertTrue(session.getSPSessions().isEmpty());
        Assert.assertEquals(mockResponse.getCookie(StorageBackedSessionManager.DEFAULT_COOKIE_NAME).getValue(),
                session.getId());
        
        Thread.sleep(1000);
        
        // checkTimeout should update the last activity time.
        session.checkTimeout();
        Assert.assertNotEquals(session.getCreationInstant(), session.getLastActivityInstant());

        // Do a lookup and compare the results.
        long creation = session.getCreationInstant();
        long lastActivity = session.getLastActivityInstant();
        String sessionId = session.getId();
        session = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(sessionId)));
        Assert.assertNotNull(session);
        Assert.assertEquals(session.getPrincipalName(), "joe");
        Assert.assertEquals(session.getCreationInstant(), creation);
        Assert.assertEquals(session.getLastActivityInstant(), lastActivity);
        
        // Test a destroy and a failed lookup.
        sessionManager.destroySession(sessionId, true);
        Assert.assertNull(sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(sessionId))));
    }
    
    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testAddress() throws SessionException, ResolverException {
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("192.168.1.1");
        HttpServletRequestResponseContext.loadCurrent(mockRequest, new MockHttpServletResponse());
        
        // Interleave checks of addresses of the two types.
        IdPSession session = sessionManager.createSession("joe");
        Assert.assertTrue(session.checkAddress("192.168.1.1"));
        Assert.assertFalse(session.checkAddress("192.168.1.2"));
        Assert.assertTrue(session.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertTrue(session.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertFalse(session.checkAddress("fe80::ca2a:14ff:fe2a:3e05"));
        Assert.assertTrue(session.checkAddress("192.168.1.1"));
        
        // Try a bad address type.
        Assert.assertFalse(session.checkAddress("1,1,1,1"));
        
        // Interleave manipulation of a session between two copies to check for resync.
        IdPSession one = sessionManager.createSession("joe");
        IdPSession two = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(one.getId())));
        
        Assert.assertTrue(one.checkAddress("192.168.1.1"));
        Assert.assertFalse(two.checkAddress("192.168.1.2"));
        Assert.assertTrue(two.checkAddress("fe80::ca2a:14ff:fe2a:3e04"));
        Assert.assertFalse(one.checkAddress("fe80::ca2a:14ff:fe2a:3e05"));
        
        sessionManager.destroySession(session.getId(), true);
    }

    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testAuthenticationResults() throws ResolverException, SessionException, InterruptedException {
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        
        IdPSession session = sessionManager.createSession("joe");
        Assert.assertTrue(session.getAuthenticationResults().isEmpty());

        // Add some results.
        AuthenticationResult foo = new AuthenticationResult("AuthenticationFlow/Foo", new UsernamePrincipal("joe"));
        foo.getSubject().getPrincipals().add(new TestPrincipal("test1"));
        AuthenticationResult bar = new AuthenticationResult("AuthenticationFlow/Bar", new UsernamePrincipal("joe"));
        bar.getSubject().getPrincipals().add(new TestPrincipal("test2"));
        AuthenticationResult baz = new AuthenticationResult("AuthenticationFlow/Baz", new UsernamePrincipal("joe"));

        Assert.assertNull(session.addAuthenticationResult(foo));
        Assert.assertNull(session.addAuthenticationResult(bar));
        try {
            session.addAuthenticationResult(baz);
            Assert.fail("An unserializable AuthenticationResult should not have worked");
        } catch (SessionException e) {
            
        }
        
        // Test various methods and removals.
        Assert.assertEquals(session.getAuthenticationResults().size(), 2);
        
        Assert.assertFalse(session.removeAuthenticationResult(baz));
        Assert.assertTrue(session.removeAuthenticationResult(bar));
        
        Assert.assertEquals(session.getAuthenticationResults().size(), 1);
        
        // Test access and compare to original.
        Assert.assertNull(session.getAuthenticationResult("AuthenticationFlow/Bar"));
        AuthenticationResult foo2 = session.getAuthenticationResult("AuthenticationFlow/Foo");
        Assert.assertSame(foo, foo2);
        
        // Update timestamp.
        long now = System.currentTimeMillis();
        foo.setLastActivityInstant(now);
        session.updateAuthenticationResultActivity(foo);
        
        // Load from storage and re-test.
        IdPSession session2 = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertNull(session2.getAuthenticationResult("AuthenticationFlow/Bar"));
        foo2 = session2.getAuthenticationResult("AuthenticationFlow/Foo");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getAuthenticationInstant(), foo2.getAuthenticationInstant());
        Assert.assertEquals(foo.getLastActivityInstant(), foo2.getLastActivityInstant());
        Assert.assertEquals(foo.getSubject(), foo2.getSubject());
        
        // Test removal while multiple objects are active.
        session2 = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertTrue(session.removeAuthenticationResult(foo));
        Assert.assertNull(session2.getAuthenticationResult("AuthenticationFlow/Foo"));
        
        sessionManager.destroySession(session.getId(), true);
    }
    
    @Test(threadPoolSize = 10, invocationCount = 10,  timeOut = 10000)
    public void testSPSessions() throws ResolverException, SessionException, InterruptedException {
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        
        IdPSession session = sessionManager.createSession("joe");
        Assert.assertTrue(session.getSPSessions().isEmpty());

        // Add some sessions.
        SPSession foo = new BasicSPSession("https://sp.example.org/shibboleth",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);
        SPSession bar = new BasicSPSession("https://sp2.example.org/shibboleth",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);

        Assert.assertNull(session.addSPSession(foo));
        Assert.assertNull(session.addSPSession(bar));
        
        // Test various methods and removals.
        Assert.assertEquals(session.getSPSessions().size(), 2);
        
        Assert.assertTrue(session.removeSPSession(bar));
        Assert.assertFalse(session.removeSPSession(bar));
        
        Assert.assertEquals(session.getSPSessions().size(), 1);
        
        // Test access and compare to original.
        Assert.assertNull(session.getSPSession("https://sp2.example.org/shibboleth"));
        SPSession foo2 = session.getSPSession("https://sp.example.org/shibboleth");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getCreationInstant(), foo2.getCreationInstant());
        Assert.assertEquals(foo.getExpirationInstant(), foo2.getExpirationInstant());
        
        // Load from storage and re-test.
        IdPSession session2 = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertNull(session.getSPSession("https://sp2.example.org/shibboleth"));
        foo2 = session2.getSPSession("https://sp.example.org/shibboleth");
        Assert.assertNotNull(foo2);
        Assert.assertEquals(foo.getCreationInstant(), foo2.getCreationInstant());
        Assert.assertEquals(foo.getExpirationInstant(), foo2.getExpirationInstant());

        // Test removal while multiple objects are active.
        session2 = sessionManager.resolveSingle(new CriteriaSet(new SessionIdCriterion(session.getId())));
        Assert.assertTrue(session.removeSPSession(foo));
        Assert.assertNull(session2.getSPSession("https://sp.example.org/shibboleth"));
        
        sessionManager.destroySession(session.getId(), true);
    }
    
    @Test
    public void testSecondaryLookup() throws ResolverException, SessionException, InterruptedException {
        
        HttpServletRequestResponseContext.loadCurrent(new MockHttpServletRequest(), new MockHttpServletResponse());
        
        IdPSession session = sessionManager.createSession("joe");
        IdPSession session2 = sessionManager.createSession("joe2");

        // Add some sessions.
        SPSession foo = new ExtendedSPSession("https://sp.example.org/shibboleth",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);
        SPSession bar = new ExtendedSPSession("https://sp2.example.org/shibboleth",
                System.currentTimeMillis(), System.currentTimeMillis() + 60 * 60 * 1000);

        Assert.assertNull(session.addSPSession(foo));
        Assert.assertNull(session.addSPSession(bar));

        Assert.assertNull(session2.addSPSession(foo));
        Assert.assertNull(session2.addSPSession(bar));
        
        // Do a lookup.
        Assert.assertFalse(sessionManager.resolve(new CriteriaSet(
                new SPSessionCriterion("https://sp.example.org/shibboleth", "None"))).iterator().hasNext());
        
        List<IdPSession> sessions = new ArrayList<>();
        for (final IdPSession s : sessionManager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY)))) {
            sessions.add(s);
        }
        Assert.assertEquals(sessions.size(), 2);
        
        sessionManager.destroySession(session.getId(), true);
        
        sessions.clear();
        for (final IdPSession s : sessionManager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp2.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY)))) {
            sessions.add(s);
        }
        Assert.assertEquals(sessions.size(), 1);
        
        sessionManager.destroySession(session2.getId(), true);
        
        sessions.clear();
        for (final IdPSession s : sessionManager.resolve(
                new CriteriaSet(new SPSessionCriterion("https://sp2.example.org/shibboleth",
                        ExtendedSPSession.SESSION_KEY)))) {
            sessions.add(s);
        }
        Assert.assertEquals(sessions.size(), 0);
    }

    private static class ExtendedSPSession extends BasicSPSession {

        public static final String SESSION_KEY = "PerSessionNameWouldGoHere";
        
        public ExtendedSPSession(String id, long creation, long expiration) {
            super(id, creation, expiration);
        }

        /** {@inheritDoc} */
        public String getSPSessionKey() {
            return SESSION_KEY;
        }
    }

    private static class ExtendedSPSessionSerializer extends BasicSPSessionSerializer {

        public ExtendedSPSessionSerializer(long offset) {
            super(offset);
        }
        
        /** {@inheritDoc} */
        @Override
        protected SPSession doDeserialize(JsonObject obj, String id, long creation, long expiration)
                throws IOException {
            // Check if field got serialized.
            obj.getString("sk");
            return new ExtendedSPSession(id, creation, expiration);
        }

        /** {@inheritDoc} */
        @Override
        protected void doSerializeAdditional(SPSession instance, JsonGenerator generator) {
            generator.write("sk", ExtendedSPSession.SESSION_KEY);
        }

    }
}