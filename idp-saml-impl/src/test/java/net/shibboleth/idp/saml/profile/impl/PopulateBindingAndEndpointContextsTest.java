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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.binding.impl.DefaultEndpointResolver;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.profile.SAMLEventIds;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.profile.SAML2ActionTestingSupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.google.common.base.Predicates;

/** Unit test for {@link PopulateBindingAndEndpointContexts}. */
public class PopulateBindingAndEndpointContextsTest extends XMLObjectBaseTestCase {

    private static final String RELAY_STATE = "foo";
    private static final String LOCATION = "https://sp.example.org/ACS";
    private static final String LOCATION_POST = "https://sp.example.org/POST2";
    private static final String LOCATION_ART = "https://sp.example.org/Art2";

    private RequestContext rc;

    private BrowserSSOProfileConfiguration profileConfig;
    
    private ProfileRequestContext prc;
    
    private PopulateBindingAndEndpointContexts action;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        final AuthnRequest request = SAML2ActionTestingSupport.buildAuthnRequest();
        request.setAssertionConsumerServiceURL(LOCATION_POST);
        request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        
        profileConfig = new BrowserSSOProfileConfiguration();
        
        rc = new RequestContextBuilder().setInboundMessage(request).setRelyingPartyProfileConfigurations(
                Collections.<ProfileConfiguration>singletonList(profileConfig)).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        prc.getInboundMessageContext().getSubcontext(SAMLBindingContext.class, true).setRelayState(RELAY_STATE);
        
