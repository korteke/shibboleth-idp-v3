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

package net.shibboleth.idp.saml.attribute.encoding;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.saml.attribute.mapping.AttributeMapper;

import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link AbstractSAML2NameIDEncoder}.
 */
public class AbstractSAML2NameIDEncoderTest {

    @Test public void abstractSAML2NameIdentifierEncoder() {
        
        AbstractSAML2NameIDEncoder encoder = new AbstractSAML2NameIDEncoder() {
            public NameID encode(IdPAttribute attribute) throws AttributeEncodingException {
                return null;
            }

            @Nullable public AttributeMapper<RequestedAttribute, IdPRequestedAttribute> getMapper(String name) {
                return null;
            }

            public boolean apply(String input) {
                return false;
            }
        };
        // Again, use constants
        Assert.assertEquals(encoder.getProtocol(), "urn:oasis:names:tc:SAML:2.0:protocol");
    }
}