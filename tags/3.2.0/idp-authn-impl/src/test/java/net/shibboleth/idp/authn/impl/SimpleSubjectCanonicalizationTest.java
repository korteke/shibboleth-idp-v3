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

import java.util.Arrays;

import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.collection.Pair;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link SimpleSubjectCanonicalization} unit test. */
public class SimpleSubjectCanonicalizationTest extends PopulateAuthenticationContextTest {
    
    private SimpleSubjectCanonicalization action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new SimpleSubjectCanonicalization();
        action.setTransforms(Arrays.asList(new Pair<>("^(.+)@osu\\.edu$", "$1")));
        action.initialize();
    }
    
    @Test public void testNoContext() {
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
    }

    @Test public void testNoPrincipal() {
        Subject subject = new Subject();
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setSubject(subject);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT);
        Assert.assertNotNull(prc.getSubcontext(SubjectCanonicalizationContext.class, false).getException());
    }

    @Test public void testMultiPrincipals() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePrincipal("foo"));
        subject.getPrincipals().add(new UsernamePrincipal("bar"));
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setSubject(subject);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT);
        Assert.assertNotNull(prc.getSubcontext(SubjectCanonicalizationContext.class, false).getException());
    }

    @Test public void testSuccess() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePrincipal("foo"));
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setSubject(subject);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        SubjectCanonicalizationContext sc = prc.getSubcontext(SubjectCanonicalizationContext.class, false);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
    }

    @Test public void testTransform() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePrincipal("foo@osu.edu"));
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setSubject(subject);
        
        final Event event = action.execute(src);
        
        ActionTestingSupport.assertProceedEvent(event);
        SubjectCanonicalizationContext sc = prc.getSubcontext(SubjectCanonicalizationContext.class, false);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
    }
    
}