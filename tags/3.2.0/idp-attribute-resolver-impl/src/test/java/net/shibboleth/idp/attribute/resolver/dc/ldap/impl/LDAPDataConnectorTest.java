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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.ExecutableSearchBuilder;
import net.shibboleth.idp.attribute.resolver.dc.impl.TestCache;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.velocity.VelocityEngine;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * Tests for {@link LDAPDataConnector}
 */
public class LDAPDataConnectorTest extends OpenSAMLInitBaseTestCase {

    /** The connector name. */
    private static final String TEST_CONNECTOR_NAME = "ldapAttributeConnector";

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
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10389));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer
                .importFromLDIF(true,
                        "src/test/resources/data/net/shibboleth/idp/attribute/resolver/impl/dc/ldap/ldapDataConnectorTest.ldif");
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
        ConnectionFactory connectionFactory = new DefaultConnectionFactory("ldap://localhost:10389");
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

    @Test public void initializeAndGetters() throws ComponentInitializationException, ResolutionException {

        LDAPDataConnector connector = new LDAPDataConnector();
        connector.setId(TEST_CONNECTOR_NAME);

        try {
            connector.initialize();
            Assert.fail("No connection factory");
        } catch (ComponentInitializationException e) {
            // OK
        }

        connector.setConnectionFactory(new DefaultConnectionFactory("ldap://localhost:55555"));
        try {
            connector.initialize();
            Assert.fail("No search executor");
        } catch (ComponentInitializationException e) {
            // OK
        }

        SearchExecutor searchExecutor = new SearchExecutor();
        searchExecutor.setBaseDn(TEST_BASE_DN);
        searchExecutor.setReturnAttributes(TEST_RETURN_ATTRIBUTES);
        connector.setSearchExecutor(searchExecutor);
        try {
            connector.initialize();
            Assert.fail("No filter builder");
        } catch (ComponentInitializationException e) {
            // OK
        }

        ExecutableSearchBuilder requestBuilder =
                new ParameterizedExecutableSearchFilterBuilder("(uid={principalName})");
        connector.setExecutableSearchBuilder(requestBuilder);
        try {
            connector.initialize();
            Assert.fail("Invalid Connection Factory");
        } catch (ComponentInitializationException e) {
            // OK
        }

        ConnectionFactory connectionFactory = new DefaultConnectionFactory("ldap://localhost:10389");
        connector.setConnectionFactory(connectionFactory);

        SearchResultMappingStrategy mappingStrategy = new StringAttributeValueMappingStrategy();
        connector.setMappingStrategy(mappingStrategy);

        try {
            connector.resolve(null);
            Assert.fail("Need to initialize first");
        } catch (UninitializedComponentException e) {
            // OK
        }

        connector.initialize();
        try {
            connector.setConnectionFactory(null);
            Assert.fail("Setter after initialize");
        } catch (UnmodifiableComponentException e) {
            // OK
        }
        Assert.assertEquals(connector.getConnectionFactory(), connectionFactory);
        Assert.assertEquals(connector.getSearchExecutor(), searchExecutor);
        Assert.assertEquals(connector.getExecutableSearchBuilder(), requestBuilder);
        Assert.assertEquals(connector.getMappingStrategy(), mappingStrategy);
    }

    @Test public void failFastInitialize() throws ComponentInitializationException {
        LDAPDataConnector connector = new LDAPDataConnector();
        connector.setId(TEST_CONNECTOR_NAME);

        ConnectionFactory connectionFactory = new DefaultConnectionFactory("ldap://localhost:55555");
        connector.setConnectionFactory(connectionFactory);
        connector.setSearchExecutor(new SearchExecutor());
        connector.setExecutableSearchBuilder(new ParameterizedExecutableSearchFilterBuilder("(uid={principalName})"));

        try {
            connector.initialize();
            Assert.fail("No failfast");
        } catch (ComponentInitializationException e) {
            // OK
        }

        connector.setValidator(new ConnectionFactoryValidator(connectionFactory, false));
        connector.initialize();
    }

    @Test public void resolve() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(uid={principalName})");
        resolve(builder);
    }

    @Test public void resolveMulti() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(objectclass=inetOrgPerson)");
        resolveMulti(builder);
    }

    @Test public void resolveWithDepends() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(&(cn={principalName})(eduPersonAffiliation={affiliation[0]}))");
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Map<String, List<IdPAttributeValue<?>>> dependsAttributes = new HashMap<>();
        List<IdPAttributeValue<?>> attributeValues = new ArrayList<>();
        attributeValues.add(new StringAttributeValue("student"));
        dependsAttributes.put("affiliation", attributeValues);
        ExecutableSearchFilter filter = builder.build(context, dependsAttributes);
        Assert.assertEquals(filter.getSearchFilter().format(), "(&(cn=PETER_THE_PRINCIPAL)(eduPersonAffiliation=student))");
    }

    @Test public void resolveWithMultiValueDepends() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder(
                        "(&(cn={principalName})(eduPersonEntitlement={entitlement[0]})(eduPersonEntitlement={entitlement[1]}))");
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Map<String, List<IdPAttributeValue<?>>> dependsAttributes = new HashMap<>();
        List<IdPAttributeValue<?>> attributeValues = new ArrayList<>();
        attributeValues.add(new StringAttributeValue("entitlement1"));
        attributeValues.add(new StringAttributeValue("entitlement*"));
        dependsAttributes.put("entitlement", attributeValues);
        ExecutableSearchFilter filter = builder.build(context, dependsAttributes);
        Assert.assertEquals(
                filter.getSearchFilter().format(),
                "(&(cn=PETER_THE_PRINCIPAL)(eduPersonEntitlement=entitlement1)(eduPersonEntitlement=entitlement\\2a))");
    }

    @Test public void escape() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(cn={principalName})");
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext("domain\\user*", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        ExecutableSearchFilter filter = builder.build(context, null);
        Assert.assertEquals(filter.getSearchFilter().format(), "(cn=domain\\5cuser\\2a)");
    }

    @Test public void resolveTemplate() throws ComponentInitializationException, ResolutionException {
        TemplatedExecutableSearchFilterBuilder builder = new TemplatedExecutableSearchFilterBuilder();
        builder.setTemplateText("(uid=${resolutionContext.principal})");
        builder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        builder.initialize();
        resolve(builder);
    }
    
    @Test(expectedExceptions={ResolutionException.class}) public void resolveTemplateExcept() throws ComponentInitializationException, ResolutionException {
        TemplatedExecutableSearchFilterBuilder builder = new TemplatedExecutableSearchFilterBuilder();
        builder.setTemplateText("(uid=${resolutionContext.AttributeRecipientID.toString().substring(99, 106)})");
        builder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        builder.initialize();
        resolve(builder);
    }


    @Test public void resolveTemplateWithDepends() throws ComponentInitializationException, ResolutionException {
        TemplatedExecutableSearchFilterBuilder builder = new TemplatedExecutableSearchFilterBuilder();
        builder.setTemplateText("(&(cn=${resolutionContext.principal})(eduPersonAffiliation=${affiliation[0]}))");
        builder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Map<String, List<IdPAttributeValue<?>>> dependsAttributes = new HashMap<>();
        List<IdPAttributeValue<?>> attributeValues = new ArrayList<>();
        attributeValues.add(new StringAttributeValue("student"));
        dependsAttributes.put("affiliation", attributeValues);
        ExecutableSearchFilter filter = builder.build(context, dependsAttributes);
        Assert.assertEquals(filter.getSearchFilter().format(), "(&(cn=PETER_THE_PRINCIPAL)(eduPersonAffiliation=student))");
    }

    @Test public void resolveTemplateWithMultiValueDepends() throws ComponentInitializationException, ResolutionException {
        TemplatedExecutableSearchFilterBuilder builder = new TemplatedExecutableSearchFilterBuilder();
        builder.setTemplateText(
                "(&(cn=${resolutionContext.principal})(eduPersonEntitlement=${entitlement[0]})(eduPersonEntitlement=${entitlement[1]}))");
        builder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Map<String, List<IdPAttributeValue<?>>> dependsAttributes = new HashMap<>();
        List<IdPAttributeValue<?>> attributeValues = new ArrayList<>();
        attributeValues.add(new StringAttributeValue("entitlement1"));
        attributeValues.add(new StringAttributeValue("entitlement*"));
        dependsAttributes.put("entitlement", attributeValues);
        ExecutableSearchFilter filter = builder.build(context, dependsAttributes);
        Assert.assertEquals(
                filter.getSearchFilter().format(),
                "(&(cn=PETER_THE_PRINCIPAL)(eduPersonEntitlement=entitlement1)(eduPersonEntitlement=entitlement\\2a))");
    }

    @Test public void escapeTemplate() throws ComponentInitializationException, ResolutionException {
        TemplatedExecutableSearchFilterBuilder builder = new TemplatedExecutableSearchFilterBuilder();
        builder.setTemplateText("(cn=${resolutionContext.principal})");
        builder.setVelocityEngine(VelocityEngine.newVelocityEngine());
        builder.initialize();
        AttributeResolutionContext context =
                TestSources.createResolutionContext("domain\\user*", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        ExecutableSearchFilter filter = builder.build(context, null);
        Assert.assertEquals(filter.getSearchFilter().format(), "(cn=domain\\5cuser\\2a)");
    }

    protected void resolve(ExecutableSearchBuilder builder) throws ComponentInitializationException,
            ResolutionException {
        final LDAPDataConnector connector = createLdapDataConnector(builder, new StringAttributeValueMappingStrategy());
        connector.initialize();

        final AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        final Map<String, IdPAttribute> attrs = connector.resolve(context);
        Assert.assertNotNull(attrs);
        // check total attributes: uid, cn, sn, mail
        Assert.assertTrue(attrs.size() == 4);
        // check uid
        Assert.assertTrue(attrs.get("uid").getValues().size() == 1);
        Assert.assertEquals(new StringAttributeValue(TestSources.PRINCIPAL_ID), attrs.get("uid").getValues().iterator()
                .next());
        // check cn
        Assert.assertTrue(attrs.get("cn").getValues().size() == 3);
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Peter Principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Peter J Principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("pete principal")));
        // check sn
        Assert.assertTrue(attrs.get("sn").getValues().size() == 1);
        Assert.assertEquals(new StringAttributeValue("Principal"), attrs.get("sn").getValues().iterator().next());
        // check mail
        Assert.assertTrue(attrs.get("mail").getValues().size() == 2);
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("peter.principal@shibboleth.net")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("peterprincipal@shibboleth.net")));
    }

    protected void resolveMulti(ExecutableSearchBuilder builder) throws ComponentInitializationException,
            ResolutionException {
        final LDAPDataConnector connector = createLdapDataConnector(builder, new StringAttributeValueMappingStrategy());
        connector.initialize();
        
        final AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        final Map<String, IdPAttribute> attrs = connector.resolve(context);
        Assert.assertNotNull(attrs);
        // check total attributes: uid, cn, sn, mail
        Assert.assertTrue(attrs.size() == 4);
        // check uid
        Assert.assertTrue(attrs.get("uid").getValues().size() == 3);
        Assert.assertTrue(attrs.get("uid").getValues().contains(new StringAttributeValue(TestSources.PRINCIPAL_ID)));
        Assert.assertTrue(attrs.get("uid").getValues().contains(new StringAttributeValue("PAUL_THE_PRINCIPAL")));
        Assert.assertTrue(attrs.get("uid").getValues().contains(new StringAttributeValue("PHILIP_THE_PRINCIPAL")));
        // check cn
        Assert.assertTrue(attrs.get("cn").getValues().size() == 5);
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Peter Principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Peter J Principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("pete principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Paul Principal")));
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Philip Principal")));
        // check sn
        Assert.assertTrue(attrs.get("sn").getValues().size() == 3);
        Assert.assertTrue(attrs.get("sn").getValues().contains(new StringAttributeValue("Principal")));
        // check mail
        Assert.assertTrue(attrs.get("mail").getValues().size() == 8);
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("peter.principal@shibboleth.net")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("peterprincipal@shibboleth.net")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("paul.principal@shibboleth.net")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("paulprincipal@shibboleth.net")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(EmptyAttributeValue.ZERO_LENGTH));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("\"\"")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("  ")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue(" philip.principal@shibboleth.net ")));
    }
    
    @Test(expectedExceptions = ResolutionException.class) public void resolveNoFilter()
            throws ComponentInitializationException, ResolutionException {
        LDAPDataConnector connector = createLdapDataConnector(new ExecutableSearchBuilder<ExecutableSearchFilter>() {

            @Override
            @Nonnull public ExecutableSearchFilter build(@Nonnull AttributeResolutionContext resolutionContext,
                    @Nonnull Map<String, List<IdPAttributeValue<?>>> dependencyAttributes) throws ResolutionException {
                return null;
            }
        }, null);
        connector.initialize();

        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        connector.resolve(context);
    }

    @Test(expectedExceptions = ResolutionException.class) public void resolveNoResultIsError()
            throws ComponentInitializationException, ResolutionException {
        final StringAttributeValueMappingStrategy mappingStrategy = new StringAttributeValueMappingStrategy();
        mappingStrategy.setNoResultAnError(true);
        final LDAPDataConnector connector = createLdapDataConnector(null, mappingStrategy);
        connector.initialize();

        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        try {
            Map<String, IdPAttribute> res = connector.resolve(context);
            Assert.assertNotNull(res);
        } catch (ResolutionException e) {
            Assert.fail("Resolution exception occurred", e);
        }

        context =
                TestSources.createResolutionContext("NOT_A_PRINCIPAL", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        connector.resolve(context);
    }

    @Test(expectedExceptions = ResolutionException.class) public void resolveMultipleResultsIsError()
            throws ComponentInitializationException, ResolutionException {
        final StringAttributeValueMappingStrategy mappingStrategy = new StringAttributeValueMappingStrategy();
        mappingStrategy.setMultipleResultsAnError(true);
        final LDAPDataConnector connector = createLdapDataConnector(new ParameterizedExecutableSearchFilterBuilder("(sn={principalName})"), mappingStrategy);
        connector.initialize();

        AttributeResolutionContext context =
                TestSources.createResolutionContext("NOT_A_PRINCIPAL", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        try {
            Map<String, IdPAttribute> res = connector.resolve(context);
            Assert.assertNull(res);
        } catch (ResolutionException e) {
            Assert.fail("Resolution exception occurred", e);
        }

        context =
                TestSources.createResolutionContext("PRINCIPAL", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        connector.resolve(context);
    }

    @Test public void resolveWithCache() throws ComponentInitializationException, ResolutionException {
        LDAPDataConnector connector = createLdapDataConnector(null, null);
        final TestCache cache = new TestCache();
        connector.setResultsCache(cache);
        connector.initialize();

        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Assert.assertTrue(cache.size() == 0);
        Map<String, IdPAttribute> optional = connector.resolve(context);
        Assert.assertTrue(cache.size() == 1);
        Assert.assertEquals(cache.iterator().next(), optional);
    }
    
    /** See IDP-573. */
    @Test public void resolveEmptyAttribute() throws ComponentInitializationException, ResolutionException {
        ParameterizedExecutableSearchFilterBuilder builder =
                new ParameterizedExecutableSearchFilterBuilder("(uid={principalName})");

        final DataConnector connector = createLdapDataConnector(builder, new StringAttributeValueMappingStrategy());
        connector.initialize();

        final AttributeResolutionContext context =
                TestSources.createResolutionContext("PHILIP_THE_PRINCIPAL", TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        final Map<String, IdPAttribute> attrs = connector.resolve(context);
        Assert.assertNotNull(attrs);
        // check total attributes: uid, cn, sn, mail
        Assert.assertTrue(attrs.size() == 4);
        // check uid
        Assert.assertTrue(attrs.get("uid").getValues().size() == 1);
        Assert.assertEquals(attrs.get("uid").getValues().iterator().next(), new StringAttributeValue(
                "PHILIP_THE_PRINCIPAL"));
        // check cn
        Assert.assertTrue(attrs.get("cn").getValues().size() == 1);
        Assert.assertTrue(attrs.get("cn").getValues().contains(new StringAttributeValue("Philip Principal")));
        // check sn
        Assert.assertTrue(attrs.get("sn").getValues().size() == 1);
        Assert.assertEquals(attrs.get("sn").getValues().iterator().next(), new StringAttributeValue("Principal"));
        // check mail
        Assert.assertTrue(attrs.get("mail").getValues().size() == 4);
        Assert.assertTrue(attrs.get("mail").getValues().contains(new EmptyAttributeValue(EmptyType.ZERO_LENGTH_VALUE)));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("\"\"")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue("  ")));
        Assert.assertTrue(attrs.get("mail").getValues().contains(new StringAttributeValue(" philip.principal@shibboleth.net ")));
    }
    
}