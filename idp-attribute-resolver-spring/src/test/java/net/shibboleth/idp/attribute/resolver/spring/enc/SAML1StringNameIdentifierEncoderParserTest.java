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
import net.shibboleth.idp.attribute.resolver.spring.enc.impl.SAML1StringNameIdentifierEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1StringNameIdentifierEncoder;

import org.opensaml.saml.saml1.core.NameIdentifier;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML1StringNameIdentifierEncoderParser}.
 */
public class SAML1StringNameIdentifierEncoderParserTest extends BaseAttributeDefinitionParserTest {

    @Test public void specified() {
        SAML1StringNameIdentifierEncoder encoder =
                getAttributeEncoder("saml1StringNameIdentifier.xml", SAML1StringNameIdentifierEncoder.class);

        Assert.assertEquals(encoder.getNameFormat(), "NAMEIDENTIFIER_FORMAT");
        Assert.assertEquals(encoder.getNameQualifier(),"NAMEIDENTIFIER_QUALIFIER");
    }
    
    @Test public void defaultCase() {
        SAML1StringNameIdentifierEncoder encoder =
                getAttributeEncoder("saml1StringNameIdentifierDefault.xml", SAML1StringNameIdentifierEncoder.class);

        Assert.assertEquals(encoder.getNameFormat(), NameIdentifier.UNSPECIFIED);
        Assert.assertNull(encoder.getNameQualifier());;
    }
    
}