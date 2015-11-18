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

import java.util.regex.Pattern;

import net.shibboleth.idp.attribute.resolver.ad.impl.RegexSplitAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.PrescopedAttributeDefinitionParser;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link PrescopedAttributeDefinitionParser}.
 */
public class RegexSplitAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void defaultCase() {
        RegexSplitAttributeDefinition attrDef =
                getAttributeDefn("regexDefault.xml", RegexSplitAttributeDefinition.class);

        Assert.assertEquals(attrDef.getId(), "regexSplitDefault");

        Pattern pat = attrDef.getRegularExpression();

        Assert.assertTrue(pat.matcher("at1-FOOBLECONNECTector").matches());
        Assert.assertFalse(pat.matcher("AT1-foobleconneECTOR").matches());
    }

    @Test public void sensitive() {
        RegexSplitAttributeDefinition attrDef =
                getAttributeDefn("regexSensitive.xml", RegexSplitAttributeDefinition.class);

        Assert.assertEquals(attrDef.getId(), "regexSplitSensitive");

        Pattern pat = attrDef.getRegularExpression();

        Assert.assertTrue(pat.matcher("at1-FOOBLECONNECTector").matches());
        Assert.assertFalse(pat.matcher("AT1-foobleconneECTOR").matches());
    }

    @Test public void insensitive() {
        RegexSplitAttributeDefinition attrDef =
                getAttributeDefn("regexInsensitive.xml", RegexSplitAttributeDefinition.class);

        Assert.assertEquals(attrDef.getId(), "regexSplitInsensitive");

        Pattern pat = attrDef.getRegularExpression();

        Assert.assertTrue(pat.matcher("at1-FOOBLECONNECTector").matches());
        Assert.assertTrue(pat.matcher("AT1-foobleconneECTOR").matches());
    }

    @Test public void none() {
        try {
            getAttributeDefn("regexNone.xml", RegexSplitAttributeDefinition.class);
            Assert.fail();
        } catch (BeanDefinitionStoreException e) {
            // OK
        }
    }

}