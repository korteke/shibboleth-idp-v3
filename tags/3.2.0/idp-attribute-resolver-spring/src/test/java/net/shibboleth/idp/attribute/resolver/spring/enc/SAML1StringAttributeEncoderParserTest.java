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
import net.shibboleth.idp.attribute.resolver.spring.enc.impl.SAML1StringAttributeEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1StringAttributeEncoder;
import net.shibboleth.idp.saml.xml.SAMLConstants;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML1StringAttributeEncoderParser}.
 */
public class SAML1StringAttributeEncoderParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void specified() {
        SAML1StringAttributeEncoder encoder =
                getAttributeEncoder("saml1String.xml", SAML1StringAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "SAML1_STRING_ATTRIBUTE_NAME");
        Assert.assertEquals(encoder.getNamespace(),"SAML1_STRING_ATTRIBUTE_NAME_SPACE");
    }
    
    @Test public void defaultCase() {
        SAML1StringAttributeEncoder encoder =
                getAttributeEncoder("saml1StringDefault.xml", SAML1StringAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "ATTRIBUTE");
        Assert.assertEquals(encoder.getNamespace(), SAMLConstants.SAML1_ATTR_NAMESPACE_URI);
    }
    
    @Test(expectedExceptions={BeanDefinitionStoreException.class,})  public void noName() {
        getAttributeEncoder("saml1StringNoName.xml", SAML1StringAttributeEncoder.class);
    }
}