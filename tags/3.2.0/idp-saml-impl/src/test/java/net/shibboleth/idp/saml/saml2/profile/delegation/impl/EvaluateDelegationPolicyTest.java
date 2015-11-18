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

import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.impl.EvaluateDelegationPolicy.PolicyMaxChainLengthStrategy;
import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.ext.saml2delrestrict.Delegate;
import org.opensaml.saml.ext.saml2delrestrict.DelegationRestrictionType;
import org.opensaml.saml.saml2.core.Advice;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 *
 */
public class EvaluateDelegationPolicyTest extends OpenSAMLInitBaseTestCase {
    
    private EvaluateDelegationPolicy action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private SSOSProfileConfiguration ssosProfileConfig;
    
    private List<ProfileConfiguration> profileConfigs;
    
    private Assertion delegatedAssertion;
    
    private DelegationRestrictionType delegatedRestrictionsCondition;
    private DelegationPolicy delegationPolicy;
    
    private String[] delegates = 
            new String []{"http:/foo.example.org", "http://bar.example.org", "http://baz.exqmple.org"};
    
    private Long policyMaxChainLength = delegates.length+1L;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        Response response = SAML2ActionTestingSupport.buildResponse();
        response.getAssertions().add(SAML2ActionTestingSupport.buildAssertion());
        
        ssosProfileConfig = new SSOSProfileConfiguration();
        ssosProfileConfig.setDelegationPredicate(Predicates.<ProfileRequestContext>alwaysTrue());
               
        
        profileConfigs = new ArrayList<>();
        profileConfigs.add(ssosProfileConfig);
        
        rc = new RequestContextBuilder()
            .setInboundMessage(SAML2ActionTestingSupport.buildAuthnRequest())
            .setOutboundMessage(response)
            .setRelyingPartyProfileConfigurations(profileConfigs)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        delegatedAssertion = SAML2ActionTestingSupport.buildAssertion();
        
        delegatedRestrictionsCondition = 
                (DelegationRestrictionType) XMLObjectSupport.getBuilder(DelegationRestrictionType.TYPE_NAME)
                .buildObject(Condition.DEFAULT_ELEMENT_NAME, DelegationRestrictionType.TYPE_NAME);
        
        for (String entityID : delegates) {
            Delegate delegate = (Delegate) XMLObjectSupport.buildXMLObject(Delegate.DEFAULT_ELEMENT_NAME);
            delegate.setNameID(SAML2ActionTestingSupport.buildNameID(entityID));
            delegatedRestrictionsCondition.getDelegates().add(delegate);
        }
        
        delegatedAssertion.setConditions((Conditions) XMLObjectSupport.buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME));
        delegatedAssertion.getConditions().getConditions().add(delegatedRestrictionsCondition);
        
        delegationPolicy = (DelegationPolicy) XMLObjectSupport.buildXMLObject(DelegationPolicy.DEFAULT_ELEMENT_NAME);
        delegationPolicy.setMaximumTokenDelegationChainLength(policyMaxChainLength);
        
        delegatedAssertion.setAdvice((Advice) XMLObjectSupport.buildXMLObject(Advice.DEFAULT_ELEMENT_NAME));
        delegatedAssertion.getAdvice().getChildren().add(delegationPolicy);
        
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedToken(delegatedAssertion);
        
        action = new EvaluateDelegationPolicy();
    }
    
    @Test
    public void testDefaultChainLengthStrategy() throws ComponentInitializationException {
        action.initialize();
        //The non-static strategy reads instance vars populated during execute();
        action.execute(rc);
        
        PolicyMaxChainLengthStrategy strategy = action.new PolicyMaxChainLengthStrategy();
        Assert.assertEquals(strategy.apply(prc), policyMaxChainLength);
    }
    
    @Test
    public void testDefaultChainLengthStrategyNoAssertion() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        action.initialize();
        //The non-static strategy reads instance vars populated during execute();
        action.execute(rc);
        
        PolicyMaxChainLengthStrategy strategy = action.new PolicyMaxChainLengthStrategy();
        Assert.assertNull(strategy.apply(prc));
    }
    
    @Test
    public void testDefaultChainLengthStrategyNoPolicy() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).getAttestedToken().setAdvice(null);
        
        action.initialize();
        //The non-static strategy reads instance vars populated during execute();
        action.execute(rc);
        
        PolicyMaxChainLengthStrategy strategy = action.new PolicyMaxChainLengthStrategy();
        Assert.assertNull(strategy.apply(prc));
    }
    
    @Test
    public void testSuccessNoInboundChain() throws ComponentInitializationException {
        delegatedAssertion.setConditions(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }
    
    @Test
    public void testSuccessChainShorterThanPolicy() throws ComponentInitializationException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }
    
    @Test
    public void testSuccessNoChainDefaultChainPolicy() throws ComponentInitializationException {
        delegatedAssertion.setConditions(null);
        action.setPolicyMaxChainLengthStrategy(new MockChainLengthStrategy(null));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }
    
    @Test
    public void testChainLongerThanPolicy() throws ComponentInitializationException {
        action.setPolicyMaxChainLengthStrategy(new MockChainLengthStrategy(delegates.length-1L));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_SEC_CFG);
    }
    
    @Test
    public void testChainEqualToPolicy() throws ComponentInitializationException {
        action.setPolicyMaxChainLengthStrategy(new MockChainLengthStrategy((long)delegates.length));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_SEC_CFG);
    }
    
    @Test
    public void testPredicateDisallows() throws ComponentInitializationException {
        ssosProfileConfig.setDelegationPredicate(Predicates.<ProfileRequestContext>alwaysFalse());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_SEC_CFG);
    }
    
    @Test
    public void testNoDelegatedAssertion() throws ComponentInitializationException {
        action.setAssertionTokenStrategy(new Function<ProfileRequestContext, Assertion>() {
            @Nullable public Assertion apply(@Nullable ProfileRequestContext input) {
                return null;
            }});
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.NO_CREDENTIALS);
    }
    
    @Test
    public void testNoRelyingPartyContext() throws ComponentInitializationException {
        prc.removeSubcontext(RelyingPartyContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test
    public void testNoProfileConfig() throws ComponentInitializationException {
        prc.getSubcontext(RelyingPartyContext.class).setProfileConfig(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    
    
    // Helpers
    
    private static class MockChainLengthStrategy implements Function<ProfileRequestContext, Long> {
        private Long length;
        public MockChainLengthStrategy(Long value) {
            length = value;
        }
        @Nullable public Long apply(@Nullable ProfileRequestContext input) {
            return length;
        }
    }

}
