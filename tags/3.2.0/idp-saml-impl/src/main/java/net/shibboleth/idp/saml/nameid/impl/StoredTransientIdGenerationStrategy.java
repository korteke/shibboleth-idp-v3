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

import net.shibboleth.idp.saml.nameid.TransientIdParameters;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy;

import org.opensaml.saml.common.SAMLException;
import org.opensaml.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates transients using a {@link StorageService} to manage the reverse mappings.
 * 
 * <p>The identifier itself is the record key, and the value combines the principal name with the
 * identifier of the recipient.</p>
 */
public class StoredTransientIdGenerationStrategy extends AbstractIdentifiableInitializableComponent
        implements TransientIdGenerationStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoredTransientIdGenerationStrategy.class);

    /** Store used to map identifiers to principals. */
    @NonnullAfterInit private StorageService idStore;

    /** Generator of random, hex-encoded, identifiers. */
    @NonnullAfterInit private IdentifierGenerationStrategy idGenerator;

    /** Size, in bytes, of the identifier. */
    private int idSize;

    /** Length, in milliseconds, identifiers are valid. */
    @Duration @Positive private long idLifetime;

    /** Constructor. */
    public StoredTransientIdGenerationStrategy() {
        idSize = 16;
        idLifetime = 1000 * 60 * 60 * 4;
    }

    /**
     * Set the ID store we should use.
     * 
     * @param store the store to use.
     */
    public void setIdStore(@Nonnull final StorageService store) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idStore = Constraint.isNotNull(store, "StorageService cannot be null");
    }

    /**
     * Set the ID generator we should use.
     * 
     * @param generator identifier generation strategy to use
     */
    public void setIdGenerator(@Nonnull final IdentifierGenerationStrategy generator) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idGenerator = Constraint.isNotNull(generator, "IdentifierGenerationStrategy cannot be null");
    }
    
    /**
     * Get the size, in bytes, of the id.
     * 
     * @return  id size, in bytes
     */
    @Positive public int getIdSize() {
        return idSize;
    }
    
    /**
     * Set the size, in bytes, of the id.
     * 
     * @param size size, in bytes, of the id
     */
    public void setIdSize(@Positive final int size) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idSize = (int) Constraint.isGreaterThan(0, size, "ID size must be positive");
    }
    
    /**
     * Get the time, in milliseconds, ids are valid.
     * 
     * @return  time, in milliseconds, ids are valid
     */
    @Positive public long getIdLifetime() {
        return idLifetime;
    }

    /**
     * Set the time, in milliseconds, ids are valid.
     * 
     * @param lifetime time, in milliseconds, ids are valid
     */
    public void setIdLifetime(@Duration @Positive final long lifetime) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idLifetime = Constraint.isGreaterThan(0, lifetime, "ID lifetime must be positive");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == idStore) {
            throw new ComponentInitializationException("StorageService cannot be null");
        }

        if (idGenerator == null) {
            idGenerator = new RandomIdentifierGenerationStrategy(idSize);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String generate(@Nonnull @NotEmpty final String relyingPartyId,
            @Nonnull @NotEmpty final String principalName) throws SAMLException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        try {
            final String principalTokenId = new TransientIdParameters(relyingPartyId, principalName).encode();
    
            // This code used to store the entries keyed by the ID *and* the value, which I think
            // was used to prevent generation of multiple IDs if the resolver runs multiple times.
            // This is the source of the current V2 bug that causes the same transient to be reused
            // for the same SP within the TTL window. If we need to prevent multiple resolutions, I
            // suggest we do that by storing transactional state for resolver plugins in the context
            // tree. But in practice, I'm not sure it matters much how many times this runs, that's
            // the point of a transient. So this version never reads the store, it just writes to it.
    
            final String id = idGenerator.generateIdentifier();
    
            log.debug("Creating new transient ID '{}'", id);
    
            final long expiration = System.currentTimeMillis() + idLifetime;
    
            int collisions = 0;
            while (collisions < 5) {
                if (idStore.create(TransientIdParameters.CONTEXT, id, principalTokenId, expiration)) {
                    return id;
                } else {
                    ++collisions;
                }
            }
        
            throw new SAMLException("Exceeded allowable number of collisions");
        } catch (final IOException e) {
            throw new SAMLException(e);
        }
    }

}