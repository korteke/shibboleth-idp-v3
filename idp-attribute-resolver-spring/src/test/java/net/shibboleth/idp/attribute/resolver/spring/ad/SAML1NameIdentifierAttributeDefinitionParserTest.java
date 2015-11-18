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

import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.SAML1NameIdentifierAttributeDefinitionParser;
import net.shibboleth.idp.saml.attribute.resolver.impl.SAML1NameIdentifierAttributeDefinition;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML1NameIdentifierAttributeDefinitionParser}.
 */
public class SAML1NameIdentifierAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void defaultCase() {
        SAML1NameIdentifierAttributeDefinition attrDef =
                getAttributeDefn("saml1NameIdDefault.xml", SAML1NameIdentifierAttributeDefinition.class);

        Assert.assertEquals(attrDef.getId(), "SAML1NameIdentifier");
        Assert.assertEquals(attrDef.getNameIdFormat(), "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        Assert.assertNull(attrDef.getNameIdQualifier());
    }

    @Test public void attributes() {
        SAML1NameIdentifierAttributeDefinition attrDef =
                getAttributeDefn("saml1NameIdAttributes.xml", SAML1NameIdentifierAttributeDefinition.class);

        Assert.assertEquals(attrDef.getId(), "SAML1NameIdentifierAttributes");
        Assert.assertEquals(attrDef.getNameIdFormat(), "format");
        Assert.assertEquals(attrDef.getNameIdQualifier(), "qualifier");
    }
}