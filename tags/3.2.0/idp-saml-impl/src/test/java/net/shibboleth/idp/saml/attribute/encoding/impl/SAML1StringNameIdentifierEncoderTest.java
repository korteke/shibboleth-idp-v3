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

package net.shibboleth.idp.saml.attribute.encoding.impl;

import java.util.Collections;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link SAML1StringNameIdentifierEncoder}.
 */
public class SAML1StringNameIdentifierEncoderTest extends OpenSAMLInitBaseTestCase {

    @Test public void settersGetters() {
        final SAML1StringNameIdentifierEncoder encoder = new SAML1StringNameIdentifierEncoder();

        Assert.assertEquals(encoder.getNameFormat(), "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

        Assert.assertNull(encoder.getNameQualifier());

        encoder.setNameFormat("nameFormat");
        encoder.setNameQualifier("nameQualifier");

        Assert.assertEquals(encoder.getNameFormat(), "nameFormat");
        Assert.assertEquals(encoder.getNameQualifier(), "nameQualifier");
    }

    @Test public void equalsHash() {
        final SAML1StringNameIdentifierEncoder enc1 = new SAML1StringNameIdentifierEncoder();
        enc1.setNameFormat("nameFormat");
        enc1.setNameQualifier("nameQualifier");

        final SAML1StringNameIdentifierEncoder enc2 = new SAML1StringNameIdentifierEncoder();
        enc2.setNameFormat("nameFormat");
        enc2.setNameQualifier("nameQualifier");

        final SAML1StringNameIdentifierEncoder enc3 = new SAML1StringNameIdentifierEncoder();
        enc3.setNameFormat("nameQualifier");
        enc3.setNameQualifier("nameFormat");

        Assert.assertFalse(enc1.equals(null));
        Assert.assertFalse(enc1.equals(new Integer(1)));
        Assert.assertFalse(enc1.equals(enc3));
        Assert.assertTrue(enc1.equals(enc2));
        Assert.assertTrue(enc1.equals(enc1));

        Assert.assertEquals(enc1.hashCode(), enc2.hashCode());
        Assert.assertNotEquals(enc1.hashCode(), enc3.hashCode());
    }

    @Test public void encode() throws AttributeEncodingException {
        IdPAttribute attribute = new IdPAttribute("id");
        attribute.setValues(Collections.singleton(new StringAttributeValue("value")));

        final SAML1StringNameIdentifierEncoder enc1 = new SAML1StringNameIdentifierEncoder();

        NameIdentifier nameId = enc1.encode(attribute);
        Assert.assertEquals(nameId.getValue(), "value");
        Assert.assertEquals(nameId.getFormat(), "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        Assert.assertNull(nameId.getNameQualifier());

        enc1.setNameFormat("nameFormat");
        enc1.setNameQualifier("nameQualifier");
        nameId = enc1.encode(attribute);
        Assert.assertEquals(nameId.getValue(), "value");
        Assert.assertEquals(nameId.getFormat(), "nameFormat");
        Assert.assertEquals(nameId.getNameQualifier(), "nameQualifier");
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void innappropriateTypes()
            throws AttributeEncodingException {
        IdPAttribute attribute = new IdPAttribute("id");

        final SAML1StringNameIdentifierEncoder enc1 = new SAML1StringNameIdentifierEncoder();

        try {
            enc1.encode(attribute);
            Assert.fail();
        } catch (Exception e) {
            // OK
        }

        final IdPAttributeValue<?> empty = new IdPAttributeValue<String>() {
            @Override
            @Nonnull public String getValue() {
                return null;
            }
            public String getDisplayValue() {
                return null;
            }
        };

        attribute.setValues(Collections.singleton(empty));
        try {
            enc1.encode(attribute);
            Assert.fail();
        } catch (AttributeEncodingException e) {
            // OK
        }

        final IdPAttributeValue<?> wrong = new IdPAttributeValue<Integer>() {
            @Override
            @Nonnull public Integer getValue() {
                return new Integer(3);
            }
            public String getDisplayValue() {
                return "3";
            }
        };

        attribute.setValues(Collections.singleton(wrong));
        enc1.encode(attribute);
    }
}
