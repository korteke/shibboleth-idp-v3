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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class FinalizeSAMLTokenProcessingTest extends OpenSAMLInitBaseTestCase {
    
    private FinalizeSAMLTokenProcessing action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private SubjectCanonicalizationContext c14NContext;
    
    private String expectedPrincipalName = "morpheus";
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        Response response = SAML2ActionTestingSupport.buildResponse();
        
        rc = new RequestContextBuilder()
            .setInboundMessage(SAML2ActionTestingSupport.buildAuthnRequest())
            .setOutboundMessage(response)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        
        c14NContext  = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        c14NContext.setPrincipalName(expectedPrincipalName);
        
        action = new FinalizeSAMLTokenProcessing();
    }
    
    @Test
    public void testSuccess() throws ComponentInitializationException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertNull(prc.getSubcontext(SubjectCanonicalizationContext.class));
        
        SubjectContext subjectContext = prc.getSubcontext(SubjectContext.class);
        Assert.assertNotNull(subjectContext);
        Assert.assertEquals(subjectContext.getPrincipalName(), expectedPrincipalName);
    }

    @Test
    public void testNoC14NContext() throws ComponentInitializationException {
        prc.removeSubcontext(SubjectCanonicalizationContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
        
        Assert.assertNull(prc.getSubcontext(SubjectContext.class));
    }
    
    @Test
    public void testNoPrincipalName() throws ComponentInitializationException {
        prc.getSubcontext(SubjectCanonicalizationContext.class).setPrincipalName(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.INVALID_SUBJECT_C14N_CTX);
        
        Assert.assertNotNull(prc.getSubcontext(SubjectCanonicalizationContext.class));
        
        Assert.assertNull(prc.getSubcontext(SubjectContext.class));
    }
}
