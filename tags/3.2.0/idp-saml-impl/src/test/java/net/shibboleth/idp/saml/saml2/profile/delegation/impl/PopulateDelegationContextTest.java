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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationRequest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/**
 *
 */
public class PopulateDelegationContextTest extends OpenSAMLInitBaseTestCase {
    
    private AuthnRequest authnRequest;
    
    private Response response;
    
    private Assertion assertion;
    
    private BrowserSSOProfileConfiguration browserSSOProfileConfig;
    
    private List<ProfileConfiguration> profileConfigs;
    
    private List<PublicKey> publicKeys;
    
    private int numKeys = 3;
    
    private SAMLPeerEntityContext samlPeerContext;
    
    private SAMLMetadataContext samlMetadataContext;
    
    private PopulateDelegationContext action;
    
    private MockServletContext servletContext;
    
    private MockHttpServletRequest servletRequest;
    
    private RequestContext rc;
    
    private ProfileRequestContext prc;
    
    public PopulateDelegationContextTest() throws NoSuchAlgorithmException, NoSuchProviderException {
        publicKeys = new ArrayList<>();
        for (int i=0; i<numKeys; i++) {
            publicKeys.add(KeySupport.generateKeyPair("RSA", 2048, null).getPublic()); 
        }
    }
    
    @BeforeMethod
    protected void setUp() throws ComponentInitializationException {
        servletContext = new MockServletContext();
        servletContext.setContextPath("/idp");
        servletRequest = new MockHttpServletRequest(servletContext);
        servletRequest.setScheme("https");
        servletRequest.setServerName("idp.example.org");
        servletRequest.setServerPort(443);
        servletRequest.setRequestURI("/idp/profile/SAML2/Redirect/SSO");
        servletRequest.setContextPath("/idp");
        
        authnRequest = SAML2ActionTestingSupport.buildAuthnRequest();
        authnRequest.setIssuer(SAML2ActionTestingSupport.buildIssuer(ActionTestingSupport.INBOUND_MSG_ISSUER));
        
        response = SAML2ActionTestingSupport.buildResponse();
        response.setIssuer(SAML2ActionTestingSupport.buildIssuer(ActionTestingSupport.OUTBOUND_MSG_ISSUER));
        
        assertion = SAML2ActionTestingSupport.buildAssertion();
        assertion.setID(SAML2ActionTestingSupport.ASSERTION_ID);
        assertion.setIssuer(SAML2ActionTestingSupport.buildIssuer(ActionTestingSupport.OUTBOUND_MSG_ISSUER));
        assertion.setSubject(SAML2ActionTestingSupport.buildSubject("morpheus"));
        assertion.getAuthnStatements().add(SAML2ActionTestingSupport.buildAuthnStatement());
        assertion.getAttributeStatements().add(SAML2ActionTestingSupport.buildAttributeStatement());
        response.getAssertions().add(assertion);
        
        browserSSOProfileConfig = new BrowserSSOProfileConfiguration();
        
        profileConfigs = new ArrayList<>();
        profileConfigs.add(browserSSOProfileConfig);
        
        rc = new RequestContextBuilder()
            .setServletContext(servletContext)
            .setHttpRequest(servletRequest)
            .setInboundMessage(authnRequest)
            .setOutboundMessage(response)
            .setRelyingPartyProfileConfigurations(profileConfigs)
            .buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);

        RelyingPartyContext rpcContext = prc.getSubcontext(RelyingPartyContext.class);
        samlPeerContext = rpcContext.getSubcontext(SAMLPeerEntityContext.class, true);
        samlPeerContext.setEntityId(ActionTestingSupport.INBOUND_MSG_ISSUER);
        samlPeerContext.setRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        rpcContext.setRelyingPartyIdContextTree(samlPeerContext);
        samlMetadataContext = samlPeerContext.getSubcontext(SAMLMetadataContext.class, true);
        samlMetadataContext.setRoleDescriptor(buildSPSSODescriptor());
        
        MetadataCredentialResolver mcr = new MetadataCredentialResolver();
        mcr.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        mcr.initialize();
        
