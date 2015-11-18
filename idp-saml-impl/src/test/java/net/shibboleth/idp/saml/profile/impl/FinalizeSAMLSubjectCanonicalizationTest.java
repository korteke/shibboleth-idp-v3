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

package net.shibboleth.idp.saml.profile.impl;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link FinalizeSAMLSubjectCanonicalization} unit test. */
public class FinalizeSAMLSubjectCanonicalizationTest {
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    private FinalizeSAMLSubjectCanonicalization action; 
    
    @BeforeMethod public void setUp() throws Exception {
        rc = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);

        action = new FinalizeSAMLSubjectCanonicalization();
        action.initialize();
    }
    
    @Test public void testNoContext() {
        final Event event = action.execute(rc);
        
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
        Assert.assertNull(prc.getSubcontext(SubjectContext.class));
    }

    @Test public void testNoPrincipal() {
        prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
        Assert.assertNull(prc.getSubcontext(SubjectContext.class));
    }
    
    @Test public void testMatch() {
        prc.getSubcontext(SubjectCanonicalizationContext.class, true).setPrincipalName("foo");
        
        final Event event = action.execute(rc);
        
        ActionTestingSupport.assertProceedEvent(event);
        final SubjectContext sc = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getPrincipalName(), "foo");
    }

}