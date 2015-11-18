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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;


/**
 * An identity provider session belonging to a particular subject and client device.
 */
@ThreadSafe
public interface IdPSession extends IdentifiedComponent {

    /** Name of {@link org.slf4j.MDC} attribute that holds the current session ID: <code>idp.session.id</code>. */
    public static final String MDC_ATTRIBUTE = "idp.session.id";

    /**
     * Get the canonical principal name for the session.
     * 
     * @return the principal name
     */
    @Nonnull @NotEmpty public String getPrincipalName();

    /**
     * Get the time, in milliseconds since the epoch, when this session was created.
     * 
     * @return time this session was created, never less than 0
     */
    @Positive public long getCreationInstant();
    
    /**
     * Get the last activity instant, in milliseconds since the epoch, for the session.
     * 
     * @return last activity instant, in milliseconds since the epoch, for the session, never less than 0
     */
    @Positive public long getLastActivityInstant();
    
    /**
     * Test the session's validity based on the supplied client address, possibly binding it
     * to the session if appropriate.
     * 
     * @param address client address for validation
     * 
     * @return true iff the session is valid for the specified client address
     * @throws SessionException if an error occurs binding the address to the session
     */
    public boolean checkAddress(@Nonnull @NotEmpty final String address) throws SessionException;
    
    /**
     * Test the session's validity based on inactivity, while updating the last activity time.
     * 
     * @return true iff the session is still valid
     * @throws SessionException if an error occurs updating the activity time
     */
    public boolean checkTimeout() throws SessionException;
    
    /**
     * Get the unmodifiable set of {@link AuthenticationResult}s associated with this session.
     * 
     * @return unmodifiable set of results
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Set<AuthenticationResult> getAuthenticationResults();

    /**
     * Get an associated {@link AuthenticationResult} given its flow ID.
     * 
     * @param flowId the ID of the {@link AuthenticationResult}
     * 
     * @return the authentication result, or null
     */
    @Nullable public AuthenticationResult getAuthenticationResult(@Nonnull @NotEmpty final String flowId);

    /**
     * Add a new {@link AuthenticationResult} to this IdP session, replacing any
     * existing result of the same flow ID.
     * 
     * @param result the result to add
     * 
     * @return a previously existing result replaced by the new one, if any
     * @throws SessionException if an error occurs updating the session
     */
    @Nullable public AuthenticationResult addAuthenticationResult(@Nonnull final AuthenticationResult result)
            throws SessionException;

    /**
     * Update the recorded activity timestamp for an {@link AuthenticationResult} associated with this
     * session.
     * 
     * @param result the result to update
     * 
     * @throws SessionException if an error occurs updating the session
     */
    public void updateAuthenticationResultActivity(@Nonnull final AuthenticationResult result)
            throws SessionException;
    
    /**
     * Disassociate an {@link AuthenticationResult} from this IdP session.
     * 
     * @param result the result to disassociate
     * 
     * @return true iff the given result had been associated with this IdP session and now is not
     * @throws SessionException if an error occurs accessing the session
     */
    public boolean removeAuthenticationResult(@Nonnull final AuthenticationResult result) throws SessionException;
    
    /**
     * Gets the unmodifiable collection of service sessions associated with this session.
     * 
     * @return unmodifiable collection of service sessions associated with this session
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Set<SPSession> getSPSessions();

    /**
     * Get the SPSession for a given service.
     * 
     * @param serviceId ID of the service
     * 
     * @return the session service or null if no session exists for that service, may be null
     */
    @Nullable public SPSession getSPSession(@Nonnull @NotEmpty final String serviceId);
    
    /**
     * Add a new SP session to this IdP session, replacing any existing session for the same
     * service.
     * 
     * @param spSession the SP session
     * 
     * @return a previously existing SPSession replaced by the new one, if any
     * @throws SessionException if an error occurs accessing the session
     */
    @Nullable public SPSession addSPSession(@Nonnull final SPSession spSession)
            throws SessionException;
    
    /**
     * Disassociate the given SP session from this IdP session.
     * 
     * @param spSession the SP session
     * 
     * @return true iff the given SP session had been associated with this IdP session and now is not
     * @throws SessionException if an error occurs accessing the SP session
     */
    public boolean removeSPSession(@Nonnull final SPSession spSession) throws SessionException;
    
}