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

package net.shibboleth.idp.authn.config;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/** Configuration of profiles for authentication. */
public interface AuthenticationProfileConfiguration extends ProfileConfiguration {
    
    /**
     * Get the default authentication methods to use, expressed as custom principals.
     * 
     * @return  default authentication methods to use
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable List<Principal> getDefaultAuthenticationMethods();
    
    /**
     * Get the allowable authentication flows for this profile.
     * 
     * <p>The flow IDs returned MUST NOT contain the
     * {@link net.shibboleth.idp.authn.AuthenticationFlowDescriptor#FLOW_ID_PREFIX}
     * prefix common to all interceptor flows.</p>
     * 
     * @return  a set of authentication flow IDs to allow 
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable Set<String> getAuthenticationFlows();

    /**
     * Get an ordered list of post-authentication interceptor flows to run for this profile.
     * 
     * <p>The flow IDs returned MUST NOT contain the
     * {@link net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor#FLOW_ID_PREFIX}
     * prefix common to all interceptor flows.</p>
     * 
     * @return  a set of interceptor flow IDs to enable
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable List<String> getPostAuthenticationFlows();

    /**
     * Get the name identifier formats to use with this relying party, in order of preference.
     * 
     * @return  name identifier formats to use
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable List<String> getNameIDFormatPrecedence();

}