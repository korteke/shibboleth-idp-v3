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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.openliberty.xmltooling.Konstantz;
import org.openliberty.xmltooling.soapbinding.Framework;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.AbstractMessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler implementation that handles sbf:Framework header on the inbound SOAP envelope.
 */
public class ProcessFrameworkHandler extends AbstractMessageHandler {
    
    /** Default Framework version. */
    public static final String DEFAULT_VERSION = "2.0";
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(ProcessFrameworkHandler.class);
    
    /** The expected version value. */
    private String expectedVersion = DEFAULT_VERSION;

    /**
     * Get the expected version value.
     * 
     * <p>Defaults to: {@link #DEFAULT_VERSION}</p>
     * 
     * @return the expected version, or null
     */
    @Nullable public String getExpectedVersion() {
        return expectedVersion;
    }

    /**
     * Set the expected version value. 
     * 
     * <p>Defaults to: {@link #DEFAULT_VERSION}</p>
     * 
     * @param version the new version value
     */
    public void setExpectedVersion(@Nullable final String version) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        expectedVersion = StringSupport.trimOrNull(version);
    }

    /** {@inheritDoc} */
    protected void doInvoke(MessageContext messageContext) throws MessageHandlerException {
        Framework header = getFramework(messageContext);
        String headerVersion = header != null ? StringSupport.trimOrNull(header.getVersion()) : null;
        log.debug("Checking inbound message Liberty ID-WSF Framework version value: {}", headerVersion);
        if (Objects.equals(getExpectedVersion(), headerVersion)) {
            log.debug("Inbound Liberty ID-WSF Framework version matched expected value");
            SOAPMessagingSupport.registerUnderstoodHeader(messageContext, header);
        } else {
            log.warn("Inbound Liberty ID-WSF Framework version '{}' did not match the expected value '{}'", 
                    headerVersion, getExpectedVersion());
            SOAPMessagingSupport.registerSOAP11Fault(messageContext, 
                    new QName(Konstantz.SBF_NS, Konstantz.Status.FRAMEWORK_VERSION_MISMATCH.getCode()),
                    "Framework version not supported: " + headerVersion, null, null, null);
            throw new MessageHandlerException("Inbound Liberty ID-WSF Framework version " 
                    + "did not match the expected value");
        }
    }
        
    /**
     * Get message Action header.
     * 
     * @param messageContext the current message context
     * @return the message Action header
     */
    protected Framework getFramework(@Nonnull final MessageContext messageContext) {
        List<XMLObject> frameworks = SOAPMessagingSupport.getInboundHeaderBlock(messageContext, 
                Framework.DEFAULT_ELEMENT_NAME);
        if (frameworks != null && !frameworks.isEmpty()) {
            return (Framework) frameworks.get(0);
        }
        return null; 
    }

}
