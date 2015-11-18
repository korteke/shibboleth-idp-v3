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
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.resolver.dc.impl.ExecutableSearch;

/** A statement that can be executed against a database to fetch results. */
public interface ExecutableStatement extends ExecutableSearch {

    /**
     * Executes the statement and returns the results. This method <strong>MUST NOT</strong> close the given
     * {@link Connection}.
     * 
     * @param connection ready-to-use connection to the database
     * 
     * @return the result of the executed statement
     * 
     * @throws SQLException thrown if there is a problem executing the statement
     */
    @Nonnull public ResultSet execute(@Nonnull Connection connection) throws SQLException;
}