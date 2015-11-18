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

package net.shibboleth.idp.authn.spnego.impl;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.codec.Base64Support;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.mockito.Matchers;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SPNEGOAuthnControllerTest {

    private static final String TEST_CONVERSATION_KEY = "e1s1";

    private static final String NTLMSSP_HEADER_DATA = Base64Support.encode(new byte[] {(byte) 0x4E, (byte) 0x54,
            (byte) 0x4C, (byte) 0x4D, (byte) 0x53, (byte) 0x53, (byte) 0x50}, false);

    private static final String NEGOTIATE_HEADER_DATA = Base64Support.encode("testdata".getBytes(), false);

    private SPNEGOAuthnController controller = new SPNEGOAuthnController();

    private GSSContextAcceptor mockGSSContextAcceptor;

    private SPNEGOAuthnController mockedGSSController = new SPNEGOAuthnController() {
        @Override
        @Nonnull
        protected GSSContextAcceptor createGSSContextAcceptor(@Nonnull final SPNEGOContext spnegoCtx)
                throws GSSException {
            return mockGSSContextAcceptor;

        }
    };

    @BeforeMethod
    public void setup() {
        mockGSSContextAcceptor = mock(GSSContextAcceptor.class);
    }

    @Test(expectedExceptions = {ExternalAuthenticationException.class})
    public void withoutConversationKeyParameter_startSPNEGO_shouldThrowExternalAuthenticationException()
            throws ExternalAuthenticationException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        controller.startSPNEGO(TEST_CONVERSATION_KEY, req, null);
    }

    @Test(expectedExceptions = ExternalAuthenticationException.class)
    public void givenMismatchedKeys_startSPNEGO_shouldThrowExternalAuthenticationException()
            throws ExternalAuthenticationException, IOException {
        controller.startSPNEGO("e1s2",
                buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, new StubExternalAuthentication()), null);
    }

    @Test(expectedExceptions = ExternalAuthenticationException.class)
    public void givenNullKey_startSPNEGO_shouldReturnAuthenticationException() throws ExternalAuthenticationException,
            IOException {
        MockHttpServletRequest req = buildConversationHttpServletRequest(null, new StubExternalAuthentication());
        controller.startSPNEGO(TEST_CONVERSATION_KEY, req, null);
    }

    @Test
    public void withoutSPNEGOContext_startSPNEGO_shouldReturnAuthenticationError()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        prc.addSubcontext(new AuthenticationContext());
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        ModelAndView mv = controller.startSPNEGO(TEST_CONVERSATION_KEY, req, new MockHttpServletResponse());
        assertAuthenticationError(req, mv, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void withoutKerberosSettings_startSPNEGO_shouldReturnAuthenticationError()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        AuthenticationContext ac = new AuthenticationContext();
        SPNEGOContext sc = new SPNEGOContext();
        ac.addSubcontext(sc);
        prc.addSubcontext(ac);
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        ModelAndView mv = controller.startSPNEGO(TEST_CONVERSATION_KEY, req, new MockHttpServletResponse());
        assertAuthenticationError(req, mv, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void givenKerberosSettings_startSPNEGO_shouldReturnModelAndView() throws ExternalAuthenticationException,
            IOException {
        MockHttpServletRequest req = buildKerberosContextHttpServletRequest();
        ModelAndView modelAndView = controller.startSPNEGO(TEST_CONVERSATION_KEY, req, new MockHttpServletResponse());
        assertModelAndView(modelAndView, req);
    }

    @Test
    public void givenKerberosSettings_startSPNEGO_shouldPreserveQueryString() throws ExternalAuthenticationException,
            IOException {
        MockHttpServletRequest req = buildKerberosContextHttpServletRequest();
        req.setQueryString("dummy query string");
        ModelAndView modelAndView = controller.startSPNEGO(TEST_CONVERSATION_KEY, req, new MockHttpServletResponse());
        assertModelAndView(modelAndView, req);
    }

    @Test
    public void givenKerberosSettings_startSPNEGO_shouldReplyUnauthorizedNegotiate()
            throws ExternalAuthenticationException, IOException {
        MockHttpServletRequest req = buildKerberosContextHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        controller.startSPNEGO(TEST_CONVERSATION_KEY, req, res);
        assertResponseUnauthorizedNegotiate(res);
    }

    @Test
    public void withoutNegotiateToken_continueSPNEGO_shouldReturnModelAndView() throws ExternalAuthenticationException,
            IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        ModelAndView mv =
                controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate", req, new MockHttpServletResponse());
        assertModelAndView(mv, req);
    }

    @Test
    public void withoutNegotiateToken_continueSPNEGO_shouldPreserveQueryString()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        req.setQueryString("dummy query string");
        ModelAndView mv =
                controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate", req, new MockHttpServletResponse());
        assertModelAndView(mv, req);
    }

    @Test
    public void withoutNegotiateToken_continueSPNEGO_shouldReplyUnauthorizedNegotiate()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        MockHttpServletResponse res = new MockHttpServletResponse();
        controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate", req, res);
        assertResponseUnauthorizedNegotiate(res);
    }

    @Test
    public void withoutSPNEGOContext_continueSPNEGO_shouldReturnAuthenticationError()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        prc.addSubcontext(new AuthenticationContext());
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        ModelAndView mv =
                controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, null);
        assertAuthenticationError(req, mv, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void withoutKerberosSettings_continueSPNEGO_shouldReturnAuthenticationError()
            throws ExternalAuthenticationException, IOException {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ProfileRequestContext prc = new ProfileRequestContext();
        ea.setProfileRequestContext(prc);
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        ModelAndView mv =
                controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, null);
        assertAuthenticationError(req, mv, AuthnEventIds.INVALID_AUTHN_CTX);
    }

    @Test
    public void givenFailedGSSContextAcceptorInstantiation_continueSPNEGO_shouldReturnAuthenticationException()
            throws ExternalAuthenticationException, IOException {
        final GSSException expected = new GSSException(0);
        SPNEGOAuthnController failedGSSController = new SPNEGOAuthnController() {
            @Override
            @Nonnull
            protected GSSContextAcceptor createGSSContextAcceptor(@Nonnull final SPNEGOContext spnegoCtx)
                    throws GSSException {
                throw expected;
            }
        };
        MockHttpServletRequest req = buildKerberosContextHttpServletRequest();
        ModelAndView mv =
                failedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req,
                        null);

        Assert.assertSame((GSSException) ((ExternalAuthenticationException) req
                .getAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY)).getCause(), expected);
        assertAuthenticationExceptionCause(req, mv, GSSException.class);
    }

    @Test
    public void givenSuccessfulGSSContextAcceptorInstantiation_continueSPNEGO_shouldHaveSetAcceptorInSPNEGOContext()
            throws ExternalAuthenticationException, IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(false);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        MockHttpServletResponse res = new MockHttpServletResponse();
        mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, res);

        StubExternalAuthentication ea =
                (StubExternalAuthentication) req.getSession(true).getAttribute(
                        ExternalAuthentication.CONVERSATION_KEY + TEST_CONVERSATION_KEY);
        ProfileRequestContext prc = ea.getProfileRequestContext(req);
        AuthenticationContext authnContext = prc.getSubcontext(AuthenticationContext.class);
        SPNEGOContext spnegoContext = authnContext != null ? authnContext.getSubcontext(SPNEGOContext.class) : null;
        Assert.assertNotNull(spnegoContext);
        Assert.assertEquals(spnegoContext.getContextAcceptor(), mockGSSContextAcceptor);
    }

    @Test
    public void givenHeaderAuthorizationNegotiate_withNTLMdata_continueSPNEGO_shouldReturnAuthenticationError()
            throws ExternalAuthenticationException, IOException {
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NTLMSSP_HEADER_DATA);
        ModelAndView mv =
                controller.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NTLMSSP_HEADER_DATA, req, null);
        assertAuthenticationError(req, mv, SPNEGOAuthnController.NTLM_UNSUPPORTED);
    }

    @Test
    public void whenAcceptSecContextThrowsException_continueSPNEGO_shouldReturnAuthenticationException()
            throws ExternalAuthenticationException, IOException, LoginException, GSSException,
            PrivilegedActionException, Exception {
        RuntimeException e = new RuntimeException();
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenThrow(e);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        ModelAndView mv =
                mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req,
                        null);

        Assert.assertSame((RuntimeException) ((ExternalAuthenticationException) req
                .getAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY)).getCause(), e);
        assertAuthenticationExceptionCause(req, mv, RuntimeException.class);
    }

    @Test
    public void withoutGSSContext_continueSPNEGO_shouldReturnModelAndView() throws LoginException, GSSException,
            PrivilegedActionException, ExternalAuthenticationException, IOException, Exception {
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(null);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        ModelAndView modelAndView =
                mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "", req, new MockHttpServletResponse());
        assertModelAndView(modelAndView, req);
    }

    @Test
    public void withoutGSSContext_continueSPNEGO_shouldReplyUnauthorizedNegotiate() throws LoginException,
            GSSException, PrivilegedActionException, ExternalAuthenticationException, IOException, Exception {
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(null);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        MockHttpServletResponse res = new MockHttpServletResponse();
        mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, res);
        assertResponseUnauthorizedNegotiate(res, Base64Support.encode("tokenBytes".getBytes(), false));
    }

    @Test
    public void givenGSSContextNotEstablished_continueSPNEGO_shouldReturnModelAndView() throws LoginException,
            GSSException, PrivilegedActionException, ExternalAuthenticationException, IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(false);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        ModelAndView modelAndView =
                mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "", req, new MockHttpServletResponse());
        assertModelAndView(modelAndView, req);
    }

    @Test
    public void givenGSSContextNotEstablished_continueSPNEGO_shouldReplyUnauthorizedNegotiate() throws LoginException,
            GSSException, PrivilegedActionException, ExternalAuthenticationException, IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(false);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        MockHttpServletResponse res = new MockHttpServletResponse();
        mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, res);
        assertResponseUnauthorizedNegotiate(res, Base64Support.encode("tokenBytes".getBytes(), false));
    }

    @Test
    public void givenGSSContextEstablished_andGSSException_continueSPNEGO_shouldReturnAuthenticationError()
            throws LoginException, GSSException, PrivilegedActionException, ExternalAuthenticationException,
            IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        GSSException gssException = new GSSException(0);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(true);
        when(mockGSSContext.getSrcName()).thenThrow(gssException);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        ModelAndView mv =
                mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req,
                        null);
        Assert.assertSame((GSSException) ((ExternalAuthenticationException) req
                .getAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY)).getCause(), gssException);
        assertAuthenticationExceptionCause(req, mv, GSSException.class);
    }

    @Test
    public void givenGSSContextEstablished_continueSPNEGO_shouldReturnNull() throws LoginException, GSSException,
            PrivilegedActionException, ExternalAuthenticationException, IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        GSSName mockGssName = mock(GSSName.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(true);
        when(mockGSSContext.getSrcName()).thenReturn(mockGssName);
        when(mockGssName.toString()).thenReturn("testname@realm");
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        Assert.assertNull(mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate "
                + NEGOTIATE_HEADER_DATA, req, null));
    }

    @Test
    public void givenGSSContextEstablished_continueSPNEGO_shouldSetAuthenticationSubjectAttribute()
            throws LoginException, GSSException, PrivilegedActionException, ExternalAuthenticationException,
            IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        GSSName mockGssName = mock(GSSName.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(true);
        when(mockGSSContext.getSrcName()).thenReturn(mockGssName);
        when(mockGssName.toString()).thenReturn("testname@realm");
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, null);
        Subject s = (Subject) req.getAttribute(ExternalAuthentication.SUBJECT_KEY);
        Assert.assertEquals(s.getClass(), Subject.class);
        Assert.assertTrue(s.getPrincipals(KerberosPrincipal.class).contains(new KerberosPrincipal("testname@realm")));
        Assert.assertTrue(s.getPrincipals(UsernamePrincipal.class).contains(new UsernamePrincipal("testname@realm")));
    }

    @Test
    public void givenGSSContextEstablishedButNoGSSNameIsNull_continueSPNEGO_shouldSetAuthenticationSubjectAttribute()
            throws LoginException, GSSException, PrivilegedActionException, ExternalAuthenticationException,
            IOException, Exception {
        GSSContext mockGSSContext = mock(GSSContext.class);
        when(mockGSSContextAcceptor.acceptSecContext(Matchers.<byte[]> any(), anyInt(), anyInt())).thenReturn(
                "tokenBytes".getBytes());
        when(mockGSSContextAcceptor.getContext()).thenReturn(mockGSSContext);
        when(mockGSSContext.isEstablished()).thenReturn(true);
        when(mockGSSContext.getSrcName()).thenReturn(null);
        MockHttpServletRequest req = buildSPNEGOHttpServletRequest(NEGOTIATE_HEADER_DATA);
        ModelAndView mv = mockedGSSController.continueSPNEGO(TEST_CONVERSATION_KEY, "Negotiate " + NEGOTIATE_HEADER_DATA, req, null);
        Assert.assertNull(mv);
        Assert.assertEquals(((ExternalAuthenticationException) req
                .getAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY)).getClass(),ExternalAuthenticationException.class);
    }

    private MockHttpServletRequest buildSPNEGOHttpServletRequest(String negotiateHeaderData) {
        MockHttpServletRequest req = buildKerberosContextHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Negotiate " + negotiateHeaderData);
        return req;
    }

    private MockHttpServletRequest buildKerberosContextHttpServletRequest() {
        StubExternalAuthentication ea = new StubExternalAuthentication();
        ea.setProfileRequestContext(buildKerberosProfileRequestContext());
        MockHttpServletRequest req = buildConversationHttpServletRequest(TEST_CONVERSATION_KEY, ea);
        return req;
    }

    private ProfileRequestContext buildKerberosProfileRequestContext() {
        ProfileRequestContext prc = new ProfileRequestContext();
        AuthenticationContext ac = new AuthenticationContext();
        SPNEGOContext sc = new SPNEGOContext();
        KerberosSettings ks = new KerberosSettings();
        List<KerberosRealmSettings> realms = new ArrayList<KerberosRealmSettings>();
        realms.add(new KerberosRealmSettings());
        ks.setRealms(realms);
        sc.setKerberosSettings(ks);
        ac.addSubcontext(sc);
        prc.addSubcontext(ac);
        return prc;
    }

    private MockHttpServletRequest buildConversationHttpServletRequest(String conversationKey,
            ExternalAuthentication externalAuthentication) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addParameter(ExternalAuthentication.CONVERSATION_KEY, conversationKey);
        req.getSession(true).setAttribute(ExternalAuthentication.CONVERSATION_KEY + TEST_CONVERSATION_KEY,
                externalAuthentication);
        return req;
    }

    private void assertAuthenticationError(MockHttpServletRequest request, ModelAndView mv, String expectedError) {
        Assert.assertNull(mv);
        Assert.assertEquals(request.getAttribute(ExternalAuthentication.AUTHENTICATION_ERROR_KEY).toString(),
                expectedError);
    }

    private void assertAuthenticationExceptionCause(MockHttpServletRequest request, ModelAndView mv,
            Class exceptedExceptionClass) {
        Assert.assertNull(mv);
        Assert.assertEquals(((ExternalAuthenticationException) request
                .getAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY)).getCause().getClass(),
                exceptedExceptionClass);
    }

    private void assertModelAndView(ModelAndView modelAndView, MockHttpServletRequest request) {
        Assert.assertEquals(modelAndView.getViewName(), "spnego-unavailable");
        Map<String, Object> model = modelAndView.getModel();
        Assert.assertTrue(model.containsKey("encoder"), "Model doesn't contain \"encoder\"");
        Assert.assertEquals(model.get("encoder").getClass(), Class.class);
        Assert.assertTrue(model.containsKey("errorUrl"), "Model doesn't contain \"errorUrl\"");
        Assert.assertEquals(model.get("errorUrl").getClass(), String.class);
        if (request.getQueryString() != null) {
            Assert.assertTrue(((String) model.get("errorUrl")).endsWith("/error?" + request.getQueryString()));
        } else {
            Assert.assertTrue(((String) model.get("errorUrl")).endsWith("/error"));
        }
        Assert.assertTrue(model.containsKey("request"), "Model doesn't contain \"request\"");
        Assert.assertTrue(model.get("request") instanceof HttpServletRequest);
    }

    private void assertResponseUnauthorizedNegotiate(MockHttpServletResponse response) {
        Assert.assertEquals(new Integer(response.getStatus()), new Integer(401));
        Assert.assertEquals(response.getHeader("WWW-Authenticate"), "Negotiate");
    }

    private void assertResponseUnauthorizedNegotiate(MockHttpServletResponse response, String base64token) {
        Assert.assertEquals(new Integer(response.getStatus()), new Integer(401));
        Assert.assertEquals(response.getHeader("WWW-Authenticate"), "Negotiate " + base64token);
    }

    private class StubExternalAuthentication extends ExternalAuthentication {

        private ProfileRequestContext profileRequestContext;

        public void setProfileRequestContext(ProfileRequestContext profileRequestContext) {
            this.profileRequestContext = profileRequestContext;
        }

        @Override
        protected void doStart(HttpServletRequest request) throws ExternalAuthenticationException {
        }

        @Override
        protected void doFinish(HttpServletRequest request, HttpServletResponse response)
                throws ExternalAuthenticationException, IOException {
        }

        @Override
        protected ProfileRequestContext getProfileRequestContext(HttpServletRequest request)
                throws ExternalAuthenticationException {
            return profileRequestContext;
        }

    }
}
