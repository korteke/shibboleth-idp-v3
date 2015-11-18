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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.AuthenticationErrorContext;
import net.shibboleth.idp.authn.context.AuthenticationWarningContext;
import net.shibboleth.idp.authn.context.LDAPResponseContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;

import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.BindAuthenticationHandler;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.ext.PasswordPolicyAccountState;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.jaas.LdapPrincipal;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

/** {@link ValidateUsernamePasswordAgainstLDAP} unit test. */
public class ValidateUsernamePasswordAgainstLDAPTest extends PopulateAuthenticationContextTest {

    private static final String DATA_PATH = "src/test/resources/data/net/shibboleth/idp/authn/impl/";
    
    private ValidateUsernamePasswordAgainstLDAP action;

    private InMemoryDirectoryServer directoryServer;

    private SearchDnResolver dnResolver;

    private BindAuthenticationHandler authHandler;

    private Authenticator authenticator;

    /**
     * Creates an UnboundID in-memory directory server. Leverages LDIF found in test resources.
     * 
     * @throws LDAPException if the in-memory directory server cannot be created
     */
    @BeforeClass public void setupDirectoryServer() throws LDAPException {

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=shibboleth,dc=net");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10389));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer.importFromLDIF(true, DATA_PATH + "loginLDAPTest.ldif");
        directoryServer.startListening();
    }

    /**
     * Creates an Authenticator configured to use the in-memory directory server.
     */
    @BeforeClass public void setupAuthenticator() {

        dnResolver = new SearchDnResolver(new DefaultConnectionFactory("ldap://localhost:10389"));
        dnResolver.setBaseDn("ou=people,dc=shibboleth,dc=net");
        dnResolver.setUserFilter("(uid={user})");

        authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory("ldap://localhost:10389"));

