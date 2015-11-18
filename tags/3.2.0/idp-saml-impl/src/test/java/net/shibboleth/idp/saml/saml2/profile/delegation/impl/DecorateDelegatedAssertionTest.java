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
import java.util.Objects;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationRequest;
import net.shibboleth.idp.saml.saml2.profile.delegation.impl.DecorateDelegatedAssertion.LibertySSOSEndpointURLStrategy;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import net.shibboleth.utilities.java.support.xml.XMLAssertTestNG;

import org.openliberty.xmltooling.disco.MetadataAbstract;
import org.openliberty.xmltooling.disco.ProviderID;
import org.openliberty.xmltooling.disco.SecurityContext;
import org.openliberty.xmltooling.disco.ServiceType;
import org.openliberty.xmltooling.soapbinding.Framework;
import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.KeyInfoConfirmationDataType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.soap.wsaddressing.EndpointReference;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 *
 */
public class DecorateDelegatedAssertionTest extends OpenSAMLInitBaseTestCase {
    
    private boolean print = false;
    
    private String ssosURL = "https://idp.example.org:8443/idp/profile/IDWSF/SSOS";
    
    private AuthnRequest authnRequest;
    
    private Response response;
    
    private Assertion assertion;
    
    private Element origAssertionDOM;
    
    private BrowserSSOProfileConfiguration browserSSOProfileConfig;
    
    private List<ProfileConfiguration> profileConfigs;
    
    private List<PublicKey> publicKeys;
    
    private int numKeys = 3;
    
    private List<Credential> credentials;
    
    private DecorateDelegatedAssertion action;
    
    private MockServletContext servletContext;
    
    private MockHttpServletRequest servletRequest;
    
    private RequestContext rc;
    
    private ProfileRequestContext prc;
    
    private DelegationContext delegationContext;
    
    public DecorateDelegatedAssertionTest() throws NoSuchAlgorithmException, NoSuchProviderException {
        publicKeys = new ArrayList<>();
        for (int i=0; i<numKeys; i++) {
            publicKeys.add(KeySupport.generateKeyPair("RSA", 2048, null).getPublic()); 
        }
        credentials = new ArrayList<>();
        for (PublicKey publicKey : publicKeys) {
            credentials.add(CredentialSupport.getSimpleCredential(publicKey, null));
        }
    }
    
    @BeforeMethod
    protected void setUp() throws ComponentInitializationException, MarshallingException {
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
        SAMLPeerEntityContext peerContext = rpcContext.getSubcontext(SAMLPeerEntityContext.class, true);
        peerContext.setEntityId(ActionTestingSupport.INBOUND_MSG_ISSUER);
        peerContext.setRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        rpcContext.setRelyingPartyIdContextTree(peerContext);
        
        action = new DecorateDelegatedAssertion();
        action.setHttpServletRequest(servletRequest);
        action.setLibertySSOSEndpointURL(ssosURL);
        action.setKeyInfoGeneratorManager(DefaultSecurityConfigurationBootstrap.buildBasicKeyInfoGeneratorManager());
        
        delegationContext = prc.getSubcontext(DelegationContext.class, true);
        delegationContext.setIssuingDelegatedAssertion(true);
        delegationContext.setDelegationRequested(DelegationRequest.REQUESTED_REQUIRED);
        delegationContext.setSubjectConfirmationCredentials(credentials);
        
        origAssertionDOM = XMLObjectSupport.marshall(assertion);
        assertion.releaseDOM();
        assertion.releaseChildrenDOM(true);
    }
    
    @BeforeMethod(dependsOnMethods="setUp")
    protected void printBefore() {
        if (print) {
            System.out.println(prettyPrint(authnRequest));
            System.out.println(prettyPrint(response));
        }
    }
    
    @AfterMethod
    protected void printAfter() {
        if (print) {
            System.out.println(prettyPrint(response));
        }
    }

