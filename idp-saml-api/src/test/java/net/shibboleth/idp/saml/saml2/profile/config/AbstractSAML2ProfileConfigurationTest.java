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

package net.shibboleth.idp.saml.saml2.profile.config;

import java.util.ArrayList;

import net.shibboleth.idp.saml.saml2.profile.config.AbstractSAML2ProfileConfiguration;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/** Unit test for {@link AbstractSAML2ProfileConfiguration}. */
public class AbstractSAML2ProfileConfigurationTest {

    @Test public void testEncryptNameIDsPredicate() {
        MockSAML2ProfileConfiguration config = new MockSAML2ProfileConfiguration();
        Assert.assertNotNull(config.getEncryptNameIDs());

        config.setEncryptNameIDs(Predicates.<ProfileRequestContext> alwaysFalse());
        Assert.assertSame(config.getEncryptNameIDs(), Predicates.<ProfileRequestContext> alwaysFalse());

        try {
            config.setEncryptNameIDs(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // excepted this
        }
    }

    @Test public void testEncryptAssertionsPredicate() {
        MockSAML2ProfileConfiguration config = new MockSAML2ProfileConfiguration();
        Assert.assertNotNull(config.getEncryptAssertions());

        config.setEncryptAssertions(Predicates.<ProfileRequestContext> alwaysFalse());
        Assert.assertSame(config.getEncryptAssertions(), Predicates.<ProfileRequestContext> alwaysFalse());

        try {
            config.setEncryptAssertions(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // excepted this
        }
    }

    @Test public void testEncryptAttributesPredicate() {
        MockSAML2ProfileConfiguration config = new MockSAML2ProfileConfiguration();
        Assert.assertNotNull(config.getEncryptAttributes());

        config.setEncryptAttributes(Predicates.<ProfileRequestContext> alwaysFalse());
        Assert.assertSame(config.getEncryptAttributes(), Predicates.<ProfileRequestContext> alwaysFalse());

        try {
            config.setEncryptAttributes(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // excepted this
        }
    }
    
    @Test public void testProxyCount() {
        MockSAML2ProfileConfiguration config = new MockSAML2ProfileConfiguration();
        Assert.assertEquals(config.getProxyCount(), 0);

        config.setProxyCount(1);
        Assert.assertEquals(config.getProxyCount(), 1);
    }

    @Test public void testProxyAudiences() {
        MockSAML2ProfileConfiguration config = new MockSAML2ProfileConfiguration();
        Assert.assertNotNull(config.getProxyAudiences());
        Assert.assertTrue(config.getProxyAudiences().isEmpty());

        ArrayList<String> audiences = new ArrayList<>();
        audiences.add("foo");
        audiences.add("foo");
        audiences.add("bar");

        config.setProxyAudiences(audiences);
        Assert.assertNotSame(config.getProxyAudiences(), audiences);
        Assert.assertNotNull(config.getProxyAudiences());
        Assert.assertEquals(config.getProxyAudiences().size(), 2);
        Assert.assertTrue(config.getProxyAudiences().contains("foo"));
        Assert.assertTrue(config.getProxyAudiences().contains("bar"));

        try {
            config.getProxyAudiences().add("baz");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Mock class for testing {@link AbstractSAML2ProfileConfiguration}. */
    private static class MockSAML2ProfileConfiguration extends AbstractSAML2ProfileConfiguration {

        /** Constructor. */
        public MockSAML2ProfileConfiguration() {
            super("mock");
        }
    }
}