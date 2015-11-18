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

package net.shibboleth.idp.authn;

import java.security.Principal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.principal.PrincipalSupportingComponent;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

/**
 * Describes an act of authentication.
 *
 * <p>Any authentication flow that succeeds must produce a single instance of this object.
 * It may be composite, in the sense that it may represent a combination of separate exchanges
 * that make up a single overall result, but the IdP always acts on a single result as the
 * product of a given request for a login.</p>
 */
@ThreadSafe
public class AuthenticationResult implements PrincipalSupportingComponent {
    
    /** The Subject established by the authentication result. */
    @Nonnull private final Subject subject;

    /** The identifier of the flow used to produce this result. */
    @Nonnull @NotEmpty private final String authenticationFlowId;
    
    /** The time, in milliseconds since the epoch, that the authentication completed. */
    @Positive private long authenticationInstant;

    /** The last time, in milliseconds since the epoch, this result was used to bypass authentication. */
    @Positive private long lastActivityInstant;
    
    /**
     * Constructor.
     * 
     * <p>Sets the authentication instant to the current time.</p>
     * 
     * @param flowId the workflow used to authenticate the subject
     * @param newSubject a Subject identifying the authenticated entity
     */
    public AuthenticationResult(@Nonnull @NotEmpty final String flowId, @Nonnull final Subject newSubject) {

        authenticationFlowId = Constraint.isNotNull(StringSupport.trimOrNull(flowId),
                "Authentication flow ID cannot be null nor empty");
        subject = Constraint.isNotNull(newSubject, "Subject list cannot be null or empty");
        authenticationInstant = System.currentTimeMillis();
        lastActivityInstant = authenticationInstant;
    }

    /**
     * Constructor. <p>Sets the authentication instant to the current time.</p>
     * 
     * @param flowId the workflow used to authenticate the subject
     * @param principal a Principal identifying the authenticated entity
     */
    public AuthenticationResult(@Nonnull @NotEmpty final String flowId, @Nonnull final Principal principal) {
        this(flowId, new Subject(false, ImmutableSet.of(Constraint.isNotNull(principal, "Principal cannot be null")),
                Collections.EMPTY_SET, Collections.EMPTY_SET));
    }
    
    /**
     * Get the Subject identifying the authenticated entity.
     * 
     * @return a Subject identifying the authenticated entity
     */
    @Nonnull public Subject getSubject() {
        return subject;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @Unmodifiable public <T extends Principal> Set<T> getSupportedPrincipals(
            @Nonnull final Class<T> c) {
        return subject.getPrincipals(c);
    }
    
    /**
     * Get the flow used to authenticate the principal.
     * 
     * @return flow used to authenticate the principal
     */
    @Nonnull @NotEmpty public String getAuthenticationFlowId() {
        return authenticationFlowId;
    }

    /**
     * Get the time, in milliseconds since the epoch, that the authentication completed.
     * 
     * @return time, in milliseconds since the epoch, that the authentication completed, never non-positive
     */
    @Positive public long getAuthenticationInstant() {
        return authenticationInstant;
    }

    /**
     * Set the time, in milliseconds since the epoch, that the authentication completed.
     * 
     * @param instant time, in milliseconds since the epoch, that the authentication completed, never non-positive
     */
    public void setAuthenticationInstant(@Positive final long instant) {
        authenticationInstant = Constraint.isGreaterThan(0, instant, "Authentication instant must be greater than 0");
    }
    
    /**
     * Get the last time, in milliseconds since the epoch, this result was used for authentication.
     * 
     * @return last time, in milliseconds since the epoch, this result was used for authentication
     */
    @Positive public long getLastActivityInstant() {
        return lastActivityInstant;
    }
    
    /**
     * Set the last time, in milliseconds since the epoch, result was used for authentication.
     * 
     * @param instant last time, in milliseconds since the epoch, result was used to bypass authentication
     */
    public void setLastActivityInstant(@Positive final long instant) {
        lastActivityInstant = Constraint.isGreaterThan(0, instant, "Last activity instant must be greater than 0");
    }

    /**
     * Sets the last activity instant, in milliseconds since the epoch, for this result to the current time.
     */
    public void setLastActivityInstantToNow() {
        lastActivityInstant = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return authenticationFlowId.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (obj instanceof AuthenticationResult) {
            return Objects.equals(getAuthenticationFlowId(), ((AuthenticationResult) obj).getAuthenticationFlowId())
                    && getAuthenticationInstant() == ((AuthenticationResult) obj).getAuthenticationInstant();
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("authenticationFlowId", authenticationFlowId)
                .add("authenticatedPrincipal", getSubjectName())
                .add("authenticationInstant", new DateTime(authenticationInstant))
                .add("lastActivityInstant", new DateTime(lastActivityInstant)).toString();
    }
    
    /**
     * Get a suitable principal name for logging/debugging use.
     * 
     * @return a principal name for logging/debugging
     */
    @Nullable private String getSubjectName() {
        
        Set<UsernamePrincipal> usernames = getSubject().getPrincipals(UsernamePrincipal.class);
        if (!usernames.isEmpty()) {
            return usernames.iterator().next().getName();
        }
        
        for (Principal p : getSubject().getPrincipals()) {
            return p.getName();
        }
        
        return null;
    }
    
}