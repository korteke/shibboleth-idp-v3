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

package net.shibboleth.idp.saml.saml2.profile.delegation.messaging.impl;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.net.HttpServletSupport;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.encoder.servlet.BaseHttpServletResponseXMLMessageEncoder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.encoding.SAMLMessageEncoder;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.Header;
import org.opensaml.soap.wsaddressing.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Encoder for Liberty ID-WSF 2.0 SOAP 1.1 HTTP binding carrying SAML protocol messages
 * used in SAML delegation.
 */
public class LibertyHTTPSOAP11Encoder extends BaseHttpServletResponseXMLMessageEncoder<SAMLObject> 
        implements SAMLMessageEncoder {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LibertyHTTPSOAP11Encoder.class);
    
    /** SOAP Envelope builder. */
    private SOAPObjectBuilder<Envelope> envBuilder;
    
    /** SOAP Body builder. */
    private SOAPObjectBuilder<Body> bodyBuilder;
    
    /** Constructor. */
    public LibertyHTTPSOAP11Encoder() {
        super();
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        envBuilder = (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        bodyBuilder = (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
        
        Constraint.isNotNull(envBuilder, "Envelope Builder cannot be null");
        Constraint.isNotNull(bodyBuilder, "Body Builder cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getBindingURI() {
        return LibertyConstants.SOAP_BINDING_20_URI;
    }
    
    /** {@inheritDoc} */
    public void prepareContext() throws MessageEncodingException {
        MessageContext<SAMLObject> messageContext = getMessageContext();
        XMLObject payload = null;
        
        Fault fault = SOAPMessagingSupport.getSOAP11Fault(messageContext);
        if (fault != null) {
            log.debug("Saw SOAP 1.1 Fault payload with fault code, replacing any existing context message: {}", 
                    fault.getCode() != null ? fault.getCode().getValue() : null);
            payload = fault;
            messageContext.setMessage(null);
        } else {
            payload = messageContext.getMessage();
        }
        
        if (payload == null) {
            throw new MessageEncodingException("No outbound message or Fault contained in message context");
        }
        
        if (payload instanceof Envelope) {
            storeSOAPEnvelope((Envelope) payload);
        } else {
            buildAndStoreSOAPMessage(payload);
        }
        
    }

    /** {@inheritDoc} */
    protected void doEncode() throws MessageEncodingException {
        Envelope envelope = getSOAPEnvelope();
        Element envelopeElem = marshallMessage(envelope);
        
        prepareHttpServletResponse();

        try {
            SerializeSupport.writeNode(envelopeElem, getHttpServletResponse().getOutputStream());
        } catch (IOException e) {
            throw new MessageEncodingException("Problem writing SOAP envelope to servlet output stream", e);
        }
    }
    
    /**
     * Store the constructed SOAP envelope in the message context for later encoding.
     * 
     * @param envelope the SOAP envelope
     */
    protected void storeSOAPEnvelope(Envelope envelope) {
        getMessageContext().getSubcontext(SOAP11Context.class, true).setEnvelope(envelope);
    }

    /**
     * Retrieve the previously stored SOAP envelope from the message context.
     * 
     * @return the previously stored SOAP envelope
     */
    protected Envelope getSOAPEnvelope() {
        return getMessageContext().getSubcontext(SOAP11Context.class, true).getEnvelope();
    }

    /**
     * Builds the SOAP message to be encoded.
     * 
     * @param payload body of the SOAP message
     */
    protected void buildAndStoreSOAPMessage(@Nonnull final XMLObject payload) {
        Envelope envelope = getSOAPEnvelope();
        if (envelope == null) {
            envelope = envBuilder.buildObject();
            storeSOAPEnvelope(envelope);
        }
        
        Body body = envelope.getBody();
        if (body == null) {
            body = bodyBuilder.buildObject();
            envelope.setBody(body);
        }
        
        if (!body.getUnknownXMLObjects().isEmpty()) {
            log.warn("Existing SOAP Envelope Body already contained children");
        }
        
        body.getUnknownXMLObjects().add(payload);
    }
    
    
    /**
     * <p>
     * This implementation performs the following actions on the context's {@link HttpServletResponse}:
     * <ol>
     *   <li>Adds the HTTP header: "Cache-control: no-cache, no-store"</li>
     *   <li>Adds the HTTP header: "Pragma: no-cache"</li>
     *   <li>Sets the character encoding to: "UTF-8"</li>
     *   <li>Sets the content type to: "text/xml"</li>
     *   <li>Sets the SOAPAction HTTP header the value returned by {@link #getSOAPAction()}, if
     *   that returns non-null.</li>
     * </ol>
     * </p>
     * 
     * <p>
     * Subclasses should NOT set the SOAPAction HTTP header in this method. Instead, they should override 
     * the method {@link #getSOAPAction()}.
     * </p>
     * 
     * @throws MessageEncodingException thrown if there is a problem preprocessing the transport
     */
    protected void prepareHttpServletResponse() throws MessageEncodingException {
        HttpServletResponse response = getHttpServletResponse();
        HttpServletSupport.addNoCacheHeaders(response);
        HttpServletSupport.setUTF8Encoding(response);
        HttpServletSupport.setContentType(response, "text/xml");
        
        String soapAction = getSOAPAction();
        if (soapAction != null) {
            response.setHeader("SOAPAction", soapAction);
        } else {
            response.setHeader("SOAPAction", "");
        }
        
        response.setStatus(getHTTPResponseStatusCode());
    }

    /**
     * Determine the value of the SOAPAction HTTP header to send.
     * 
     * <p>
     * The default behavior is to return the value of the SOAP Envelope's WS-Addressing Action header,
     * if present.
     * </p>
     * 
     * @return a SOAPAction HTTP header URI value
     */
    protected String getSOAPAction() {
        Envelope env = getSOAPEnvelope();
        Header header = env.getHeader();
        if (header == null) {
            return null;
        }
        List<XMLObject> objList = header.getUnknownXMLObjects(Action.ELEMENT_NAME);
        if (objList == null || objList.isEmpty()) {
            return null;
        } else {
            return ((Action)objList.get(0)).getValue();
        }
    }
    
    /**
     * Get the HTTP response status code to return.
     * 
     * @return the HTTP response status code
     */
    protected int getHTTPResponseStatusCode() {
        Integer contextStatus = getMessageContext().getSubcontext(SOAP11Context.class, true).getHTTPResponseStatus();
        if (contextStatus != null) {
            return contextStatus;
        }
        
        Envelope envelope = getSOAPEnvelope();
        if (envelope != null && envelope.getBody() != null) {
            Body body = envelope.getBody();
            List<XMLObject> faults = body.getUnknownXMLObjects(Fault.DEFAULT_ELEMENT_NAME);
            if (!faults.isEmpty()) {
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }
        
        return HttpServletResponse.SC_OK;
    }
    
    /** {@inheritDoc} */
    protected XMLObject getMessageToLog() {
        return getMessageContext().getSubcontext(SOAP11Context.class, true).getEnvelope();
    }
    
}