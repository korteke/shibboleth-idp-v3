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

package net.shibboleth.idp.profile.spring.relyingparty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import net.shibboleth.ext.spring.config.DurationToLongConverter;
import net.shibboleth.ext.spring.config.StringToIPRangeConverter;
import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataParserTest;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.idp.relyingparty.impl.DefaultRelyingPartyConfigurationResolver;
import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;
import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.idp.saml.saml1.profile.config.ArtifactResolutionProfileConfiguration;
import net.shibboleth.idp.saml.saml1.profile.config.AttributeQueryProfileConfiguration;
import net.shibboleth.idp.saml.saml1.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.config.ECPProfileConfiguration;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.security.trust.impl.ChainingTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ChainingSignatureTrustEngine;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockPropertySource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Test for a complete example RelyingParty file
 */
public class RelyingPartyGroupTest extends OpenSAMLInitBaseTestCase {

    private static final String PATH = "/net/shibboleth/idp/profile/spring/relyingparty/";

    static private String workspaceDirName;

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

    @BeforeSuite public void setupDirs() throws IOException {
        final ClassPathResource resource = new ClassPathResource("/net/shibboleth/idp/profile/spring/relyingparty");
        workspaceDirName = resource.getFile().getAbsolutePath();
    }

    private GenericApplicationContext getContext(String... files) throws FileNotFoundException, IOException {
        final Resource[] resources = new Resource[files.length];

        for (int i = 0; i < files.length; i++) {
            resources[i] = new ClassPathResource(PATH + files[i]);
        }

        final GenericApplicationContext context = new FilesystemGenericApplicationContext();
        setTestContext(context);
        ConversionServiceFactoryBean service = new ConversionServiceFactoryBean();
        context.setDisplayName("ApplicationContext");
        service.setConverters(new HashSet<>(Arrays.asList(new DurationToLongConverter(), new StringToIPRangeConverter())));
        service.afterPropertiesSet();

        context.getBeanFactory().setConversionService(service.getObject());

        PropertySourcesPlaceholderConfigurer placeholderConfig = new PropertySourcesPlaceholderConfigurer();

        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        Properties fileProps = new Properties();
        fileProps.load(new FileInputStream(new File(workspaceDirName + "/file.properties")));

        MockPropertySource mockEnvVars = new MockPropertySource(fileProps);

        mockEnvVars.setProperty("DIR", workspaceDirName);
        propertySources.replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, mockEnvVars);
        placeholderConfig.setPropertySources(propertySources);

        context.addBeanFactoryPostProcessor(placeholderConfig);

        final XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(context);

        configReader.setValidating(true);

        configReader.loadBeanDefinitions(resources);
        context.refresh();

