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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.openliberty.xmltooling.soapbinding.Sender;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.soap.messaging.AbstractHeaderGeneratingMessageHandler;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Handler implementation that adds a Liberty sb:Sender header to the outbound SOAP envelope.
 */
public class AddSenderHandler extends AbstractHeaderGeneratingMessageHandler {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(AddSenderHandler.class);
    
    /** The providerId lookup function. */
    private Function<MessageContext, String> providerIdLookupFunction;
    
    /** The providerId value to send. */
    private String providerId;
    
    /** Constructor. *
     */
    public AddSenderHandler() {
        super();
        providerIdLookupFunction = new SAMLSelfEntityIDLookupFunction();
    }

    /**
     * Set the providerId lookup function.
     * 
     * @param function the lookup function
     */
    public void setProviderIdLookupFunction(Function<MessageContext, String> function) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        providerIdLookupFunction = Constraint.isNotNull(function, 
                "ProviderId lookup function may not be null");
    }
    
    /** {@inheritDoc} */
    protected boolean doPreInvoke(@Nonnull final MessageContext messageContext) throws MessageHandlerException {
        providerId = providerIdLookupFunction.apply(messageContext);
        if (providerId != null) {
            log.debug("Resolved Liberty ID-WSF Sender providerId value: {}", providerId);
            return true;
        } else {
            log.debug("Unable to resolve Liberty ID-WSF Sender providerId value, skipping further processing");
            return false;
        }
    }

    /** {@inheritDoc} */
    protected void doInvoke(@Nonnull final MessageContext messageContext) throws MessageHandlerException {
        log.debug("Issuing Liberty ID-WSF Sender with providerId value: {}", providerId);
        Sender sender = (Sender) XMLObjectSupport.buildXMLObject(LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME);
        sender.setProviderID(providerId);
        decorateGeneratedHeader(messageContext, sender);
        SOAPMessagingSupport.addHeaderBlock(messageContext, sender);
    }
    
    /** Function to return the SAML self entityID from the MessageContext. */
    public static class SAMLSelfEntityIDLookupFunction implements Function<MessageContext, String> {

        /** {@inheritDoc} */
        @Nullable public String apply(@Nullable MessageContext input) {
            if (input != null) {
                SAMLSelfEntityContext selfContext = input.getSubcontext(SAMLSelfEntityContext.class);
                if (selfContext != null) {
                    return selfContext.getEntityId();
                }
            }
            return null;
        }
    }

}
