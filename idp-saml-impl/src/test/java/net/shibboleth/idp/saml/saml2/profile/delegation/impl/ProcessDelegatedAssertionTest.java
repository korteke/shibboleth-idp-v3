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
import java.util.Set;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
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
public class ProcessDelegatedAssertionTest extends OpenSAMLInitBaseTestCase {
    
    private ProcessDelegatedAssertion action;
    
    private RequestContext rc;
    private ProfileRequestContext prc;
    
    private SSOSProfileConfiguration ssosProfileConfig;
    
    private List<ProfileConfiguration> profileConfigs;
    
    private Assertion delegatedAssertion;
    
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
        delegatedAssertion.setSubject(SAML2ActionTestingSupport.buildSubject("morpheus"));
        
        prc.getSubcontext(LibertySSOSContext.class, true).setAttestedToken(delegatedAssertion);
        
        action = new ProcessDelegatedAssertion();
    }
    
    @Test
    public void testSuccess() throws ComponentInitializationException {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        SubjectCanonicalizationContext c14nContext = prc.getSubcontext(SubjectCanonicalizationContext.class);
        Assert.assertNotNull(c14nContext);
        Assert.assertNotNull(c14nContext.getSubject());
        Set<NameIDPrincipal> nameIdPrincipals = c14nContext.getSubject().getPrincipals(NameIDPrincipal.class);
        Assert.assertNotNull(nameIdPrincipals);
        Assert.assertEquals(nameIdPrincipals.size(), 1);
        Assert.assertSame(nameIdPrincipals.iterator().next().getNameID(), delegatedAssertion.getSubject().getNameID());
        
        Assert.assertEquals(c14nContext.getRequesterId(), ActionTestingSupport.INBOUND_MSG_ISSUER);
        Assert.assertEquals(c14nContext.getResponderId(), ActionTestingSupport.OUTBOUND_MSG_ISSUER);
    }

    @Test
    public void testNoAssertion() throws ComponentInitializationException {
        prc.removeSubcontext(LibertySSOSContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.NO_CREDENTIALS);
        
        SubjectCanonicalizationContext c14nContext = prc.getSubcontext(SubjectCanonicalizationContext.class);
        Assert.assertNull(c14nContext);
    }

    @Test
    public void testNoSubject() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).getAttestedToken().setSubject(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.INVALID_SUBJECT);
        
        SubjectCanonicalizationContext c14nContext = prc.getSubcontext(SubjectCanonicalizationContext.class);
        Assert.assertNull(c14nContext);
    }

    @Test
    public void testNoNameID() throws ComponentInitializationException {
        prc.getSubcontext(LibertySSOSContext.class).getAttestedToken().getSubject().setNameID(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, AuthnEventIds.INVALID_SUBJECT);
        
        SubjectCanonicalizationContext c14nContext = prc.getSubcontext(SubjectCanonicalizationContext.class);
        Assert.assertNull(c14nContext);
    }

}
