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

package net.shibboleth.idp.authn.principal.impl;

import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.authn.principal.impl.InexactPrincipalEvalPredicateFactory;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link InexactPrincipalEvalPredicateFactory} unit test. */
public class InexactPrincipalEvalPredicateFactoryTest {
    
    private InexactPrincipalEvalPredicateFactory factory;
    
    /**
     * We can test with UsernamePrincipals but actual usage would involve non-user-specific
     * principal types.
     */
    private UsernamePrincipal foo;
    private UsernamePrincipal bar;
    private UsernamePrincipal baz;
    
    @BeforeTest public void setUp() throws Exception {
        // Note that foo matches bar and bar2, but not itself.
        // This would be a fit for the SAML "better" operator.
        factory = new InexactPrincipalEvalPredicateFactory();
        factory.getMatchingRules().put("foo", "bar");
        factory.getMatchingRules().put("foo", "bar2");
        
        foo = new UsernamePrincipal("foo");
        bar = new UsernamePrincipal("bar");
        baz = new UsernamePrincipal("baz");
    }
        
    @Test public void testNoPrincipals() {
        // Scenario: request for foo, result contains nothing, no match.
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        Assert.assertFalse(factory.getPredicate(foo).apply(sample));
    }

    @Test public void testOnePrincipalMatch() {
        // Scenario: result contains bar, request for foo matches, bar and baz do not
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("bar"));
        Assert.assertTrue(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
        Assert.assertFalse(factory.getPredicate(baz).apply(sample));
    }

    @Test public void testOnePrincipalNoMatch() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("foo"));
        Assert.assertFalse(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
        Assert.assertFalse(factory.getPredicate(baz).apply(sample));
    }
    
    @Test public void testMultiplePrincipalMatch() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("bar"));
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("baz"));
        Assert.assertTrue(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
        Assert.assertFalse(factory.getPredicate(baz).apply(sample));
    }

    @Test public void testMultiplePrincipalMatchMap() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("bar"));
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("baz"));
        InexactPrincipalEvalPredicateFactory mapFactory = new InexactPrincipalEvalPredicateFactory();
        mapFactory.setMatchingRules(factory.getMatchingRules().asMap());
        Assert.assertTrue(mapFactory.getPredicate(foo).apply(sample));
        Assert.assertFalse(mapFactory.getPredicate(bar).apply(sample));
        Assert.assertFalse(mapFactory.getPredicate(baz).apply(sample));
    }

    @Test public void testMultiplePrincipalNoMatch() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("foo"));
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("baz"));
        Assert.assertFalse(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
        Assert.assertFalse(factory.getPredicate(baz).apply(sample));
    }
}