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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import net.shibboleth.idp.saml.nameid.PersistentIdEntry;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC-based storage management for SAML persistent IDs.
 * 
 * <p>The general DDL for the database is:
 * 
 * <pre>
 * CREATE TABLE shibpid (
 *      localEntity VARCHAR(255) NOT NULL,
 *      peerEntity VARCHAR(255) NOT NULL,
 *      persistentId VARCHAR(50) NOT NULL,
 *      principalName VARCHAR(50) NOT NULL,
 *      localId VARCHAR(50) NOT NULL,
 *      peerProvidedId VARCHAR(50) NULL,
 *      creationDate TIMESTAMP NOT NULL,
 *      deactivationDate TIMESTAMP NULL,
 *      PRIMARY KEY (localEntity, peerEntity, persistentId)
 *     );</pre>.
 *    
 * The first three columns should be defined as the primary key of the table, and the other columns
 * should be indexed.</p>
 */
public class JDBCPersistentIdStoreEx extends AbstractInitializableComponent implements PersistentIdStoreEx {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(JDBCPersistentIdStoreEx.class);
    
    /** JDBC data source for retrieving connections. */
    @NonnullAfterInit private DataSource dataSource;

    /** Timeout of SQL queries in milliseconds. */
    @Duration @NonNegative private long queryTimeout;

    /** Number of times to retry a transaction if it rolls back. */
    @NonNegative private int transactionRetry;
    
    /** Error messages that signal a transaction should be retried. */
    @Nonnull @NonnullElements private Collection<String> retryableErrors;

    /** Whether to fail if the database cannot be verified.  */
    private boolean verifyDatabase;
    
    /** Name of the database table. */
    @Nonnull @NotEmpty private String tableName;

    /** Name of the issuer entityID column. */
    @Nonnull @NotEmpty private String issuerColumn;

    /** Name of the recipient entityID column. */
    @Nonnull @NotEmpty private String recipientColumn;

    /** Name of the principal name column. */
    @Nonnull @NotEmpty private String principalNameColumn;

    /** Name of the source ID column. */
    @Nonnull @NotEmpty private String sourceIdColumn;

    /** Name of the persistent ID column. */
    @Nonnull @NotEmpty private String persistentIdColumn;

    /** Name of recipient-attached alias column. */
    @Nonnull @NotEmpty private String peerProvidedIdColumn;

    /** Name of the creation time column. */
    @Nonnull @NotEmpty private String creationTimeColumn;

    /** Name of the deactivation time column. */
    @Nonnull @NotEmpty private String deactivationTimeColumn;

    /** Parameterized select query for lookup by issued value. */
    @NonnullAfterInit private String getByIssuedSelectSQL;

    /** Parameterized select query for lookup by source ID. */
    @NonnullAfterInit private String getBySourceSelectSQL;

    /** Parameterized insert statement used to insert a new record. */
    @NonnullAfterInit private String insertSQL;

    /** Parameterized update statement used to deactivate an ID. */
    @NonnullAfterInit private String deactivateSQL;

    /** Parameterized update statement used to attach an alias to an ID. */
    @NonnullAfterInit private String attachSQL;
    
    /** Parameterized delete statement used to clear dummy rows after verification. */
    @NonnullAfterInit private String deleteSQL;

    /** Constructor. */
    public JDBCPersistentIdStoreEx() {
        transactionRetry = 3;
        retryableErrors = Collections.singletonList("23505");
        queryTimeout = 5000;
        verifyDatabase = true;
        
        tableName = "shibpid";
        issuerColumn = "localEntity";
        recipientColumn = "peerEntity";
        principalNameColumn = "principalName";
        sourceIdColumn = "localId";
        persistentIdColumn = "persistentId";
        peerProvidedIdColumn = "peerProvidedId";
        creationTimeColumn = "creationDate";
        deactivationTimeColumn = "deactivationDate";
    }
    
    /**
     * Get the source datasource used to communicate with the database.
     * 
     * @return the data source;
     */
    @NonnullAfterInit public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get the source datasource used to communicate with the database.
     * 
     * @param source the data source;
     */
    public void setDataSource(@Nonnull final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        dataSource = Constraint.isNotNull(source, "DataSource cannot be null");
    }

    /**
     * Get the SQL query timeout.
     * 
     * @return the timeout in milliseconds
     */
    @NonNegative public long getQueryTimeout() {
        return queryTimeout;
    }
    