        return context;
    }

    @Test(enabled = true) public void relyingPartyConfig() throws ResolverException, FileNotFoundException, IOException {
        GenericApplicationContext context = getContext("beans.xml", "relying-party-group.xml");

        DefaultRelyingPartyConfigurationResolver resolver =
                context.getBean(DefaultRelyingPartyConfigurationResolver.class);
        Assert.assertTrue(resolver.getRelyingPartyConfigurations().isEmpty());

        RelyingPartyConfiguration anon = resolver.getUnverifiedConfiguration();
        Assert.assertFalse(anon.isDetailedErrors());
        Assert.assertTrue(anon.getProfileConfigurations().isEmpty());

        RelyingPartyConfiguration def = resolver.getDefaultConfiguration();
        Assert.assertEquals(def.getProfileConfigurations().size(), 8);

        ProfileRequestContext ctx = new ProfileRequestContext<>();
        RelyingPartyContext rpCtx = ctx.getSubcontext(RelyingPartyContext.class, true);
        rpCtx.setRelyingPartyId("https://idp.example.org");
        final HashSet<RelyingPartyConfiguration> set = new HashSet<>();
        for (final RelyingPartyConfiguration rpc : resolver.resolve(ctx)) {
            set.add(rpc);
        }
        Assert.assertEquals(set.size(), 1);

        Assert.assertNotNull(resolver.resolveSingle(ctx));
    }

    @Test(enabled = true) public void metadataConfig() throws ResolverException, FileNotFoundException, IOException {
        GenericApplicationContext context = getContext("beans.xml", "relying-party-group.xml");
        final Collection<RelyingPartyMetadataProvider> metadataProviders =
                context.getBeansOfType(RelyingPartyMetadataProvider.class).values();

        Assert.assertEquals(metadataProviders.size(), 1);
        RelyingPartyMetadataProvider provider = metadataProviders.iterator().next();

        Assert.assertNotNull(provider.resolveSingle(AbstractMetadataParserTest.criteriaFor("http://sp.example.org/")));

    }

    @Test(enabled = true) public void relyingPartyService() throws ResolverException, FileNotFoundException,
            IOException {
        GenericApplicationContext context = getContext("beans.xml", "services.xml");

        RelyingPartyConfigurationResolver resolver = context.getBean(RelyingPartyConfigurationResolver.class);
        ProfileRequestContext ctx = new ProfileRequestContext<>();
        RelyingPartyContext rpCtx = ctx.getSubcontext(RelyingPartyContext.class, true);
        rpCtx.setRelyingPartyId("https://idp.example.org");
        final HashSet<RelyingPartyConfiguration> set = new HashSet<>();
        for (final RelyingPartyConfiguration rpc : resolver.resolve(ctx)) {
            set.add(rpc);
        }
        Assert.assertEquals(set.size(), 1);

        Assert.assertNotNull(resolver.resolveSingle(ctx));
    }

    @Test(enabled = true) public void metadataService() throws ResolverException, FileNotFoundException, IOException {
        GenericApplicationContext context = getContext("beans.xml", "services.xml");
        final Collection<MetadataResolver> resolvers = context.getBeansOfType(MetadataResolver.class).values();

        Assert.assertEquals(resolvers.size(), 1);

        Assert.assertNotNull(resolvers.iterator().next()
                .resolveSingle(AbstractMetadataParserTest.criteriaFor("http://sp.example.org/")));

    }

    @Test public void relyingParty2() throws FileNotFoundException, IOException {
        GenericApplicationContext context = getContext("relying-party-group2.xml", "beans.xml");
        DefaultRelyingPartyConfigurationResolver resolver =
                context.getBean(DefaultRelyingPartyConfigurationResolver.class);

        Assert.assertEquals(resolver.getId(), "RelyingPartyGroup[relying-party-group2.xml]");

        final List<RelyingPartyConfiguration> rps = resolver.getRelyingPartyConfigurations();
        Assert.assertEquals(rps.size(), 2);

        RelyingPartyConfiguration rp = rps.get(0);
        Assert.assertEquals(rp.getId(), "the id1");
        Assert.assertEquals(rp.getResponderId(), "IdP1");

        rp = rps.get(1);
        Assert.assertEquals(rp.getId(), "the id2");
        Assert.assertEquals(rp.getResponderId(), "IdP2");

        rp = resolver.getUnverifiedConfiguration();
        Assert.assertEquals(rp.getResponderId(), "AnonIdP");
        Assert.assertEquals(rp.getId(), "AnonymousRelyingParty");

        rp = resolver.getDefaultConfiguration();
        Assert.assertEquals(rp.getResponderId(), "DefaultIdP");
        Assert.assertEquals(rp.getId(), "DefaultRelyingParty");
    }

    @Test public void defaults() throws ResolverException, FileNotFoundException, IOException {
        GenericApplicationContext context = getContext("beans.xml", "relying-party-group.xml");

        DefaultRelyingPartyConfigurationResolver resolver =
                context.getBean(DefaultRelyingPartyConfigurationResolver.class);

        final SecurityConfiguration config = resolver.getDefaultSecurityConfiguration(null);
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getSignatureValidationConfiguration().getSignatureTrustEngine() instanceof ChainingSignatureTrustEngine);
        Assert.assertTrue(config.getClientTLSValidationConfiguration().getX509TrustEngine() instanceof ChainingTrustEngine);
        
        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration(ArtifactResolutionProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration(AttributeQueryProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration(BrowserSSOProfileConfiguration.PROFILE_ID));

        Assert.assertSame(config, resolver
                .getDefaultSecurityConfiguration(net.shibboleth.idp.saml.saml2.profile.config.ArtifactResolutionProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver
                .getDefaultSecurityConfiguration(net.shibboleth.idp.saml.saml2.profile.config.AttributeQueryProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver
                .getDefaultSecurityConfiguration(net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration(ECPProfileConfiguration.PROFILE_ID));
        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration(SSOSProfileConfiguration.PROFILE_ID));

        Assert.assertSame(config, resolver.getDefaultSecurityConfiguration("foobar"));
    }
}