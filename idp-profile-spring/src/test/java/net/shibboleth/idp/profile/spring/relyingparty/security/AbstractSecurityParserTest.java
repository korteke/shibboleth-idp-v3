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

package net.shibboleth.idp.profile.spring.relyingparty.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import net.shibboleth.ext.spring.config.DurationToLongConverter;
import net.shibboleth.ext.spring.config.StringToIPRangeConverter;
import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockPropertySource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Base mechanics for Security parser tests
 */
public class AbstractSecurityParserTest {

    private static final String PATH = "/net/shibboleth/idp/profile/spring/relyingparty/security/";
    
    protected static final String SP_ID = "https://sp.example.org/sp/shibboleth"; 
    protected static final String IDP_ID = "https://idp.example.org/idp/shibboleth";
    
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
        final ClassPathResource resource = new ClassPathResource(PATH);
        workspaceDirName = resource.getFile().getAbsolutePath();
    }

    /**
     * Set up a property placeholder called DIR which points to the test directory
     * this makes the test location insensitive but able to look at the local
     * filesystem.
     * @param context the context
     * @throws IOException 
     */
    protected void setDirectoryPlaceholder(GenericApplicationContext context) throws IOException {
        PropertySourcesPlaceholderConfigurer placeholderConfig = new PropertySourcesPlaceholderConfigurer();
        
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        MockPropertySource mockEnvVars = new MockPropertySource();
        mockEnvVars.setProperty("DIR", workspaceDirName);
        
        propertySources.replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, mockEnvVars);
        placeholderConfig.setPropertySources(propertySources);
        
        context.addBeanFactoryPostProcessor(placeholderConfig);
    }
    
    protected <T> T getBean(Class<T> claz,  boolean validating, String... files) throws IOException{
        final Resource[] resources = new Resource[files.length];
       
        for (int i = 0; i < files.length; i++) {
            resources[i] = new ClassPathResource(PATH + files[i]);
        }
        
        final GenericApplicationContext context = new FilesystemGenericApplicationContext();
        setTestContext(context);
        setDirectoryPlaceholder(context);
        
        ConversionServiceFactoryBean service = new ConversionServiceFactoryBean();
        context.setDisplayName("ApplicationContext: " + claz);
        service.setConverters(new HashSet<>(Arrays.asList(new DurationToLongConverter(), new StringToIPRangeConverter())));
        service.afterPropertiesSet();

        context.getBeanFactory().setConversionService(service.getObject());

        final XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(context);


        configReader.setValidating(true);
        
        configReader.loadBeanDefinitions(resources);
        context.refresh();
        
        return context.getBean(claz);
    }

}