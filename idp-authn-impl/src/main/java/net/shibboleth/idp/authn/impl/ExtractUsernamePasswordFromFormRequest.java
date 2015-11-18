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

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that extracts a username and password from an HTTP form body or query string,
 * creates a {@link UsernamePasswordContext}, and attaches it to the {@link AuthenticationContext}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class, false) != null</pre>
 * @post If getHttpServletRequest() != null, a pair of form or query parameters is
 * extracted to populate a {@link UsernamePasswordContext}.
 */
public class ExtractUsernamePasswordFromFormRequest extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExtractUsernamePasswordFromFormRequest.class);

    /** Parameter name for username. */
    @Nonnull @NotEmpty  private String usernameFieldName;

    /** Parameter name for password. */
    @Nonnull @NotEmpty private String passwordFieldName;
    
    /** Parameter name for SSO bypass. */
    @Nonnull @NotEmpty private String ssoBypassFieldName;

    /** Constructor. */
    ExtractUsernamePasswordFromFormRequest() {
        usernameFieldName = "username";
        passwordFieldName = "password";
        ssoBypassFieldName = "donotcache";
    }

    /**
     * Set the username parameter name.
     * 
     * @param fieldName the username parameter name
     */
    public void setUsernameFieldName(@Nonnull @NotEmpty final String fieldName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        usernameFieldName = Constraint.isNotNull(
                StringSupport.trimOrNull(fieldName), "Username field name cannot be null or empty.");
    }

    /**
     * Set the password parameter name.
     * 
     * @param fieldName the password parameter name
     */
    public void setPasswordFieldName(@Nonnull @NotEmpty final String fieldName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        passwordFieldName = Constraint.isNotNull(
                StringSupport.trimOrNull(fieldName), "Password field name cannot be null or empty.");
    }

    /**
     * Set the SSO bypass parameter name.
     * 
     * @param fieldName the SSO bypass parameter name
     */
    public void setSSOBypassFieldName(@Nonnull @NotEmpty final String fieldName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        ssoBypassFieldName = Constraint.isNotNull(
                StringSupport.trimOrNull(fieldName), "SSO Bypass field name cannot be null or empty.");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        final UsernamePasswordContext upCtx = authenticationContext.getSubcontext(UsernamePasswordContext.class, true);
        upCtx.setUsername(null);
        upCtx.setPassword(null);
        
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        
        final String username = request.getParameter(usernameFieldName);
        if (username == null || username.isEmpty()) {
            log.debug("{} No username in request", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        
        upCtx.setUsername(applyTransforms(username));

        final String password = request.getParameter(passwordFieldName);
        if (password == null || password.isEmpty()) {
            log.debug("{} No password in request", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }

        upCtx.setPassword(password);
        
        final String donotcache = request.getParameter(ssoBypassFieldName);
        if (donotcache != null && "1".equals(donotcache)) {
            log.debug("{} Recording do-not-cache instruction in authentication context", getLogPrefix());
            authenticationContext.setResultCacheable(false);
        }
    }
}