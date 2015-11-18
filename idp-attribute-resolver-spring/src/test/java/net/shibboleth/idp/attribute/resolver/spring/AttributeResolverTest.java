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

package net.shibboleth.idp.attribute.resolver.spring;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.sql.DataSource;

import net.shibboleth.ext.spring.config.IdentifiableBeanPostProcessor;
import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.LegacyPrincipalDecoder;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.idp.testing.DatabaseTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceException;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

/** A work in progress to test the attribute resolver service. */

public class AttributeResolverTest extends OpenSAMLInitBaseTestCase {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeResolverTest.class);

    /* LDAP */
    private InMemoryDirectoryServer directoryServer;

    private static final String LDAP_INIT_FILE =
            "src/test/resources/net/shibboleth/idp/attribute/resolver/spring/ldapDataConnectorTest.ldif";

    /** DataBase initialise */
    private static final String DB_INIT_FILE = "/net/shibboleth/idp/attribute/resolver/spring/RdbmsStore.sql";

    /** DataBase Populate */
    private static final String DB_DATA_FILE = "/net/shibboleth/idp/attribute/resolver/spring/RdbmsData.sql";

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

    @BeforeTest public void setupDataConnectors() throws LDAPException {

        // LDAP
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=shibboleth,dc=net");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10391));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer.importFromLDIF(true, LDAP_INIT_FILE);
        directoryServer.startListening();

        // RDBMS
        datasource = DatabaseTestingSupport.GetMockDataSource(DB_INIT_FILE, "myTestDB");
        DatabaseTestingSupport.InitializeDataSourceFromFile(DB_DATA_FILE, datasource);

    }

    /**
     * Shutdown the in-memory directory server.
     */
    @AfterTest public void teardownDataConnectors() {
        directoryServer.shutDown(true);
    }
    
    private ReloadableService<AttributeResolver> getResolver(String file) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().addBeanPostProcessor(new IdentifiableBeanPostProcessor());
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + AttributeResolverTest.class);

        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.loadBeanDefinitions(file);
        context.refresh();

        return context.getBean(ReloadableService.class);

        
    }

    @Test public void one() throws ComponentInitializationException, ServiceException, ResolutionException {

        final ReloadableService<AttributeResolver> attributeResolverService = getResolver("net/shibboleth/idp/attribute/resolver/spring/service.xml");

        attributeResolverService.initialize();

        ServiceableComponent<AttributeResolver> serviceableComponent = null;
        final AttributeResolutionContext resolutionContext =
                TestSources.createResolutionContext("PETER_THE_PRINCIPAL", "issuer", "recipient");

        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            Assert.assertEquals(resolver.getId(), "Shibboleth.Resolver");
            resolver.resolveAttributes(resolutionContext);
        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }

        Map<String, IdPAttribute> resolvedAttributes = resolutionContext.getResolvedIdPAttributes();
        log.debug("resolved attributes '{}'", resolvedAttributes);

        Assert.assertEquals(resolvedAttributes.size(), 15);

        // Static
        IdPAttribute attribute = resolvedAttributes.get("eduPersonAffiliation");
        Assert.assertNotNull(attribute);
        List<IdPAttributeValue<?>> values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("member")));
        
        // Broken (case 665)
        attribute =  resolvedAttributes.get("broken");
        Assert.assertEquals(attribute.getValues().size(), 3);
        attribute =  resolvedAttributes.get("broken2");
        Assert.assertEquals(attribute.getValues().size(), 3);
        

        // LDAP
        attribute = resolvedAttributes.get("uid");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("PETER_THE_PRINCIPAL")));

        attribute = resolvedAttributes.get("email");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue("peterprincipal@shibboleth.net")));
        Assert.assertTrue(values.contains(new StringAttributeValue("peter.principal@shibboleth.net")));

        attribute = resolvedAttributes.get("surname");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("Principal")));

        attribute = resolvedAttributes.get("commonName");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 3);
        Assert.assertTrue(values.contains(new StringAttributeValue("Peter Principal")));
        Assert.assertTrue(values.contains(new StringAttributeValue("Peter J Principal")));
        Assert.assertTrue(values.contains(new StringAttributeValue("pete principal")));

        attribute = resolvedAttributes.get("homePhone");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("555-111-2222")));

        // Computed
        attribute = resolvedAttributes.get("eduPersonTargetedID");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);

        attribute = resolvedAttributes.get("pagerNumber");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("555-123-4567")));

        attribute = resolvedAttributes.get("mobileNumber");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("444-123-4567")));

        attribute = resolvedAttributes.get("street");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("TheStreet")));

        attribute = resolvedAttributes.get("title");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("Monsieur")));

        attribute = resolvedAttributes.get("departmentNumber");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("#4321")));

        final NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat("urn:mace:shibboleth:1.0:nameIdentifier");
        nameId.setValue("MyHovercraftIsFullOfEels");
        final Subject subject = new Subject();
        subject.getPrincipals().add(new NameIDPrincipal(nameId));

        SubjectCanonicalizationContext ctx = new SubjectCanonicalizationContext();
        ctx.setSubject(subject);
        ctx.setRequesterId("REQ");
        ctx.setResponderId("RES");

        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            LegacyPrincipalDecoder decoder = (LegacyPrincipalDecoder) resolver;
            Assert.assertTrue(decoder.hasValidConnectors());
            Assert.assertEquals(decoder.canonicalize(ctx), "MyHovercraftIsFullOfEels");

        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }

    }
    
    @Test public void mappedTemplate() throws Exception {
        final ReloadableService<AttributeResolver> attributeResolverService = getResolver("net/shibboleth/idp/attribute/resolver/spring/mappedTemplateService.xml");

        attributeResolverService.initialize();

        ServiceableComponent<AttributeResolver> serviceableComponent = null;
        final AttributeResolutionContext resolutionContext =
                TestSources.createResolutionContext("PETER_THE_PRINCIPAL", "issuer", "recipient");

        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            Assert.assertEquals(resolver.getId(), "Shibboleth.Resolver");
            resolver.resolveAttributes(resolutionContext);
        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }

        Map<String, IdPAttribute> resolvedAttributes = resolutionContext.getResolvedIdPAttributes();
        log.debug("output {}", resolvedAttributes);
        Assert.assertEquals(resolvedAttributes.get("testing").getValues().size(), 2);
    }

    @Test public void id() throws ComponentInitializationException, ServiceException, ResolutionException {

        final ReloadableService<AttributeResolver> attributeResolverService = getResolver("net/shibboleth/idp/attribute/resolver/spring/service2.xml");

        attributeResolverService.initialize();

        ServiceableComponent<AttributeResolver> serviceableComponent = null;

        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            Assert.assertEquals(resolver.getId(), "TestID");
        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }

        final NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat("urn:mace:shibboleth:1.0:nameIdentifier");
        nameId.setValue("MyHovercraftIsFullOfEels");
        final Subject subject = new Subject();
        subject.getPrincipals().add(new NameIDPrincipal(nameId));

        SubjectCanonicalizationContext ctx = new SubjectCanonicalizationContext();
        ctx.setSubject(subject);
        ctx.setRequesterId("REQ");
        ctx.setResponderId("RES");

        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            LegacyPrincipalDecoder decoder = (LegacyPrincipalDecoder) resolver;
            Assert.assertFalse(decoder.hasValidConnectors());
            Assert.assertNull(decoder.canonicalize(ctx));

        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }
    }

    @Test public void selective() throws ResolutionException {
        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + AttributeResolverTest.class);

        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(
                "net/shibboleth/idp/attribute/resolver/spring/attribute-resolver-selective.xml"),
                new ClassPathResource("net/shibboleth/idp/attribute/resolver/spring/predicates.xml"));
        context.refresh();

        final AttributeResolver resolver = BaseAttributeDefinitionParserTest.getResolver(context);
        AttributeResolutionContext resolutionContext =
                TestSources.createResolutionContext("PETER", "issuer", "recipient");

        resolver.resolveAttributes(resolutionContext);

        Assert.assertEquals(resolutionContext.getResolvedIdPAttributes().size(), 1);
        Assert.assertNotNull(resolutionContext.getResolvedIdPAttributes().get("EPA1"));

        resolutionContext = TestSources.createResolutionContext("PRINCIPAL", "ISSUER", "recipient");
        resolver.resolveAttributes(resolutionContext);
        Assert.assertEquals(resolutionContext.getResolvedIdPAttributes().size(), 1);
        Assert.assertNotNull(resolutionContext.getResolvedIdPAttributes().get("EPE"));

        resolutionContext = TestSources.createResolutionContext("OTHER", "issuer", "recipient");
        resolver.resolveAttributes(resolutionContext);
        Assert.assertTrue(resolutionContext.getResolvedIdPAttributes().isEmpty());
    }
    
    @Test public void selectiveNavigate() throws ResolutionException {
        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + AttributeResolverTest.class);

        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(
                "net/shibboleth/idp/attribute/resolver/spring/attribute-resolver-selective-navigate.xml"),
                new ClassPathResource("net/shibboleth/idp/attribute/resolver/spring/predicates-navigate.xml"));
        context.refresh();

        final AttributeResolver resolver =  BaseAttributeDefinitionParserTest.getResolver(context);
        AttributeResolutionContext resolutionContext =
                TestSources.createResolutionContext("PETER", "issuer", "recipient");
        resolver.resolveAttributes(resolutionContext);
        // this should fail since navigation failed.
        Assert.assertEquals(resolutionContext.getResolvedIdPAttributes().size(), 0);

        resolutionContext =
                TestSources.createResolutionContext("PETER", "issuer", "recipient");
        // add a child so we can navigate via that
        resolutionContext.getSubcontext(ProfileRequestContext.class, true);
        resolver.resolveAttributes(resolutionContext);
        Assert.assertEquals(resolutionContext.getResolvedIdPAttributes().size(), 1);
        Assert.assertNotNull(resolutionContext.getResolvedIdPAttributes().get("EPA1"));

        resolutionContext = TestSources.createResolutionContext("PRINCIPAL", "ISSUER", "recipient");
        resolutionContext.getSubcontext(ProfileRequestContext.class, true);
        resolver.resolveAttributes(resolutionContext);
        Assert.assertEquals(resolutionContext.getResolvedIdPAttributes().size(), 1);
        Assert.assertNotNull(resolutionContext.getResolvedIdPAttributes().get("EPE"));

        resolutionContext = TestSources.createResolutionContext("OTHER", "issuer", "recipient");
        resolver.resolveAttributes(resolutionContext);
        Assert.assertTrue(resolutionContext.getResolvedIdPAttributes().isEmpty());
    }
    
    @Test public void multiFile() throws ResolutionException {
        final ReloadableService<AttributeResolver> attributeResolverService = getResolver("net/shibboleth/idp/attribute/resolver/spring/multiFileService.xml");
        
        final AttributeResolutionContext resolutionContext =
                TestSources.createResolutionContext("PETER_THE_PRINCIPAL", "issuer", "recipient");

        ServiceableComponent<AttributeResolver> serviceableComponent = null;
        try {
            serviceableComponent = attributeResolverService.getServiceableComponent();

            final AttributeResolver resolver = serviceableComponent.getComponent();
            Assert.assertEquals(resolver.getId(), "MultiFileResolver");
            resolver.resolveAttributes(resolutionContext);
        } finally {
            if (null != serviceableComponent) {
                serviceableComponent.unpinComponent();
            }
        }
        
        Assert.assertNotNull(resolutionContext.getResolvedIdPAttributes().get("eduPersonAffiliation2"));

    }
    
    static class TestPredicate implements Predicate<ProfileRequestContext> {

        private final String value;
        
        private final Function<ProfileRequestContext, String> navigate; 

        public TestPredicate(Function<ProfileRequestContext, String> profileFinder, String compare) {
            value = Constraint.isNotNull(compare, "provided compare name must not be null");
            navigate = Constraint.isNotNull(profileFinder, "provided prinicpal locator must not be null");
        }

        /** {@inheritDoc} */
        @Override public boolean apply(@Nullable ProfileRequestContext input) {
            return value.equals(navigate.apply(input));
        }
    }
}
