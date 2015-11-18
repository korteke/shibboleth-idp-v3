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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.soap.wsaddressing.messaging.WSAddressingContext;
import org.opensaml.soap.wssecurity.messaging.WSSecurityContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class PopulateOutboundMessageContextTest {
    
    private PopulateOutboundMessageContext action;
    
    private ProfileRequestContext prc;
    
    @BeforeMethod
    public void setUp() {
        prc = new ProfileRequestContext<>();
        prc.setInboundMessageContext(new MessageContext<>());
        prc.setOutboundMessageContext(new MessageContext<>());
        
        action = new PopulateOutboundMessageContext();
    }
    
    @Test
    public void testBasic() throws ComponentInitializationException {
        DateTime now = new DateTime();
        prc.getInboundMessageContext().getSubcontext(WSAddressingContext.class, true).setMessageIDURI("urn:test:abc123");
        prc.getOutboundMessageContext().getSubcontext(SAMLMessageInfoContext.class, true).setMessageIssueInstant(now);
        
        action.initialize();
        
        action.execute(prc);
        
        Assert.assertEquals(prc.getOutboundMessageContext().getSubcontext(WSAddressingContext.class, true).getRelatesToURI(),
                "urn:test:abc123");
        Assert.assertSame(prc.getOutboundMessageContext().getSubcontext(WSSecurityContext.class, true).getTimestampCreated(),
                now);
    }
    
    @Test
    public void testNoInboundMessageID() throws ComponentInitializationException {
        DateTime now = new DateTime();
        prc.getOutboundMessageContext().getSubcontext(SAMLMessageInfoContext.class, true).setMessageIssueInstant(now);
        
        action.initialize();
        
        action.execute(prc);
        
        Assert.assertNull(prc.getOutboundMessageContext().getSubcontext(WSAddressingContext.class, true).getRelatesToURI());
        Assert.assertSame(prc.getOutboundMessageContext().getSubcontext(WSSecurityContext.class, true).getTimestampCreated(),
                now);
    }
    
    @Test
    public void testNoSAMLMessageInfoContext() throws ComponentInitializationException {
        prc.getInboundMessageContext().getSubcontext(WSAddressingContext.class, true).setMessageIDURI("urn:test:abc123");
        
        action.initialize();
        
        action.execute(prc);
        
        Assert.assertEquals(prc.getOutboundMessageContext().getSubcontext(WSAddressingContext.class, true).getRelatesToURI(),
                "urn:test:abc123");
        Assert.assertNotNull(prc.getOutboundMessageContext().getSubcontext(WSSecurityContext.class, true).getTimestampCreated());
    }

    @Test
    public void testNoInboundContext() throws ComponentInitializationException {
        prc.setInboundMessageContext(null);
        
        action.initialize();
        
        action.execute(prc);
        
        Assert.assertEquals(prc.getSubcontext(EventContext.class).getEvent(), EventIds.INVALID_MSG_CTX);
    }
    
    @Test
    public void testNoOutboundContext() throws ComponentInitializationException {
        prc.setOutboundMessageContext(null);
        
        action.initialize();
        
        action.execute(prc);
        
        Assert.assertEquals(prc.getSubcontext(EventContext.class).getEvent(), EventIds.INVALID_MSG_CTX);
    }
}
