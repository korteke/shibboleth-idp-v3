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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.shibboleth.ext.spring.config.DurationToLongConverter;
import net.shibboleth.ext.spring.config.StringToIPRangeConverter;
import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;
import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockPropertySource;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for testing metadata providers.
 */
public class AbstractMetadataParserTest extends OpenSAMLInitBaseTestCase {

    private static final String PATH = "/net/shibboleth/idp/profile/spring/relyingparty/metadata/";

    protected static final String SP_ID = "https://sp.example.org/sp/shibboleth";

    protected static final String IDP_ID = "https://idp.example.org/idp/shibboleth";

    static private String workspaceDirName;

    static private File tempDir;

    static private String tempDirName;
    
    static List<GenericApplicationContext> contexts;

    protected Object parserPool;

    @BeforeSuite public void setupDirs() throws IOException {
        final Path p = Files.createTempDirectory("MetadataProviderTest");
        tempDir = p.toFile();
        tempDirName = tempDir.getAbsolutePath();

        final ClassPathResource resource =
                new ClassPathResource("/net/shibboleth/idp/profile/spring/relyingparty/metadata");
        workspaceDirName = resource.getFile().getAbsolutePath();
        contexts = new ArrayList<>();
    }

    private void emptyDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                emptyDir(f);
            }
            f.delete();
        }
    }
    
    protected void registerContext(GenericApplicationContext context) {
        synchronized(contexts) {
            contexts.add(context);
        }        
    }

    @AfterSuite public void deleteTmpDir() {
        emptyDir(tempDir);
        tempDir.delete();
        tempDir = null;
    }
    
    @AfterSuite public void tearDownContexts() {
        final Iterator<GenericApplicationContext> contextIterator = contexts.iterator(); 
        while (contextIterator.hasNext()) {
            final GenericApplicationContext context;
            synchronized (contexts) {
                context = contextIterator.next();
            }
            context.close();
        }
    }

    /**
     * Set up a property placeholder called DIR which points to the test directory this makes the test location
     * insensitive but able to look at the local filesystem.
     * 
     * @param context the context
     * @throws IOException
     */
    protected void setDirectoryPlaceholder(GenericApplicationContext context) throws IOException {
        PropertySourcesPlaceholderConfigurer placeholderConfig = new PropertySourcesPlaceholderConfigurer();
        placeholderConfig.setPlaceholderPrefix("%{");
        placeholderConfig.setPlaceholderSuffix("}");

        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        MockPropertySource mockEnvVars = new MockPropertySource();
        mockEnvVars.setProperty("DIR", workspaceDirName);
        mockEnvVars.setProperty("TMPDIR", tempDirName);

        propertySources.replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, mockEnvVars);
        placeholderConfig.setPropertySources(propertySources);

        context.addBeanFactoryPostProcessor(placeholderConfig);

    }
    
    protected ApplicationContext getApplicationContext(String contextName, String... files) throws IOException {
        final Resource[] resources = new Resource[files.length];

        for (int i = 0; i < files.length; i++) {
            resources[i] = new ClassPathResource(PATH + files[i]);
        }

        final GenericApplicationContext context = new FilesystemGenericApplicationContext();
        registerContext(context);
        
        setDirectoryPlaceholder(context);

        ConversionServiceFactoryBean service = new ConversionServiceFactoryBean();
        context.setDisplayName("ApplicationContext: " + contextName);
        service.setConverters(new HashSet<>(Arrays.asList(new DurationToLongConverter(), new StringToIPRangeConverter())));
        service.afterPropertiesSet();

        context.getBeanFactory().setConversionService(service.getObject());

        final XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(context);

        configReader.setValidating(true);

        configReader.loadBeanDefinitions(resources);
        context.refresh();
        
        return context;
    }

    protected <T> T getBean(Class<T> claz, String... files) throws IOException {
        ApplicationContext context = getApplicationContext(claz.getCanonicalName(), files);

        if (context.containsBean("shibboleth.ParserPool")) {
            parserPool = context.getBean("shibboleth.ParserPool");
        } else {
            parserPool = null;
        }

        T result = SpringSupport.getBean(context, claz);
        if (result != null) {
            return result;
        }

        RelyingPartyMetadataProvider rpProvider = context.getBean(RelyingPartyMetadataProvider.class);

        return claz.cast(rpProvider.getEmbeddedResolver());
    }

    static public CriteriaSet criteriaFor(String entityId) {
        EntityIdCriterion criterion = new EntityIdCriterion(entityId);
        return new CriteriaSet(criterion);
    }

}
