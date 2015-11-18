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

import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.ExecutableSearchBuilder;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

/**
 *
 */
public class Regressions {
    
    /** The connector name. */
    private static final String TEST_CONNECTOR_NAME = "ldapRegressionsAttributeConnector";

    /** Base DN defined in LDIF. */
    private static final String TEST_BASE_DN = "ou=people,dc=shibboleth,dc=net";
    
    /** Default search attributes for entry in LDIF. */
    private static final String[] TEST_RETURN_ATTRIBUTES = new String[] {"cn", "sn", "uid", "mail"};

    
    /** In-memory directory server. */
    private InMemoryDirectoryServer directoryServer;

    
    /**
     * Creates an UnboundID in-memory directory server. Leverages LDIF found in test resources.
     * 
     * @throws LDAPException if the in-memory directory server cannot be created
     */
    @BeforeTest public void setupDirectoryServer() throws LDAPException {

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=shibboleth,dc=net");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10390));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer
                .importFromLDIF(true,
                        "src/test/resources/data/net/shibboleth/idp/attribute/resolver/impl/dc/ldap/ldapDataConnectorTestEmpty.ldif");
        directoryServer.startListening();
    }

    /**
     * Shutdown the in-memory directory server.
     */
    @AfterTest public void teardownDirectoryServer() {
        directoryServer.shutDown(true);
    }

    /**
     * Creates an LDAP data connector using the supplied builder and strategy. Sets defaults values if the parameters
     * are null.
     * 
     * @param builder to build search requests
     * @param strategy to map search results
     * @return ldap data connector
     */
    protected LDAPDataConnector createLdapDataConnector(ExecutableSearchBuilder builder,
            SearchResultMappingStrategy strategy) {
        LDAPDataConnector connector = new LDAPDataConnector();
        connector.setId(TEST_CONNECTOR_NAME);
        ConnectionFactory connectionFactory = new DefaultConnectionFactory("ldap://localhost:10390");
        connector.setConnectionFactory(connectionFactory);
        SearchExecutor searchExecutor = new SearchExecutor();
        searchExecutor.setBaseDn(TEST_BASE_DN);
        searchExecutor.setReturnAttributes(TEST_RETURN_ATTRIBUTES);
        connector.setSearchExecutor(searchExecutor);
        connector.setExecutableSearchBuilder(builder == null ? new ParameterizedExecutableSearchFilterBuilder(
                "(uid={principalName})") : builder);
        connector.setValidator(new ConnectionFactoryValidator(connectionFactory));
        connector.setMappingStrategy(strategy == null ? new StringAttributeValueMappingStrategy() : strategy);
        return connector;
    }

    @Test public void idP573() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(uid={principalName})");

        final DataConnector connector = createLdapDataConnector(builder, new StringAttributeValueMappingStrategy());
        connector.initialize();

        final AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        final Map<String, IdPAttribute> attrs = connector.resolve(context);
        final List<IdPAttributeValue<?>> values = attrs.get("mail").getValues();
        Assert.assertEquals(values.size(), 4);
        Assert.assertTrue(values.contains(new EmptyAttributeValue(EmptyType.ZERO_LENGTH_VALUE)));
    }

}
