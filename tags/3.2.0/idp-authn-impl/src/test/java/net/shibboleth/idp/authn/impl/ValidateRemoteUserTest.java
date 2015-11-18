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

package net.shibboleth.idp.authn.impl;


import java.util.Arrays;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ValidateRemoteUser} unit test. */
public class ValidateRemoteUserTest extends PopulateAuthenticationContextTest {
    
    private ValidateRemoteUser action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new ValidateRemoteUser();
        action.setWhitelistedUsernames(Arrays.asList("bar", "baz"));
        action.setBlacklistedUsernames(Arrays.asList("foo"));
        action.setMatchExpression(Pattern.compile("^ba(r|z|n)$"));
        action.setHttpServletRequest((HttpServletRequest) src.getExternalContext().getNativeRequest());
        action.initialize();
    }

    @Test public void testMissingFlow() {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test public void testMissingUser() {
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testMissingUser2() throws ComponentInitializationException {
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testUnauthorized() throws ComponentInitializationException {
        ((MockHttpServletRequest) action.getHttpServletRequest()).setRemoteUser("bam");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }

    @Test public void testAuthorized() throws ComponentInitializationException {
        ((MockHttpServletRequest) action.getHttpServletRequest()).setRemoteUser("baz");
        
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(
                UsernamePrincipal.class).iterator().next().getName(), "baz");
    }

    @Test public void testBlacklist() throws ComponentInitializationException {
        ((MockHttpServletRequest) action.getHttpServletRequest()).setRemoteUser("foo");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }
    
    @Test public void testPattern() throws ComponentInitializationException {
        ((MockHttpServletRequest) action.getHttpServletRequest()).setRemoteUser("ban");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(
                UsernamePrincipal.class).iterator().next().getName(), "ban");
    }
    
    private void doExtract(ProfileRequestContext prc) throws ComponentInitializationException {
        final ExtractRemoteUser extract = new ExtractRemoteUser();
        extract.setHttpServletRequest(action.getHttpServletRequest());
        extract.initialize();
        extract.execute(src);
    }
}