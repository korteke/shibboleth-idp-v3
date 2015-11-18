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

import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.openliberty.xmltooling.soapbinding.Framework;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.soap.messaging.AbstractHeaderGeneratingMessageHandler;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler implementation that adds a Liberty sbf:Framework header to the outbound SOAP envelope.
 */
public class AddFrameworkHandler extends AbstractHeaderGeneratingMessageHandler {
    
    /** Default Framework version. */
    public static final String DEFAULT_VERSION = "2.0";
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(AddFrameworkHandler.class);
    
    /** The Version value. */
    private String version = DEFAULT_VERSION;
    
    /**
     * Get the version value.
     * 
     * @return the version, or null
     */
    @Nullable public String getVersion() {
        return version;
    }

    /**
     * Set the version value. 
     * 
     * @param newVersion the new version value
     */
    public void setVersion(@Nullable final String newVersion) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        version = StringSupport.trimOrNull(newVersion);
    }

    /** {@inheritDoc} */
    protected void doInvoke(@Nonnull final MessageContext messageContext) throws MessageHandlerException {
        log.debug("Issuing Liberty ID-WSF Framework header with version value: {}", getVersion());
        Framework framework = (Framework) XMLObjectSupport.buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion(getVersion());
        decorateGeneratedHeader(messageContext, framework);
        SOAPMessagingSupport.addHeaderBlock(messageContext, framework);
    }

}
