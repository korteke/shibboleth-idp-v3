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

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import javax.security.auth.Subject;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link InitializeAuthenticationContext} unit test. */
public class InitializeAuthenticationContextTest {

    /** Test that the action functions properly if there is no inbound message context. */
    @Test public void testNoInboundMessageContext() throws Exception {
        final RequestContext requestCtx = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);
        prc.setInboundMessageContext(null);

        final InitializeAuthenticationContext action = new InitializeAuthenticationContext();
        action.initialize();

        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);

        final AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        Assert.assertNotNull(authnCtx);
        Assert.assertFalse(authnCtx.isForceAuthn());
        Assert.assertFalse(authnCtx.isPassive());
        Assert.assertNull(authnCtx.getInitialAuthenticationResult());
    }

    /** Test that the action functions properly if there is no inbound message. */
    @Test public void testNoInboundMessage() throws Exception {
        final RequestContext requestCtx = new RequestContextBuilder().setInboundMessage(null).buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);

        final InitializeAuthenticationContext action = new InitializeAuthenticationContext();
        action.initialize();

        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);

        final AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        Assert.assertNotNull(authnCtx);
        Assert.assertFalse(authnCtx.isForceAuthn());
        Assert.assertFalse(authnCtx.isPassive());
        Assert.assertNull(authnCtx.getInitialAuthenticationResult());
    }

    /** Test that the action functions properly if the inbound message is not a SAML 2 AuthnRequest. */
    @Test public void testSAML1AuthnRequest() throws Exception {
        final RequestContext requestCtx =
                new RequestContextBuilder().setInboundMessage(
                        new IdPInitiatedSSORequest("https://sp.example.org/sp", null, null, null)
                        ).buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);

        final InitializeAuthenticationContext action = new InitializeAuthenticationContext();
        action.initialize();

        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);

        AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        Assert.assertNotNull(authnCtx);
        Assert.assertFalse(authnCtx.isForceAuthn());
        Assert.assertFalse(authnCtx.isPassive());
        Assert.assertNull(authnCtx.getInitialAuthenticationResult());
    }

    /** Test that the action proceeds properly if the inbound message is a SAML2 AuthnRequest. */
    @Test public void testCreateAuthenticationContext() throws Exception {
        final AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setIsPassive(true);
        authnRequest.setForceAuthn(true);

        final RequestContext requestCtx =
                new RequestContextBuilder().setInboundMessage(authnRequest).buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);

        final InitializeAuthenticationContext action = new InitializeAuthenticationContext();
        action.initialize();

        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);
        
        final AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        Assert.assertNotNull(authnCtx);
        Assert.assertTrue(authnCtx.isForceAuthn());
        Assert.assertTrue(authnCtx.isPassive());
        Assert.assertNull(authnCtx.getInitialAuthenticationResult());
    }

    /** Test that the action functions properly if there's an initial result already. */
    @Test public void testInitialResult() throws Exception {
        final RequestContext requestCtx = new RequestContextBuilder().buildRequestContext();
        final ProfileRequestContext prc = new WebflowRequestContextProfileRequestContextLookup().apply(requestCtx);
        prc.setInboundMessageContext(null);

        final InitializeAuthenticationContext action = new InitializeAuthenticationContext();
        action.initialize();

        final AuthenticationResult result = new AuthenticationResult("test", new Subject());
        prc.getSubcontext(AuthenticationContext.class, true).setAuthenticationResult(result);
        
        final Event event = action.execute(requestCtx);
        ActionTestingSupport.assertProceedEvent(event);

        final AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        Assert.assertNotNull(authnCtx);
        Assert.assertFalse(authnCtx.isForceAuthn());
        Assert.assertFalse(authnCtx.isPassive());
        Assert.assertNotNull(authnCtx.getInitialAuthenticationResult());
        Assert.assertEquals(authnCtx.getInitialAuthenticationResult(), result);
    }
}