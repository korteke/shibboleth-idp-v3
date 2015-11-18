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

/** {@link Criterion} representing a session ID. */
public final class SessionIdCriterion implements Criterion {

    /** The session ID. */
    @Nonnull @NotEmpty private final String id;

    /**
     * Constructor.
     * 
     * @param sessionId the session ID
     */
    public SessionIdCriterion(@Nonnull @NotEmpty final String sessionId) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(sessionId), "Session ID cannot be null or empty");
    }

    /**
     * Get the session ID.
     * 
     * @return the session ID
     */
    @Nonnull @NotEmpty public String getSessionId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sessionId", id).toString();
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

        if (obj instanceof SessionIdCriterion) {
            return id.equals(((SessionIdCriterion) obj).id);
        }

        return false;
    }
}