        action = new PopulateDelegationContext();
        action.setCredentialResolver(mcr);
    }

    @Test(expectedExceptions=UninitializedComponentException.class)
    public void testNotInitialized() throws Exception {
        action.execute(rc);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNoInboundMessageContext() throws Exception {
        prc.setInboundMessageContext(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_MSG_CTX);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNoInboundMessage() throws Exception {
        prc.getInboundMessageContext().setMessage(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_MSG_CTX);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testInboundMessageNotSAML2AuthnRequest() throws Exception {
        prc.getInboundMessageContext().setMessage(XMLObjectSupport.buildXMLObject(AttributeQuery.DEFAULT_ELEMENT_NAME));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testNoCredentialResolver() throws Exception {
        action = new PopulateDelegationContext();
        action.initialize();
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNoRelyingPartyContext() throws Exception {
        prc.removeSubcontext(RelyingPartyContext.class);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testActivationCondition() throws Exception {
        // Ensure that activation condition is evaled first.
        action.setActivationCondition(Predicates.alwaysFalse());

        // An empty PRC would otherwise generate lots of preExecute errors, but shouldn't even get there.
        prc.clearSubcontexts();
        prc.setInboundMessageContext(null);
        prc.setOutboundMessageContext(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNoMetadataContext() throws Exception {
        samlPeerContext.removeSubcontext(SAMLMetadataContext.class);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNoRoleDescriptor() throws Exception {
       samlMetadataContext.setRoleDescriptor(null);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testDelegationNotRequested() throws Exception {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testRequestedViaMetadataNotRequiredNotAllowed() throws Exception {
        samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true).setAttributeConsumingService(
                buildDelegationRequestAttributeConsumingService(false));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testRequestedViaMetadataRequiredNotAllowed() throws Exception {
        samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true).setAttributeConsumingService(
                buildDelegationRequestAttributeConsumingService(true));
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_SEC_CFG);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testRequestedViaMetadataNotRequiredAllowed() throws Exception {
        samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true).setAttributeConsumingService(
                buildDelegationRequestAttributeConsumingService(false));
        
        browserSSOProfileConfig.setAllowDelegation(Predicates.<ProfileRequestContext>alwaysTrue());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNotNull(delegationContext);
        Assert.assertEquals(delegationContext.isIssuingDelegatedAssertion(), true);
        Assert.assertEquals(delegationContext.getDelegationRequested(), DelegationRequest.REQUESTED_OPTIONAL);
        Assert.assertNotNull(delegationContext.getSubjectConfirmationCredentials());
        Assert.assertFalse(delegationContext.getSubjectConfirmationCredentials().isEmpty());
    }
    
    @Test
    public void testRequestedViaMetadataRequiredAllowed() throws Exception {
        samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true).setAttributeConsumingService(
                buildDelegationRequestAttributeConsumingService(true));
        
        browserSSOProfileConfig.setAllowDelegation(Predicates.<ProfileRequestContext>alwaysTrue());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNotNull(delegationContext);
        Assert.assertEquals(delegationContext.isIssuingDelegatedAssertion(), true);
        Assert.assertEquals(delegationContext.getDelegationRequested(), DelegationRequest.REQUESTED_REQUIRED);
        Assert.assertNotNull(delegationContext.getSubjectConfirmationCredentials());
        Assert.assertFalse(delegationContext.getSubjectConfirmationCredentials().isEmpty());
    }
    
    @Test
    public void testRequestedViaConditionsNotAllowed() throws Exception {
        authnRequest.setConditions(buildDelegationRequestConditions());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_SEC_CFG);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testRequestedViaConditionsAllowed() throws Exception {
        authnRequest.setConditions(buildDelegationRequestConditions());
        
        browserSSOProfileConfig.setAllowDelegation(Predicates.<ProfileRequestContext>alwaysTrue());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNotNull(delegationContext);
        Assert.assertEquals(delegationContext.isIssuingDelegatedAssertion(), true);
        Assert.assertEquals(delegationContext.getDelegationRequested(), DelegationRequest.REQUESTED_REQUIRED);
        Assert.assertNotNull(delegationContext.getSubjectConfirmationCredentials());
        Assert.assertFalse(delegationContext.getSubjectConfirmationCredentials().isEmpty());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testRequestedViaConditionsAllowedViaLegacyBoolean() throws Exception {
        authnRequest.setConditions(buildDelegationRequestConditions());
        
        browserSSOProfileConfig.setAllowingDelegation(true);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNotNull(delegationContext);
        Assert.assertEquals(delegationContext.isIssuingDelegatedAssertion(), true);
        Assert.assertEquals(delegationContext.getDelegationRequested(), DelegationRequest.REQUESTED_REQUIRED);
        Assert.assertNotNull(delegationContext.getSubjectConfirmationCredentials());
        Assert.assertFalse(delegationContext.getSubjectConfirmationCredentials().isEmpty());
    }
    
    @Test
    public void testRequiredNoKeyDescriptors() throws Exception {
        samlMetadataContext.getRoleDescriptor().getKeyDescriptors().clear();
        
        authnRequest.setConditions(buildDelegationRequestConditions());
        
        browserSSOProfileConfig.setAllowDelegation(Predicates.<ProfileRequestContext>alwaysTrue());

        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.MESSAGE_PROC_ERROR);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    @Test
    public void testNotRequiredNoKeyDescriptors() throws Exception {
        samlMetadataContext.getRoleDescriptor().getKeyDescriptors().clear();
        
        samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true).setAttributeConsumingService(
                buildDelegationRequestAttributeConsumingService(false));
        
        browserSSOProfileConfig.setAllowDelegation(Predicates.<ProfileRequestContext>alwaysTrue());
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        DelegationContext delegationContext = prc.getSubcontext(DelegationContext.class);
        Assert.assertNull(delegationContext);
    }
    
    
    // Helper methods
    
    private Conditions buildDelegationRequestConditions() {
        Audience audience = (Audience) XMLObjectSupport.buildXMLObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(prc.getSubcontext(RelyingPartyContext.class).getConfiguration().getResponderId());
        AudienceRestriction ar = (AudienceRestriction) XMLObjectSupport.buildXMLObject(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        ar.getAudiences().add(audience);
        Conditions conditions = (Conditions) XMLObjectSupport.buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.getAudienceRestrictions().add(ar);
        return conditions;
    }
    
    private AttributeConsumingService buildDelegationRequestAttributeConsumingService(boolean required) {
        RequestedAttribute ra = (RequestedAttribute) XMLObjectSupport.buildXMLObject(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        ra.setName(LibertyConstants.SERVICE_TYPE_SSOS);
        ra.setIsRequired(required);
        AttributeConsumingService acs = (AttributeConsumingService) XMLObjectSupport.buildXMLObject(AttributeConsumingService.DEFAULT_ELEMENT_NAME);
        acs.getRequestAttributes().add(ra);
        return acs;
    }
    
    private SPSSODescriptor buildSPSSODescriptor() {
        SPSSODescriptor spSSODescriptor = (SPSSODescriptor) XMLObjectSupport.buildXMLObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        
        for (PublicKey publicKey : publicKeys) {
            KeyInfo keyInfo = (KeyInfo) XMLObjectSupport.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            KeyInfoSupport.addPublicKey(keyInfo, publicKey);
            
            KeyDescriptor keyDescriptor = (KeyDescriptor) XMLObjectSupport.buildXMLObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);
            keyDescriptor.setUse(UsageType.SIGNING);
            keyDescriptor.setKeyInfo(keyInfo);
            
            spSSODescriptor.getKeyDescriptors().add(keyDescriptor);
        }
        
        EntityDescriptor ed = (EntityDescriptor) XMLObjectSupport.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        ed.setEntityID(ActionTestingSupport.INBOUND_MSG_ISSUER);
        ed.getRoleDescriptors().add(spSSODescriptor);
        
        return spSSODescriptor;
    }

}
