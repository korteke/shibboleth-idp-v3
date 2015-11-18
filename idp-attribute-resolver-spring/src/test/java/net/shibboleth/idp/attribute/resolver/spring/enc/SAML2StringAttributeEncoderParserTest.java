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
import net.shibboleth.idp.attribute.resolver.spring.enc.impl.SAML2StringAttributeEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringAttributeEncoder;

import org.opensaml.saml.saml2.core.Attribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/**
 * Test for {@link SAML2StringAttributeEncoderParser}.
 */
public class SAML2StringAttributeEncoderParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void specified() {
        SAML2StringAttributeEncoder encoder =
                getAttributeEncoder("saml2String.xml", SAML2StringAttributeEncoder.class);

        Assert.assertEquals(encoder.getName(), "Saml2String_ATTRIBUTE_NAME");
        Assert.assertEquals(encoder.getFriendlyName(),"Saml2String_ATTRIBUTE_FRIENDLY_NAME"); 
        Assert.assertEquals(encoder.getNameFormat(),"Saml2String_ATTRIBUTE_NAME_FORMAT");
    }
    
    @Test public void defaultCase() {
        SAML2StringAttributeEncoder encoder =
                getAttributeEncoder("saml2StringDefault.xml", SAML2StringAttributeEncoder.class);

        Assert.assertSame(encoder.getActivationCondition(), Predicates.alwaysTrue());
        Assert.assertTrue(encoder.getActivationCondition().apply(null));
        Assert.assertEquals(encoder.getName(), "Saml2StringName");
        Assert.assertNull(encoder.getFriendlyName()); 
        Assert.assertEquals(encoder.getNameFormat(), Attribute.URI_REFERENCE);
    }
    
    @Test(expectedExceptions={BeanDefinitionStoreException.class,})  public void noName() {
        getAttributeEncoder("saml2StringNoName.xml", SAML2StringAttributeEncoder.class);
    }
    
    @Test public void conditional() {
        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);

        loadFile(ENCODER_FILE_PATH + "predicates.xml", context);
        
        SAML2StringAttributeEncoder encoder =
                getAttributeEncoder("saml2StringConditional.xml", SAML2StringAttributeEncoder.class, context);

        Assert.assertSame(encoder.getActivationCondition(), Predicates.alwaysFalse());
        Assert.assertFalse(encoder.getActivationCondition().apply(null));
    }

}