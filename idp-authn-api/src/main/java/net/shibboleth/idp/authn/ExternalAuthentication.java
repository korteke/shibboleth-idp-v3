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

package net.shibboleth.idp.authn;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.net.URISupport;

/** Public interface supporting external authentication outside the webflow engine. */
public class ExternalAuthentication {

    /** Parameter supplied to identify the per-conversation structure in the session. */
    @Nonnull @NotEmpty public static final String CONVERSATION_KEY = "conversation";
    
    /** Request attribute to which user's principal should be bound. */
    @Nonnull @NotEmpty public static final String PRINCIPAL_KEY = "principal";

    /** Request attribute to which user's principal name should be bound. */
    @Nonnull @NotEmpty public static final String PRINCIPAL_NAME_KEY = "principal_name";

    /** Request attribute to which user's subject should be bound. */
    @Nonnull @NotEmpty public static final String SUBJECT_KEY = "subject";

    /** Request attribute to which an authentication timestamp may be bound. */
    @Nonnull @NotEmpty public static final String AUTHENTICATION_INSTANT_KEY = "authnInstant";
    
    /** Request attribute to which an error message may be bound. */
    @Nonnull @NotEmpty public static final String AUTHENTICATION_ERROR_KEY = "authnError";

    /** Request attribute to which an exception may be bound. */
    @Nonnull @NotEmpty public static final String AUTHENTICATION_EXCEPTION_KEY = "authnException";

    /** Request attribute to which a signal not to cache the result may be bound. */
    @Nonnull @NotEmpty public static final String DONOTCACHE_KEY = "doNotCache";

    /**
     * Request attribute to which a signal to revoke consent for attribute release may be bound.
     * 
     * @since 3.2.0
     */
    @Nonnull @NotEmpty public static final String REVOKECONSENT_KEY = "revokeConsent";

    /** Request attribute that indicates whether the authentication request requires forced authentication. */
    @Nonnull @NotEmpty public static final String FORCE_AUTHN_PARAM = "forceAuthn";

    /** Request attribute that indicates whether the authentication requires passive authentication. */
    @Nonnull @NotEmpty public static final String PASSIVE_AUTHN_PARAM = "isPassive";

    /** Request attribute that provides which authentication method should be attempted. */
    @Deprecated @Nonnull @NotEmpty public static final String AUTHN_METHOD_PARAM = "authnMethod";

    /** Request attribute that provides the entity ID of the relying party that is requesting authentication. */
    @Nonnull @NotEmpty public static final String RELYING_PARTY_PARAM = "relyingParty";

    /**
     * Request attribute that indicates whether we're being called as an extension of another login flow.
     * 
     * @since 3.2.0
     */
    @Nonnull @NotEmpty public static final String EXTENDED_FLOW_PARAM = "extended";

    /**
     * Computes the appropriate location to pass control to to invoke an external authentication mechanism.
     * 
     *  <p>The input location should be suitable for use in a Spring "externalRedirect" expression, and may
     *  contain a query string. The result will include any additional parameters needed to invoke the
     *  mechanism.</p>
     * 
     * @param baseLocation the base location to build off of
     * @param conversationValue the value to include as a conversation ID
     * 
     * @return the computed location
     * 
     * @since 3.2.0
     */
    @Nonnull @NotEmpty public static String getExternalRedirect(@Nonnull @NotEmpty final String baseLocation,
            @Nonnull @NotEmpty final String conversationValue) {
        Constraint.isNotEmpty(baseLocation, "Base location cannot be null or empty");
        
        final StringBuilder url = new StringBuilder(baseLocation);
        
        // Add a parameter separator for the conversation ID.
        url.append(baseLocation.indexOf('?') == -1 ? '?' : '&');
        url.append(CONVERSATION_KEY).append('=').append(URISupport.doURLEncode(conversationValue));
        
        return url.toString();
    }
    
