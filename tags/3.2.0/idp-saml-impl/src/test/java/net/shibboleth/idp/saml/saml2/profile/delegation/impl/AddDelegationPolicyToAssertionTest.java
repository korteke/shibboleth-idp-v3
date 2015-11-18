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

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Advice;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/**
 *
 */
public class AddDelegationPolicyToAssertionTest extends OpenSAMLInitBaseTestCase {

    private AddDelegationPolicyToAssertion action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private BrowserSSOProfileConfiguration browserSSOProfileConfig;
    
    private List<ProfileConfiguration> profileConfigs;
    
    private Assertion delegatedAssertion, assertionToModify;
    
    private Long expectedProfileChainLength = 3L;
    private Long expectedInboundChainLength = 5L;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        Response response = SAML2ActionTestingSupport.buildResponse();
        response.getAssertions().add(SAML2ActionTestingSupport.buildAssertion());
        
        assertionToModify = SAML2ActionTestingSupport.buildAssertion();
        assertionToModify.getAuthnStatements().add(SAML2ActionTestingSupport.buildAuthnStatement());
        response.getAssertions().add(assertionToModify);
        
        browserSSOProfileConfig = new BrowserSSOProfileConfiguration();
        browserSSOProfileConfig.setMaximumTokenDelegationChainLength(expectedProfileChainLength);
        
        profileConfigs = new ArrayList<>();
        profileConfigs.add(browserSSOProfileConfig);
        
        rc = new RequestContextBuilder()
            .setInboundMessage(SAML2ActionTestingSupport.buildAuthnRequest())
            .setOutboundMessage(response)
            .setRelyingPartyProfileConfigurations(profileConfigs)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        delegatedAssertion = SAML2ActionTestingSupport.buildAssertion();
        delegatedAssertion.setAdvice((Advice) XMLObjectSupport.buildXMLObject(Advice.DEFAULT_ELEMENT_NAME));
        DelegationPolicy delPolicy = (DelegationPolicy) XMLObjectSupport.buildXMLObject(DelegationPolicy.DEFAULT_ELEMENT_NAME);
        delPolicy.setMaximumTokenDelegationChainLength(expectedInboundChainLength);
        delegatedAssertion.getAdvice().getChildren().add(delPolicy);
        
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedToken(delegatedAssertion);
        
        action = new AddDelegationPolicyToAssertion();
    }
    
    @Test
    public void testChainLengthFromProfileConfig() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), expectedProfileChainLength);
    }
    
    @Test
    public void testNoRelyingPartyContext() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        prc.removeSubcontext(RelyingPartyContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), AddDelegationPolicyToAssertion.DEFAULT_POLICY_MAX_CHAIN_LENGTH);
    }
    
    @Test
    public void testNoProfileConfig() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        prc.getSubcontext(RelyingPartyContext.class).setProfileConfig(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), AddDelegationPolicyToAssertion.DEFAULT_POLICY_MAX_CHAIN_LENGTH);
    }
    
    @Test
    public void testChainLengthFromDelegatedAssertion() throws ComponentInitializationException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), expectedInboundChainLength);
    }
    
    @Test
    public void testNoDelegatedAssertion() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), expectedProfileChainLength);
    }
    
    @Test
    public void testNoDelegatedAssertionAdvice() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).getAttestedToken().setAdvice(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), AddDelegationPolicyToAssertion.DEFAULT_POLICY_MAX_CHAIN_LENGTH);
    }
    
    @Test
    public void testNoDelegatedAssertionPolicy() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).getAttestedToken().getAdvice().getChildren().clear();
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(getOutboundChainLength(), AddDelegationPolicyToAssertion.DEFAULT_POLICY_MAX_CHAIN_LENGTH);
    }
    
    @Test
    public void testActivationCondition() throws ComponentInitializationException {
        action.setActivationCondition(Predicates.alwaysFalse());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertNull(getOutboundChainLength());
    }
    
    @Test
    public void testNoAssertionsToModify() throws ComponentInitializationException {
        ((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().clear();
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertNull(getOutboundChainLength());
    }
    
    
    
    // Helpers
    
    private Long getOutboundChainLength() {
        Advice advice = assertionToModify.getAdvice();
        if (advice != null) {
            List<XMLObject> policies = advice.getChildren(DelegationPolicy.DEFAULT_ELEMENT_NAME);
            if (policies != null && !policies.isEmpty()) {
                Assert.assertEquals(policies.size(), 1);
                return ((DelegationPolicy)policies.get(0)).getMaximumTokenDelegationChainLength();
            }
        }
        return null;
    }

}
