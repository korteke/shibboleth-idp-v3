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

import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.impl.PopulateLibertyContext.TokenStrategy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.wssecurity.SAML20AssertionToken;
import org.opensaml.soap.wssecurity.messaging.Token.ValidationStatus;
import org.opensaml.soap.wssecurity.messaging.WSSecurityContext;
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
public class PopulateLibertyContextTest extends OpenSAMLInitBaseTestCase {
    
    private PopulateLibertyContext action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private SAML20AssertionToken delegatedToken;
    private Assertion delegatedAssertion;
    private String delegatedConfirmationMethod;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        Response response = SAML2ActionTestingSupport.buildResponse();
        response.getAssertions().add(SAML2ActionTestingSupport.buildAssertion());
        
        rc = new RequestContextBuilder()
            .setInboundMessage(SAML2ActionTestingSupport.buildAuthnRequest())
            .setOutboundMessage(response)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        Assertion dummyAssertion = SAML2ActionTestingSupport.buildAssertion();
        dummyAssertion.setSubject(SAML2ActionTestingSupport.buildSubject("neo"));
        SubjectConfirmation dummyConfirmation = (SubjectConfirmation) XMLObjectSupport.buildXMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        dummyConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        dummyAssertion.getSubject().getSubjectConfirmations().add(dummyConfirmation);
        
        delegatedConfirmationMethod = SubjectConfirmation.METHOD_HOLDER_OF_KEY;
        
        delegatedAssertion = SAML2ActionTestingSupport.buildAssertion();
        delegatedAssertion.setSubject(SAML2ActionTestingSupport.buildSubject("morpheus"));
        SubjectConfirmation delegatedConfirmation = (SubjectConfirmation) XMLObjectSupport.buildXMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        delegatedConfirmation.setMethod(delegatedConfirmationMethod);
        delegatedAssertion.getSubject().getSubjectConfirmations().add(delegatedConfirmation);
        
        WSSecurityContext wssContext = prc.getInboundMessageContext().getSubcontext(WSSecurityContext.class, true);
        
        SAML20AssertionToken dummyToken = new SAML20AssertionToken(dummyAssertion);
        dummyToken.setSubjectConfirmation(dummyConfirmation);
        dummyToken.setValidationStatus(ValidationStatus.INVALID);
        wssContext.getTokens().add(dummyToken);
        
        delegatedToken = new SAML20AssertionToken(delegatedAssertion);
        delegatedToken.setSubjectConfirmation(delegatedConfirmation);
        delegatedToken.setValidationStatus(ValidationStatus.VALID);
        wssContext.getTokens().add(delegatedToken);
        
        action = new PopulateLibertyContext();
    }
    
    @Test
    public void testDefaultTokenStrategySuccess() throws ComponentInitializationException {
        action.initialize();
        
        TokenStrategy strategy = action.new TokenStrategy();
        Assert.assertSame(strategy.apply(prc), delegatedToken);
    }
    
    @Test
    public void testDefaultTokenStrategyNullInput() throws ComponentInitializationException {
        action.initialize();
        
        TokenStrategy strategy = action.new TokenStrategy();
        Assert.assertNull(strategy.apply(null));
    }
    
    @Test
    public void testDefaultTokenStrategyNoWSSContext() throws ComponentInitializationException {
        action.initialize();
        
        prc.getInboundMessageContext().removeSubcontext(WSSecurityContext.class);
        
        TokenStrategy strategy = action.new TokenStrategy();
        Assert.assertNull(strategy.apply(prc));
    }
    
    @Test
    public void testDefaultTokenStrategyNoTokens() throws ComponentInitializationException {
        action.initialize();
        
        prc.getInboundMessageContext().getSubcontext(WSSecurityContext.class).getTokens().clear();
        
        TokenStrategy strategy = action.new TokenStrategy();
        Assert.assertNull(strategy.apply(prc));
    }
    
    @Test
    public void testSuccess() throws ComponentInitializationException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        LibertySSOSContext libertyContext = prc.getSubcontext(LibertySSOSContext.class);
        Assert.assertNotNull(libertyContext);
        Assert.assertNotNull(libertyContext.getAttestedToken());
        Assert.assertSame(libertyContext.getAttestedToken(), delegatedAssertion);
        Assert.assertNotNull(libertyContext.getAttestedSubjectConfirmationMethod());
        Assert.assertEquals(libertyContext.getAttestedSubjectConfirmationMethod(), delegatedConfirmationMethod);
    }
    
    @Test
    public void testNoResolvedAssertionToken() throws ComponentInitializationException {
        action.setAssertionTokenStrategy(new Function<ProfileRequestContext, SAML20AssertionToken>() {
            @Nullable public SAML20AssertionToken apply(@Nullable ProfileRequestContext input) {
                return null;
            }});
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.NO_CREDENTIALS);
        
        LibertySSOSContext libertyContext = prc.getSubcontext(LibertySSOSContext.class);
        Assert.assertNull(libertyContext);
    }
    
    @Test
    public void testActivationCondition() throws ComponentInitializationException {
        //This would otherwise cause to fail
        action.setAssertionTokenStrategy(new Function<ProfileRequestContext, SAML20AssertionToken>() {
            @Nullable public SAML20AssertionToken apply(@Nullable ProfileRequestContext input) {
                return null;
            }});
        
        action.setActivationCondition(Predicates.alwaysFalse());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        LibertySSOSContext libertyContext = prc.getSubcontext(LibertySSOSContext.class);
        Assert.assertNull(libertyContext);
    }

}
