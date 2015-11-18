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
import net.shibboleth.idp.saml.xmlobject.ScopedValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml1.core.Attribute;
import org.opensaml.saml.saml1.core.AttributeValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link SAML1ScopedStringAttributeEncoder} Unit test.
 */
public class SAML1ScopedStringAttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    /** The name we give the test attribute. */
    private final static String ATTR_NAME = "foo";

    /** A test value. */
    private final static String VALUE_1 = "ValueTheFirst";

    /** A second test value. */
    private final static String VALUE_2 = "Second";

    /** A test scope . */
    private final static String SCOPE_1 = "scope1.example.org";

    /** A second test value. */
    private final static String SCOPE_2 = "scope2";

    private final static String DELIMITER = "#";

    private final static String ATTRIBUTE_NAME = "Scope";

    private final ScopedStringAttributeValue value1 = new ScopedStringAttributeValue(VALUE_1, SCOPE_1);

    private final ScopedStringAttributeValue value2 = new ScopedStringAttributeValue(VALUE_2, SCOPE_2);

    public SAML1ScopedStringAttributeEncoder makeEncoder() {
        final SAML1ScopedStringAttributeEncoder encoder = new SAML1ScopedStringAttributeEncoder();
        encoder.setName(ATTR_NAME);
        encoder.setScopeAttributeName(ATTRIBUTE_NAME);
        encoder.setNamespace("NameSpace");
        encoder.setEncodeType(true);
        return encoder;
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void empty()
            throws AttributeEncodingException, ComponentInitializationException {
        final IdPAttribute inputAttribute;

        inputAttribute = new IdPAttribute(ATTR_NAME);

        SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        encoder.initialize();
        encoder.encode(inputAttribute);
    }

    @Test(expectedExceptions = {AttributeEncodingException.class,}) public void inappropriate()
            throws AttributeEncodingException, ComponentInitializationException {
        final SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        final int[] intArray = {1, 2, 3, 4};
        final Collection<IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), new StringAttributeValue("dd"),
                        new IdPAttributeValue<Object>() {
                            @Override
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
        try {
            encoder.encode(inputAttribute);
            Assert.fail("not initialized");
        } catch (UninitializedComponentException e) {
            // OK
        }
        encoder.initialize();

        encoder.encode(inputAttribute);
    }

    @Test public void single() throws ComponentInitializationException, AttributeEncodingException {
        final SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        try {
            encoder.setScopeAttributeName(null);
            encoder.initialize();
            Assert.fail("Missing attribute name should throw");
        } catch (ComponentInitializationException e) {
            // OK
        }
        encoder.setScopeAttributeName(ATTRIBUTE_NAME);
        encoder.setScopeDelimiter(DELIMITER);

        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), value1);

        final IdPAttribute inputAttribute;
        inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        encoder.initialize();
        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();

        Assert.assertEquals(children.size(), 1, "Encoding one entry");

        final XMLObject child = children.get(0);

        Assert.assertEquals(child.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");

        Assert.assertTrue(child instanceof ScopedValue, "Child of result attribute should be a string");

        final ScopedValue childAsScopedValue = (ScopedValue) child;

        Assert.assertEquals(childAsScopedValue.getValue(), VALUE_1, "Input equals output");
        Assert.assertEquals(childAsScopedValue.getScope(), SCOPE_1, "Input equals output");

    }

    @Test public void multi() throws ComponentInitializationException, AttributeEncodingException {
        final SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        encoder.initialize();
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), value1, value2);

        final IdPAttribute inputAttribute;
        inputAttribute = new IdPAttribute(ATTR_NAME);
        inputAttribute.setValues(values);

        final Attribute outputAttribute = encoder.encode(inputAttribute);

        Assert.assertNotNull(outputAttribute);

        final List<XMLObject> children = outputAttribute.getOrderedChildren();
        Assert.assertEquals(children.size(), 2, "Encoding two entries");

        Assert.assertTrue(children.get(0) instanceof ScopedValue && children.get(1) instanceof ScopedValue,
                "Child of result attribute should be a string");

        final ScopedValue child1 = (ScopedValue) children.get(0);
        Assert.assertEquals(child1.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");

        final ScopedValue child2 = (ScopedValue) children.get(1);
        Assert.assertEquals(child2.getElementQName(), AttributeValue.DEFAULT_ELEMENT_NAME,
                "Attribute Value not inside <AttributeValue/>");
        //
        // order of results is not guaranteed so sense the result from the length
        //
        if (child1.getValue().length() == VALUE_1.length()) {
            Assert.assertEquals(child1.getValue(), VALUE_1, "Input matches output");
            Assert.assertEquals(child2.getValue(), VALUE_2, "Input matches output");
            Assert.assertEquals(child1.getScope(), SCOPE_1, "Input matches output");
            Assert.assertEquals(child2.getScope(), SCOPE_2, "Input matches output");
        } else if (child1.getValue().length() == VALUE_2.length()) {
            Assert.assertEquals(child2.getValue(), VALUE_1, "Input matches output");
            Assert.assertEquals(child1.getValue(), VALUE_2, "Input matches output");
            Assert.assertEquals(child2.getScope(), SCOPE_1, "Input matches output");
            Assert.assertEquals(child1.getScope(), SCOPE_2, "Input matches output");
        } else {
            Assert.fail("Value mismatch");
        }
    }

    @Test public void singleInline() throws ComponentInitializationException, AttributeEncodingException {
        final SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        encoder.setScopeType("wibble");
        try {
            encoder.initialize();
            Assert.fail("Bad type should throw");
        } catch (ComponentInitializationException e) {
            // OK
        }

        encoder.setScopeType("inline");
        encoder.setScopeDelimiter(DELIMITER);
        encoder.initialize();

        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), value1);

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

        Assert.assertEquals(childAsString.getValue(), VALUE_1 + DELIMITER + SCOPE_1, "Input equals output");
    }

    @Test public void multiInline() throws Exception {
        final SAML1ScopedStringAttributeEncoder encoder = makeEncoder();
        encoder.setScopeType("inline");
        encoder.setScopeDelimiter(DELIMITER);
        encoder.setScopeAttributeName(null);
        encoder.initialize();
        final Collection<? extends IdPAttributeValue<?>> values =
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}), value1, value2);

        final IdPAttribute inputAttribute;
        inputAttribute = new IdPAttribute(ATTR_NAME);
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
        if (child1.getValue().equals(VALUE_1 + DELIMITER + SCOPE_1)) {
            Assert.assertEquals(child2.getValue(), VALUE_2 + DELIMITER + SCOPE_2, "Input matches output");
        } else {
            Assert.assertEquals(child1.getValue(), VALUE_2 + DELIMITER + SCOPE_2, "Input matches output");
            Assert.assertEquals(child2.getValue(), VALUE_1 + DELIMITER + SCOPE_1, "Input matches output");
        }
    }

}