        authenticator = new Authenticator(dnResolver, authHandler);
    }

    /**
     * Shutdown the in-memory directory server.
     */
    @AfterClass public void teardownDirectoryServer() {
        directoryServer.shutDown(true);
    }

    @BeforeMethod public void setUp() throws Exception {
        super.setUp();

        action = new ValidateUsernamePasswordAgainstLDAP();

        Map<String,Collection<String>> mappings = new HashMap<>();
        mappings.put("UnknownUsername", Collections.singleton("DN_RESOLUTION_FAILURE"));
        mappings.put("InvalidPassword", Collections.singleton("INVALID_CREDENTIALS"));
        mappings.put("ExpiringPassword", Collections.singleton("ACCOUNT_WARNING"));
        mappings.put("ExpiredPassword", Arrays.asList("PASSWORD_EXPIRED", "CHANGE_AFTER_RESET"));
        action.setClassifiedMessages(mappings);

        action.setHttpServletRequest(new MockHttpServletRequest());
    }

    @Test public void testMissingFlow() throws Exception {
        action.setAuthenticator(authenticator);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    @Test public void testMissingUser() throws Exception {
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(authenticator);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testMissingUser2() throws Exception {
        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        ac.getSubcontext(UsernamePasswordContext.class, true);
        action.setAuthenticator(authenticator);
        action.initialize();

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        Assert.assertNull(ac.getSubcontext(AuthenticationErrorContext.class, false));
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testBadConfig() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(new Authenticator(new SearchDnResolver(), authHandler));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNotNull(aec);
        ActionTestingSupport.assertEvent(event, "UnknownUsername");
        Assert.assertEquals(aec.getClassifiedErrors().size(), 1);
        Assert.assertTrue(aec.isClassifiedError("UnknownUsername"));
    }

    @Test public void testBadConfig2() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(new Authenticator(dnResolver, new BindAuthenticationHandler(new DefaultConnectionFactory("ldap://unknown:389"))));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        
        Assert.assertNull(ac.getAuthenticationResult());
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNotNull(aec);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.AUTHN_EXCEPTION);
        System.err.println("EXCEPTIONS:: " + aec.getExceptions());
        Assert.assertEquals(aec.getExceptions().size(), 1);   
        Assert.assertEquals(aec.getClassifiedErrors().size(), 0);
    }

    @Test public void testBadUsername() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(authenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.DN_RESOLUTION_FAILURE);
        
        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNotNull(aec);
        ActionTestingSupport.assertEvent(event, "UnknownUsername");
        Assert.assertEquals(aec.getClassifiedErrors().size(), 1);
        Assert.assertTrue(aec.isClassifiedError("UnknownUsername"));
    }

    @Test public void testEmptyPassword() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(authenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        Assert.assertNull(ac.getSubcontext(AuthenticationErrorContext.class, false));
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }

    @Test public void testBadPassword() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setAuthenticator(authenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNotNull(aec);
        ActionTestingSupport.assertEvent(event, "InvalidPassword");
        Assert.assertEquals(aec.getClassifiedErrors().size(), 1);
        Assert.assertTrue(aec.isClassifiedError("InvalidPassword"));
    }

    @Test public void testExpiredPassword() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        Authenticator errorAuthenticator = new Authenticator(dnResolver, authHandler);
        errorAuthenticator.setAuthenticationResponseHandlers(new AuthenticationResponseHandler() {            
            public void handle(AuthenticationResponse response) throws LdapException {
                response.setAccountState(new PasswordPolicyAccountState(PasswordPolicyControl.Error.PASSWORD_EXPIRED));
            }
        });
        action.setAuthenticator(errorAuthenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        Assert.assertNull(ac.getAuthenticationResult());
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNotNull(aec);
        ActionTestingSupport.assertEvent(event, "ExpiredPassword");
        Assert.assertEquals(aec.getClassifiedErrors().size(), 2);
        Assert.assertTrue(aec.isClassifiedError("ExpiredPassword"));
        Assert.assertTrue(aec.isClassifiedError("InvalidPassword"));
    }

    @Test public void testChangeAfterReset() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        Authenticator errorAuthenticator = new Authenticator(dnResolver, authHandler);
        errorAuthenticator.setAuthenticationResponseHandlers(new AuthenticationResponseHandler() {            
            public void handle(AuthenticationResponse response) throws LdapException {
                response.setAccountState(new PasswordPolicyAccountState(PasswordPolicyControl.Error.CHANGE_AFTER_RESET));
            }
        });
        action.setAuthenticator(errorAuthenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        AuthenticationResult result = ac.getAuthenticationResult();
        Assert.assertNotNull(result);
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNull(aec);
        AuthenticationWarningContext awc = ac.getSubcontext(AuthenticationWarningContext.class, false);
        Assert.assertNotNull(awc);
        ActionTestingSupport.assertEvent(event, "ExpiredPassword");
        Assert.assertEquals(awc.getClassifiedWarnings().size(), 1);
        Assert.assertTrue(awc.isClassifiedWarning("ExpiredPassword"));

        UsernamePrincipal up = result.getSubject().getPrincipals(UsernamePrincipal.class).iterator().next();
        Assert.assertNotNull(up);
        Assert.assertEquals(up.getName(), "PETER_THE_PRINCIPAL");
        LdapPrincipal lp = result.getSubject().getPrincipals(LdapPrincipal.class).iterator().next();
        Assert.assertNotNull(lp);
        Assert.assertEquals(lp.getName(), "PETER_THE_PRINCIPAL");
        Assert.assertNotNull(lp.getLdapEntry());
    }

    @Test public void testExpiringPassword() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        Authenticator warningAuthenticator = new Authenticator(dnResolver, authHandler);
        warningAuthenticator.setAuthenticationResponseHandlers(new AuthenticationResponseHandler() {            
            public void handle(AuthenticationResponse response) throws LdapException {
                response.setAccountState(new AccountState(new AccountState.DefaultWarning(java.util.Calendar.getInstance(), 10)));
            }
        });
        action.setAuthenticator(warningAuthenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNull(aec);

        AuthenticationResult result = ac.getAuthenticationResult();
        Assert.assertNotNull(result);
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);

        ActionTestingSupport.assertEvent(event, "ExpiringPassword");
        
        AuthenticationWarningContext awc = ac.getSubcontext(AuthenticationWarningContext.class, false);
        Assert.assertNotNull(awc);
        Assert.assertEquals(awc.getClassifiedWarnings().size(), 1);
        Assert.assertTrue(awc.isClassifiedWarning("ExpiringPassword"));

        UsernamePrincipal up = result.getSubject().getPrincipals(UsernamePrincipal.class).iterator().next();
        Assert.assertNotNull(up);
        Assert.assertEquals(up.getName(), "PETER_THE_PRINCIPAL");
        LdapPrincipal lp = result.getSubject().getPrincipals(LdapPrincipal.class).iterator().next();
        Assert.assertNotNull(lp);
        Assert.assertEquals(lp.getName(), "PETER_THE_PRINCIPAL");
        Assert.assertNotNull(lp.getLdapEntry());
    }

    @Test public void testAuthorized() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class, false);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        action.setAuthenticator(authenticator);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        AuthenticationErrorContext aec = ac.getSubcontext(AuthenticationErrorContext.class, false);
        Assert.assertNull(aec);

        AuthenticationResult result = ac.getAuthenticationResult();
        Assert.assertNotNull(result);
        LDAPResponseContext lrc = ac.getSubcontext(LDAPResponseContext.class, false);
        Assert.assertNotNull(lrc.getAuthenticationResponse());
        Assert.assertEquals(lrc.getAuthenticationResponse().getAuthenticationResultCode(), AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS);

        UsernamePrincipal up = result.getSubject().getPrincipals(UsernamePrincipal.class).iterator().next();
        Assert.assertNotNull(up);
        Assert.assertEquals(up.getName(), "PETER_THE_PRINCIPAL");
        LdapPrincipal lp = result.getSubject().getPrincipals(LdapPrincipal.class).iterator().next();
        Assert.assertNotNull(lp);
        Assert.assertEquals(lp.getName(), "PETER_THE_PRINCIPAL");
        Assert.assertNotNull(lp.getLdapEntry());
    }

    private void doExtract(ProfileRequestContext prc) throws Exception {
        ExtractUsernamePasswordFromFormRequest extract = new ExtractUsernamePasswordFromFormRequest();
        extract.setHttpServletRequest(action.getHttpServletRequest());
        extract.initialize();
        extract.execute(src);
    }
}