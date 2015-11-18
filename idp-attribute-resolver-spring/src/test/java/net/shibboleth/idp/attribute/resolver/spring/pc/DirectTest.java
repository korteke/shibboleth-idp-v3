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

package net.shibboleth.idp.attribute.resolver.spring.pc;

import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.attribute.principalconnector.impl.PrincipalConnector;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.impl.TransformingNameIDDecoder;
import net.shibboleth.idp.saml.nameid.impl.TransformingNameIdentifierDecoder;

import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for direct principal connector parsers.
 */
public class DirectTest extends BaseAttributeDefinitionParserTest {

    @Test public void simple() throws NameDecoderException {
        final PrincipalConnector connector = getPrincipalConnector("direct.xml");
        
        Assert.assertTrue(connector.getNameIDDecoder() instanceof TransformingNameIDDecoder);
        Assert.assertTrue(connector.getNameIdentifierDecoder() instanceof TransformingNameIdentifierDecoder);
        Assert.assertEquals(connector.getFormat(), "https://example.org/direct");
        Assert.assertTrue(connector.getRelyingParties().isEmpty());
        
        final NameID id = new NameIDBuilder().buildObject();
        id.setFormat("https://example.org/sealer");
        id.setValue("The_value");
        
        Assert.assertEquals(connector.decode(new SubjectCanonicalizationContext(), id), "The_value");
    }
    
    @Test public void relyingParties() {
        final PrincipalConnector connector = getPrincipalConnector("directRPs.xml");
        
        Assert.assertTrue(connector.getNameIDDecoder() instanceof TransformingNameIDDecoder);
        Assert.assertTrue(connector.getNameIdentifierDecoder() instanceof TransformingNameIdentifierDecoder);
        Assert.assertEquals(connector.getFormat(), "http://example.org/schema");
        Assert.assertEquals(connector.getRelyingParties().size(), 2);
        Assert.assertTrue(connector.getRelyingParties().contains("SP1"));
        Assert.assertTrue(connector.getRelyingParties().contains("SP2"));
    }

}
