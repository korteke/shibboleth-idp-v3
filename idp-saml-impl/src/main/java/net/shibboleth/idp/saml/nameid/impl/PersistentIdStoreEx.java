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

package net.shibboleth.idp.saml.nameid.impl;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.nameid.IPersistentIdStore;
import net.shibboleth.idp.saml.nameid.PersistentIdEntry;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.joda.time.DateTime;

/**
 * Storage and retrieval interface for SAML persistent IDs redesigned to support appropriately
 * atomic behavior.
 */
public interface PersistentIdStoreEx extends IPersistentIdStore {
    
    /**
     * Get the {@link PersistentIdEntry} for a previously issued ID triple.
     * 
     * @param nameQualifier the NameQualifier value
     * @param spNameQualifier the SPNameQualifier value
     * @param persistentId the persistent ID value
     * 
     * @return {@link PersistentIdEntry} for the given inputs or null if none exists
     * @throws IOException if an error occurs accessing the store
     */
    @Nullable PersistentIdEntry getByIssuedValue(@Nonnull @NotEmpty final String nameQualifier,
            @Nonnull @NotEmpty final String spNameQualifier, @Nonnull @NotEmpty final String persistentId)
                    throws IOException;

// Checkstyle: ParameterNumber OFF
    /**
     * Get the {@link PersistentIdEntry} for a given subject and audience, creating one if allowable
     * and necessary.
     * 
     * @param nameQualifier the NameQualifier value
     * @param spNameQualifier the SPNameQualifier value
     * @param sourceId source attribute underlying the persistent ID
     * @param principal principal name of subject (may or may not be the same as the sourceId)
     * @param allowCreate whether it's permissible to establish/issue a new identifier
     * @param computedIdStrategy optional source of initial computed IDs for compatibilty with that mechanism
     * 
     * @return {@link PersistentIdEntry} for the given inputs, or null if none exists and allowCreate is false
     * @throws IOException if an error occurs accessing the store
     */
    @Nullable PersistentIdEntry getBySourceValue(@Nonnull @NotEmpty final String nameQualifier,
            @Nonnull @NotEmpty final String spNameQualifier, @Nonnull @NotEmpty final String sourceId,
            @Nonnull @NotEmpty final String principal, final boolean allowCreate,
            @Nullable final ComputedPersistentIdGenerationStrategy computedIdStrategy) throws IOException;
// Checkstyle: ParameterNumber ON
    
    /**
     * Deactivate/revoke a persistent ID.
     * 
     * @param nameQualifier the NameQualifier value
     * @param spNameQualifier the SPNameQualifier value
     * @param persistentId ID to deactivate
     * @param deactivation deactivation time (if null the current time is used)
     * 
     * @throws IOException if there is an error updating the store
     */
    void deactivate(@Nonnull @NotEmpty final String nameQualifier, @Nonnull @NotEmpty final String spNameQualifier,
            @Nonnull @NotEmpty final String persistentId, @Nullable final DateTime deactivation) throws IOException;

    /**
     * Attach an SPProvidedID value to an existing entry.
     * 
     * @param nameQualifier the NameQualifier value
     * @param spNameQualifier the SPNameQualifier value
     * @param persistentId ID to deactivate
     * @param spProvidedId the value to attach
     * 
     * @throws IOException if there is an error updating the store
     */
    void attach(@Nonnull @NotEmpty final String nameQualifier, @Nonnull @NotEmpty final String spNameQualifier,
            @Nonnull @NotEmpty final String persistentId, @Nonnull @NotEmpty final String spProvidedId)
                    throws IOException;
    
}