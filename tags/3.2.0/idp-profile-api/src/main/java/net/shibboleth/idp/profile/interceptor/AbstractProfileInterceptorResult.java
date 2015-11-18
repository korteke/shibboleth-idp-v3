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

package net.shibboleth.idp.profile.interceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Base class for profile interceptor results.
 */
public abstract class AbstractProfileInterceptorResult extends AbstractIdentifiableInitializableComponent implements
        ProfileInterceptorResult {

    /** Storage context. */
    @Nonnull @NotEmpty private String storageContext;

    /** Storage key. */
    @Nonnull @NotEmpty private String storageKey;

    /** Storage value. */
    @Nonnull @NotEmpty private String storageValue;

    /** Storage expiration. */
    @Nullable @Positive @Duration private Long storageExpiration;

    /**
     * Constructor.
     *
     * @param context storage context
     * @param key storage key
     * @param value storage value
     * @param expiration storage expiration
     */
    public AbstractProfileInterceptorResult(
            @Nonnull @NotEmpty final String context,
            @Nonnull @NotEmpty final String key,
            @Nonnull @NotEmpty final String value,
            @Nullable @Positive @Duration final Long expiration) {

        storageContext =
                Constraint.isNotNull(StringSupport.trimOrNull(context), "Storage context cannot be null nor empty");
        storageKey = Constraint.isNotNull(StringSupport.trimOrNull(key), "Storage key cannot be null nor empty");
        storageValue = Constraint.isNotNull(StringSupport.trimOrNull(value), "Storage value cannot be null nor empty");
        if (expiration != null) {
            storageExpiration = Constraint.isGreaterThan(0, expiration, "Storage expiration must be greater than 0");
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getStorageContext() {
        return storageContext;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getStorageKey() {
        return storageKey;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getStorageValue() {
        return storageValue;
    }

    /** {@inheritDoc} */
    @Nullable @Positive public Long getStorageExpiration() {
        return storageExpiration;
    }
}