    /**
     * Initialize a request for external authentication by seeking out the information stored in
     * the servlet session and exposing it as request attributes.
     * 
     * @param request servlet request
     * 
     * @return a handle to subsequent use of
     *      {@link #finishExternalAuthentication(java.lang.String, HttpServletRequest, HttpServletResponse)}
     * 
     * @throws ExternalAuthenticationException if an error occurs
     */
    @Nonnull @NotEmpty public static String startExternalAuthentication(@Nonnull final HttpServletRequest request)
            throws ExternalAuthenticationException {
        final String conv = request.getParameter(CONVERSATION_KEY);
        if (conv == null || conv.isEmpty()) {
            throw new ExternalAuthenticationException("No conversation key found in request");
        }
        
        final Object obj = request.getSession().getAttribute(CONVERSATION_KEY + conv);
        if (obj == null || !(obj instanceof ExternalAuthentication)) {
            throw new ExternalAuthenticationException("No conversation state found in session for key (" + conv + ")");
        }
        
        ((ExternalAuthentication) obj).doStart(request);
        return conv;
    }
    
    /**
     * Complete a request for external authentication by seeking out the information stored in
     * request attributes and transferring to the session's conversation state, and then transfer
     * control back to the authentication web flow.
     * 
     * @param key   the value returned by {@link #startExternalAuthentication(HttpServletRequest)}
     * @param request servlet request
     * @param response servlet response
     * 
     * @throws ExternalAuthenticationException if an error occurs
     * @throws IOException if the redirect cannot be issued
     */
    public static void finishExternalAuthentication(@Nonnull @NotEmpty final String key,
            @Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
            throws ExternalAuthenticationException, IOException {
        
        final Object obj = request.getSession().getAttribute(CONVERSATION_KEY + key);
        if (obj == null || !(obj instanceof ExternalAuthentication)) {
            throw new ExternalAuthenticationException("No conversation state found in session for key (" + key + ")");
        }
        
        request.getSession().removeAttribute(CONVERSATION_KEY + key);
        
        ((ExternalAuthentication) obj).doFinish(request, response);
    }

    /**
     * Get the {@link ProfileRequestContext} associated with a request.
     * 
     * @param key   the value returned by {@link #startExternalAuthentication(HttpServletRequest)}
     * @param request servlet request
     * 
     * @return the profile request context
     * @throws ExternalAuthenticationException if an error occurs
     */
    @Nonnull public static ProfileRequestContext getProfileRequestContext(@Nonnull @NotEmpty final String key,
            @Nonnull final HttpServletRequest request) throws ExternalAuthenticationException {
        
        final Object obj = request.getSession().getAttribute(CONVERSATION_KEY + key);
        if (obj == null || !(obj instanceof ExternalAuthentication)) {
            throw new ExternalAuthenticationException("No conversation state found in session");
        }
        
        return ((ExternalAuthentication) obj).getProfileRequestContext(request);
    }
    
    /**
     * Initialize a request for external authentication by seeking out the information stored in
     * the servlet session and exposing it as request attributes.
     * 
     * @param request servlet request
     * 
     * @throws ExternalAuthenticationException if an error occurs
     */
    protected void doStart(@Nonnull final HttpServletRequest request) throws ExternalAuthenticationException {
        throw new ExternalAuthenticationException("Not implemented");
    }

    /**
     * Complete a request for external authentication by seeking out the information stored in
     * request attributes and transferring to the session's conversation state, and then transfer
     * control back to the authentication web flow.
     * 
     * @param request servlet request
     * @param response servlet response
     * 
     * @throws ExternalAuthenticationException if an error occurs
     * @throws IOException if the redirect cannot be issued
     */
    protected void doFinish(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
            throws ExternalAuthenticationException, IOException {
        throw new ExternalAuthenticationException("Not implemented");
    }
    
    /**
     * Get the {@link ProfileRequestContext} associated with a request.
     * 
     * @param request servlet request
     * 
     * @return the profile request context
     * @throws ExternalAuthenticationException if an error occurs
     */
    @Nonnull protected ProfileRequestContext getProfileRequestContext(@Nonnull final HttpServletRequest request)
            throws ExternalAuthenticationException {
        throw new ExternalAuthenticationException("Not implemented");
    }
    
}