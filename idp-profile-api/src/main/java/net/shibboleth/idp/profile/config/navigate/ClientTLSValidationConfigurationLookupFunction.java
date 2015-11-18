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

package net.shibboleth.idp.profile.config.navigate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.AbstractRelyingPartyLookupFunction;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;

import org.opensaml.core.config.ConfigurationService;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.x509.tls.ClientTLSValidationConfiguration;

/**
 * A function that returns a {@link ClientTLSValidationConfiguration} list by way of various lookup strategies.
 * 
 * <p>If a specific setting is unavailable, a null value is returned.</p>
 */
public class ClientTLSValidationConfigurationLookupFunction
        extends AbstractRelyingPartyLookupFunction<List<ClientTLSValidationConfiguration>> {
    
    /** A resolver for default security configurations. */
    @Nullable private RelyingPartyConfigurationResolver rpResolver;

    /**
     * Set the resolver for default security configurations.
     * 
     * @param resolver the resolver to use
     */
    public void setRelyingPartyConfigurationResolver(@Nullable final RelyingPartyConfigurationResolver resolver) {
        rpResolver = resolver;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public List<ClientTLSValidationConfiguration> apply(@Nullable final ProfileRequestContext input) {
        
        final List<ClientTLSValidationConfiguration> configs = new ArrayList<>();
        
        final RelyingPartyContext rpc = getRelyingPartyContextLookupStrategy().apply(input);
        if (rpc != null) {
            final ProfileConfiguration pc = rpc.getProfileConfig();
            if (pc != null && pc.getSecurityConfiguration() != null) {
                configs.add(pc.getSecurityConfiguration().getClientTLSValidationConfiguration());
            }
        }
        
        // Check for a per-profile default (relying party independent) config.
        if (input != null && rpResolver != null) {
            final SecurityConfiguration defaultConfig =
                    rpResolver.getDefaultSecurityConfiguration(input.getProfileId());
            if (defaultConfig != null && defaultConfig.getClientTLSValidationConfiguration() != null) {
                configs.add(defaultConfig.getClientTLSValidationConfiguration());
            }
        }

        configs.add(ConfigurationService.get(ClientTLSValidationConfiguration.class));
        
        return configs;
    }

}