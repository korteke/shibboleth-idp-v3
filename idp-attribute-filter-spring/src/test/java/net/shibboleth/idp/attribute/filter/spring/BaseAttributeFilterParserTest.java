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

package net.shibboleth.idp.attribute.filter.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;
import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.SAMLAttributeDataConnector;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

import com.google.common.base.Function;

/**
 * Basis of all the matcher and rule parser tests.
 */
public class BaseAttributeFilterParserTest extends XMLObjectBaseTestCase {

    private static final String ATTRIBUTE_PATH = "/net/shibboleth/idp/attribute/filter/attribute/";

    protected static final String MATCHER_PATH = "/net/shibboleth/idp/attribute/filter/matcher/";

    protected static final String POLICY_RULE_PATH = "/net/shibboleth/idp/attribute/filter/policyrule/";
    
    private GenericApplicationContext pendingTeardownContext = null;
    
    @AfterMethod public void tearDownTestContext() {
        if (null == pendingTeardownContext ) {
            return;
        }
        pendingTeardownContext .close();
        pendingTeardownContext  = null;
    }
    
    protected void setTestContext(GenericApplicationContext context) {
        tearDownTestContext();
        pendingTeardownContext = context;
    }

    /**
     * Helper function to return attributes pulled from a file (on the classpath). The file is expected to contain a
     * single <mdattr:EntityAttributes/> statement.
     * 
     * @param xmlFileName the file within the test directory.
     * @return the att
     * @throws ComponentInitializationException
     * @throws ResolutionException
     */
    protected Map<String, IdPAttribute> getAttributes(String xmlFileName) throws ComponentInitializationException,
            ResolutionException {

        final EntityAttributes obj = (EntityAttributes) unmarshallElement(ATTRIBUTE_PATH + xmlFileName);

        SAMLAttributeDataConnector connector = new SAMLAttributeDataConnector();
        connector.setId(xmlFileName);
        connector.setAttributesStrategy(new Function<AttributeResolutionContext, List<Attribute>>() {
            @Override @Nullable public List<Attribute> apply(@Nullable AttributeResolutionContext input) {
                return obj.getAttributes();
            }
        });

        connector.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        return connector.resolve(context);
    }
    
    protected <Type> Type getBean(Class<Type> claz, GenericApplicationContext context) {
        Collection<Type> beans = context.getBeansOfType(claz).values();
        Assert.assertEquals(beans.size(), 1);

        return beans.iterator().next();
    }

    protected <Type> Type getBean(String fileName, Class<Type> claz, GenericApplicationContext context) {

        SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                new SchemaTypeAwareXMLBeanDefinitionReader(context);

        beanDefinitionReader.loadBeanDefinitions(fileName, MATCHER_PATH + "customBean.xml");

        context.refresh();
        return getBean(claz, context);
    }

    protected PolicyRequirementRule getPolicyRule(String fileName, boolean isAfp) throws ComponentInitializationException {

        GenericApplicationContext context = new FilesystemGenericApplicationContext();
        context.setDisplayName("ApplicationContext: Policy Rule");

        setTestContext(context);
        final String path = POLICY_RULE_PATH + (isAfp?"afp/":"") + fileName; 
        final AttributeFilterPolicy policy =
                getBean(path, AttributeFilterPolicy.class, context);

        policy.initialize();
        return policy.getPolicyRequirementRule();
    }

    protected Matcher getMatcher(String fileName, boolean isAfp) throws ComponentInitializationException {

        GenericApplicationContext context = new FilesystemGenericApplicationContext();
        context.setDisplayName("ApplicationContext: Matcher");
        setTestContext(context);
        
        final String path = MATCHER_PATH + (isAfp?"afp/":"") + fileName; 

        final AttributeRule rule = getBean(path, AttributeRule.class, context);

        rule.initialize();
        return rule.getMatcher();

    }

}