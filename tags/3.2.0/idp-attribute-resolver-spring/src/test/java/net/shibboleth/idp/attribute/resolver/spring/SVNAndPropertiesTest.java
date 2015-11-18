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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.ext.spring.resource.SVNBasicAuthenticationManager;
import net.shibboleth.ext.spring.resource.SVNResource;
import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * Test for an SVN resource with replaced properties
 */
public class SVNAndPropertiesTest extends OpenSAMLInitBaseTestCase {

    private static final String[] EXPECTED_EPA = {"member", "student", "faculty",};

    private static final String[] EXPECTED_UID = {"5ADF5AE5-15DE-481F-A6B0-6C50A423F295",};

    private static final String[] EXPECTED_EPE = {"urn:example.org:entitlement:entitlement1",
            "urn:mace:dir:entitlement:common-lib-terms",};

    private File theDir;

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

    protected void assertEquals(IdPAttributeValue<?> value, String[] expected) {
        if (value instanceof StringAttributeValue) {
            StringAttributeValue stringValue = (StringAttributeValue) value;

            for (String s : expected) {
                if (s.equals(stringValue.getValue())) {
                    // OK
                    return;
                }
            }
            Assert.fail("Attribute value " + stringValue.getValue() + " did not match any of the expected "
                    + expected.toString());
        } else {
            Assert.fail("Attribute value was not of a string");
        }
    }
    
    @BeforeMethod public void makeDir() throws IOException {
        Path p = Files.createTempDirectory("SVNResourceTest");
        theDir = p.toFile();
    }

    private void emptyDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                emptyDir(f);
            }
            f.delete();
        }
    }

    @AfterMethod public void emptyDir() {
        emptyDir(theDir);
        theDir.delete();
        theDir = null;
    }

    @Test public void attributesTest() throws ResolutionException, SVNException {
        final GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + AttributeResolverTest.class);

        final SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        final ISVNAuthenticationManager authnManager = new SVNBasicAuthenticationManager(Collections.EMPTY_LIST);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        clientManager.setAuthenticationManager(authnManager);

        SVNURL url =  SVNURL.create( "https", null, "svn.shibboleth.net",
                        -1,
                        "/java-identity-provider/trunk/idp-attribute-resolver-spring/src/test/resources/net/shibboleth/idp/attribute/resolver/spring/",
                        false);

        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource(
                "net/shibboleth/idp/attribute/resolver/spring/PropertyPlaceholder.xml"), 
                new SVNResource(clientManager, url, theDir, -1, "attribute-resolver-svn.xml"));

        context.refresh();

        final AttributeResolver resolver = BaseAttributeDefinitionParserTest.getResolver(context);

        AttributeResolutionContext attributeContext = new AttributeResolutionContext();
        resolver.resolveAttributes(attributeContext);

        final Map<String, IdPAttribute> attributes = attributeContext.getResolvedIdPAttributes();

        Assert.assertEquals(attributes.size(), 3);

        IdPAttribute attribute = attributes.get("eduPersonAffiliation");
        Assert.assertNotNull(attribute);
        List<IdPAttributeValue<?>> values = attribute.getValues();
        Assert.assertEquals(values.size(), 3);
        for (IdPAttributeValue<?> value : values) {
            assertEquals(value, EXPECTED_EPA);
        }

        attribute = attributes.get("uid");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 1);
        for (IdPAttributeValue<?> value : values) {
            assertEquals(value, EXPECTED_UID);
        }

        attribute = attributes.get("eduPersonEntitlement");
        Assert.assertNotNull(attribute);
        values = attribute.getValues();
        Assert.assertEquals(values.size(), 2);
        for (IdPAttributeValue<?> value : values) {
            assertEquals(value, EXPECTED_EPE);
        }
    }
}
