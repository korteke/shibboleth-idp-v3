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

import net.shibboleth.ext.spring.resource.HTTPResource;
import net.shibboleth.ext.spring.resource.ResourceTestHelper;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * This test mirrors the online documentation for the HTTPResource as well as the inline example in services.xml.
 * 
 */
public class HTTPResourceConfigurationTests {
    
    private File theDir = null;
    private GenericApplicationContext theContext = null;
    private GenericApplicationContext globalContext = null;
    
    @BeforeSuite public void setup() throws IOException {
        Path p = Files.createTempDirectory("HTTPResourceTest");
        theDir = p.toFile();
        
        globalContext = new GenericApplicationContext();
        final XmlBeanDefinitionReader globalContextDefinitionReader =
                new XmlBeanDefinitionReader(globalContext);
        
        globalContextDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        globalContextDefinitionReader.loadBeanDefinitions(new ClassPathResource("net/shibboleth/idp/profile/spring/relyingparty/metadata/parent.xml"));
        globalContext.refresh();
        
        theContext = new GenericApplicationContext(globalContext);
        theContext.getBeanFactory().registerSingleton("theDir", theDir);

        final XmlBeanDefinitionReader beanDefinitionReader =
                new XmlBeanDefinitionReader(theContext);
        
        beanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource("net/shibboleth/idp/profile/spring/relyingparty/metadata/HTTPResources.xml"));
        theContext.refresh();
    }
    
    @AfterSuite public void tearDown() {
        if (null != theDir) {
            emptyDir(theDir);
        }
        if (null != theContext) {
            theContext.destroy();
        }
        if (null != globalContext) {
            globalContext.destroy();
        }
    }
    
    private void emptyDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                emptyDir(f);
            }
            f.delete();
        }
    }

    
    private Resource getResource(String beanName) {
        
        return theContext.getBean(beanName, HTTPResource.class);
    }

    private void testResource(Resource r) throws IOException {
        ResourceTestHelper.compare(r, new ClassPathResource("net/shibboleth/idp/profile/spring/relyingparty/metadata/document.xml"));
    }
    
    
    @Test public void basic() throws IOException {
        testResource(getResource("basic"));
    }

    @Test public void inMemory() throws IOException {
        testResource(getResource("inMemory"));
    }

    @Test public void file() throws IOException {
        testResource(getResource("fileResource"));
    }
}
