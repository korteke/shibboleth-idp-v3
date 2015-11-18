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

package net.shibboleth.idp.attribute.resolver.spring.enc;

import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.enc.impl.SAML1Base64AttributeEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1ByteAttributeEncoder;
import net.shibboleth.idp.saml.xml.SAMLConstants;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML1Base64AttributeEncoderParser}.
 */
public class SAML1Base64AttributeEncoderParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void specified() {
        SAML1ByteAttributeEncoder encoder =
                getAttributeEncoder("saml1Base64.xml", SAML1ByteAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "SAML1_BASE64_ATTRIBUTE_NAME");
        Assert.assertEquals(encoder.getNamespace(),"SAML1_BASE64_ATTRIBUTE_NAME_SPACE");
    }
    
    @Test public void defaultCase() {
        SAML1ByteAttributeEncoder encoder =
                getAttributeEncoder("saml1Base64Default.xml", SAML1ByteAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "Base64_ATTRIBUTE");
        Assert.assertEquals(encoder.getNamespace(), SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
    }
    
    @Test(expectedExceptions={BeanDefinitionStoreException.class,})  public void noName() {
        getAttributeEncoder("saml1Base64NoName.xml", SAML1ByteAttributeEncoder.class);
    }
}