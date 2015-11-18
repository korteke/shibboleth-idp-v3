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

package net.shibboleth.idp.saml.profile.impl;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml1.core.Request;
import org.opensaml.saml.saml1.profile.SAML1ActionTestingSupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link InitializeOutboundMessageContext} unit test. */
public class InitializeOutboundMessageContextTest extends OpenSAMLInitBaseTestCase {

    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    private Request attributeQuery;
    
    private InitializeOutboundMessageContext action;

    @BeforeMethod public void setUp() throws ComponentInitializationException {
        attributeQuery = SAML1ActionTestingSupport.buildAttributeQueryRequest(
                SAML1ActionTestingSupport.buildSubject("jdoe"));
        src = new RequestContextBuilder().setInboundMessage(attributeQuery).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.setOutboundMessageContext(null);
        action = new InitializeOutboundMessageContext();
        action.initialize();
    }

    @Test public void testNoRelyingPartyContext() {
        prc.removeSubcontext(RelyingPartyContext.class);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CTX);
        Assert.assertNull(prc.getOutboundMessageContext());
    }

    @Test public void testNoPeerEntityContext() {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CTX);
        Assert.assertNull(prc.getOutboundMessageContext());
    }

    @Test public void testPeerEntityContextNoIssuer() {
        SAMLPeerEntityContext ctx = prc.getInboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true);
        prc.getSubcontext(RelyingPartyContext.class).setRelyingPartyIdContextTree(ctx);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(prc.getOutboundMessageContext());
        ctx = prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class);
        Assert.assertNotNull(ctx);
        Assert.assertNull(ctx.getEntityId());
    }

    @Test public void testPeerEntityContextIssuer() {
        SAMLPeerEntityContext ctx = prc.getInboundMessageContext().getSubcontext(SAMLPeerEntityContext.class, true);
        prc.getSubcontext(RelyingPartyContext.class).setRelyingPartyIdContextTree(ctx);
        attributeQuery.getAttributeQuery().setResource("issuer");
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(prc.getOutboundMessageContext());
        ctx = prc.getOutboundMessageContext().getSubcontext(SAMLPeerEntityContext.class);
        Assert.assertNotNull(ctx);
        Assert.assertEquals(ctx.getEntityId(), "issuer");
    }
    
    // TODO more tests

}