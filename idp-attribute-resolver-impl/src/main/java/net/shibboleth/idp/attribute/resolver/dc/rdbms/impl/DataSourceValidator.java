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

package net.shibboleth.idp.attribute.resolver.dc.rdbms.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.attribute.resolver.dc.ValidationException;
import net.shibboleth.idp.attribute.resolver.dc.Validator;

/**
 * Validator implementation that invokes {@link DataSource#getConnection()} to determine if the DataSource is properly
 * configured.
 */
public class DataSourceValidator implements Validator {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DataSourceValidator.class);

    /** JDBC data source to validate. */
    private final DataSource dataSource;

    /** whether validate should throw, default value is {@value} . */
    private final boolean throwOnValidateError;

    /**
     * Creates a new DataSource validator.
     * 
     * @param source to validate
     */
    public DataSourceValidator(final DataSource source) {
        this(source, true);
    }

    /**
     * Creates a new DataSource validator.
     * 
     * @param source to validate
     * @param throwOnError whether {@link #validate()} should throw or log errors
     */
    public DataSourceValidator(final DataSource source, final boolean throwOnError) {
        dataSource = source;
        throwOnValidateError = throwOnError;
    }

    /**
     * Returns the data source.
     *
     * @return data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Returns whether {@link #validate()} should throw or log errors.
     *
     * @return whether {@link #validate()} should throw or log errors
     */
    public boolean isThrowValidateError() {
        return throwOnValidateError;
    }

    /** {@inheritDoc} */
    @Override public void validate() throws ValidationException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                log.error("Unable to retrieve connections from configured data source");
                if (throwOnValidateError) {
                    throw new ValidationException("Unable to retrieve connections from configured data source");
                }
            }
        } catch (final SQLException e) {
            if (e.getSQLState() != null) {
                log.error("Datasource validation failed with SQL state: {}, SQL Code: {}",
                        new Object[] {e.getSQLState(), e.getErrorCode(), e});
            } else {
                log.error("Datasource validation failed", e);
            }
            if (throwOnValidateError) {
                throw new ValidationException("Invalid connector configuration", e);
            }
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing database connection; SQL State: {}, SQL Code: {}",
                        new Object[] {e.getSQLState(), e.getErrorCode(), e});
            }
        }
    }
}
