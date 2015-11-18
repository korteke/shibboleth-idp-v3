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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * {@link SAML2ByteAttributeEncoder} Unit test.
 * 
 * Identical code to the {@link SAML1ByteAttributeEncoder} except that the type of assertion and encoder is changed.
 */
public class SAML2ByteAttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    /** A test value. */
    private final static byte[] BYTE_ARRAY_1 = {1, 2, 3, 4, 5};

    /** A second test value. */
    private final static byte[] BYTE_ARRAY_2 = {4, 3, 2, 1};

    private SAML2ByteAttributeEncoder encoder;

    @BeforeClass public void initTest() throws ComponentInitializationException {
        encoder = new SAML2ByteAttributeEncoder();
        encoder.setName(ATTR_NAME);
        encoder.setNameFormat("NameSpace");
        encoder.setFriendlyName("friendly");
        encoder.setEncodeType(true);
        encoder.initialize();
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void empty() throws Exception {
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);

        encoder.encode(inputAttribute);
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void inappropriate() throws Exception {
        final int[] intArray = {1, 2, 3, 4};
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new StringAttributeValue("foo"), new ScopedStringAttributeValue("foo", "bar"),
                        new IdPAttributeValue<Object>() {
                            public Object getValue() {
                                return intArray;
                            }
                            public String getDisplayValue() {
                                return intArray.toString();
                            }
                        });
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        encoder.encode(inputAttribute);
    }

    @Test public void single() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new StringAttributeValue("foo"), new ByteAttributeValue(BYTE_ARRAY_1));
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        List<XMLObject> children = outputAttribute.getOrderedChildren();

        Assert.assertEquals(children.size(), 1, "Encoding one entry");

        XMLObject child = children.get(0);
        Assert.assertEquals(child.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME);

        Assert.assertTrue(child instanceof XSBase64Binary, "Child of result attribute should be a base64Binary");

        XSBase64Binary childAsString = (XSBase64Binary) child;

        byte childAsBa[] = Base64Support.decode(childAsString.getValue());

        Assert.assertEquals(childAsBa, BYTE_ARRAY_1, "Input equals output");
    }

    @Test public void multi() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(BYTE_ARRAY_1), new ByteAttributeValue(BYTE_ARRAY_2));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 2, "Encoding two entries");

        XMLObject child = children.get(0);
        Assert.assertEquals(child.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        Assert.assertTrue(child instanceof XSBase64Binary, "Child of result attribute should be a base64Binary");

        XSBase64Binary childAsString = (XSBase64Binary) child;
        Assert.assertEquals(child.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        final byte[] res0 = Base64Support.decode(childAsString.getValue());
        
        child = children.get(1);
        Assert.assertTrue(child instanceof XSBase64Binary, "Child of result attribute should be a base64Binary");

        childAsString = (XSBase64Binary) child;
        final byte[] res1 = Base64Support.decode(childAsString.getValue());

        //
        // order of results is not guaranteed so sense the result from the length
        //
        if (BYTE_ARRAY_1.length == res0.length) {
            Assert.assertEquals(BYTE_ARRAY_1, res0, "Input matches output");
            Assert.assertEquals(BYTE_ARRAY_2, res1, "Input matches output");
        } else if (BYTE_ARRAY_1.length == res1.length) {
            Assert.assertEquals(BYTE_ARRAY_1, res1, "Input matches output");
            Assert.assertEquals(BYTE_ARRAY_2, res0, "Input matches output");
        } else {
            Assert.assertTrue(BYTE_ARRAY_1.length == res1.length || BYTE_ARRAY_2.length == res1.length,
                    "One of the output's size should match an input size");
        }
    }

}