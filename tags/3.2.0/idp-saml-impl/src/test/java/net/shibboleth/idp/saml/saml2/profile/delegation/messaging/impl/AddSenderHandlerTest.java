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

import javax.annotation.Nullable;

import net.shibboleth.idp.saml.saml2.profile.delegation.impl.LibertyConstants;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.openliberty.xmltooling.soapbinding.Sender;
import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.soap.SOAPMessagingBaseTestCase;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/**
 *
 */
public class AddSenderHandlerTest extends SOAPMessagingBaseTestCase {
    
    private AddSenderHandler handler;
    
    @BeforeMethod
    protected void setUp() throws ComponentInitializationException {
        handler = new AddSenderHandler();
    }
    
    @Test
    public void testInputPresentDefaultStrategy() throws ComponentInitializationException, MessageHandlerException {
        getMessageContext().getSubcontext(SAMLSelfEntityContext.class, true).setEntityId("urn:test:foo");
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        
        Assert.assertFalse(SOAPMessagingSupport.getOutboundHeaderBlock(getMessageContext(), LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME).isEmpty());
        Sender sender = (Sender) SOAPMessagingSupport.getOutboundHeaderBlock(getMessageContext(), LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME).get(0);
        Assert.assertEquals(sender.getProviderID(), "urn:test:foo");
    }
    
    @Test
    public void testNoInputDefaultStrategy() throws ComponentInitializationException, MessageHandlerException {
        handler.initialize();
        handler.invoke(getMessageContext());
        
        
        Assert.assertTrue(SOAPMessagingSupport.getOutboundHeaderBlock(getMessageContext(), LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME).isEmpty());
    }
    
    @Test
    public void testInputPresentNonDefaultStrategy() throws ComponentInitializationException, MessageHandlerException {
        getMessageContext().getSubcontext(TestContext.class, true).value="urn:test:abc123";
        
        handler.setProviderIdLookupFunction(new Function<MessageContext, String>() {
            @Nullable public String apply(@Nullable MessageContext input) {
                return input.getSubcontext(TestContext.class).value;
            }
        });
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        
        Assert.assertFalse(SOAPMessagingSupport.getOutboundHeaderBlock(getMessageContext(), LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME).isEmpty());
        Sender sender = (Sender) SOAPMessagingSupport.getOutboundHeaderBlock(getMessageContext(), LibertyConstants.SOAP_BINDING_SENDER_ELEMENT_NAME).get(0);
        Assert.assertEquals(sender.getProviderID(), "urn:test:abc123");
    }
    
    
    
    public static class TestContext extends BaseContext {
        public String value;
    }

}
