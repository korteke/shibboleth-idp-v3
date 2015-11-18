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

import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.SecurityConfigurationSupport;

import com.google.common.base.Function;

/**
 * A function that returns a {@link EncryptionConfiguration} list intended for self-encryption cases.
 * 
 * <p>A self-specific {@link EncryptionConfiguration} may be supplied. This, if present will be composed
 * with the global config {@link SecurityConfigurationSupport#getGlobalEncryptionConfiguration()}</p>
 */
public class SelfEncryptionConfigurationLookupFunction 
        implements Function<ProfileRequestContext,List<EncryptionConfiguration>> {
    
    /** The self-encryption configuration. */
    @Nullable private EncryptionConfiguration selfConfig;
    
    /** A resolver for default security configurations. */
    @Nullable private RelyingPartyConfigurationResolver rpResolver;
    
    /** Flag indicating whether the profile default configuration should be included in the returned list. */
    private boolean includeProfileDefaultConfiguration;

    /**
     * Set the resolver for default security configurations.
     * 
     * @param resolver the resolver to use
     */
    public void setRelyingPartyConfigurationResolver(@Nullable final RelyingPartyConfigurationResolver resolver) {
        rpResolver = resolver;
    }
    
    /**
     * Set the self-encryption configuration.
     * 
     * @param config the self-encryption {@link EncryptionConfiguration}
     */
    public void setSelfConfiguration(@Nullable final EncryptionConfiguration config) {
        selfConfig = config;
    }
    
    /**
     *  Set the flag indicating whether the profile default configuration should be included in the returned list.
     *  
     * @param flag true if profile default should be included, false otherwise
     */
    public void setIncludeProfileDefaultConfiguration(boolean flag) {
        includeProfileDefaultConfiguration = flag;
    }
    
    /** {@inheritDoc} */
    @Override
    @Nullable public List<EncryptionConfiguration> apply(@Nullable final ProfileRequestContext input) {
        
        final List<EncryptionConfiguration> configs = new ArrayList<>();
        
        if (selfConfig != null) {
            configs.add(selfConfig);
        }
        
        // Check for a per-profile default config.
        if (includeProfileDefaultConfiguration && input != null && rpResolver != null) {
            final SecurityConfiguration defaultConfig =
                    rpResolver.getDefaultSecurityConfiguration(input.getProfileId());
            if (defaultConfig != null && defaultConfig.getEncryptionConfiguration() != null) {
                configs.add(defaultConfig.getEncryptionConfiguration());
            }
        }

        configs.add(SecurityConfigurationSupport.getGlobalEncryptionConfiguration());
        
        return configs;
    }

}