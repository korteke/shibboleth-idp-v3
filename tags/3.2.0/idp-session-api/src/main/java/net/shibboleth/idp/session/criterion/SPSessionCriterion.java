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

package net.shibboleth.idp.session.criterion;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.Criterion;

/** {@link Criterion} representing a service ID and an implementation-specific service session key. */
public final class SPSessionCriterion implements Criterion {

    /** The service ID. */
    @Nonnull @NotEmpty private final String id;

    /** Service session key. */
    @Nonnull @NotEmpty private final String key;
    
    /**
     * Constructor.
     * 
     * @param serviceId the service ID
     * @param spSessionKey the custom key associated with the SP session
     */
    public SPSessionCriterion(@Nonnull @NotEmpty final String serviceId,
            @Nonnull @NotEmpty final String spSessionKey) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(serviceId), "Service ID cannot be null or empty");
        key = Constraint.isNotNull(StringSupport.trimOrNull(spSessionKey),
                "SPSession key cannot be null or empty");
    }

    /**
     * Get the service ID.
     * 
     * @return the service ID
     */
    @Nonnull @NotEmpty public String getServiceId() {
        return id;
    }

    /**
     * Get the service session key.
     * 
     * @return the service session key
     */
    @Nonnull @NotEmpty public String getSPSessionKey() {
        return key;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("serviceId", id)
                .add("SPSessionKey", key)
                .toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof SPSessionCriterion) {
            return id.equals(((SPSessionCriterion) obj).id)
                    && key.equals(((SPSessionCriterion) obj).key);
        }

        return false;
    }
}