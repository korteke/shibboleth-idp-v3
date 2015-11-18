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

package net.shibboleth.idp.session;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link IdPSession} unit test. */
public class IdPSessionTest {

    /** Tests that everything is properly initialized during object construction. */
    @Test public void testInstantiation() throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(50);

        AbstractIdPSession session = new DummyIdPSession("test", "foo");
        Assert.assertNotNull(session.getAuthenticationResults());
        Assert.assertFalse(session.getAuthenticationResults().iterator().hasNext());
        Assert.assertTrue(session.getCreationInstant() > start);
        Assert.assertEquals(session.getId(), "test");
        Assert.assertEquals(session.getPrincipalName(), "foo");
        Assert.assertEquals(session.getLastActivityInstant(), session.getCreationInstant());
        Assert.assertNotNull(session.getSPSessions());
        Assert.assertFalse(session.getSPSessions().iterator().hasNext());

        try {
            new DummyIdPSession(null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new DummyIdPSession("", "");
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new DummyIdPSession("  ", "  ");
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new DummyIdPSession("test", null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }
    }

    /** Tests mutating the last activity instant. */
    @Test public void testLastActivityInstant() throws Exception {
        AbstractIdPSession session = new DummyIdPSession("test", "foo");

        long now = System.currentTimeMillis();
        // this is here to allow the event's last activity time to deviate from the time 'now'
        Thread.sleep(50);

        session.setLastActivityInstant(System.currentTimeMillis());
        Assert.assertTrue(session.getLastActivityInstant() > now);

        session.setLastActivityInstant(now);
        Assert.assertEquals(session.getLastActivityInstant(), now);
    }

    /** Tests mutating the last activity instant. */
    @Test public void testAddressValidation() throws Exception {
        AbstractIdPSession session = new DummyIdPSession("test", "foo");

        Assert.assertTrue(session.checkAddress("127.0.0.1"));
        Assert.assertTrue(session.checkAddress("127.0.0.1"));
        Assert.assertFalse(session.checkAddress("127.0.0.2"));
        Assert.assertTrue(session.checkAddress("::1"));
        Assert.assertTrue(session.checkAddress("::1"));
        Assert.assertFalse(session.checkAddress("fe80::5a55:caff:fef2:65a3"));
    }
    
    /** Tests adding service sessions. 
     * @throws SessionException */
    @Test public void testAddSPSessions() throws SessionException {
        long now = System.currentTimeMillis();
        long exp = now + 60000L;
        
        BasicSPSession svcSession1 = new BasicSPSession("svc1", now, exp);
        BasicSPSession svcSession2 = new BasicSPSession("svc2", now, exp);
        BasicSPSession svcSession3 = new BasicSPSession("svc3", now, exp);

        AbstractIdPSession session = new DummyIdPSession("test", "foo");
        session.addSPSession(svcSession1);
        Assert.assertEquals(session.getSPSessions().size(), 1);
        Assert.assertTrue(session.getSPSessions().contains(svcSession1));
        Assert.assertEquals(session.getSPSession("svc1"), svcSession1);

        session.addSPSession(svcSession2);
        Assert.assertEquals(session.getSPSessions().size(), 2);
        Assert.assertTrue(session.getSPSessions().contains(svcSession1));
        Assert.assertEquals(session.getSPSession("svc1"), svcSession1);
        Assert.assertTrue(session.getSPSessions().contains(svcSession2));
        Assert.assertEquals(session.getSPSession("svc2"), svcSession2);

        session.addSPSession(svcSession3);
        Assert.assertEquals(session.getSPSessions().size(), 3);
        Assert.assertTrue(session.getSPSessions().contains(svcSession1));
        Assert.assertEquals(session.getSPSession("svc1"), svcSession1);
        Assert.assertTrue(session.getSPSessions().contains(svcSession2));
        Assert.assertEquals(session.getSPSession("svc2"), svcSession2);
        Assert.assertTrue(session.getSPSessions().contains(svcSession3));
        Assert.assertEquals(session.getSPSession("svc3"), svcSession3);

        try {
            session.addSPSession(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            Assert.assertEquals(session.getSPSessions().size(), 3);
            Assert.assertTrue(session.getSPSessions().contains(svcSession1));
            Assert.assertEquals(session.getSPSession("svc1"), svcSession1);
            Assert.assertTrue(session.getSPSessions().contains(svcSession2));
            Assert.assertEquals(session.getSPSession("svc2"), svcSession2);
            Assert.assertTrue(session.getSPSessions().contains(svcSession3));
            Assert.assertEquals(session.getSPSession("svc3"), svcSession3);
        }

        session.addSPSession(svcSession1);
        Assert.assertEquals(session.getSPSessions().size(), 3);
        Assert.assertTrue(session.getSPSessions().contains(svcSession1));
        Assert.assertEquals(session.getSPSession("svc1"), svcSession1);
    }

    /** Tests removing service sessions. 
     * @throws SessionException */
    @Test public void testRemoveSPSession() throws SessionException {
        long now = System.currentTimeMillis();
        long exp = now + 60000L;

        BasicSPSession svcSession1 = new BasicSPSession("svc1", now, exp);
        BasicSPSession svcSession2 = new BasicSPSession("svc2", now, exp);

        AbstractIdPSession session = new DummyIdPSession("test", "foo");
        session.addSPSession(svcSession1);
        session.addSPSession(svcSession2);

        Assert.assertTrue(session.removeSPSession(svcSession1));
        Assert.assertEquals(session.getSPSessions().size(), 1);
        Assert.assertFalse(session.getSPSessions().contains(svcSession1));
        Assert.assertTrue(session.getSPSessions().contains(svcSession2));
        Assert.assertEquals(session.getSPSession("svc2"), svcSession2);

        Assert.assertFalse(session.removeSPSession(svcSession1));
        Assert.assertEquals(session.getSPSessions().size(), 1);
        Assert.assertFalse(session.getSPSessions().contains(svcSession1));
        Assert.assertTrue(session.getSPSessions().contains(svcSession2));
        Assert.assertEquals(session.getSPSession("svc2"), svcSession2);

        try {
            session.removeSPSession(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            Assert.assertEquals(session.getSPSessions().size(), 1);
            Assert.assertFalse(session.getSPSessions().contains(svcSession1));
            Assert.assertTrue(session.getSPSessions().contains(svcSession2));
            Assert.assertEquals(session.getSPSession("svc2"), svcSession2);
        }
    }

    /** Tests remove authentication results. 
     * @throws SessionException */
    @Test public void testRemoveAuthenticationResult() throws SessionException {
        AuthenticationResult event1 = new AuthenticationResult("foo", new UsernamePrincipal("john"));
        AuthenticationResult event2 = new AuthenticationResult("bar", new UsernamePrincipal("john"));
        AuthenticationResult event3 = new AuthenticationResult("baz", new UsernamePrincipal("john"));

        AbstractIdPSession session = new DummyIdPSession("test", "foo");
        session.addAuthenticationResult(event1);
        session.addAuthenticationResult(event2);
        session.addAuthenticationResult(event3);

        session.removeAuthenticationResult(event2);
        Assert.assertEquals(session.getAuthenticationResults().size(), 2);
        Assert.assertTrue(session.getAuthenticationResults().contains(event1));
        Assert.assertEquals(session.getAuthenticationResult("foo"), event1);
        Assert.assertTrue(session.getAuthenticationResults().contains(event3));
        Assert.assertEquals(session.getAuthenticationResult("baz"), event3);

        session.removeAuthenticationResult(event3);
        Assert.assertEquals(session.getAuthenticationResults().size(), 1);
        Assert.assertTrue(session.getAuthenticationResults().contains(event1));
        Assert.assertEquals(session.getAuthenticationResult("foo"), event1);

        try {
            session.removeAuthenticationResult(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            Assert.assertEquals(session.getAuthenticationResults().size(), 1);
            Assert.assertTrue(session.getAuthenticationResults().contains(event1));
            Assert.assertEquals(session.getAuthenticationResult("foo"), event1);
        }
    }

    /**
     * Dummy concrete class for testing purposes.
     */
    private class DummyIdPSession extends AbstractIdPSession {

        /**
         * Constructor.
         *
         * @param sessionId
         * @param canonicalName
         */
        public DummyIdPSession(String sessionId, String canonicalName) {
            super(sessionId, canonicalName, System.currentTimeMillis());
        }

        /** {@inheritDoc} */
        public void updateAuthenticationResultActivity(AuthenticationResult result) throws SessionException {

        }
    }
}