    /**
     * Set the SQL query timeout. Defaults to 5000.
     * 
     * @param timeout the timeout to set in milliseconds
     */
    public void setQueryTimeout(@Duration @NonNegative final long timeout) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        queryTimeout = Constraint.isGreaterThanOrEqual(0, timeout, "Timeout must be greater than or equal to 0");
    }

    /**
     * Set the number of retries to attempt for a failed transaction. Defaults to 3.
     * 
     * @param retries the number of retries
     */
    public void setTransactionRetries(@NonNegative final int retries) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        transactionRetry = (int) Constraint.isGreaterThanOrEqual(0, retries,
                "Timeout must be greater than or equal to 0");
    }

    /**
     * Set the error messages to check for classifying a driver error as retryable, generally indicating
     * a lock violation or duplicate insert that signifies a broken database.
     * 
     * @param errors retryable messages
     */
    public void setRetryableErrors(@Nullable @NonnullElements final Collection<String> errors) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        retryableErrors = new ArrayList(StringSupport.normalizeStringCollection(errors));
    }
    
    /**
     * Set whether to allow startup if the database cannot be verified.
     * 
     * <p>Verification consists not only of a liveness check, but the successful insertion of
     * a dummy row, a failure to insert a duplicate, and then deletion of the row.</p>
     * 
     * @param flag flag to set
     */
    public void setVerifyDatabase(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        verifyDatabase = flag;
    }

    /**
     * Set the table name.
     * 
     * @param name table name
     */
    public void setTableName(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        tableName = Constraint.isNotNull(StringSupport.trimOrNull(name), "Table name cannot be null or empty");
    }

    /**
     * Set the name of the issuer entityID column.
     * 
     * @param name name of issuer column
     */
    public void setLocalEntityColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        issuerColumn = Constraint.isNotNull(StringSupport.trimOrNull(name), "Column name cannot be null or empty");
    }

    /**
     * Set the name of the recipient entityID column.
     * 
     * @param name name of recipient column
     */
    public void setPeerEntityColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        recipientColumn = Constraint.isNotNull(StringSupport.trimOrNull(name), "Column name cannot be null or empty");
    }

    /**
     * Set the name of the principal name column.
     * 
     * @param name name of principal name column
     */
    public void setPrincipalNameColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        principalNameColumn = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Column name cannot be null or empty");
    }

    /**
     * Set the name of the source ID column.
     * 
     * @param name name of source ID column
     */
    public void setSourceIdColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sourceIdColumn = Constraint.isNotNull(StringSupport.trimOrNull(name), "Column name cannot be null or empty");
    }

    /**
     * Set the name of the persistent ID column.
     * 
     * @param name name of the persistent ID column
     */
    public void setPersistentIdColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        persistentIdColumn = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Column name cannot be null or empty");
    }

    /**
     * Set the name of the peer-provided ID column.
     * 
     * @param name name of peer-provided ID column
     */
    public void setPeerProvidedIdColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        peerProvidedIdColumn = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Column name cannot be null or empty");
    }

    /**
     * Set the name of the creation time column.
     * 
     * @param name name of creation time column
     */
    public void setCreateTimeColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        creationTimeColumn = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Column name cannot be null or empty");
    }

    /**
     * Set the name of the deactivation time column.
     * 
     * @param name name of deactivation time column
     */
    public void setDeactivationTimeColumn(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        deactivationTimeColumn = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Column name cannot be null or empty");
    }

    /**
     * Set the SELECT statement used to lookup records by issued value.
     * 
     * @param sql statement text, which must contain three parameters (NameQualifier, SPNameQualifier, value)
     */
    public void setGetByIssuedSelectSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        getByIssuedSelectSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql),
                "SQL statement cannot be null or empty");
    }

    /**
     * Set the SELECT statement used to lookup records by source ID.
     * 
     * @param sql statement text, which must contain six parameters
     * (NameQualifier, SPNameQualifier, source ID, NameQualifier, SPNameQualifier, source ID)
     */
    public void setGetBySourceSelectSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        getBySourceSelectSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql),
                "SQL statement cannot be null or empty");
    }

    /**
     * Set the INSERT statement used to insert new records.
     * 
     * @param sql statement text, which must contain 8 parameters
     *  (NameQualifier, SPNameQualifier, value, principal, source ID, SPProvidedID, creation time, deactivation time)
     */
    public void setInsertSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        insertSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql), "SQL statement cannot be null or empty");
    }

    /**
     * Set the UPDATE statement used to deactivate issued values.
     * 
     * @param sql statement text, which must contain four parameters
     *  (deactivation TS, NameQualifier, SPNameQualifier, value)
     */
    public void setDeactivateSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        deactivateSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql), "SQL statement cannot be null or empty");
    }

    /**
     * Set the UPDATE statement used to attach an SPProvidedID to an issued value.
     * 
     * @param sql statement text, which must contain four parameters
     *  (SPProvidedID, NameQualifier, SPNameQualifier, value)
     */
    public void setAttachSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        attachSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql), "SQL statement cannot be null or empty");
    }

    /**
     * Set the DELETE statement used to clear dummy row(s) created during verification.
     * 
     * @param sql statement text, which must contain one parameter (NameQualifier)
     */
    public void setDeleteSQL(@Nonnull @NotEmpty final String sql) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        deleteSQL = Constraint.isNotNull(StringSupport.trimOrNull(sql), "SQL statement cannot be null or empty");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (null == dataSource) {
            throw new ComponentInitializationException(getLogPrefix() + " No database connection provided");
        }
        
        if (getByIssuedSelectSQL == null) {
            getByIssuedSelectSQL = "SELECT * FROM " + tableName + " WHERE " + issuerColumn + "= ? AND "
                    + recipientColumn + "= ? AND " + persistentIdColumn + "= ? AND "
                    + deactivationTimeColumn + " IS NULL";
        }
        
        if (getBySourceSelectSQL == null) {
            getBySourceSelectSQL = "SELECT * FROM " + tableName + " WHERE " + issuerColumn + "= ? AND "
                    + recipientColumn + "= ? AND " + sourceIdColumn + "= ? "
                    + "AND (" + deactivationTimeColumn + " IS NULL OR "
                    + deactivationTimeColumn + " = (SELECT MAX(" + deactivationTimeColumn
                    + ") FROM " + tableName + " WHERE " + issuerColumn + "= ? AND "
                    + recipientColumn + "= ? AND " + sourceIdColumn + "= ?)) ORDER BY "
                    + creationTimeColumn + " DESC";
        }
        
        if (insertSQL == null) {
            insertSQL = "INSERT INTO " + tableName + " ("
                    + issuerColumn + ", "
                    + recipientColumn + ", "
                    + persistentIdColumn + ", "
                    + principalNameColumn + ", "
                    + sourceIdColumn + ", "
                    + peerProvidedIdColumn + ", "
                    + creationTimeColumn + ", "
                    + deactivationTimeColumn
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }
                
        if (deactivateSQL == null) {
            deactivateSQL = "UPDATE " + tableName + " SET " + deactivationTimeColumn + "= ? WHERE "
                    + issuerColumn + "= ? AND " + recipientColumn + "= ? AND " + persistentIdColumn + "= ?";
        }

        if (attachSQL == null) {
            attachSQL = "UPDATE " + tableName + " SET " + peerProvidedIdColumn + "= ? WHERE "
                    + issuerColumn + "= ? AND " + recipientColumn + "= ? AND " + persistentIdColumn + "= ?";
        }
        
        if (deleteSQL == null) {
            deleteSQL = "DELETE FROM " + tableName + " WHERE " + issuerColumn + "= ?";
        }
        
        try {
            verifyDatabase();
            log.info("{} Data source successfully verified", getLogPrefix());
        } catch (final SQLException e) {
            if (verifyDatabase) {
                log.error("{} Exception verifying database", getLogPrefix(), e);
                throw new ComponentInitializationException(
                        "The database was not reachable or was not defined with an appropriate table + primary key");
            } else {
                log.warn("{} The database was not reachable or was not defined with an appropriate table + primary key",
                        getLogPrefix(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public PersistentIdEntry getByIssuedValue(@Nonnull @NotEmpty final String nameQualifier,
            @Nonnull @NotEmpty final String spNameQualifier, @Nonnull @NotEmpty final String persistentId)
                    throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        log.debug("{} Selecting previously issued persistent ID entry", getLogPrefix(), getByIssuedSelectSQL);

        log.trace("{} Prepared statement: {}", getLogPrefix(), getByIssuedSelectSQL);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 1, nameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 2, spNameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 3, persistentId);

        try (final Connection dbConn = getConnection(true)) {
            final PreparedStatement statement = dbConn.prepareStatement(getByIssuedSelectSQL);
            statement.setQueryTimeout((int) (queryTimeout / 1000));

            statement.setString(1, nameQualifier);
            statement.setString(2, spNameQualifier);
            statement.setString(3, persistentId);

            final List<PersistentIdEntry> entries = buildIdentifierEntries(statement.executeQuery());

            if (entries == null || entries.size() == 0) {
                return null;
            }

            if (entries.size() > 1) {
                log.warn("{} More than one record found, only the first will be returned", getLogPrefix());
            }

            return entries.get(0);
        } catch (final SQLException e) {
            throw new IOException(e);
        }
    }

// Checkstyle: MethodLength|CyclomaticComplexity|ParameterNumber OFF
    /** {@inheritDoc} */
    @Override
    @Nullable public PersistentIdEntry getBySourceValue(@Nonnull @NotEmpty final String nameQualifier,
            @Nonnull @NotEmpty final String spNameQualifier, @Nonnull @NotEmpty final String sourceId,
            @Nonnull @NotEmpty final String principal, final boolean allowCreate,
            @Nullable final ComputedPersistentIdGenerationStrategy computedIdStrategy) throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        log.debug("{} Obtaining persistent ID for source ID: {}", getLogPrefix(), sourceId);

        log.trace("{} Prepared statement: {}", getLogPrefix(), getBySourceSelectSQL);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 1, nameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 2, spNameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 3, sourceId);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 4, nameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 5, spNameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 6, sourceId);

        int retries = transactionRetry;
        while (true) {
            try (final Connection dbConn = getConnection(false)) {
                final PreparedStatement statement = dbConn.prepareStatement(getBySourceSelectSQL);
                statement.setQueryTimeout((int) (queryTimeout / 1000));
                statement.setString(1, nameQualifier);
                statement.setString(2, spNameQualifier);
                statement.setString(3, sourceId);
                statement.setString(4, nameQualifier);
                statement.setString(5, spNameQualifier);
                statement.setString(6, sourceId);
        
                log.debug("{} Getting active and/or last inactive persistent Id entry", getLogPrefix());
                final List<PersistentIdEntry> entries = buildIdentifierEntries(statement.executeQuery());
                if (entries != null && entries.size() > 0 && (entries.get(0).getDeactivationTime() == null
                        || entries.get(0).getDeactivationTime().getTime() > System.currentTimeMillis())) {
                    log.debug("{} Returning existing active persistent ID: {}", getLogPrefix(),
                            entries.get(0).getPersistentId());
                    dbConn.commit();
                    return entries.get(0);
                } else if (!allowCreate) {
                    log.debug("{} No existing persistent ID and creation is not permitted", getLogPrefix());
                    dbConn.commit();
                    return null;
                }

                final PersistentIdEntry newEntry = new PersistentIdEntry();
                newEntry.setIssuerEntityId(nameQualifier);
                newEntry.setRecipientEntityId(spNameQualifier);
                newEntry.setSourceId(sourceId);
                newEntry.setPrincipalName(principal);
                newEntry.setCreationTime(new Timestamp(System.currentTimeMillis()));

                if ((entries == null || entries.size() == 0) && computedIdStrategy != null) {
                    log.debug("{} Issuing new computed persistent ID", getLogPrefix());
                    newEntry.setPersistentId(
                            computedIdStrategy.generate(nameQualifier, spNameQualifier, principal, sourceId));
                } else {
                    log.debug("{} Issuing new random persistent ID", getLogPrefix());
                    newEntry.setPersistentId(UUID.randomUUID().toString());
                    if (entries != null && entries.size() > 0) {
                        newEntry.setPeerProvidedId(entries.get(0).getPeerProvidedId());
                    }
                }
                store(newEntry, dbConn);
                dbConn.commit();
                return newEntry;
            } catch (final SQLException e) {
                boolean retry = false;
                for (final String msg : retryableErrors) {
                    if (e.getSQLState().contains(msg)) {
                        log.warn("{} Caught retryable SQL exception", getLogPrefix(), e);
                        retry = true;
                    }
                }
                
                if (retry) {
                    if (--retries < 0) {
                        log.warn("{} Error retryable, but retry limit exceeded", getLogPrefix());
                        throw new IOException(e);
                    } else {
                        log.info("{} Retrying persistent ID lookup/create operation", getLogPrefix());
                    }
                } else {
                    throw new IOException(e);
                }
            } catch (final SAMLException e) {
                throw new IOException(e);
            }
        }
    }
