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

import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decoder for Liberty ID-WSF 2.0 SOAP 1.1 HTTP binding carrying SAML protocol messages
 * used in SAML delegation.
 * 
 * <p>
 * This decoder takes a mandatory {@link MessageHandler} instance which is used to determine
 * and populate the message that is returned as the {@link MessageContext#getMessage()}.
 * </p>
 * 
 *  <p>
 *  A SOAP message-oriented message exchange style might just populate the Envelope as the message.
 *  An application-specific payload-oriented message exchange would handle a specific type
 * of payload structure.  
 * </p>
 * 
 */
public class LibertyHTTPSOAP11Decoder extends BaseHttpServletRequestXMLMessageDecoder<SAMLObject> 
        implements SAMLMessageDecoder {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LibertyHTTPSOAP11Decoder.class);
    
    /** Message handler to use in processing the message body. */
    private MessageHandler<SAMLObject> bodyHandler;
    
    /**
     * Constructor.
     */
    public LibertyHTTPSOAP11Decoder() {
        super();
        setBodyHandler(new SAMLSOAPDecoderBodyHandler());
    }
    
    /** {@inheritDoc} */
    public String getBindingURI() {
        return LibertyConstants.SOAP_BINDING_20_URI;
    }

    /**
     * Get the configured body handler MessageHandler.
     * 
     * @return Returns the bodyHandler.
     */
    public MessageHandler<SAMLObject> getBodyHandler() {
        return bodyHandler;
    }

    /**
     * Set the configured body handler MessageHandler.
     * 
     * @param newBodyHandler The bodyHandler to set.
     */
    public void setBodyHandler(MessageHandler<SAMLObject> newBodyHandler) {
        bodyHandler = newBodyHandler;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (getBodyHandler() == null) {
            throw new ComponentInitializationException("Body handler MessageHandler cannot be null");
        }
    }    

    /** {@inheritDoc} */
    @Override
    protected void doDecode() throws MessageDecodingException {
        MessageContext<SAMLObject> messageContext = new MessageContext<>();
        HttpServletRequest request = getHttpServletRequest();

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new MessageDecodingException("This message decoder only supports the HTTP POST method");
        }

        log.debug("Unmarshalling SOAP message");
        Envelope soapMessage;
        try {
            soapMessage = (Envelope) unmarshallMessage(request.getInputStream());
            messageContext.getSubcontext(SOAP11Context.class, true).setEnvelope(soapMessage);
        } catch (IOException e) {
            log.error("Unable to obtain input stream from HttpServletRequest", e);
            throw new MessageDecodingException("Unable to obtain input stream from HttpServletRequest", e);
        }
        
        try {
            getBodyHandler().invoke(messageContext);
        } catch (MessageHandlerException e) {
            log.error("Error processing SOAP Envelope body", e);
            throw new MessageDecodingException("Error processing SOAP Envelope body", e);
        }
        
        if (messageContext.getMessage() == null) {
            log.warn("Body handler did not properly populate the message in message context");
            throw new MessageDecodingException("Body handler did not properly populate the message in message context");
        }
        
        setMessageContext(messageContext);
        
        populateBindingContext(getMessageContext());
        
        SAMLObject samlMessage = getMessageContext().getMessage();
        log.debug("Decoded SOAP messaged which included SAML message of type {}", samlMessage.getElementQName());
    }
    
    /**
     * Populate the context which carries information specific to this binding.
     * 
     * @param messageContext the current message context
     */
    protected void populateBindingContext(MessageContext<SAMLObject> messageContext) {
        SAMLBindingContext bindingContext = messageContext.getSubcontext(SAMLBindingContext.class, true);
        bindingContext.setBindingUri(getBindingURI());
        bindingContext.setHasBindingSignature(false);
        bindingContext.setIntendedDestinationEndpointURIRequired(false);
    }
 
    /** {@inheritDoc} */
    @Override
    protected XMLObject getMessageToLog() {
        return getMessageContext().getSubcontext(SOAP11Context.class, true).getEnvelope();
    }

}