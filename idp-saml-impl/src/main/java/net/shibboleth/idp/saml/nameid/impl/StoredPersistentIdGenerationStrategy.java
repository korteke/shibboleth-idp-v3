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
import javax.sql.DataSource;

import net.shibboleth.idp.saml.nameid.IPersistentIdStore;
import net.shibboleth.idp.saml.nameid.PersistentIdEntry;
import net.shibboleth.idp.saml.nameid.PersistentIdStore;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages persistent IDs via a {@link PersistentIdStore}, generating them either randomly or via a
 * {@link ComputedPersistentIdGenerationStrategy} (for compatibility with existing data).
 */
@SuppressWarnings("deprecation")
public class StoredPersistentIdGenerationStrategy extends AbstractInitializableComponent
        implements PersistentIdGenerationStrategy {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoredPersistentIdGenerationStrategy.class);

    /** Updated version of persistent identifier data store layer. */
    @NonnullAfterInit private PersistentIdStoreEx pidStore;

    /** A DataSource to auto-provision a {@link JDBCPersistentIdStoreEx} instance. */
    @Nullable private DataSource dataSource;
    
    /** Deprecated version of persistent identifier data store. */
    @Nullable private PersistentIdStore deprecatedStore;
    
    /** Optional generator of computed ID values. */
    @Nullable private ComputedPersistentIdGenerationStrategy computedIdStrategy;

    /**
     * Set an {@link IPersistentIdStore} used to store the IDs.
     * 
     * <p>The marker interface is used, allowing injection of either the broken or the updated
     * version of the interface.</p>
     * 
     * @param store the ID store to use
     */
    public void setIDStore(@Nullable final IPersistentIdStore store) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if (store instanceof PersistentIdStoreEx) {
            pidStore = (PersistentIdStoreEx) store;
            deprecatedStore = null;
        } else if (store instanceof PersistentIdStore) {
            deprecatedStore = (PersistentIdStore) store;
            pidStore = null;
        } else {
            pidStore = null;
            deprecatedStore = null;
        }
    }
    
    /**
     * Set a data source to inject into an auto-provisioned instance of {@link JDBCPersistentIdStoreEx}
     * to use as the storage strategy.
     * 
     * @param source the data source
     */
    public void setDataSource(@Nullable final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        dataSource = source;
    }
    
    /**
     * Set a strategy to use to compute IDs for the first time.
     * 
     * @param strategy  computed ID strategy
     */
    public void setComputedIdStrategy(@Nullable final ComputedPersistentIdGenerationStrategy strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        computedIdStrategy = strategy;
    }
    
    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == pidStore) {
            if (dataSource != null) {
                log.debug("Creating JDBCPersistentStoreEx instance around supplied DataSource");
                final JDBCPersistentIdStoreEx newStore = new JDBCPersistentIdStoreEx();
                newStore.setDataSource(dataSource);
                newStore.initialize();
                pidStore = newStore;
            } else if (deprecatedStore != null) {
                if (deprecatedStore instanceof JDBCPersistentIdStore) {
                    log.warn("Transferring settings from deprecated JDBCPersistentStore, please update configuration");
                    final JDBCPersistentIdStoreEx newStore = new JDBCPersistentIdStoreEx();
                    // Don't validate the database because legacy configs won't have primary key defined.
                    newStore.setVerifyDatabase(false);
                    newStore.setDataSource(((JDBCPersistentIdStore) deprecatedStore).getDataSource());
                    newStore.setQueryTimeout(((JDBCPersistentIdStore) deprecatedStore).getQueryTimeout());
                    newStore.setLocalEntityColumn(((JDBCPersistentIdStore) deprecatedStore).getLocalEntityColumn());
                    newStore.setPeerEntityColumn(((JDBCPersistentIdStore) deprecatedStore).getPeerEntityColumn());
                    newStore.setPersistentIdColumn(((JDBCPersistentIdStore) deprecatedStore).getPersistentIdColumn());
                    newStore.setPrincipalNameColumn(((JDBCPersistentIdStore) deprecatedStore).getPrincipalNameColumn());
                    newStore.setSourceIdColumn(((JDBCPersistentIdStore) deprecatedStore).getSourceIdColumn());
                    newStore.setPeerProvidedIdColumn(
                            ((JDBCPersistentIdStore) deprecatedStore).getPeerProvidedIdColumn());
                    newStore.setCreateTimeColumn(((JDBCPersistentIdStore) deprecatedStore).getCreateTimeColumn());
                    newStore.setDeactivationTimeColumn(
                            ((JDBCPersistentIdStore) deprecatedStore).getDeactivationTimeColumn());
                    newStore.initialize();
                    pidStore = newStore;
                } else {
                    throw new ComponentInitializationException(
                            "Non-JDBC version of deprecated PersistentIdStore interface is not usable in this version");
                }
            }
            
            if (null == pidStore) {
                throw new ComponentInitializationException("PersistentIdStore cannot be null");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String generate(@Nonnull @NotEmpty final String assertingPartyId,
            @Nonnull @NotEmpty final String relyingPartyId, @Nonnull @NotEmpty final String principalName,
            @Nonnull @NotEmpty final String sourceId) throws SAMLException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        try {
            final PersistentIdEntry idEntry = pidStore.getBySourceValue(assertingPartyId, relyingPartyId, sourceId,
                    principalName, true, computedIdStrategy);
            if (idEntry == null) {
                log.debug("No persistent ID returned from storage for '{}'", principalName);
                throw new SAMLException("No persistent ID returned from storage");
            }
            
            log.debug("Obtained persistent ID entry: {}", idEntry);
    
            final String pid = StringSupport.trimOrNull(idEntry.getPersistentId());
            if (null == pid) {
                log.debug("Returned persistent ID was null");
                throw new SAMLException("Returned persistent ID was null");
            }
    
            return pid;
        } catch (final IOException e) {
            log.debug("ID storage error obtaining persistent identifier", e);
            throw new SAMLException("ID storage error obtaining persistent identifier", e);
        }
    }
    
}