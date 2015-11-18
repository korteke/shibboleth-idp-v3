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

package net.shibboleth.idp.saml.nameid;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.joda.time.DateTime;

/**
 * Storage and retrieval interface for SAML persistent IDs.
 * 
 * <p>This interface is deprecated and is no longer used within the IdP. It is "racy" in that
 * the operations are too granular to ensure transactional behavior for multiple requests
 * from the same user for the same service, which is supported now, and was much less common
 * in V2.</p>
 * 
 * @deprecated
 */
public interface PersistentIdStore extends IPersistentIdStore {

    /**
     * Get whether a persistent ID is not in use, active or otherwise.
     * 
     * @param persistentId  the ID to check
     * 
     * @return true iff the ID is available for use
     * @throws IOException if an error occurs accessing the store
     */
    boolean isAvailable(@Nonnull @NotEmpty final String persistentId) throws IOException;
    
    /**
     * Store a persistent ID.
     * 
     * @param entry entry to store
     * 
     * @throws IOException if there is an error updating the store
     */
    void store(@Nonnull final PersistentIdEntry entry) throws IOException;
        
    /**
     * Get the count of persistent ID entries for a (principal, issuer, source ID) tuple.
     * 
     * @param issuer entityID of the ID issuer
     * @param recipient entityID of the recipient the ID is for
     * @param sourceId source ID underlying the persistent ID
     * 
     * @return the number of identifiers
     * @throws IOException if an error occurs accessing the store
     */
    int getCount(@Nonnull @NotEmpty final String issuer, @Nonnull @NotEmpty final String recipient,
            @Nonnull @NotEmpty final String sourceId) throws IOException;
    
    /**
     * Get the active persistent ID entry for a given ID.
     * 
     * @param persistentId the persistent ID to lookup
     * 
     * @return entry for the given ID or null if none exists
     * @throws IOException if an error occurs accessing the store
     */
    @Nullable PersistentIdEntry getActiveEntry(@Nonnull @NotEmpty final String persistentId) throws IOException;
    
    /**
     * Get the active persistent ID entry for an (issuer, recipient, source ID) tuple.
     * 
     * @param issuer entityID of the ID issuer
     * @param recipient entityID of the recipient the ID is for
     * @param sourceId source ID underlying the persistent ID
     * 
     * @return the active identifier
     * @throws IOException if an error occurs accessing the store
     */
    @Nullable PersistentIdEntry getActiveEntry(@Nonnull @NotEmpty final String issuer,
            @Nonnull @NotEmpty final String recipient, @Nonnull @NotEmpty final String sourceId) throws IOException;

    /**
     * Deactivate a persistent ID.
     * 
     * @param persistentId ID to deactivate
     * @param deactivation deactivation time, if null the current time is used
     * 
     * @throws IOException if there is an error updating the store
     */
    void deactivate(@NotEmpty final String persistentId, @Nullable final DateTime deactivation) throws IOException;

}