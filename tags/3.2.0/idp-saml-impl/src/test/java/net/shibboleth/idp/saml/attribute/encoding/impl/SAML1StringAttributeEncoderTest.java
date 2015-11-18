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
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
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
 * {@link SAML1StringAttributeEncoder} Unit test.
 */
public class SAML1StringAttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    /** A test value. */
    private final static String STRING_1 = "Value The First";

    /** A second test value. */
    private final static String STRING_2 = "Second string the value is";

    private SAML1StringAttributeEncoder encoder;

    @BeforeClass public void initTest() throws ComponentInitializationException {
        encoder = new SAML1StringAttributeEncoder();
        encoder.setName(ATTR_NAME);
        encoder.setNamespace("NameSpace");
        encoder.setEncodeType(true);
        encoder.initialize();
    }

    @Test(expectedExceptions={AttributeEncodingException.class,})   public void empty() throws Exception {
        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);

        encoder.encode(inputAttribute);
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void inappropriate() throws Exception {
        encoder.initialize();
        final int[] intArray = {1, 2, 3, 4};
        final Collection<IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}),
                        new IdPAttributeValue<Object>() {
                            public Object getValue() {
                                return intArray;
                            }
                            public String getDisplayValue() {
                                return intArray.toString();
                            }
                        });

        final IdPAttribute inputAttribute;
        inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        encoder.encode(inputAttribute);
    }

    @Test public void single() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), new StringAttributeValue(STRING_1));

        final IdPAttribute inputAttribute;
        inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();

        Assert.assertEquals(children.size(), 1, "Encoding one entry");

        final XMLObject child = children.get(0);

        Assert.assertEquals(child.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");

        Assert.assertTrue(child instanceof XSString, "Child of result attribute should be a string");

        final XSString childAsString = (XSString) child;

        Assert.assertEquals(childAsString.getValue(), STRING_1, "Input equals output");
    }

    @Test public void multi() throws Exception {
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}),
                        new StringAttributeValue(STRING_1), new StringAttributeValue(STRING_2));

        final IdPAttribute inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 2, "Encoding two entries");

        Assert.assertTrue(children.get(0) instanceof XSString && children.get(1) instanceof XSString,
                "Child of result attribute should be a string");

        final XSString child1 = (XSString) children.get(0);
        Assert.assertEquals(child1.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");

        final XSString child2 = (XSString) children.get(1);
        Assert.assertEquals(child2.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        //
        // order of results is not guaranteed so sense the result from the length
        //
        if (child1.getValue().length() == STRING_1.length()) {
            Assert.assertEquals(child1.getValue(), STRING_1, "Input matches output");
            Assert.assertEquals(child2.getValue(), STRING_2, "Input matches output");
        } else if (child1.getValue().length() == STRING_2.length()) {
            Assert.assertEquals(child2.getValue(), STRING_1, "Input matches output");
            Assert.assertEquals(child1.getValue(), STRING_2, "Input matches output");
        } else {
            Assert.assertTrue(
                    child1.getValue().length() == STRING_1.length() || child1.getValue().length() == STRING_2.length(),
                    "One of the output's size should match an input size");
        }
    }

}