// Checkstyle: MethodLength|CyclomaticComplexity|ParameterNumber ON
    
    /** {@inheritDoc} */
    @Override
    public void deactivate(@Nonnull @NotEmpty final String nameQualifier,
            @Nonnull @NotEmpty final String spNameQualifier, @Nonnull @NotEmpty final String persistentId,
            @Nullable final DateTime deactivation) throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final Timestamp deactivationTime;
        if (deactivation == null) {
            deactivationTime = new Timestamp(System.currentTimeMillis());
        } else {
            deactivationTime = new Timestamp(deactivation.getMillis());
        }

        log.debug("Deactivating persistent id {} as of {}", persistentId, deactivationTime);

        log.trace("{} Prepared statement: {}", getLogPrefix(), deactivateSQL);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 1, deactivationTime);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 2, nameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 3, spNameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 4, persistentId);
        
        try (final Connection dbConn = getConnection(true)) {
            final PreparedStatement statement = dbConn.prepareStatement(deactivateSQL);
            statement.setQueryTimeout((int) (queryTimeout / 1000));
            statement.setTimestamp(1, deactivationTime);
            statement.setString(2, nameQualifier);
            statement.setString(3, spNameQualifier);
            statement.setString(4, persistentId);
            final int rowCount = statement.executeUpdate();
            if (rowCount != 1) {
                log.warn("{} Unexpected result, statement affected {} rows", getLogPrefix(), rowCount);
            }
            
        } catch (final SQLException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void attach(@Nonnull @NotEmpty final String nameQualifier, @Nonnull @NotEmpty final String spNameQualifier,
            @Nonnull @NotEmpty final String persistentId, @Nonnull @NotEmpty final String spProvidedId)
            throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        log.debug("Attaching SPProvidedID {} to persistent id {}", spProvidedId, persistentId);

        log.trace("{} Prepared statement: {}", getLogPrefix(), attachSQL);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 1, spProvidedId);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 2, nameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 3, spNameQualifier);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 4, persistentId);
        
        try (final Connection dbConn = getConnection(true)) {
            final PreparedStatement statement = dbConn.prepareStatement(deactivateSQL);
            statement.setQueryTimeout((int) (queryTimeout / 1000));
            statement.setString(1, spProvidedId);
            statement.setString(2, nameQualifier);
            statement.setString(3, spNameQualifier);
            statement.setString(4, persistentId);
            final int rowCount = statement.executeUpdate();
            if (rowCount != 1) {
                log.warn("{} Unexpected result, statement affected {} rows", getLogPrefix(), rowCount);
            }
        } catch (final SQLException e) {
            throw new IOException(e);
        }
    }
    
