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

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test case for Liberty HTTP SOAP 1.1 decoder.
 */
public class LibertyHTTPSOAP11DecoderTest extends XMLObjectBaseTestCase {
    
    private LibertyHTTPSOAP11Decoder decoder;
    
    private MockHttpServletRequest httpRequest;
    
    @BeforeMethod
    protected void setUp() throws Exception {
        httpRequest = new MockHttpServletRequest();
        httpRequest.setMethod("POST");
        
        decoder = new LibertyHTTPSOAP11Decoder();
        decoder.setParserPool(parserPool);
        decoder.setHttpServletRequest(httpRequest);
        decoder.initialize();
    }

    /**
     * Tests decoding a SOAP 1.1 message.
     */
    @Test
    public void testDecoding() throws Exception {
        String requestContent = "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap11:Body><samlp:Response ID=\"foo\" IssueInstant=\"1970-01-01T00:00:00.000Z\" Version=\"2.0\" "
                + "xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"><samlp:Status><samlp:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/></samlp:Status></samlp:Response>"
                + "</soap11:Body></soap11:Envelope>";
        httpRequest.setContent(requestContent.getBytes());

        decoder.decode();
        MessageContext<SAMLObject> messageContext = decoder.getMessageContext();

        Assert.assertNotNull(messageContext.getSubcontext(SOAP11Context.class).getEnvelope());
        Assert.assertTrue(messageContext.getMessage() instanceof Response);
    }
    
    protected String encodeMessage(XMLObject message) throws MarshallingException {
        marshallerFactory.getMarshaller(message).marshall(message);
        return SerializeSupport.nodeToString(message.getDOM());
    }
}