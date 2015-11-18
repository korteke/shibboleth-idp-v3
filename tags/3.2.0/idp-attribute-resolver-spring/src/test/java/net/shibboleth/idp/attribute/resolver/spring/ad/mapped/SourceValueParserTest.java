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

import net.shibboleth.idp.attribute.resolver.ad.mapped.impl.SourceValue;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.mapped.impl.SourceValueParser;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SourceValueParser}.
 */
public class SourceValueParserTest extends BaseAttributeDefinitionParserTest {

    private SourceValue getSourceValue(String fileName) {

        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + SourceValueParserTest.class);

        return getBean(ATTRIBUTE_FILE_PATH + "mapped/" + fileName, SourceValue.class, context);
    }

    @Test public void simple() {
        SourceValue value = getSourceValue("sourceValue.xml");
        
        Assert.assertFalse(value.isIgnoreCase());
        Assert.assertFalse(value.isPartialMatch());
        try {
            Assert.assertNull(value.getValue());
            Assert.fail();
        } catch (ConstraintViolationException e) {
            
        }
    }
    
    @Test public void values1() {
        SourceValue value = getSourceValue("sourceValueAttributes1.xml");
        
        Assert.assertTrue(value.isIgnoreCase());
        Assert.assertTrue(value.isPartialMatch());
        Assert.assertEquals(value.getValue(), "sourceValueAttributes1");
    }

    @Test public void values2() {
        SourceValue value = getSourceValue("sourceValueAttributes2.xml");
        
        Assert.assertFalse(value.isIgnoreCase());
        Assert.assertFalse(value.isPartialMatch());
        try {
            Assert.assertEquals(value.getValue(), "sourceValueAttributes2");
            Assert.fail();
        } catch (ConstraintViolationException e) {
            
        }
    }
}