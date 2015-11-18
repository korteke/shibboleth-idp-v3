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

package net.shibboleth.idp.saml.saml1.profile.impl;

import net.shibboleth.idp.saml.profile.impl.BaseIdPInitiatedSSORequestMessageDecoder;
import net.shibboleth.idp.saml.profile.impl.IdPInitiatedSSORequest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the {@link IdPInitiatedSSORequestMessageDecoder}.
 */
public class IdPInitiatedSSORequestMessageDecoderTest {
    
    private IdPInitiatedSSORequestMessageDecoder decoder;
    
    private MockHttpServletRequest request;
    
    private String entityId = "http://sp.example.org";
    
    private String acsUrl = "http://sp.example.org/acs";
    
    private String relayState = "myRelayState";
    
    private String sessionID = "abc123";
    
    private String messageID;
    
    private Long time;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        // Note: protocol takes time in seconds, so divide by 1000.
        // Components usually produce milliseconds, so later multiply or divide by 1000 in assertions as appropriate.
        time = System.currentTimeMillis()/1000;
        
        messageID = "_" + sessionID + "!" + time.toString();
        
        request = new MockHttpServletRequest();
        request.setRequestedSessionId(sessionID);
        
        decoder = new IdPInitiatedSSORequestMessageDecoder();
        decoder.setHttpServletRequest(request);
        decoder.initialize();
    }
    
    @Test
    public void testDecoder() throws MessageDecodingException {
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.PROVIDER_ID_PARAM,  entityId);
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.SHIRE_PARAM,  acsUrl);
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.TARGET_PARAM,  relayState);
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.TIME_PARAM,  time.toString());
        
        decoder.decode();
        
        MessageContext<IdPInitiatedSSORequest> messageContext = decoder.getMessageContext();
        Assert.assertNotNull(messageContext);
        IdPInitiatedSSORequest ssoRequest = messageContext.getMessage();
        Assert.assertNotNull(ssoRequest);
        
        Assert.assertEquals(ssoRequest.getEntityId(), entityId, "Incorrect decoded entityId value");
        Assert.assertEquals(ssoRequest.getAssertionConsumerServiceURL(), acsUrl, "Incorrect decoded ACS URL value");
        Assert.assertEquals(ssoRequest.getRelayState(), relayState, "Incorrect decoded relay state value");
        Assert.assertEquals(Long.valueOf(ssoRequest.getTime()/1000), time, "Incorrect decoded time value");
        
        Assert.assertEquals(messageContext.getSubcontext(SAMLPeerEntityContext.class, true).getEntityId(), entityId,
                "Incorrect decoded entityId value in peer context");
        
        SAMLBindingContext bindingContext = messageContext.getSubcontext(SAMLBindingContext.class, true);
        Assert.assertEquals(bindingContext.getRelayState(), relayState, "Incorrect decoded relay state value in binding context");
        Assert.assertEquals(bindingContext.getBindingUri(), "urn:mace:shibboleth:1.0:profiles:AuthnRequest",
                "Incorrect binding URI in binding context");
        
        SAMLMessageInfoContext msgInfoContext = messageContext.getSubcontext(SAMLMessageInfoContext.class, true);
        Assert.assertEquals(msgInfoContext.getMessageIssueInstant(), new DateTime(time*1000, ISOChronology.getInstanceUTC()),
                "Incorrect decoded issue instant value in message info context");
        Assert.assertEquals(msgInfoContext.getMessageId(), messageID, "Incorrect decoded message ID value in message info context");
    }

    @Test(expectedExceptions=MessageDecodingException.class)
    public void testMissingTarget() throws MessageDecodingException {
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.PROVIDER_ID_PARAM,  entityId);
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.SHIRE_PARAM,  acsUrl);
        request.addParameter(BaseIdPInitiatedSSORequestMessageDecoder.TIME_PARAM,  time.toString());
        
        decoder.decode();
    }
}