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
import java.util.Collections;
import java.util.List;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml1.core.Attribute;
import org.opensaml.saml.saml1.core.AttributeValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * {@link SAML1XMLObjectAttributeEncoder} Unit test.
 * 
 * Identical code to the {@link SAML1ByteAttributeEncoder} except that the type of assertion and encoder is changed.
 */
public class SAML1XMLObjectAttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    /** A test value. */
    private final static String STRING_1 = "Value The First";

    /** A second test value. */
    private final static String STRING_2 = "Second string the value is";

    private SAML1XMLObjectAttributeEncoder encoder;

    private SAML1StringAttributeEncoder strEncoder;

    @BeforeClass public void initTest() throws ComponentInitializationException {
        encoder = new SAML1XMLObjectAttributeEncoder();
        encoder.setName(ATTR_NAME);
        encoder.setNamespace("NameSpace");
        encoder.initialize();
        strEncoder = new SAML1StringAttributeEncoder();
        strEncoder.setName(ATTR_NAME);
        strEncoder.setNamespace("NameSpace");
        strEncoder.setEncodeType(true);
        strEncoder.initialize();
    }

    /**
     * Create an XML object from a string which we can test against later. We use the Saml1StringEncoder to do the work
     * because it is handy in this package.
     * 
     * @param value that we encode
     * @return an XML object
     */
    private XMLObjectAttributeValue ObjectFor(final String value) {
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(Collections.singleton(new StringAttributeValue(value)));
        try {
            return new XMLObjectAttributeValue(strEncoder.encode(inputAttribute));
        } catch (AttributeEncodingException e) {
            return null;
        }
    }

    /**
     * Check that the input XML object is what was expected.
     * 
     * @param input the objects in question.
     * @param possibles the strings that they might be encoding.
     */
    private static void CheckValues(final XMLObject input, final String... possibles) {
        Assert.assertTrue(input instanceof Attribute);
        final Attribute attribute = (Attribute) input;

        final List<XMLObject> children = attribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 1, "Data must have but one entry");
        Assert.assertTrue(children.get(0) instanceof XSString, "Data must contain one string");
        final String s = ((XSString) children.get(0)).getValue();

        for (String possible : possibles) {
            if (s.equals(possible)) {
                return;
            }
        }
        Assert.assertTrue(false, "No potential match");
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void empty() throws Exception {

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);

        encoder.encode(inputAttribute);
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void inappropriate() throws Exception {
        final int[] intArray = {1, 2, 3, 4};
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}),
                        new ScopedStringAttributeValue("foo", "bar"), new IdPAttributeValue<Object>() {
                            @Override
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
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), ObjectFor(STRING_1));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);
        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 1, "Encoding one entry");
        Assert.assertEquals(children.get(0).getElementQName(),
                AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        Assert.assertEquals(children.get(0).getOrderedChildren().size(), 1,
                "Expected exactly one child inside the <AttributeValue/>");

        CheckValues(children.get(0).getOrderedChildren().get(0), STRING_1);
    }

    @Test public void testMulti() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values = Arrays.asList(ObjectFor(STRING_1), ObjectFor(STRING_2));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);
        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 2, "Encoding two entries");

        Assert.assertEquals(children.get(0).getElementQName(),
                AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        Assert.assertEquals(children.get(0).getOrderedChildren().size(), 1,
                "Expected exactly one child inside the <AttributeValue/> for first Attribute");

        Assert.assertEquals(children.get(1).getElementQName(),
                AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        Assert.assertEquals(children.get(1).getOrderedChildren().size(), 1,
                "Expected exactly one child inside the <AttributeValue/> for second Attribute");

        CheckValues(children.get(0).getOrderedChildren().get(0), STRING_1, STRING_2);
        CheckValues(children.get(1).getOrderedChildren().get(0), STRING_1, STRING_2);
    }
}
