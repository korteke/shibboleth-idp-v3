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

package net.shibboleth.idp.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

/**
 *
 */
public class DatabaseTestingSupport {

    static Logger log = LoggerFactory.getLogger(DatabaseTestingSupport.class);
    
    public static final String IDP_ENTITY_ID = "https://idp.example.org/idp";

    public static final String PRINCIPAL_ID = "PETER_THE_PRINCIPAL";

    public static final String SP_ENTITY_ID = "https://sp.example.org/sp";
    

    public static void InitializeDataSource(@Nullable String initializingSQLFile, DataSource source) {

        final String sql = ReadSqlFromFile(initializingSQLFile);
        if (sql == null) {
            return;
        }
        ExecuteUpdate(sql, source);
    }

    protected static String ReadSqlFromFile(@Nullable String initializingSQLFile) {

        final String file = StringSupport.trimOrNull(initializingSQLFile);

        if (null == file) {
            return null;
        }

        final InputStream is = DatabaseTestingSupport.class.getResourceAsStream(file);

        if (null == is) {
            log.warn("Could not locate SQL file called {} ", file);
            return null;
        }
        String sql;
        try {
            sql = StringSupport.trimOrNull(CharStreams.toString(new InputStreamReader(is)));
        } catch (IOException e) {
            log.warn("Could not read SQL file called {}.", file);
            return null;
        }

        if (null == sql) {
            log.warn("SQL file called {} was empty.", file);
            return null;
        }

        return sql;
    }

    protected static void ExecuteUpdate(@Nullable String sql, DataSource source) {

        log.debug("Applying SQL: \n {}", sql);

        try {
            Connection dbConn = source.getConnection();
            Statement statement = dbConn.createStatement();

            statement.executeUpdate(sql);
        } catch (SQLException e) {
            log.warn("Could not contact data source {} or execute commands", source, e);
            return;
        }
    }

    /**
     * Summons up an in memory database with the provided identifier. The contents of the resource stream (if any) are
     * then submitted to the database (so as to allow initializing to a known state.
     * 
     * @param initializingSQLFile a file in the classpath with SQL files. For instance
     *            "/data/net/shibboleth/idp/attribute/resolver/impl/dc/StoredIdStore.sql"
     * @param identifier a name to uniquify this database.
     * @return a DataSource which can then be used for testing.
     */
    public static DataSource GetMockDataSource(@Nullable String initializingSQLFile, @Nonnull String identifier) {

        return GetDataSourceFromUrl(initializingSQLFile, "jdbc:hsqldb:mem:" + identifier);
    }

    /**
     * Summons up a database connection to  an hsqldb server running somewhere.
     * @param initializingSQLFile a file in the classpath with SQL files. For instance
     *            "/data/net/shibboleth/idp/attribute/resolver/impl/dc/StoredIdStore.sql"
     * @param server the server name and database name.  For instance "//localhost/testdb"
     * @return a DataSource which can then be used for testing
     */
    public static DataSource GetDataSourceFromHsqlServer(@Nullable String initializingSQLFile, @Nonnull String server) {

        return GetDataSourceFromUrl(initializingSQLFile, "jdbc:hsqldb:hsql:" + server);
    }

    public static void InitializeDataSourceFromFile(String sqlFile, DataSource source) {
        final String sql = ReadSqlFromFile(sqlFile);
        final String[] statements = sql.split(";");
        for (String statement : statements) {
            ExecuteUpdate(statement.trim(), source);
        }
    }

    protected static DataSource GetDataSourceFromUrl(String initializingSQLFile, String JdbcUri) {
        JDBCDataSource jdbcSource = new JDBCDataSource();

        jdbcSource.setUrl(JdbcUri);
        jdbcSource.setUser("SA");
        jdbcSource.setPassword("");

        InitializeDataSource(initializingSQLFile, jdbcSource);

        return jdbcSource;
    }
}
