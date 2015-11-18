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

import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.openliberty.xmltooling.soapbinding.Sender;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.AbstractMessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.messaging.context.SAMLPresenterEntityContext;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler implementation that handles the sb:Sender header on the inbound SOAP envelope.
 * 
 * <p>
 * If the header is present, the providerId value is stored in the message context via
 * {@link SAMLPresenterEntityContext#setEntityId(String)}.
 * </p>
 */
public class ProcessSenderHandler extends AbstractMessageHandler {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(ProcessSenderHandler.class);

    /** {@inheritDoc} */
    protected void doInvoke(MessageContext messageContext) throws MessageHandlerException {
        Sender header = getSender(messageContext);
        String headerValue = header != null ? StringSupport.trimOrNull(header.getProviderID()) : null;
        log.debug("Extracted inbound Liberty ID-WSF Sender providerId value: {}", headerValue);
        if (header != null && headerValue != null) {
            messageContext.getSubcontext(SAMLPresenterEntityContext.class, true).setEntityId(headerValue);
            SOAPMessagingSupport.registerUnderstoodHeader(messageContext, header);
        }
    }
    
    /**
     * Get Sender value.
     * 
     * @param messageContext the current message context
     * @return the Sender header
     */
    protected Sender getSender(@Nonnull final MessageContext messageContext) {
        List<XMLObject> senders = SOAPMessagingSupport.getInboundHeaderBlock(messageContext, 
                LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME);
        if (senders != null && !senders.isEmpty()) {
            return (Sender) senders.get(0);
        }
        return null; 
    }

}
