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

package net.shibboleth.idp.attribute.resolver.dc.ldap.impl;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.attribute.resolver.dc.ValidationException;
import net.shibboleth.idp.attribute.resolver.dc.Validator;

/**
 * Validator implementation that invokes {@link Connection#open()} to determine if the ConnectionFactory is properly
 * configured.
 */
public class ConnectionFactoryValidator implements Validator {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ConnectionFactoryValidator.class);

    /** Connection factory to validate. */
    private final ConnectionFactory connectionFactory;

    /** whether validate should throw, default value is {@value} . */
    private final boolean throwOnValidateError;

    /**
     * Creates a new connection factory validator.
     *
     * @param factory to validate
     */
    public ConnectionFactoryValidator(final ConnectionFactory factory) {
        this(factory, true);
    }

    /**
     * Creates a new connection factory validator.
     * 
     * @param factory to validate
     * @param throwOnError whether {@link #validate()} should throw or log errors
     */
    public ConnectionFactoryValidator(final ConnectionFactory factory, final boolean throwOnError) {
        connectionFactory = factory;
        throwOnValidateError = throwOnError;
    }

    /**
     * Returns the connection factory.
     *
     * @return connection factory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
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
            connection = connectionFactory.getConnection();
            if (connection == null) {
                log.error("Unable to retrieve connections from configured connection factory");
                if (throwOnValidateError) {
                    throw new LdapException("Unable to retrieve connection from connection factory");
                }
            }
            connection.open();
        } catch (final LdapException e) {
            log.error("Connection factory validation failed", e);
            if (throwOnValidateError) {
                throw new ValidationException(e);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
