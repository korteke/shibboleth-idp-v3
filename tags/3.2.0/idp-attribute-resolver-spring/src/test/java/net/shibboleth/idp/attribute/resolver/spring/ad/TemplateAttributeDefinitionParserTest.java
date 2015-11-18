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

package net.shibboleth.idp.attribute.resolver.spring.ad;

import net.shibboleth.idp.attribute.resolver.ad.impl.TemplateAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.CryptoTransientIdAttributeDefinitionParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link CryptoTransientIdAttributeDefinitionParser}
 */
public class TemplateAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    @Test(enabled=false)
    public void noAttr() throws ComponentInitializationException {

        TemplateAttributeDefinition defn = getAttributeDefn("templateNoAttributes.xml", "externalBeans.xml", TemplateAttributeDefinition.class);
        
        Assert.assertEquals(defn.getId(), "templateId");
        Assert.assertNull(defn.getTemplateText());
        Assert.assertTrue(defn.getSourceAttributes().isEmpty());
    }
    
    
    @Test
    public void withAttr() throws ComponentInitializationException {

        try {
            getAttributeDefn("templateAttributes.xml", "externalBeans.xml", TemplateAttributeDefinition.class);
            Assert.fail("should not find bean");
        } catch (BeanCreationException e) {
            // OK
        }
        TemplateAttributeDefinition defn = getAttributeDefn("templateAttributes.xml", "velocity2.xml", TemplateAttributeDefinition.class);
        
        Assert.assertEquals(defn.getId(), "templateIdAttr");
        Assert.assertEquals(defn.getTemplateText(), "TheTemplate");
        Assert.assertEquals(defn.getSourceAttributes().size(), 2);
        Assert.assertTrue(defn.getSourceAttributes().contains("att1"));
        Assert.assertTrue(defn.getSourceAttributes().contains("att2"));
    }

}
