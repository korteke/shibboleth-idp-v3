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

package net.shibboleth.idp.attribute.resolver.spring.dc.rdbms;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.dc.impl.ExecutableSearchBuilder;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.impl.DataSourceValidator;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.impl.ExecutableStatement;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.impl.RDBMSDataConnector;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.impl.StringResultMappingStrategy;
import net.shibboleth.idp.attribute.resolver.spring.dc.rdbms.impl.RDBMSDataConnectorParser;
import net.shibboleth.idp.testing.DatabaseTestingSupport;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.cache.Cache;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/** Test for {@link RDBMSDataConnectorParser}. */
public class RDBMSDataConnectorParserTest {

    private static final String INIT_FILE = "/net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/RdbmsStore.sql";

    private static final String DATA_FILE = "/net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/RdbmsData.sql";

    private DataSource datasource;

    private GenericApplicationContext pendingTeardownContext = null;
    
    @AfterMethod public void tearDownTestContext() {
        if (null == pendingTeardownContext ) {
            return;
        }
        pendingTeardownContext.close();
        pendingTeardownContext = null;
    }
    
    protected void setTestContext(GenericApplicationContext context) {
        tearDownTestContext();
        pendingTeardownContext = context;
    }

    /**
     * Creates an HSQLDB database instance.
     * 
     * @throws ClassNotFoundException if the database driver cannot be found
     * @throws SQLException if the database cannot be initialized
     */
    @BeforeTest public void setupDatabaseServer() throws ClassNotFoundException, SQLException {

        datasource = DatabaseTestingSupport.GetMockDataSource(INIT_FILE, "RDBMSDataConnectorStore");
        DatabaseTestingSupport.InitializeDataSourceFromFile(DATA_FILE, datasource);
    }
    
    @Test public void v2Config() throws Exception {
        RDBMSDataConnector dataConnector =
                getRdbmsDataConnector("net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-v2.xml");
        Assert.assertNotNull(dataConnector);
        doTest(dataConnector);
        final StringResultMappingStrategy mappingStrategy = (StringResultMappingStrategy) dataConnector.getMappingStrategy();
        Assert.assertEquals(mappingStrategy.getResultRenamingMap().size(), 1);
        Assert.assertEquals(mappingStrategy.getResultRenamingMap().get("homephone"), "phonenumber");
    }

    @Test public void hybridConfig() throws Exception {
        RDBMSDataConnector dataConnector =
                getRdbmsDataConnector(
                        "net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-v2-hybrid.xml",
                        "net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-spring-context.xml");
        Assert.assertNotNull(dataConnector);
        doTest(dataConnector);
        final StringResultMappingStrategy mappingStrategy = (StringResultMappingStrategy) dataConnector.getMappingStrategy();
        Assert.assertEquals(mappingStrategy.getResultRenamingMap().size(), 1);
        Assert.assertEquals(mappingStrategy.getResultRenamingMap().get("homephone"), "phonenumber");
    }

    @Test public void v2PropsConfig() throws Exception {
        final Resource props = new ClassPathResource("net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-v2.properties");
        RDBMSDataConnector dataConnector =
                getRdbmsDataConnector(props,
                        "net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-v2-props.xml");
        Assert.assertNotNull(dataConnector);
        doTest(dataConnector);
    }

    @Test public void springConfig() throws Exception {
        RDBMSDataConnector dataConnector =
                getRdbmsDataConnector("net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-spring.xml");
        Assert.assertNotNull(dataConnector);
        doTest(dataConnector);
    }

    @Test public void springPropsConfig() throws Exception {
        final Resource props = new ClassPathResource("net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-v3.properties");

        RDBMSDataConnector dataConnector =
                getRdbmsDataConnector(props, "net/shibboleth/idp/attribute/resolver/spring/dc/rdbms/rdbms-attribute-resolver-spring-props.xml");
        Assert.assertNotNull(dataConnector);
        doTest(dataConnector);
    }

    protected RDBMSDataConnector getRdbmsDataConnector(final String... beanDefinitions) throws IOException {
        return getRdbmsDataConnector(null, beanDefinitions);
    }

    protected RDBMSDataConnector getRdbmsDataConnector(Resource properties, final String... beanDefinitions) throws IOException {
        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + RDBMSDataConnectorParserTest.class);
        
        if (null != properties) {
            ConfigurableEnvironment env = context.getEnvironment();
            env.getPropertySources().replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, new ResourcePropertySource(properties));
            
           env.setPlaceholderPrefix("%{");
           env.setPlaceholderSuffix("}");
        }

        XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(context);

        configReader.loadBeanDefinitions("net/shibboleth/idp/attribute/resolver/spring/externalBeans.xml");
        
        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.setValidating(true);
        beanDefinitionReader.loadBeanDefinitions(beanDefinitions);
        context.refresh();

        return (RDBMSDataConnector) context.getBean("myDatabase");
    }

    protected void doTest(final RDBMSDataConnector dataConnector) throws ResolutionException {

        String id = dataConnector.getId();
        Assert.assertEquals("myDatabase", id);
        Assert.assertEquals(300000, dataConnector.getNoRetryDelay());

        ComboPooledDataSource dataSource = (ComboPooledDataSource) dataConnector.getDataSource();
        Assert.assertNotNull(dataSource);
        Assert.assertEquals("jdbc:hsqldb:mem:RDBMSDataConnectorStore", dataSource.getJdbcUrl());
        Assert.assertEquals("SA", dataSource.getUser());
        Assert.assertEquals(3, dataSource.getAcquireIncrement());
        Assert.assertEquals(24, dataSource.getAcquireRetryAttempts());
        Assert.assertEquals(5000, dataSource.getAcquireRetryDelay());
        Assert.assertEquals(true, dataSource.isBreakAfterAcquireFailure());
        Assert.assertEquals(1, dataSource.getMinPoolSize());
        Assert.assertEquals(5, dataSource.getMaxPoolSize());
        Assert.assertEquals(300, dataSource.getMaxIdleTime());
        Assert.assertEquals(360, dataSource.getIdleConnectionTestPeriod());

        Assert.assertFalse(dataConnector.isConnectionReadOnly());
        final DataSourceValidator validator = (DataSourceValidator) dataConnector.getValidator();
        Assert.assertNotNull(validator);
        Assert.assertTrue(validator.isThrowValidateError());
        Assert.assertNotNull(validator.getDataSource());

        final ExecutableSearchBuilder<ExecutableStatement> searchBuilder = dataConnector.getExecutableSearchBuilder();
        Assert.assertNotNull(searchBuilder);

        final StringResultMappingStrategy mappingStrategy = (StringResultMappingStrategy) dataConnector.getMappingStrategy();
        AssertJUnit.assertNotNull(mappingStrategy);
        AssertJUnit.assertTrue(mappingStrategy.isNoResultAnError());
        AssertJUnit.assertTrue(mappingStrategy.isMultipleResultsAnError());

        final Cache<String, Map<String, IdPAttribute>> resultCache = dataConnector.getResultsCache();
        Assert.assertNotNull(resultCache);
    }
}
