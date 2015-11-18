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

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.openliberty.xmltooling.soapbinding.Framework;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.soap.SOAPMessagingBaseTestCase;
import org.opensaml.soap.messaging.SOAPMessagingSupport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class ProcessFrameworkHandlerTest extends SOAPMessagingBaseTestCase {
    
    private ProcessFrameworkHandler handler;
    
    @BeforeMethod
    protected void setUp() throws ComponentInitializationException {
        handler = new ProcessFrameworkHandler();
    }
    
    @Test
    public void testDefaultExpected() throws ComponentInitializationException, MessageHandlerException {
        Framework framework = buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion("2.0");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), framework);
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        Assert.assertTrue(SOAPMessagingSupport.checkUnderstoodHeader(getMessageContext(), framework));
    }
    
    @Test(expectedExceptions=MessageHandlerException.class)
    public void testDefaultUnexpected() throws ComponentInitializationException, MessageHandlerException {
        Framework framework = buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion("3.0");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), framework);
        
        handler.initialize();
        handler.invoke(getMessageContext());
    }
    
    @Test
    public void testNonDefaultExpected() throws ComponentInitializationException, MessageHandlerException {
        Framework framework = buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion("3.0");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), framework);
        
        handler.setExpectedVersion("3.0");
        
        handler.initialize();
        handler.invoke(getMessageContext());
        
        Assert.assertTrue(SOAPMessagingSupport.checkUnderstoodHeader(getMessageContext(), framework));
    }
    
    @Test(expectedExceptions=MessageHandlerException.class)
    public void testNonDefaultUnexpected() throws ComponentInitializationException, MessageHandlerException {
        Framework framework = buildXMLObject(Framework.DEFAULT_ELEMENT_NAME);
        framework.setVersion("3.0");
        SOAPMessagingSupport.addHeaderBlock(getMessageContext(), framework);
        
        handler.setExpectedVersion("2.5");
        
        handler.initialize();
        handler.invoke(getMessageContext());
    }

}
