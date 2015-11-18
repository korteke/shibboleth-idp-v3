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

import javax.annotation.Nullable;

import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.MoreObjects;

/**
 * A context, usually attached to {@link AuthenticationContext}, containing data about an LDAP authentication operation.
 */
public class LDAPResponseContext extends BaseContext {

    /** Authentication response. */
    @Nullable private AuthenticationResponse authenticationResponse;

    /**
     * Get the LDAP authentication response.
     * 
     * @return LDAP authentication response
     */
    @Nullable public AuthenticationResponse getAuthenticationResponse() {
        return authenticationResponse;
    }

    /**
     * Set the LDAP authentication response.
     * 
     * @param response of an LDAP authentication
     * 
     * @return this context
     */
    public LDAPResponseContext setAuthenticationResponse(@Nullable final AuthenticationResponse response) {
        authenticationResponse = response;
        return this;
    }

    /**
     * Check for the presence of account state warnings.
     *
     * @return  true if account state warnings exist
     */
    public boolean hasAccountStateWarning() {
        final AccountState state = authenticationResponse.getAccountState();
        return state != null ? state.getWarning() != null : false;
    }

    /**
     * Check for the presence of account state errors.
     *
     * @return  true if account state errors exist
     */
    public boolean hasAccountStateError() {
        final AccountState state = authenticationResponse.getAccountState();
        return state != null ? state.getError() != null : false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("authenticationResponse", authenticationResponse)
                .toString();
    }

}