    @Test(expectedExceptions=UninitializedComponentException.class)
    public void testNotInitialized() throws Exception {
        action.execute(rc);
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testNoConfiguredEndpointNoStrategy() throws Exception {
        action = new DecorateDelegatedAssertion();
        action.setLibertySSOSEndpointURL(null);
        action.setLibertySSOSEndpointURLLookupStrategy(null);
        action.setKeyInfoGeneratorManager(DefaultSecurityConfigurationBootstrap.buildBasicKeyInfoGeneratorManager());
        action.initialize();
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testNoKeyInfoManager() throws Exception {
        action = new DecorateDelegatedAssertion();
        action.setLibertySSOSEndpointURL(ssosURL);
        action.initialize();
    }
    
    @Test
    public void testNoRelyingPartyContext() throws Exception {
        prc.removeSubcontext(RelyingPartyContext.class);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testNoAssertions() throws Exception {
        response.getAssertions().clear();
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testActivationCondition() throws Exception {
        // Ensure that activation condition is evaled first.
        action.setActivationCondition(Predicates.alwaysFalse());

        // This would otherwise generate preExecute error, but shouldn't even get there.
        delegationContext.setSubjectConfirmationCredentials(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testNoDelegationContext() throws Exception {
        prc.removeSubcontext(DelegationContext.class);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testUndecoratedAssertion();
    }
    
    
    @Test
    public void testDelegationNotActive() throws Exception {
        delegationContext.setIssuingDelegatedAssertion(false);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testDelegationActive() throws Exception {
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testDecoratedAssertion();
    }
    
    @Test
    public void testDelegationActiveNoCredentials() throws Exception {
        delegationContext.setSubjectConfirmationCredentials(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testEndpointViaDefaultStrategy() throws Exception {
        action.setLibertySSOSEndpointURL(null);
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
        
        testDecoratedAssertion();
    }
    
    @Test
    public void testEndpointStrategyProducesNull() throws Exception {
        action.setLibertySSOSEndpointURL(null);
        action.setLibertySSOSEndpointURLLookupStrategy(
                new Function<Pair<ProfileRequestContext,HttpServletRequest>, String>() {
                    public String apply(@Nullable Pair<ProfileRequestContext, HttpServletRequest> input) {
                        return null;
                    }
                });
        
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
        
        testUndecoratedAssertion();
    }
    
    @Test
    public void testDefaultEndpointStrategy() {
        // Sanity check
        Assert.assertEquals(servletRequest.getRequestURL().toString(), "https://idp.example.org/idp/profile/SAML2/Redirect/SSO");
        
        Pair<ProfileRequestContext, HttpServletRequest> input = new Pair<>(prc, (HttpServletRequest)servletRequest);
        
        LibertySSOSEndpointURLStrategy strategy = new DecorateDelegatedAssertion.LibertySSOSEndpointURLStrategy();
        Assert.assertEquals(strategy.apply(input), ssosURL);
    }
    
    
    
    
    // Helper methods
    
    private String prettyPrint(XMLObject xmlObject) {
        try {
            Element element = XMLObjectSupport.marshall(xmlObject);
            return SerializeSupport.prettyPrintXML(element);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void testUndecoratedAssertion() throws MarshallingException {
        Element currentAssertionDOM = XMLObjectSupport.marshall(assertion);
        assertion.releaseDOM();
        assertion.releaseChildrenDOM(true);
        Assert.assertNotSame(origAssertionDOM.getOwnerDocument(), currentAssertionDOM.getOwnerDocument());
        XMLAssertTestNG.assertXMLEqual(origAssertionDOM.getOwnerDocument(), currentAssertionDOM.getOwnerDocument());
    }
    
    private void testDecoratedAssertion() throws MarshallingException {
        Element currentAssertionDOM = XMLObjectSupport.marshall(assertion);
        assertion.releaseDOM();
        assertion.releaseChildrenDOM(true);
        Assert.assertNotSame(origAssertionDOM.getOwnerDocument(), currentAssertionDOM.getOwnerDocument());
        XMLAssertTestNG.assertXMLNotEqual(origAssertionDOM.getOwnerDocument(), currentAssertionDOM.getOwnerDocument());
        
        // SubjectConfirmation
        Assert.assertNotNull(assertion.getSubject().getSubjectConfirmations());
        Assert.assertEquals(assertion.getSubject().getSubjectConfirmations().size(), 1);
        SubjectConfirmation sc = assertion.getSubject().getSubjectConfirmations().get(0);
        Assert.assertEquals(sc.getMethod(), SubjectConfirmation.METHOD_HOLDER_OF_KEY);
        Assert.assertNotNull(sc.getNameID());
        Assert.assertEquals(sc.getNameID().getValue(), ActionTestingSupport.INBOUND_MSG_ISSUER);
        Assert.assertTrue(sc.getSubjectConfirmationData() instanceof KeyInfoConfirmationDataType);
        KeyInfoConfirmationDataType confData = (KeyInfoConfirmationDataType) sc.getSubjectConfirmationData();
        Assert.assertEquals(confData.getKeyInfos().size(), numKeys);
        Assert.assertEquals(((KeyInfo)confData.getKeyInfos().get(0)).getKeyValues().size(), 1);
        
        // Audience
        Assert.assertNotNull(assertion.getConditions());
        Assert.assertEquals(assertion.getConditions().getAudienceRestrictions().size(), 1);
        Assert.assertTrue(assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().size() > 0);
        boolean sawAudience = false;
        for (Audience audience : assertion.getConditions().getAudienceRestrictions().get(0).getAudiences()) {
            if (Objects.equals(audience.getAudienceURI(), ActionTestingSupport.OUTBOUND_MSG_ISSUER)) {
                sawAudience = true;
            }
        }
        Assert.assertTrue(sawAudience);
        
        // Endpoint Attribute
        Assert.assertEquals(assertion.getAttributeStatements().size(), 1);
        Attribute ssosAttrib = null;
        for (Attribute attrib : assertion.getAttributeStatements().get(0).getAttributes()) {
            if (Objects.equals(attrib.getName(), LibertyConstants.SERVICE_TYPE_SSOS)) {
                ssosAttrib = attrib;
                break;
            }
        }
        Assert.assertNotNull(ssosAttrib);
        Assert.assertEquals(ssosAttrib.getAttributeValues().size(), 1);
        Assert.assertTrue(ssosAttrib.getAttributeValues().get(0) instanceof XSAny);
        XSAny attribValue = (XSAny) ssosAttrib.getAttributeValues().get(0);
        Assert.assertEquals(attribValue.getUnknownXMLObjects(EndpointReference.ELEMENT_NAME).size(), 1);
        EndpointReference epr = (EndpointReference) attribValue.getUnknownXMLObjects(EndpointReference.ELEMENT_NAME).get(0);
        Assert.assertNotNull(epr.getAddress());
        Assert.assertEquals(epr.getAddress().getValue(), ssosURL);
        
        Assert.assertNotNull(epr.getMetadata());
        
        Assert.assertEquals(epr.getMetadata().getUnknownXMLObjects(LibertyConstants.DISCO_ABSTRACT_ELEMENT_NAME).size(), 1);
        Assert.assertEquals(((MetadataAbstract)epr.getMetadata().getUnknownXMLObjects(
                LibertyConstants.DISCO_ABSTRACT_ELEMENT_NAME).get(0)).getValue(), LibertyConstants.SSOS_EPR_METADATA_ABSTRACT);
        
        Assert.assertEquals(epr.getMetadata().getUnknownXMLObjects(LibertyConstants.DISCO_SERVICE_TYPE_ELEMENT_NAME).size(), 1);
        Assert.assertEquals(((ServiceType)epr.getMetadata().getUnknownXMLObjects(
                LibertyConstants.DISCO_SERVICE_TYPE_ELEMENT_NAME).get(0)).getValue(), LibertyConstants.SERVICE_TYPE_SSOS);
        
        Assert.assertEquals(epr.getMetadata().getUnknownXMLObjects(LibertyConstants.DISCO_PROVIDERID_ELEMENT_NAME).size(), 1);
        Assert.assertEquals(((ProviderID)epr.getMetadata().getUnknownXMLObjects(
                LibertyConstants.DISCO_PROVIDERID_ELEMENT_NAME).get(0)).getValue(), ActionTestingSupport.OUTBOUND_MSG_ISSUER);
        
        Assert.assertEquals(epr.getMetadata().getUnknownXMLObjects(Framework.DEFAULT_ELEMENT_NAME).size(), 1);
        Assert.assertEquals(((Framework)epr.getMetadata().getUnknownXMLObjects(
                Framework.DEFAULT_ELEMENT_NAME).get(0)).getVersion(), "2.0");
        
        Assert.assertEquals(epr.getMetadata().getUnknownXMLObjects(LibertyConstants.DISCO_SECURITY_CONTEXT_ELEMENT_NAME).size(), 1);
        SecurityContext secContext = (SecurityContext) epr.getMetadata().getUnknownXMLObjects(LibertyConstants.DISCO_SECURITY_CONTEXT_ELEMENT_NAME).get(0);
        Assert.assertEquals(secContext.getSecurityMechIDs().size(), 1);
        Assert.assertEquals(secContext.getSecurityMechIDs().get(0).getValue(), LibertyConstants.SECURITY_MECH_ID_CLIENT_TLS_PEER_SAML_V2);
        Assert.assertEquals(secContext.getTokens().size(), 1);
        Assert.assertEquals(secContext.getTokens().get(0).getRef(), "#" + SAML2ActionTestingSupport.ASSERTION_ID);
        Assert.assertEquals(secContext.getTokens().get(0).getUsage(), LibertyConstants.TOKEN_USAGE_SECURITY_TOKEN);
    }

}
