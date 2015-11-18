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

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.openliberty.xmltooling.soapbinding.Sender;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.messaging.context.SAMLPresenterEntityContext;
import org.opensaml.soap.SOAPMessagingBaseTestCase;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class ProcessSenderHandlerTest extends SOAPMessagingBaseTestCase {
    
    private ProcessSenderHandler handler;
    
    @BeforeMethod
    protected void setUp() throws ComponentInitializationException {
        handler = new ProcessSenderHandler();
    }
    
    @Test
    public void testHeaderPresent() throws ComponentInitializationException, MessageHandlerException {
        Sender sender = buildXMLObject(LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME);
        sender.setProviderID("urn:test:foo");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), sender);
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        Assert.assertEquals(getMessageContext().getSubcontext(SAMLPresenterEntityContext.class, true).getEntityId(), "urn:test:foo");
        
        Assert.assertTrue(SOAPMessagingSupport.checkUnderstoodHeader(getMessageContext(), sender));
    }
    
    @Test
    public void testHeaderNotPresent() throws ComponentInitializationException, MessageHandlerException {
        handler.initialize();
        handler.invoke(getMessageContext());
        
        Assert.assertNull(getMessageContext().getSubcontext(SAMLPresenterEntityContext.class, true).getEntityId());
    }
    
    @Test
    public void testHeaderEmptyValue() throws ComponentInitializationException, MessageHandlerException {
        Sender sender = buildXMLObject(LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME);
        sender.setProviderID("     ");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), sender);
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        Assert.assertNull(getMessageContext().getSubcontext(SAMLPresenterEntityContext.class, true).getEntityId());
        
        Assert.assertFalse(SOAPMessagingSupport.checkUnderstoodHeader(getMessageContext(), sender));
    }

}
