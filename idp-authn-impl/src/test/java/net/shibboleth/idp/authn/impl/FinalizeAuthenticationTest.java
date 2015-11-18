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

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.TestPrincipal;
import net.shibboleth.idp.authn.principal.impl.ExactPrincipalEvalPredicateFactory;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link FinalizeAuthentication} unit test. */
public class FinalizeAuthenticationTest extends PopulateAuthenticationContextTest {
    
    private FinalizeAuthentication action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new FinalizeAuthentication();
        action.initialize();
    }
    
    @Test public void testNotSet() {
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(prc.getSubcontext(SubjectContext.class));
    }

    @Test public void testMismatch() {
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("foo");
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("bar");
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_SUBJECT_CTX);
    }

    @Test public void testRequestUnsupported() {
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final AuthenticationResult active = new AuthenticationResult("test2", new Subject());
        active.getSubject().getPrincipals().add(new TestPrincipal("bar2"));
        
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        authCtx.setAuthenticationResult(active);
        authCtx.getPrincipalEvalPredicateFactoryRegistry().register(
                TestPrincipal.class, "florp", new ExactPrincipalEvalPredicateFactory());
        
        final RequestedPrincipalContext rpCtx = new RequestedPrincipalContext();
        rpCtx.setMatchingPrincipal(new TestPrincipal("bar1"));
        rpCtx.setOperator("florp");
        rpCtx.setRequestedPrincipals(Collections.<Principal>singletonList(new TestPrincipal("bar1")));
        authCtx.addSubcontext(rpCtx);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.REQUEST_UNSUPPORTED);
    }

    @Test public void testSwitchesPrincipal() {
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final AuthenticationResult active = new AuthenticationResult("test2", new Subject());
        active.getSubject().getPrincipals().add(new TestPrincipal("bar2"));
        
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        authCtx.setAuthenticationResult(active);
        authCtx.getPrincipalEvalPredicateFactoryRegistry().register(
                TestPrincipal.class, "florp", new ExactPrincipalEvalPredicateFactory());
        
        final RequestedPrincipalContext rpCtx = new RequestedPrincipalContext();
        rpCtx.setMatchingPrincipal(new TestPrincipal("bar1"));
        rpCtx.setOperator("florp");
        rpCtx.setRequestedPrincipals(Collections.<Principal>singletonList(new TestPrincipal("bar2")));
        authCtx.addSubcontext(rpCtx);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        SubjectContext sc = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
        Assert.assertEquals(sc.getAuthenticationResults().size(), 1);
        Assert.assertEquals(rpCtx.getMatchingPrincipal().getName(), "bar2");
    }

    @Test public void testNothingActive() {
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        SubjectContext sc = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
    }

    @Test public void testOneActive() {
        final AuthenticationResult active = new AuthenticationResult("test2", new Subject());
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        authCtx.setActiveResults(Arrays.asList(active));
        authCtx.setAuthenticationResult(active);
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        SubjectContext sc = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
        Assert.assertEquals(sc.getAuthenticationResults().size(), 1);
    }

    @Test public void testMultipleActive() {
        final AuthenticationResult active1 = new AuthenticationResult("test1", new Subject());
        final AuthenticationResult active2 = new AuthenticationResult("test2", new Subject());
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        authCtx.setActiveResults(Arrays.asList(active1));
        authCtx.setAuthenticationResult(active2);
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        final SubjectContext sc = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
        Assert.assertEquals(sc.getAuthenticationResults().size(), 2);
    }
}