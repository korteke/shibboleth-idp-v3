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
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLPresenterEntityContext;
import org.opensaml.saml.ext.saml2delrestrict.Delegate;
import org.opensaml.saml.ext.saml2delrestrict.DelegationRestrictionType;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/**
 *
 */
public class AddDelegationRestrictionToAssertionsTest extends OpenSAMLInitBaseTestCase {
    
    private AddDelegationRestrictionToAssertions action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private Assertion delegatedAssertion;
    private String delegatedConfirmationMethod;
    
    private DelegationRestrictionType delegatedRestrictionsCondition;
    
    private String[] initialDelegates = 
            new String []{"http:/foo.example.org", "http://bar.example.org", "http://baz.exqmple.org"};
    
    private String presenterEntityID = "http://portal.example.org";
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        Response response = SAML2ActionTestingSupport.buildResponse();
        response.getAssertions().add(SAML2ActionTestingSupport.buildAssertion());
        
        rc = new RequestContextBuilder()
            .setInboundMessage(SAML2ActionTestingSupport.buildAuthnRequest())
            .setOutboundMessage(response)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        delegatedAssertion = SAML2ActionTestingSupport.buildAssertion();
        
        delegatedRestrictionsCondition = 
                (DelegationRestrictionType) XMLObjectSupport.getBuilder(DelegationRestrictionType.TYPE_NAME)
                .buildObject(Condition.DEFAULT_ELEMENT_NAME, DelegationRestrictionType.TYPE_NAME);
        
        for (String entityID : initialDelegates) {
            Delegate delegate = (Delegate) XMLObjectSupport.buildXMLObject(Delegate.DEFAULT_ELEMENT_NAME);
            delegate.setNameID(SAML2ActionTestingSupport.buildNameID(entityID));
            delegatedRestrictionsCondition.getDelegates().add(delegate);
        }
        
        delegatedAssertion.setConditions((Conditions) XMLObjectSupport.buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME));
        delegatedAssertion.getConditions().getConditions().add(delegatedRestrictionsCondition);
        
        delegatedConfirmationMethod = SubjectConfirmation.METHOD_HOLDER_OF_KEY;
        
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedToken(delegatedAssertion);
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedSubjectConfirmationMethod(delegatedConfirmationMethod);
        
        prc.getInboundMessageContext().getSubcontext(SAMLPresenterEntityContext.class, true).setEntityId(presenterEntityID);
        
        action = new AddDelegationRestrictionToAssertions();
    }
    
    @Test
    public void testSuccessCloneExistingDelegates() throws ComponentInitializationException, MarshallingException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().size(), 1);
        Assertion newAssertion = ((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().get(0);
        Assert.assertNotNull(newAssertion.getConditions());
        
        List<DelegationRestrictionType> drts = getDelegationRestrictionConditions(newAssertion.getConditions());
        Assert.assertEquals(drts.size(), 1);
        DelegationRestrictionType drt = drts.get(0);
        Assert.assertEquals(drt.getDelegates().size(), initialDelegates.length+1);
        
        Delegate newDelegate = drt.getDelegates().get(initialDelegates.length);
        Assert.assertNotNull(newDelegate.getNameID());
        Assert.assertEquals(newDelegate.getNameID().getValue(), presenterEntityID);
        Assert.assertNotNull(newDelegate.getConfirmationMethod());
        Assert.assertEquals(newDelegate.getConfirmationMethod(), delegatedConfirmationMethod);
        Assert.assertNotNull(newDelegate.getDelegationInstant());
    }
    
    @Test
    public void testSuccessNoExistingDelegates() throws ComponentInitializationException, MarshallingException {
        delegatedAssertion.setConditions(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        Assert.assertEquals(((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().size(), 1);
        Assertion newAssertion = ((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().get(0);
        Assert.assertNotNull(newAssertion.getConditions());
        
        List<DelegationRestrictionType> drts = getDelegationRestrictionConditions(newAssertion.getConditions());
        Assert.assertEquals(drts.size(), 1);
        DelegationRestrictionType drt = drts.get(0);
        Assert.assertEquals(drt.getDelegates().size(), 1);
        
        Delegate newDelegate = drt.getDelegates().get(0);
        Assert.assertNotNull(newDelegate.getNameID());
        Assert.assertEquals(newDelegate.getNameID().getValue(), presenterEntityID);
        Assert.assertNotNull(newDelegate.getConfirmationMethod());
        Assert.assertEquals(newDelegate.getConfirmationMethod(), delegatedConfirmationMethod);
        Assert.assertNotNull(newDelegate.getDelegationInstant());
    }
    
    @Test
    public void testActivationCondition() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class); // This would otherwise cause failure
        action.setActivationCondition(Predicates.alwaysFalse());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }
    
    @Test
    public void testNoAssertionsToModify() throws ComponentInitializationException {
        ((Response)prc.getOutboundMessageContext().getMessage()).getAssertions().clear();
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }
    
    @Test
    public void testNoResponse() throws ComponentInitializationException {
        prc.getOutboundMessageContext().setMessage(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_MSG_CTX);
    }
    
    @Test
    public void testNoLibertyContext() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test
    public void testNoDelegatedAssertion() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).setAttestedToken(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test
    public void testNoDelegatedConfirmationMethod() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).setAttestedSubjectConfirmationMethod(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test
    public void testNoPresenter() throws ComponentInitializationException {
        prc.getInboundMessageContext().removeSubcontext(SAMLPresenterEntityContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test
    public void testNoPresenterEntityID() throws ComponentInitializationException {
        prc.getInboundMessageContext().getSubcontext(SAMLPresenterEntityContext.class).setEntityId(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }
    
    
    // Helpers 
    
    private List<DelegationRestrictionType> getDelegationRestrictionConditions(Conditions conditions) {
        ArrayList<DelegationRestrictionType> drts = new ArrayList<>();
        for (Condition conditionChild : conditions.getConditions()) {
            if (DelegationRestrictionType.TYPE_NAME.equals(conditionChild.getSchemaType())) {
                if (conditionChild instanceof DelegationRestrictionType) {
                    drts.add((DelegationRestrictionType) conditionChild);
                }
            }
        }
        return drts;
    }

}
