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

import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.profile.ActionTestingSupport;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.authn.principal.AuthenticationMethodPrincipal;
import net.shibboleth.idp.saml.saml1.profile.SAML1ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.Response;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AddAuthenticationStatementToAssertion} unit test. */
public class AddAuthenticationStatementToAssertionTest extends OpenSAMLInitBaseTestCase {

    private RequestContext rc;
    
    private ProfileRequestContext prc;

    private AddAuthenticationStatementToAssertion action;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        rc = new RequestContextBuilder().setOutboundMessage(
                SAML1ActionTestingSupport.buildResponse()).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);

        action = new AddAuthenticationStatementToAssertion();
        action.setHttpServletRequest(new MockHttpServletRequest());
        action.initialize();
    }
    
    /** Test that the action errors out properly if there is no authentication context. */
    @Test public void testNoAuthnContext() throws Exception {
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    /** Test that the action errors out properly if there is no relying party context. */
    @Test public void testNoRelyingPartyContext() throws Exception {
        prc.getSubcontext(AuthenticationContext.class, true);
        prc.removeSubcontext(RelyingPartyContext.class);

        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    /** Test that the action errors out properly if there is no context. */
    @Test public void testNoContext() throws Exception {
        prc.setOutboundMessageContext(null);
        prc.getSubcontext(AuthenticationContext.class, true).setAuthenticationResult(
                new AuthenticationResult("Test", new AuthenticationMethodPrincipal("Test")));

        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_MSG_CTX);
    }

    /** Test that the action proceeds properly returning no assertions if there is no authentication result. */
    @Test public void testNoAuthenticationStatement() throws Exception {
        prc.getSubcontext(AuthenticationContext.class, true);

        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    /** Test that the authentication statement is properly added. */
    @Test public void testAddAuthenticationStatement() throws Exception {
        final long now = System.currentTimeMillis();
        // this is here to allow the event's creation time to deviate from the 'start' time
        Thread.sleep(50);
        
        prc.getSubcontext(AuthenticationContext.class, true).setAuthenticationResult(
                new AuthenticationResult("Test", new AuthenticationMethodPrincipal("Test")));

        ((MockHttpServletRequest) action.getHttpServletRequest()).setRemoteAddr("127.0.0.1");
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertNotNull(prc.getOutboundMessageContext().getMessage());
        Assert.assertTrue(prc.getOutboundMessageContext().getMessage() instanceof Response);

        final Response response = (Response) prc.getOutboundMessageContext().getMessage();
        Assert.assertEquals(response.getAssertions().size(), 1);
        Assert.assertNotNull(response.getAssertions().get(0));

        final Assertion assertion = response.getAssertions().get(0);
        Assert.assertEquals(assertion.getAuthenticationStatements().size(), 1);
        Assert.assertNotNull(assertion.getAuthenticationStatements().get(0));

        final AuthenticationStatement authenticationStatement = assertion.getAuthenticationStatements().get(0);
        Assert.assertTrue(authenticationStatement.getAuthenticationInstant().getMillis() > now);
        Assert.assertEquals(authenticationStatement.getAuthenticationMethod(), "Test");
        
        Assert.assertNotNull(authenticationStatement.getSubjectLocality());
        Assert.assertEquals(authenticationStatement.getSubjectLocality().getIPAddress(), "127.0.0.1");
    }
    
    /** Test that the authentication statement is properly added with the right method. */
    @Test public void testAddAuthenticationStatementAndMethod() throws Exception {
        final Subject subject = new Subject();
        subject.getPrincipals().add(new AuthenticationMethodPrincipal("Foo"));
        subject.getPrincipals().add(new AuthenticationMethodPrincipal("Bar"));
        prc.getSubcontext(AuthenticationContext.class, true).setAuthenticationResult(
                new AuthenticationResult("Test", subject));
        final RequestedPrincipalContext requested = new RequestedPrincipalContext();
        requested.setMatchingPrincipal(new AuthenticationMethodPrincipal("Bar"));
        prc.getSubcontext(AuthenticationContext.class, false).addSubcontext(requested);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertNotNull(prc.getOutboundMessageContext().getMessage());
        Assert.assertTrue(prc.getOutboundMessageContext().getMessage() instanceof Response);

        final Response response = (Response) prc.getOutboundMessageContext().getMessage();
        Assert.assertEquals(response.getAssertions().size(), 1);
        Assert.assertNotNull(response.getAssertions().get(0));

        Assertion assertion = response.getAssertions().get(0);
        Assert.assertEquals(assertion.getAuthenticationStatements().size(), 1);
        Assert.assertNotNull(assertion.getAuthenticationStatements().get(0));

        final AuthenticationStatement authenticationStatement = assertion.getAuthenticationStatements().get(0);
        Assert.assertEquals(authenticationStatement.getAuthenticationMethod(), "Bar");
    }
    
}