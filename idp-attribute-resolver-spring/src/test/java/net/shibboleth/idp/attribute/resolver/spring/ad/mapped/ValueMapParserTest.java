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

import net.shibboleth.idp.attribute.resolver.ad.mapped.impl.ValueMap;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.mapped.impl.ValueMapParser;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link ValueMapParser}.
 */
public class ValueMapParserTest extends BaseAttributeDefinitionParserTest {

    private ValueMap getValueMap(String fileName) {

        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + ValueMapParserTest.class);

        return getBean(ATTRIBUTE_FILE_PATH + "mapped/" + fileName, ValueMap.class, context);
    }

    @Test public void valueMap() {
        
        ValueMap value = getValueMap("valueMap.xml");
        Assert.assertEquals(value.getReturnValue(), "return");
        Assert.assertEquals(value.getSourceValues().size(), 1);
        Assert.assertEquals(value.getSourceValues().iterator().next().getPattern().pattern(), "source");
    }
    
    @Test public void noSourceValues() {
        
        try {
            getValueMap("valueMapNoSourceValue.xml");
            Assert.fail();
        } catch (BeanDefinitionStoreException e) {
            // OK
        }
    }
    
    @Test public void noValues() {
        
        try {
            getValueMap("valueMapNoValues.xml");
            Assert.fail();
        } catch (BeanDefinitionStoreException e) {
            // OK
        }
    }
 }