        // Set these up so the context will be seen as anonymous or not based on metadata in the outbound context.
        prc.getSubcontext(RelyingPartyContext.class).setVerificationLookupStrategy(new SAMLVerificationLookupStrategy());
        prc.getSubcontext(RelyingPartyContext.class).setRelyingPartyIdContextTree(
                prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true));
        
        action = new PopulateBindingAndEndpointContexts();
        action.setEndpointResolver(new DefaultEndpointResolver());
        action.setEndpointType(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        final List<BindingDescriptor> bindings = new ArrayList<>();
        bindings.add(new BindingDescriptor());
        bindings.get(0).setId(SAMLConstants.SAML2_POST_BINDING_URI);
        bindings.get(0).initialize();
        action.setBindings(bindings);
        action.initialize();
    }

    @Test(expectedExceptions = ComponentInitializationException.class)
    public void testNoResolver() throws ComponentInitializationException {
        final PopulateBindingAndEndpointContexts badaction = new PopulateBindingAndEndpointContexts();
        badaction.initialize();
    }
    
    @Test(expectedExceptions = ComponentInitializationException.class)
    public void testBadEndpointType() throws ComponentInitializationException {
        final PopulateBindingAndEndpointContexts badaction = new PopulateBindingAndEndpointContexts();
        badaction.setEndpointType(AuthnRequest.DEFAULT_ELEMENT_NAME);
        badaction.initialize();
    }
    
    @Test
    public void testNoOutboundContext() {
        prc.setOutboundMessageContext(null);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_MSG_CTX);
    }

    @Test
    public void testNoBindings() throws ComponentInitializationException {
        final BindingDescriptor binding = new BindingDescriptor();
        binding.setId(SAMLConstants.SAML2_POST_BINDING_URI);
        binding.setActivationCondition(Predicates.<ProfileRequestContext>alwaysFalse());
        binding.initialize();
        final PopulateBindingAndEndpointContexts badaction = new PopulateBindingAndEndpointContexts();
        badaction.setEndpointResolver(new DefaultEndpointResolver());
        badaction.setBindings(Collections.singletonList(binding));
        badaction.initialize();
        
        final Event event = badaction.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }
    
    @Test
    public void testNoMetadata() {
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        
        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
        
        final SAMLEndpointContext epCtx = prc.getOutboundMessageContext().getSubcontext(
                SAMLPeerEntityContext.class, false).getSubcontext(SAMLEndpointContext.class, false);
        Assert.assertNotNull(epCtx);
        Assert.assertNotNull(epCtx.getEndpoint());
        Assert.assertEquals(epCtx.getEndpoint().getBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        Assert.assertEquals(epCtx.getEndpoint().getLocation(), LOCATION_POST);
    }

    /** An SP with no endpoints in metadata. */
    @Test
    public void testNoEndpoints() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPNoEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }

    /** An SP with no endpoints in metadata interacting with signed requests. */
    @Test
    public void testSignedNoEndpoints() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPNoEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
        
        // Allow signed, but request isn't.
        profileConfig.setSkipEndpointValidationWhenSigned(true);
        event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
        
        // Request is signed but we don't care.
        profileConfig.setSkipEndpointValidationWhenSigned(false);
        prc.getInboundMessageContext().getSubcontext(SAMLBindingContext.class).setHasBindingSignature(true);
        event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);

        // Request is signed and we care.
        profileConfig.setSkipEndpointValidationWhenSigned(true);
        prc.getInboundMessageContext().getSubcontext(SAMLBindingContext.class).setHasBindingSignature(true);
        event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
    }
    
    /** No endpoint with the location requested. */
    @Test
    public void testBadLocation() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(LOCATION);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }

    /** No endpoint at a location with the right binding requested. */
    @Test
    public void testBadBinding() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);

        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(SAMLConstants.SAML2_SOAP11_BINDING_URI);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }

    /** Endpoint matches but we don't support the binding. */
    @Test
    public void testUnsupportedBinding() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(LOCATION_ART);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }
    
    /** No endpoint with a requested index. */
    @Test
    public void testBadIndex() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);

        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceIndex(10);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(null);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(null);

        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }
    
    /** Test SOAP case. */
    @Test
    public void testSynchronous() throws ComponentInitializationException {
        prc.getInboundMessageContext().getSubcontext(SAMLBindingContext.class).setBindingUri(
                SAMLConstants.SAML2_SOAP11_BINDING_URI);
        
        final BindingDescriptor binding = new BindingDescriptor();
        binding.setId(SAMLConstants.SAML2_SOAP11_BINDING_URI);
        binding.setSynchronous(true);
        binding.initialize();
        final PopulateBindingAndEndpointContexts badaction = new PopulateBindingAndEndpointContexts();
        badaction.setEndpointResolver(new DefaultEndpointResolver());
        badaction.setBindings(Collections.singletonList(binding));
        badaction.initialize();
        
        final Event event = badaction.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_SOAP11_BINDING_URI);
        Assert.assertSame(binding, bindingCtx.getBindingDescriptor());
    }
    
    /** Requested location/binding are in metadata. */
    @Test
    public void testInMetadata() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        
        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
        
        final SAMLEndpointContext epCtx = prc.getOutboundMessageContext().getSubcontext(
                SAMLPeerEntityContext.class, false).getSubcontext(SAMLEndpointContext.class, false);
        Assert.assertNotNull(epCtx);
        Assert.assertNotNull(epCtx.getEndpoint());
        Assert.assertEquals(epCtx.getEndpoint().getBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        Assert.assertEquals(epCtx.getEndpoint().getLocation(), LOCATION_POST);
    }

    /** Requested index is in metadata. */
    @Test
    public void testIndexInMetadata() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);

        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceIndex(2);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(null);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(null);

        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);

        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
        
        final SAMLEndpointContext epCtx = prc.getOutboundMessageContext().getSubcontext(
                SAMLPeerEntityContext.class, false).getSubcontext(SAMLEndpointContext.class, false);
        Assert.assertNotNull(epCtx);
        Assert.assertNotNull(epCtx.getEndpoint());
        Assert.assertEquals(epCtx.getEndpoint().getBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        Assert.assertEquals(epCtx.getEndpoint().getLocation(), LOCATION_POST);
    }

    /** No endpoint with a requested index. */
    @Test
    public void testIndexUnsupportedBinding() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);

        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceIndex(3);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(null);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(null);

        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, SAMLEventIds.ENDPOINT_RESOLUTION_FAILED);
    }
    
    /** Get the default endpoint. */
    @Test
    public void testDefault() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);

        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setAssertionConsumerServiceURL(null);
        ((AuthnRequest) prc.getInboundMessageContext().getMessage()).setProtocolBinding(null);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);

        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
        
        final SAMLEndpointContext epCtx = prc.getOutboundMessageContext().getSubcontext(
                SAMLPeerEntityContext.class, false).getSubcontext(SAMLEndpointContext.class, false);
        Assert.assertNotNull(epCtx);
        Assert.assertNotNull(epCtx.getEndpoint());
        Assert.assertEquals(epCtx.getEndpoint().getBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        Assert.assertEquals(epCtx.getEndpoint().getLocation(), LOCATION_POST.replace("POST2", "POST"));
    }
    
    /** Test a SAML 1 request (use of SAML2 bindings here is just for simplicity in testing). */
    @Test
    public void testSAML1InMetadata() throws UnmarshallingException {
        final EntityDescriptor entity = loadMetadata("/net/shibboleth/idp/saml/impl/profile/SPWithEndpoints.xml");
        final SAMLMetadataContext mdCtx = new SAMLMetadataContext();
        mdCtx.setEntityDescriptor(entity);
        mdCtx.setRoleDescriptor(entity.getSPSSODescriptor("required"));
        prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true).addSubcontext(mdCtx);
        
        final IdPInitiatedSSORequest saml1Request = new IdPInitiatedSSORequest("foo", LOCATION_POST, null, null);
        prc.getInboundMessageContext().setMessage(saml1Request);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        
        final SAMLBindingContext bindingCtx = prc.getOutboundMessageContext().getSubcontext(SAMLBindingContext.class);
        Assert.assertNotNull(bindingCtx);
        Assert.assertNotNull(bindingCtx.getBindingDescriptor());
        Assert.assertEquals(bindingCtx.getRelayState(), RELAY_STATE);
        Assert.assertEquals(bindingCtx.getBindingUri(), SAMLConstants.SAML2_POST_BINDING_URI);
        
        final SAMLEndpointContext epCtx = prc.getOutboundMessageContext().getSubcontext(
                SAMLPeerEntityContext.class, false).getSubcontext(SAMLEndpointContext.class, false);
        Assert.assertNotNull(epCtx);
        Assert.assertNotNull(epCtx.getEndpoint());
        Assert.assertEquals(epCtx.getEndpoint().getBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        Assert.assertEquals(epCtx.getEndpoint().getLocation(), LOCATION_POST);
    }
    
    @Nonnull private EntityDescriptor loadMetadata(@Nonnull @NotEmpty final String path) throws UnmarshallingException {
        
        try {
            final URL url = getClass().getResource(path);
            Document doc = parserPool.parse(new FileInputStream(new File(url.toURI())));
            final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
            return (EntityDescriptor) unmarshaller.unmarshall(doc.getDocumentElement());
        } catch (FileNotFoundException | XMLParserException | URISyntaxException e) {
            throw new UnmarshallingException(e);
        }
    }
    
}