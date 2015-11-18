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

package net.shibboleth.idp.attribute;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link ScopedStringAttributeValue}. */
public class StringAttributeValueTest {

    /** Test proper instantiation of the object. */
    @Test public void instantiation() {
        StringAttributeValue value = new StringAttributeValue(" foo ");
        Assert.assertEquals(value.getValue(), " foo ");

        try {
            new StringAttributeValue(null);
            Assert.fail("able to set null attribute value");
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new StringAttributeValue("");
            Assert.fail("able to set empty attribute value");
        } catch (ConstraintViolationException e) {
            // expected this
        }

    }

    /** Test equality of two objects. */
    @Test public void equality() {
        StringAttributeValue value1 = new StringAttributeValue(" foo ");
        StringAttributeValue value2 = new StringAttributeValue("foo");
        StringAttributeValue value3 = new StringAttributeValue(" baz ");

        Assert.assertFalse(value1.equals(value2));
        Assert.assertFalse(value1.equals(value2));
        Assert.assertTrue(value1.equals(value1));
        Assert.assertFalse(value2.equals(value1));
        Assert.assertNotEquals(value1.hashCode(), value2.hashCode());

        Assert.assertFalse(value1.equals(value3));
        Assert.assertFalse(value1.equals(null));
        Assert.assertFalse(value1.equals(this));
        Assert.assertFalse(value3.equals(value1));

        Assert.assertFalse(value2.equals(value3));
        Assert.assertFalse(value3.equals(value2));
        value2.toString();
    }
    
    /** Test valueOf(). */
    @Test public void valueOf() {
        Assert.assertEquals(StringAttributeValue.valueOf(null), EmptyAttributeValue.NULL);
        Assert.assertEquals(StringAttributeValue.valueOf(""), EmptyAttributeValue.ZERO_LENGTH);
        Assert.assertEquals(StringAttributeValue.valueOf(" "), new StringAttributeValue(" "));
    }
}