// Checkstyle: MethodLength|CyclomaticComplexity|ParameterNumber ON
    
    /**
     * Store a record containing the values from the input object.
     * 
     * @param entry new object to store
     * @param dbConn connection to obtain a statement from.
     * 
     * @throws SQLException if an error occurs
     */
    void store(@Nonnull final PersistentIdEntry entry, @Nonnull final Connection dbConn) throws SQLException {
        
        log.debug("{} Storing new persistent ID entry", getLogPrefix());
        
        if (StringSupport.trimOrNull(entry.getIssuerEntityId()) == null
                || StringSupport.trimOrNull(entry.getRecipientEntityId()) == null
                || StringSupport.trimOrNull(entry.getPersistentId()) == null
                || StringSupport.trimOrNull(entry.getPrincipalName()) == null
                || StringSupport.trimOrNull(entry.getSourceId()) == null
                || entry.getCreationTime() == null) {
            throw new SQLException("Required field was empty/null, store operation not possible");
        }
        
        log.trace("{} Prepared statement: {}", getLogPrefix(), insertSQL);
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 1, entry.getIssuerEntityId());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 2, entry.getRecipientEntityId());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 3, entry.getPersistentId());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 4, entry.getPrincipalName());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 5, entry.getSourceId());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 6, entry.getPeerProvidedId());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 7, entry.getCreationTime());
        log.trace("{} Setting prepared statement parameter {}: {}", getLogPrefix(), 8, entry.getDeactivationTime());
        
        final PreparedStatement statement = dbConn.prepareStatement(insertSQL);
        statement.setQueryTimeout((int) (queryTimeout / 1000));
    
        statement.setString(1, entry.getIssuerEntityId());
        statement.setString(2, entry.getRecipientEntityId());
        statement.setString(3, entry.getPersistentId());
        statement.setString(4, entry.getPrincipalName());
        statement.setString(5, entry.getSourceId());
        if (entry.getPeerProvidedId() != null) {
            statement.setString(6, entry.getPeerProvidedId());
        } else {
            statement.setNull(6, Types.VARCHAR);
        }
        statement.setTimestamp(7, entry.getCreationTime());
        if (entry.getDeactivationTime() != null) {
            statement.setTimestamp(8, entry.getDeactivationTime());
        } else {
            statement.setNull(8, Types.TIMESTAMP);
        }
    
        statement.executeUpdate();
    }

    /**
     * Obtain a connection from the data source.
     * 
     * <p>The caller must close the connection.</p>
     * 
     * @param autoCommit auto-commit setting to apply to the connection
     * 
     * @return a fresh connection
     * @throws SQLException if an error occurs
     */
    @Nonnull private Connection getConnection(final boolean autoCommit) throws SQLException {
        final Connection conn = dataSource.getConnection();
        conn.setAutoCommit(autoCommit);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return conn;
    }
    
    /**
     * Check the database and the presence of a uniqueness constraint.
     * 
     * @throws SQLException if the database cannot be verified 
     */
    private void verifyDatabase() throws SQLException {
        
        final String uuid = UUID.randomUUID().toString();
        
        final PersistentIdEntry newEntry = new PersistentIdEntry();
        newEntry.setIssuerEntityId("http://dummy.com/idp/" + uuid);
        newEntry.setRecipientEntityId("http://dummy.com/sp/" + uuid);
        newEntry.setSourceId("dummy");
        newEntry.setPrincipalName("dummy");
        newEntry.setCreationTime(new Timestamp(System.currentTimeMillis()));
        newEntry.setPersistentId(uuid);
        
        try (final Connection conn = getConnection(true)) {
            store(newEntry, conn);
        } finally {
            
        }

        boolean keyMissing = false;
        try (final Connection conn = getConnection(true)) {
            store(newEntry, conn);
            keyMissing = true;
        } catch (final SQLException e) {
            if (!retryableErrors.contains(e.getSQLState())) {
                log.warn("{} Duplicate insert failed as required with SQL State '{}', ensure this value is "
                        + "configured as a retryable error", getLogPrefix(), e.getSQLState());
            }
        } finally {
            
        }

        try (final Connection conn = getConnection(true)) {
            final PreparedStatement statement = conn.prepareStatement(deleteSQL);
            statement.setQueryTimeout((int) (queryTimeout / 1000));
            statement.setString(1, "http://dummy.com/idp/" + uuid);
            statement.executeUpdate();
        } finally {
            
        }
        
        if (keyMissing) {
            throw new SQLException("Duplicate insertion succeeded, primary key missing from table");
        }
    }
    
    /**
     * Builds a list of {@link PersistentIdEntry}s from a result set.
     * 
     * @param resultSet the result set
     * 
     * @return list of {@link PersistentIdEntry}s
     * 
     * @throws SQLException thrown if there is a problem reading the information from the database
     */
    @Nonnull @NonnullElements @Live private List<PersistentIdEntry> buildIdentifierEntries(
            @Nonnull final ResultSet resultSet) throws SQLException {
        final ArrayList<PersistentIdEntry> entries = new ArrayList<>();
    
        while (resultSet.next()) {
            PersistentIdEntry entry = new PersistentIdEntry();
            entry.setIssuerEntityId(resultSet.getString(issuerColumn));
            entry.setRecipientEntityId(resultSet.getString(recipientColumn));
            entry.setPrincipalName(resultSet.getString(principalNameColumn));
            entry.setPersistentId(resultSet.getString(persistentIdColumn));
            entry.setSourceId(resultSet.getString(sourceIdColumn));
            entry.setPeerProvidedId(resultSet.getString(peerProvidedIdColumn));
            entry.setCreationTime(resultSet.getTimestamp(creationTimeColumn));
            entry.setDeactivationTime(resultSet.getTimestamp(deactivationTimeColumn));
            entries.add(entry);
    
            log.trace("{} Entry {} added to results", getLogPrefix(), entry.toString());
        }
    
        return entries;
    }

    /**
     * Return a string which is to be prepended to all log messages.
     * 
     * @return "Stored Id Store:"
     */
    @Nonnull @NotEmpty private String getLogPrefix() {
        return "Stored Id Store:";
    }
    
}