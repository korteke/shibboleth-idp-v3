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
import net.shibboleth.idp.attribute.resolver.spring.enc.impl.SAML2XMLObjectAttributeEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2XMLObjectAttributeEncoder;

import org.opensaml.saml.saml2.core.Attribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML2XMLObjectAttributeEncoderParser}.
 */
public class SAML2XMLObjectAttributeEncoderParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void specified() {
        SAML2XMLObjectAttributeEncoder encoder =
                getAttributeEncoder("saml2XmlObject.xml", SAML2XMLObjectAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "Saml2XmlObject_ATTRIBUTE_NAME");
        Assert.assertEquals(encoder.getFriendlyName(),"Saml2XmlObject_ATTRIBUTE_FRIENDLY_NAME"); 
        Assert.assertEquals(encoder.getNameFormat(),"Saml2XmlObject_ATTRIBUTE_NAME_FORMAT");
    }
    
    @Test public void defaultCase() {
        SAML2XMLObjectAttributeEncoder encoder =
                getAttributeEncoder("saml2XmlObjectDefault.xml", SAML2XMLObjectAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "XmlObjectName");
        Assert.assertNull(encoder.getFriendlyName()); 
        Assert.assertEquals(encoder.getNameFormat(), Attribute.URI_REFERENCE);
    }
    
    @Test(expectedExceptions={BeanDefinitionStoreException.class,})  public void noName() {
        getAttributeEncoder("saml2XmlObjectNoName.xml", SAML2XMLObjectAttributeEncoder.class);
    }
}