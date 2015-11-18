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

package net.shibboleth.idp.saml.attribute.resolver.impl;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.nameid.impl.JDBCPersistentIdStoreEx;
import net.shibboleth.idp.saml.nameid.impl.StoredPersistentIdGenerationStrategy;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A data connector that delegates generation of IDs to a {@link StoredPersistentIdGenerationStrategy}
 * that makes use of a {@link JDBCPersistentIdStore}.
 */
public class StoredIDDataConnector extends ComputedIDDataConnector {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoredIDDataConnector.class);

    /** The {@link JDBCPersistentIdStore} used to manage IDs. */
    @Nonnull private final JDBCPersistentIdStoreEx idStore;

    /** Persistent ID data store. */
    @Nonnull private final StoredPersistentIdGenerationStrategy storedIdStrategy;
    
    /** Constructor. */
    public StoredIDDataConnector() {
        idStore = new JDBCPersistentIdStoreEx();
        storedIdStrategy = new StoredPersistentIdGenerationStrategy();
    }
    
    /**
     * Get the {@link DataSource} used to communicate with the database.
     * 
     * @return the {@link DataSource}
     */
    @NonnullAfterInit public DataSource getDataSource() {
        return idStore.getDataSource();
    }

    /**
     * Set the {@link DataSource} used to communicate with the database.
     * 
     * @param source the {@link DataSource}.
     */
    public void setDataSource(@Nonnull final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idStore.setDataSource(source);
    }

    /**
     * Get the data store used to manage stored IDs.
     * 
     * @return data store used to manage stored IDs
     */
    @Nonnull public JDBCPersistentIdStoreEx getStoredIDStore() {
        return idStore;
    }

    /**
     * Get the SQL query timeout.
     * 
     * @return the timeout in milliseconds
     */
    @NonNegative public long getQueryTimeout() {
        return idStore.getQueryTimeout();
    }

    /**
     * Set the SQL query timeout.
     * 
     * @param timeout the timeout to set in seconds
     */
    public void setQueryTimeout(@Duration @NonNegative final long timeout) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idStore.setQueryTimeout(timeout);
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        idStore.setVerifyDatabase(false);
        idStore.initialize();

        storedIdStrategy.setIDStore(idStore);
        storedIdStrategy.setComputedIdStrategy(getComputedIdStrategy());
        storedIdStrategy.initialize();
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        storedIdStrategy.destroy();
        idStore.destroy();
        
        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final String principalName = resolutionContext.getPrincipal();
        if (Strings.isNullOrEmpty(principalName)) {
            log.warn("{} No principal available, skipping ID creation", getLogPrefix());
            return null;
        }

        final String sourceId = resolveSourceAttribute(workContext);
        if (Strings.isNullOrEmpty(sourceId)) {
            return null;
        }

        final String attributeIssuerID = resolutionContext.getAttributeIssuerID();
        if (Strings.isNullOrEmpty(attributeIssuerID)) {
            log.warn("{} Could not get attribute issuer ID, skipping ID creation", getLogPrefix());
            return null;
        }

        final String attributeRecipientID = resolutionContext.getAttributeRecipientID();
        if (Strings.isNullOrEmpty(attributeRecipientID)) {
            log.warn("{} Could not get attribute recipient ID, skipping ID creation", getLogPrefix());
            return null;
        }

        try {
            return encodeAsAttribute(storedIdStrategy.generate(attributeIssuerID, attributeRecipientID, principalName,
                    sourceId));
        } catch (final SAMLException e) {
            throw new ResolutionException(e);
        }
    }

}