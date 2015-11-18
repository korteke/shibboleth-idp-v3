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

package net.shibboleth.idp.relyingparty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;
import net.shibboleth.utilities.java.support.resolver.Resolver;

import org.opensaml.profile.context.ProfileRequestContext;

/** Resolves a {@link RelyingPartyConfiguration} for a given profile request context. */
public interface RelyingPartyConfigurationResolver extends Resolver<RelyingPartyConfiguration,ProfileRequestContext>,
        IdentifiedComponent {

    /**
     * Return the default security configuration for the profile.
     * 
     * @param profileId the profile ID (available via
     *      {@link net.shibboleth.idp.profile.config.ProfileConfiguration#getId()}
     * @return the configured default configuration
     */
    @Nullable SecurityConfiguration getDefaultSecurityConfiguration(@Nonnull @NotEmpty final String profileId);
    
}