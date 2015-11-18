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

package net.shibboleth.idp.saml.session.impl;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.session.SAML2SPSession;
import net.shibboleth.idp.session.context.LogoutPropagationContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.profile.SAML2ActionTestingSupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link PrepareInboundMessageContext} unit test. */
public class PrepareInboundMessageContextTest extends OpenSAMLInitBaseTestCase {

    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    private PrepareInboundMessageContext action;

    @BeforeMethod public void setUp() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.setInboundMessageContext(null);
               
        final SAML2SPSession session = new SAML2SPSession("https://sp.example.org", System.currentTimeMillis(),
                System.currentTimeMillis() + 1800000, SAML2ActionTestingSupport.buildNameID("jdoe"), "foo");
        prc.getSubcontext(LogoutPropagationContext.class, true).setSession(session);
        
        action = new PrepareInboundMessageContext();
        action.initialize();
    }

    @Test public void testNoLogoutPropagationContext() {
        prc.removeSubcontext(LogoutPropagationContext.class);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
        Assert.assertNull(prc.getInboundMessageContext());
    }

    @Test public void testNoSession() {
        prc.getSubcontext(LogoutPropagationContext.class).setSession(null);
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
        Assert.assertNull(prc.getInboundMessageContext());
    }

    @Test public void testSuccess() {
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(prc.getInboundMessageContext());
        final SAMLPeerEntityContext ctx = prc.getInboundMessageContext().getSubcontext(SAMLPeerEntityContext.class);
        Assert.assertNotNull(ctx);
        Assert.assertEquals(ctx.getEntityId(), prc.getSubcontext(LogoutPropagationContext.class).getSession().getId());
    }

}