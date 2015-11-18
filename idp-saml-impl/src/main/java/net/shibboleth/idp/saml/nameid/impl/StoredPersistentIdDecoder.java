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

import org.opensaml.saml.saml2.core.NameID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.nameid.IPersistentIdStore;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDDecoder;
import net.shibboleth.idp.saml.nameid.PersistentIdEntry;
import net.shibboleth.idp.saml.nameid.PersistentIdStore;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * An abstract action which contains the logic to decode SAML persistent IDs that are managed with a store.
 * This reverses the work done by {@link StoredPersistentIdGenerationStrategy}.
 */
@SuppressWarnings("deprecation")
public class StoredPersistentIdDecoder extends AbstractIdentifiableInitializableComponent implements NameIDDecoder {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoredPersistentIdDecoder.class);

    /** Updated version of persistent identifier data store layer. */
    @NonnullAfterInit private PersistentIdStoreEx pidStore;

    /** A DataSource to auto-provision a {@link JDBCPersistentIdStoreEx} instance. */
    @Nullable private DataSource dataSource;

    /** Deprecated version of persistent identifier data store. */
    @Nullable private PersistentIdStore deprecatedStore;
    
    /**
     * Get the data store.
     * 
     * @return the data store
     */
    @NonnullAfterInit public IPersistentIdStore getPersistentIdStore() {
        return deprecatedStore;
    }

    /**
     * Set a data store to use.
     * 
     * @param store the data store
     */
    public void setPersistentIdStore(@Nullable final IPersistentIdStore store) {
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
    
    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    
        if (null == pidStore) {
            if (dataSource != null) {
                log.debug("Creating JDBCPersistentStoreEx instance around supplied DataSource");
                final JDBCPersistentIdStoreEx newStore = new JDBCPersistentIdStoreEx();
                // Don't validate the database because this side is just reading data.
                newStore.setVerifyDatabase(false);
                newStore.setDataSource(dataSource);
                newStore.initialize();
                pidStore = newStore;
            } else if (deprecatedStore != null) {
                if (deprecatedStore instanceof JDBCPersistentIdStore) {
                    log.warn("Transferring settings from deprecated JDBCPersistentStore, please update configuration");
                    final JDBCPersistentIdStoreEx newStore = new JDBCPersistentIdStoreEx();
                    // Don't validate the database because this side is just reading data.
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
    @Nonnull @NotEmpty public String decode(@Nonnull final SubjectCanonicalizationContext c14nContext,
            @Nonnull final NameID nameID) throws NameDecoderException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (nameID.getValue() == null) {
            throw new NameDecoderException("Persistent NameID was empty");
        }

        String recipientID = nameID.getSPNameQualifier();
        if (recipientID == null) {
            recipientID = c14nContext.getRequesterId();
            if (recipientID == null) {
                throw new NameDecoderException("SPNameQualifier and requester ID were null");
            }
        }
        
        String issuerID = nameID.getNameQualifier();
        if (issuerID == null) {
            issuerID = c14nContext.getResponderId();
            if (issuerID == null) {
                throw new NameDecoderException("NameQualifier and responder ID were null");
            }
        }
        
        try {
            final PersistentIdEntry entry = pidStore.getByIssuedValue(issuerID, recipientID, nameID.getValue());
            if (entry == null || entry.getPrincipalName() == null) {
                log.info("No entry found for persistent ID {}", nameID.getValue());
                return null;
            }
            return entry.getPrincipalName();
        } catch (final IOException e) {
            log.error("I/O error looking up persistent ID", e);
            return null;
        }        
    }

}