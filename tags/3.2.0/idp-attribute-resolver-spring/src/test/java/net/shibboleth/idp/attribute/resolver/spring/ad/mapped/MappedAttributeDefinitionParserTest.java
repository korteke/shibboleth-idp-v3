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

package net.shibboleth.idp.attribute.resolver.spring.ad.mapped;

import net.shibboleth.idp.attribute.resolver.ad.mapped.impl.MappedAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.mapped.impl.MappedAttributeDefinitionParser;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link MappedAttributeDefinitionParser}.
 */
public class MappedAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    private MappedAttributeDefinition getDefinition(String fileName) {
        return getAttributeDefn("mapped/" + fileName, MappedAttributeDefinition.class);
    }

    @Test public void defaultCase() {
        MappedAttributeDefinition defn = getDefinition("mapped.xml");

        Assert.assertTrue(defn.isPassThru());
        Assert.assertEquals(defn.getValueMaps().size(), 2);
        Assert.assertEquals(defn.getDefaultAttributeValue().getValue(), "foobar");
    }

    @Test public void noDefault() {
        MappedAttributeDefinition defn = getDefinition("mappedNoDefault.xml");

        Assert.assertFalse(defn.isPassThru());
        Assert.assertEquals(defn.getValueMaps().size(), 1);
        Assert.assertNull(defn.getDefaultAttributeValue());
    }

    @Test public void noValues() {

        try {
            getDefinition("mappedNoValueMap.xml");
            Assert.fail();
        } catch (BeanDefinitionStoreException e) {
            // OK
        }
    }
}
