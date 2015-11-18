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

package net.shibboleth.idp.session;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;

/**
 * Implementation support for a concrete {@link SPSession} implementation.
 */
@ThreadSafe
public class BasicSPSession implements SPSession {
    
    /** The unique identifier of the service. */
    @Nonnull @NotEmpty private final String serviceId;
    
    /** The time, in milliseconds since the epoch, when this session was created. */
    @Duration @Positive private final long creationInstant;

    /** The time, in milliseconds since the epoch, when this session expires. */
    @Duration @Positive private final long expirationInstant;
    
    /**
     * Constructor.
     * 
     * @param id the identifier of the service associated with this session
     * @param creation creation time of session, in milliseconds since the epoch
     * @param expiration expiration time of session, in milliseconds since the epoch
     */
    public BasicSPSession(@Nonnull @NotEmpty final String id, @Duration @Positive final long creation,
            @Duration @Positive final long expiration) {
        serviceId = Constraint.isNotNull(StringSupport.trimOrNull(id), "Service ID cannot be null nor empty");
        creationInstant = Constraint.isGreaterThan(0, creation, "Creation instant must be greater than 0");
        expirationInstant = Constraint.isGreaterThan(0, expiration, "Expiration instant must be greater than 0");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getId() {
        return serviceId;
    }
    
    /** {@inheritDoc} */
    @Override
    @Positive public long getCreationInstant() {
        return creationInstant;
    }

    /** {@inheritDoc} */
    @Override
    @Positive  public long getExpirationInstant() {
        return expirationInstant;
    }

    /** {@inheritDoc} */
    @Override
    public String getSPSessionKey() {
        // A basic session doesn't have a secondary lookup key.
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return serviceId.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof BasicSPSession) {
            return Objects.equals(serviceId, ((BasicSPSession) obj).getId());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", serviceId)
                .add("creationInstant", new DateTime(creationInstant))
                .add("expirationInstant", new DateTime(expirationInstant)).toString();
    }
    
}