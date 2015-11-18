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

package net.shibboleth.idp.authn.context;

import java.security.Principal;

import javax.annotation.Nullable;
import javax.security.auth.Subject;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.BaseContext;

/**
 * A context representing the state of an externalized authentication attempt,
 * a case where authentication happens outside of a web flow.
 */
public final class ExternalAuthenticationContext extends BaseContext {
    
    /** Value of flowExecutionUrl on branching from flow. */
    @Nullable private String flowExecutionUrl;

    /** A {@link Principal} that was authenticated. */
    @Nullable private Principal principal;

    /** Name of a principal that was authenticated. */
    @Nullable private String principalName;

    /** Name of a {@link Subject} that was authenticated. */
    @Nullable private Subject subject;

    /** Time of authentication. */
    @Nullable private DateTime authnInstant;
    
    /** Error message. */
    @Nullable private String authnError;
    
    /** Exception. */
    @Nullable private Exception authnException;
    
    /** Flag preventing caching of result for SSO. */
    private boolean doNotCache;
    
    /**
     * Get the flow execution URL to return control to.
     * 
     * @return return location
     */
    @Nullable public String getFlowExecutionUrl() {
        return flowExecutionUrl;
    }
    
    /**
     * 
     * Set the flow execution URL to return control to.
     * 
     * @param url   return location
     */
    public void setFlowExecutionUrl(@Nullable final String url) {
        flowExecutionUrl = url;
    }

    /**
     * Get a {@link Principal} that was authenticated.
     * 
     * @return the principal
     */
    @Nullable public Principal getPrincipal() {
        return principal;
    }

    /**
     * Set a {@link Principal} that was authenticated.
     * 
     * @param prin principal to set
     */
    public void setPrincipal(@Nullable final Principal prin) {
        principal = prin;
    }

    /**
     * Get the name of a principal that was authenticated.
     * 
     * @return name of a principal
     */
    @Nullable public String getPrincipalName() {
        return principalName;
    }

    /**
     * Set the name of a principal that was authenticated.
     * 
     * @param name name of principal to set
     */
    public void setPrincipalName(@Nullable final String name) {
        principalName = name;
    }

    /**
     * Get a {@link Subject} that was authenticated.
     * 
     * @return subject that was authenticated
     */
    @Nullable public Subject getSubject() {
        return subject;
    }

    /**
     * Set a {@link Subject} that was authenticated.
     * 
     * @param sub The subject to set.
     */
    public void setSubject(@Nullable final Subject sub) {
        subject = sub;
    }

    /**
     * Get the time of authentication.
     * 
     * @return time of authentication
     */
    @Nullable public DateTime getAuthnInstant() {
        return authnInstant;
    }

    /**
     * Set the time of authentication.
     * 
     * @param instant time of authentication to set
     */
    public void setAuthnInstant(DateTime instant) {
        authnInstant = instant;
    }

    /**
     * Get an error message from the authentication process.
     * 
     * @return an error message
     */
    @Nullable public String getAuthnError() {
        return authnError;
    }

    /**
     * Set an error message from the authentication process.
     * 
     * @param message message to set
     */
    public void setAuthnError(String message) {
        authnError = message;
    }

    /**
     * Get an exception from the authentication process.
     * 
     * @return an exception
     */
    @Nullable public Exception getAuthnException() {
        return authnException;
    }

    /**
     * Set an exception from the authentication process.
     * 
     * @param exception exception to set
     */
    public void setAuthnException(Exception exception) {
        authnException = exception;
    }
    
    /**
     * Get the "do not cache" flag.
     * 
     * @return true iff the result of the authentication should not be cached
     */
    public boolean doNotCache() {
        return doNotCache;
    }
    
    /**
     * Set the "do not cache" flag.
     * 
     * @param flag flag to set 
     */
    public void setDoNotCache(final boolean flag) {
        doNotCache = flag;
    }
    
}