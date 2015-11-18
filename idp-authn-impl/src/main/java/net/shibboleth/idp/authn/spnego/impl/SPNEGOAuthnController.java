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

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.codec.HTMLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * MVC controller for managing the SPNEGO exchanges implemented as an {@link ExternalAuthentication} mechanism.
 * 
 * The handler methods either return contents back to the browser by returning an appropriate ResponseEntity<String>
 * object, or they return back to the flow by calling
 * {@link ExternalAuthentication#finishExternalAuthentication(String, HttpServletRequest, HttpServletResponse)} and
 * returning null. On unrecoverable errors, an exception is thrown.
 */
public class SPNEGOAuthnController {
    
    /** Event ID indicating that SPNEGO is not supported by the client or is not available for other reasons. */
    @Nonnull @NotEmpty public static final String SPNEGO_NOT_AVAILABLE = "SPNEGONotAvailable";

    /** Event ID indicating that NTLM was attempted by the client. */
    @Nonnull @NotEmpty public static final String NTLM_UNSUPPORTED = "NTLMUnsupported";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SPNEGOAuthnController.class);

    /**
     * Handle initial request that starts SPNEGO.
     * 
     * @param conversationKey the SWF conversation key
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * 
     * @return the response view
     * @throws IOException 
     * @throws ExternalAuthenticationException 
     */
    @RequestMapping(value = "/{conversationKey}", method = RequestMethod.GET)
    @Nullable public ModelAndView startSPNEGO(@PathVariable @Nonnull @NotEmpty final String conversationKey,
            @Nonnull final HttpServletRequest httpRequest, @Nonnull final HttpServletResponse httpResponse)
                    throws ExternalAuthenticationException, IOException {
        
        final String key = ExternalAuthentication.startExternalAuthentication(httpRequest);
        if (!key.equals(conversationKey)) {
            throw new ExternalAuthenticationException("Conversation key on query string doesn't match URL path");
        }
        final ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(key, httpRequest);

        final SPNEGOContext spnegoCtx = getSPNEGOContext(prc);
        if (spnegoCtx == null || spnegoCtx.getKerberosSettings() == null) {
            log.error("Kerberos settings not found in profile request context");
            finishWithError(conversationKey, httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
            return null;
        }
        
        // Start the SPNEGO exchange.
        log.trace("SPNEGO negotiation started, answering request with 401 (WWW-Authenticate: Negotiate)");
        return replyUnauthorizedNegotiate(prc, httpRequest, httpResponse);
    }

// Checkstyle: CyclomaticComplexity|MethodLength OFF
    /**
     * Process an input GSS token from the client and attempt to complete the context establishment process.
     * 
     * @param conversationKey the conversation key
     * @param authorizationHeader the token from the client
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * 
     * @return the response view
     * @throws ExternalAuthenticationException 
     * @throws IOException 
     */
    @RequestMapping(value = "/{conversationKey}", method = RequestMethod.GET, headers = "Authorization")
    @Nullable public ModelAndView continueSPNEGO(@PathVariable @Nonnull @NotEmpty final String conversationKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Nonnull @NotEmpty final String authorizationHeader,
            @Nonnull final HttpServletRequest httpRequest, @Nonnull final HttpServletResponse httpResponse)
                    throws ExternalAuthenticationException, IOException {

        final ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(conversationKey, httpRequest);

        if (!authorizationHeader.startsWith("Negotiate ")) {
            return replyUnauthorizedNegotiate(prc, httpRequest, httpResponse);
        }

        final SPNEGOContext spnegoCtx = getSPNEGOContext(prc);
        if (spnegoCtx == null || spnegoCtx.getKerberosSettings() == null) {
            log.error("Kerberos settings not found in profile request context");
            finishWithError(conversationKey, httpRequest, httpResponse, AuthnEventIds.INVALID_AUTHN_CTX);
            return null;
        }

        GSSContextAcceptor acceptor = spnegoCtx.getContextAcceptor();
        if (acceptor == null) {
            try {
                acceptor = createGSSContextAcceptor(spnegoCtx);
                spnegoCtx.setContextAcceptor(acceptor);
            } catch (final GSSException e) {
                log.error("Unable to create GSSContextAcceptor", e);
                finishWithException(conversationKey, httpRequest, httpResponse,
                        new ExternalAuthenticationException(SPNEGO_NOT_AVAILABLE, e));
                return null;
            }
        }

        final byte[] gssapiData = Base64.decodeBase64(authorizationHeader.substring(10).getBytes());
        log.trace("SPNEGO negotiation, Authorization header received, gssapi-data: {}", gssapiData);

        // NTLM Authentication is not supported.
        if (isNTLMMechanism(gssapiData)) {
            log.warn("NTLM is unsupported, failing context negotiation");
            acceptor.logout();
            finishWithError(conversationKey, httpRequest, httpResponse, NTLM_UNSUPPORTED);
            return null;
        }

        byte[] tokenBytes;
        try {
            tokenBytes = acceptor.acceptSecContext(gssapiData, 0, gssapiData.length);
            log.trace("GSS token accepted");
        } catch (final Exception e) {
            log.debug("Exception processing GSS token", e);
            acceptor.logout();
            finishWithException(conversationKey, httpRequest, httpResponse,
                    new ExternalAuthenticationException(SPNEGO_NOT_AVAILABLE, e));
            return null;
        }

        // If the context is established, we can attempt to retrieve the name of the "context initiator."
        // In the case of the Kerberos mechanism, the context initiator is the Kerberos principal of the client.
        if (acceptor.getContext() != null && acceptor.getContext().isEstablished()) {
            log.debug("GSS security context is complete");
            try {
                final GSSName clientGSSName = acceptor.getContext().getSrcName();
                if (clientGSSName == null) {
                    // This case should never happen, but we observed it. Handle it as authentication failure.
                    log.error("Error extracting principal name from security context");
                    acceptor.logout();
                    finishWithException(conversationKey, httpRequest, httpResponse,
                            new ExternalAuthenticationException(SPNEGO_NOT_AVAILABLE));
                    return null;
                }
                final KerberosPrincipal kerberosPrincipal = new KerberosPrincipal(clientGSSName.toString());

                log.info("SPNEGO/Kerberos authentication succeeded for principal: {}", clientGSSName.toString());

                acceptor.logout();
                finishWithSuccess(conversationKey, httpRequest, httpResponse, kerberosPrincipal);
            } catch (final GSSException e) {
                log.error("Error extracting principal name from security context", e);
                acceptor.logout();
                finishWithException(conversationKey, httpRequest, httpResponse,
                        new ExternalAuthenticationException(SPNEGO_NOT_AVAILABLE, e));
            }
        } else {
            // The context is not complete yet.
            // return "WWW-Authenticate: Negotiate <data>" to the browser
            log.trace("SPNEGO negotiation in process, output token: {}", tokenBytes);
            return replyUnauthorizedNegotiate(prc, httpRequest, httpResponse, Base64.encodeBase64String(tokenBytes));
        }
        
        return null;
    }
// Checkstyle: CyclomaticComplexity|MethodLength ON
    
    /**
     * Respond to a user signaling that an error occurred.
     * 
     * @param conversationKey the conversation key
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * 
     * @throws IOException 
     * @throws ExternalAuthenticationException 
     */
    @RequestMapping(value = "/{conversationKey}/error", method = RequestMethod.GET)
    public void handleError(@PathVariable final String conversationKey, @Nonnull final HttpServletRequest httpRequest,
            @Nonnull final HttpServletResponse httpResponse) throws ExternalAuthenticationException, IOException {

        log.warn("SPNEGO authentication problem signaled by client");
        finishWithError(conversationKey, httpRequest, httpResponse, SPNEGO_NOT_AVAILABLE);
    }

    /**
     * Finish the authentication process successfully.
     * 
     * <p>Sets the attribute {@link ExternalAuthentication#SUBJECT_KEY}.</p>
     * 
     * @param key the conversation key
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * @param kerberosPrincipal the Kerberos principal to return
     * 
     * @throws IOException 
     * @throws ExternalAuthenticationException 
     */
    private void finishWithSuccess(@Nonnull @NotEmpty final String key, @Nonnull final HttpServletRequest httpRequest,
            @Nonnull final HttpServletResponse httpResponse, @Nonnull final KerberosPrincipal kerberosPrincipal)
                    throws ExternalAuthenticationException, IOException {

        // Store the user as a username and as a real KerberosPrincipal object.
        final Subject subject = new Subject();
        subject.getPrincipals().add(new UsernamePrincipal(kerberosPrincipal.getName()));
        subject.getPrincipals().add(kerberosPrincipal);

        // Finish the external authentication task and return to the flow.
        httpRequest.setAttribute(ExternalAuthentication.SUBJECT_KEY, subject);
        ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
    }

    /**
     * Finish the authentication process with an error.
     * 
     * <p>Sets the attribute {@link ExternalAuthentication#AUTHENTICATION_ERROR_KEY}.</p>
     * 
     * @param key the conversation key
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * @param error the error string/event to return
     * 
     * @throws IOException 
     * @throws ExternalAuthenticationException 
     */
    private void finishWithError(@Nonnull @NotEmpty final String key, @Nonnull final HttpServletRequest httpRequest,
            @Nonnull final HttpServletResponse httpResponse, @Nonnull @NotEmpty final String error)
                    throws ExternalAuthenticationException, IOException {

        // Finish the external authentication task and return to the flow.
        httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_ERROR_KEY, error);
        ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
    }

    /**
     * Finish the authentication process with an exception.
     * 
     * <p>Sets the attribute {@link ExternalAuthentication#AUTHENTICATION_EXCEPTION_KEY}.</p>
     * 
     * @param key the conversation key
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * @param ex the exception that has been thrown
     * 
     * @throws IOException 
     * @throws ExternalAuthenticationException 
     */
    private void finishWithException(@Nonnull @NotEmpty final String key, @Nonnull final HttpServletRequest httpRequest,
            @Nonnull final HttpServletResponse httpResponse, @Nonnull final Exception ex)
                    throws ExternalAuthenticationException, IOException {

        // Finish the external authentication task and return to the flow.
        httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY, ex);
        ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
    }

    /**
     * Navigate to the {@link SPNEGOContext} in the context tree.
     * 
     * @param prc profile request context
     * 
     * @return the child context, or null
     */
    @Nullable private SPNEGOContext getSPNEGOContext(@Nonnull final ProfileRequestContext prc) {
        final AuthenticationContext authnContext = prc.getSubcontext(AuthenticationContext.class);
        return authnContext != null ? authnContext.getSubcontext(SPNEGOContext.class) : null;
    }

    /**
     * Create a new {@link GSSContextAcceptor} object.
     * (Created in a separate method to support unit testing.)
     * 
     * @param spnegoCtx the {@link SPNEGOContext} conteining the {@link KerberosSettings}
     * @return a new {@link GSSContextAcceptor}
     * @throws GSSException if an error occurs while creating the {@link GSSContextAcceptor}.
     */
    @Nonnull
    protected GSSContextAcceptor createGSSContextAcceptor(@Nonnull final SPNEGOContext spnegoCtx) throws GSSException {
        return new GSSContextAcceptor(spnegoCtx.getKerberosSettings());
    }

    /**
     * Send back an empty Negotiate challenge.
     * 
     * @param profileRequestContext profile request context
     * @param httpRequest servlet request
     * @param httpResponse servlet response
     * 
     * @return a {@link ModelAndView} wrapping the response
     */
    @Nonnull private ModelAndView replyUnauthorizedNegotiate(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final HttpServletRequest httpRequest, @Nonnull final HttpServletResponse httpResponse) {
        return replyUnauthorizedNegotiate(profileRequestContext, httpRequest, httpResponse, "");
    }

    /**
     * Send back a Negotiate challenge token.
     * 
     * @param profileRequestContext profile request context
     * @param httpRequest servlet request
     * @param httpResponse servlet response
     * @param base64Token challenge token to send back
     * 
     * @return a {@link ModelAndView} wrapping the response
     */
    @Nonnull private ModelAndView replyUnauthorizedNegotiate(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final HttpServletRequest httpRequest, @Nonnull final HttpServletResponse httpResponse,
            @Nonnull final String base64Token) {
        
        final StringBuilder authenticateHeader = new StringBuilder("Negotiate");
        if (!base64Token.isEmpty()) {
            authenticateHeader.append(" " + base64Token);
        }
        httpResponse.addHeader(HttpHeaders.WWW_AUTHENTICATE, authenticateHeader.toString());
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return createModelAndView(profileRequestContext, httpRequest, httpResponse);
    }

    /**
     * Create a {@link ModelAndView} object to return.
     * 
     * @param profileRequestContext profile request context
     * @param httpRequest the HTTP request
     * @param httpResponse the HTTP response
     * 
     * @return the ModelAndView object
     */
    @Nonnull private ModelAndView createModelAndView(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final HttpServletRequest httpRequest, @Nonnull final HttpServletResponse httpResponse) {
        final ModelAndView modelAndView = new ModelAndView("spnego-unavailable");
        modelAndView.addObject("profileRequestContext", profileRequestContext);
        modelAndView.addObject("request", httpRequest);
        modelAndView.addObject("response", httpResponse);
        modelAndView.addObject("encoder", HTMLEncoder.class);
        final StringBuffer errorUrl = httpRequest.getRequestURL();
        errorUrl.append("/error");
        final String queryString = httpRequest.getQueryString();
        if (queryString != null) {
            errorUrl.append("?").append(queryString);
        }
        modelAndView.addObject("errorUrl", errorUrl.toString());
        return modelAndView;
    }

    /**
     * Check if the GSS-API data represents an NTLM mechanism request.
     * 
     * @param token token retrieved from the Authorization header.
     * 
     * @return true iff it represents a NTLM mechanism
     */
    private boolean isNTLMMechanism(@Nonnull final byte[] token) {
        byte[] headerNTLM = {(byte) 0x4E, (byte) 0x54, (byte) 0x4C, (byte) 0x4D, (byte) 0x53, (byte) 0x53, (byte) 0x50};
        return Arrays.equals(headerNTLM, Arrays.copyOfRange(token, 0, 7));
    }
    
}