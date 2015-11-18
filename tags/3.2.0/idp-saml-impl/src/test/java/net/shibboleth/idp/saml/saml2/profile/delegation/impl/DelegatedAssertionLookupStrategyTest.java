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

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class DelegatedAssertionLookupStrategyTest extends OpenSAMLInitBaseTestCase {
    
    private DelegatedAssertionLookupStrategy strategy;
    
    private ProfileRequestContext prc;
    
    private Assertion delegatedAssertion;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        RequestContext rc = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        delegatedAssertion = SAML2ActionTestingSupport.buildAssertion();
        
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedToken(delegatedAssertion);
        
        strategy = new DelegatedAssertionLookupStrategy();
    }
    
    @Test
    public void testSuccess() {
        Assertion assertion = strategy.apply(prc);
        Assert.assertNotNull(assertion);
        Assert.assertSame(assertion, delegatedAssertion);
    }
    
    @Test
    void testNoPRC() {
        Assertion assertion = strategy.apply(null);
        Assert.assertNull(assertion);
    }
    
    @Test
    public void testNoLibertyContext() {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        Assertion assertion = strategy.apply(prc);
        Assert.assertNull(assertion);
    }

    @Test
    public void testNoAssertion() {
        prc.getSubcontext(LibertySSOSContext.class).setAttestedToken(null);
        
        Assertion assertion = strategy.apply(prc);
        Assert.assertNull(assertion);
    }

}
