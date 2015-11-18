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
import net.shibboleth.idp.authn.principal.impl.ExactPrincipalEvalPredicateFactory;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link ExactPrincipalEvalPredicateFactory} unit test. */
public class ExactPrincipalEvalPredicateFactoryTest {
    
    private ExactPrincipalEvalPredicateFactory factory;
    
    /**
     * We can test with UsernamePrincipals but actual usage would involve non-user-specific
     * principal types.
     */
    private UsernamePrincipal foo;
    private UsernamePrincipal bar;
    
    @BeforeTest public void setUp() throws Exception {
        factory = new ExactPrincipalEvalPredicateFactory();
        foo = new UsernamePrincipal("foo");
        bar = new UsernamePrincipal("bar");
    }
        
    @Test public void testNoPrincipals() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        Assert.assertFalse(factory.getPredicate(foo).apply(sample));
    }

    @Test public void testOnePrincipal() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("foo"));
        Assert.assertTrue(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
    }

    @Test public void testMultiplePrincipal() {
        AuthenticationResult sample = new AuthenticationResult("test", new Subject());
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("foo"));
        sample.getSubject().getPrincipals().add(new UsernamePrincipal("baz"));
        Assert.assertTrue(factory.getPredicate(foo).apply(sample));
        Assert.assertFalse(factory.getPredicate(bar).apply(sample));
    }

}