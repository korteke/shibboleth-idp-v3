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
package net.shibboleth.idp.cas.config.impl;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import org.opensaml.profile.context.ProfileRequestContext;

/**
 * Lookup function for extracting CAS profile configuration from the profile request context.
 *
 * @author Marvin S. Addison
 */
public class ConfigLookupFunction<T extends AbstractProtocolConfiguration> implements Function<ProfileRequestContext, T> {

    /** Type of profile configuration class. */
    private final Class<T> configClass;

    /**
     * Creates a new instance.
     *
     * @param clazz Profile configuration class.
     */
    public ConfigLookupFunction(final Class<T> clazz) {
        configClass = clazz;
    }

    @Override
    @Nullable
    public T apply(@Nullable ProfileRequestContext profileRequestContext) {
        if (profileRequestContext != null) {
            final RelyingPartyContext rpContext = profileRequestContext.getSubcontext(RelyingPartyContext.class, false);
            if (rpContext != null && configClass.isInstance(rpContext.getProfileConfig())) {
                return configClass.cast(rpContext.getProfileConfig());
            }
        }
        return null;
    }
}
