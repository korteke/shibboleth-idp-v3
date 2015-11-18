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

package net.shibboleth.idp.saml.saml2.profile.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.profile.impl.BaseIdPInitiatedSSORequestMessageDecoder;
import net.shibboleth.idp.saml.profile.impl.IdPInitiatedSSORequest;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** Decodes an incoming Shibboleth Authentication Request message. */
public class IdPInitiatedSSORequestMessageDecoder extends BaseIdPInitiatedSSORequestMessageDecoder<SAMLObject> 
    implements SAMLMessageDecoder {

    /** Protocol binding implemented by this decoder. */
    @Nonnull @NotEmpty private static final String BINDING_URI = "urn:mace:shibboleth:2.0:profiles:AuthnRequest";
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(IdPInitiatedSSORequestMessageDecoder.class);

    /** Builder of SAML 2 {@link AuthnRequest} objects. */
    private final SAMLObjectBuilder<AuthnRequest> requestBuilder;

    /** Builder of SAML 2 {@link Issuer} objects. */
    private final SAMLObjectBuilder<Issuer> issuerBuilder;
    
    /** Builder of SAML 2 {@link NameIDPolicy} objects. */
    private final SAMLObjectBuilder<NameIDPolicy> nipBuilder;
    
    /** The IdP-initiated request structure parsed from the inbound request. */
    @Nullable private IdPInitiatedSSORequest ssoRequest;

    /** Constructor. */
    public IdPInitiatedSSORequestMessageDecoder() {
        final XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory(); 
        
        requestBuilder = (SAMLObjectBuilder<AuthnRequest>)
                factory.<AuthnRequest>getBuilderOrThrow(AuthnRequest.DEFAULT_ELEMENT_NAME);
        issuerBuilder = (SAMLObjectBuilder<Issuer>)
                factory.<Issuer>getBuilderOrThrow(Issuer.DEFAULT_ELEMENT_NAME);
        nipBuilder = (SAMLObjectBuilder<NameIDPolicy>)
                factory.<NameIDPolicy>getBuilderOrThrow(NameIDPolicy.DEFAULT_ELEMENT_NAME);
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getBindingURI() {
        return BINDING_URI;
    }
    
    /**
     * Get the internally constructed instance of {@link IdPInitiatedSSORequest}.
     * 
     * @return the internal SSO request instance
     */
    @Nullable protected IdPInitiatedSSORequest getIdPInitiatedSSORequest() {
       return ssoRequest; 
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doDecode() throws MessageDecodingException {
        ssoRequest = buildIdPInitiatedSSORequest();
        
        final MessageContext<SAMLObject> messageContext = new MessageContext<>();
        messageContext.setMessage(buildAuthnRequest());
        
        populateBindingContext(messageContext);
        
        setMessageContext(messageContext);
    }
    
    /**
     * Build a synthetic AuthnRequest instance from the IdP-initiated SSO request structure.
     * 
     * @return the synthetic AuthnRequest message instance
     * 
     * @throws MessageDecodingException if the inbound request does not contain an entityID value
     */
    @Nonnull protected AuthnRequest buildAuthnRequest() throws MessageDecodingException {
        final AuthnRequest authnRequest = requestBuilder.buildObject();
        
        final Issuer requestIssuer = issuerBuilder.buildObject();
        requestIssuer.setValue(ssoRequest.getEntityId());
        authnRequest.setIssuer(requestIssuer);
        
        final NameIDPolicy nip = nipBuilder.buildObject();
        nip.setAllowCreate(true);
        authnRequest.setNameIDPolicy(nip);
        authnRequest.setAssertionConsumerServiceURL(ssoRequest.getAssertionConsumerServiceURL());
        authnRequest.setIssueInstant(new DateTime(ssoRequest.getTime(), ISOChronology.getInstanceUTC()));
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setID(getMessageID());
        
        return authnRequest;
    }
    
    /**
     * Populate the context which carries information specific to this binding.
     * 
     * @param messageContext the current message context
     */
    protected void populateBindingContext(@Nonnull final MessageContext<SAMLObject> messageContext) {
        String relayState = ssoRequest.getRelayState();
        log.debug("Decoded SAML RelayState of: {}", relayState);
        
        SAMLBindingContext bindingContext = messageContext.getSubcontext(SAMLBindingContext.class, true);
        bindingContext.setRelayState(relayState);
        
        bindingContext.setBindingUri(getBindingURI());
        bindingContext.setHasBindingSignature(false);
        bindingContext.setIntendedDestinationEndpointURIRequired(false);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable protected String getMessageToLog() {
        final SAMLObject message = getMessageContext().getMessage();
        if (message == null) {
            log.warn("Decoded message was null, nothing to log");
            return null;
        }
        
        final StringBuilder builder = new StringBuilder();
        builder.append("SAML 2 IdP-initiated request was: " + ssoRequest.toString());
        builder.append("\nSynthetically constructed SAML 2 AuthnRequest was: \n");
        
        try {
            final Element dom = XMLObjectSupport.marshall(message);
            builder.append(SerializeSupport.prettyPrintXML(dom));
            return builder.toString();
        } catch (MarshallingException e) {
            log.error("Unable to marshall message for logging purposes", e);
            return null;
        }
    }
}