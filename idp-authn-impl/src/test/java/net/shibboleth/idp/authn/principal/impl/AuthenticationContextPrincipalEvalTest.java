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

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.authn.principal.impl.InexactPrincipalEvalPredicateFactory;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the {@link AuthenticationContext#isAcceptable(Principal)} method using a mocked up
 * registry of evaluation predicates.
 */
public class AuthenticationContextPrincipalEvalTest {
    
    /**
     * We can test with UsernamePrincipals but actual usage would involve non-user-specific
     * principal types.
     */
    private UsernamePrincipal foo;
    private UsernamePrincipal bar;
    private UsernamePrincipal baz;
    
    private AuthenticationContext authnContext;
    
    @BeforeClass public void setUp() throws Exception {
        final InexactPrincipalEvalPredicateFactory factory = new InexactPrincipalEvalPredicateFactory();
        factory.getMatchingRules().put("foo", "bar");
        factory.getMatchingRules().put("foo", "bar2");

        authnContext = new AuthenticationContext();
        authnContext.getPrincipalEvalPredicateFactoryRegistry().register(
                UsernamePrincipal.class, "better", factory);
        authnContext.getPrincipalEvalPredicateFactoryRegistry().register(
                UsernamePrincipal.class, "exact", new ExactPrincipalEvalPredicateFactory());
        
        foo = new UsernamePrincipal("foo");
        bar = new UsernamePrincipal("bar");
        baz = new UsernamePrincipal("baz");
    }
    
    @Test public void testNoRequested() {
        authnContext.removeSubcontext(RequestedPrincipalContext.class);
        Assert.assertTrue(authnContext.isAcceptable(foo));
        Assert.assertTrue(authnContext.isAcceptable(bar));
        Assert.assertTrue(authnContext.isAcceptable(baz));
    }

    @Test public void testUnknownOperator() {
        final RequestedPrincipalContext rpCtx = authnContext.getSubcontext(RequestedPrincipalContext.class, true);
        rpCtx.setOperator("unknown");
        rpCtx.setRequestedPrincipals(Collections.<Principal>singletonList(foo));
        Assert.assertFalse(authnContext.isAcceptable(foo));
    }

    @Test public void testExact() {
        final RequestedPrincipalContext rpCtx = authnContext.getSubcontext(RequestedPrincipalContext.class, true);
        rpCtx.setOperator("exact");
        rpCtx.setRequestedPrincipals(Arrays.<Principal>asList(foo, bar));
        Assert.assertTrue(authnContext.isAcceptable(foo));
        Assert.assertTrue(authnContext.isAcceptable(bar));
        Assert.assertFalse(authnContext.isAcceptable(baz));
    }

    @Test public void testBetterKnown() {
        final RequestedPrincipalContext rpCtx = authnContext.getSubcontext(RequestedPrincipalContext.class, true);
        rpCtx.setOperator("better");
        rpCtx.setRequestedPrincipals(Arrays.<Principal>asList(foo));
        Assert.assertFalse(authnContext.isAcceptable(foo));
        Assert.assertTrue(authnContext.isAcceptable(bar));
        Assert.assertFalse(authnContext.isAcceptable(baz));
    }

    @Test public void testBetterUnknown() {
        final RequestedPrincipalContext rpCtx = authnContext.getSubcontext(RequestedPrincipalContext.class, true);
        rpCtx.setOperator("better");
        rpCtx.setRequestedPrincipals(Arrays.<Principal>asList(bar));
        Assert.assertFalse(authnContext.isAcceptable(foo));
        Assert.assertFalse(authnContext.isAcceptable(bar));
        Assert.assertFalse(authnContext.isAcceptable(baz));
    }

}