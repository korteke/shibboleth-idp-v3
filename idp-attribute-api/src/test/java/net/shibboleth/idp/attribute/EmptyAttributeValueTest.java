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

import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link EmptyAttributeValue}. */
public class EmptyAttributeValueTest {

    /** Test proper instantiation of the object. */
    @Test public void instantiation() {
        EmptyAttributeValue value = new EmptyAttributeValue(EmptyType.NULL_VALUE);
        Assert.assertEquals(value.getValue(), EmptyType.NULL_VALUE);

        try {
            new EmptyAttributeValue(null);
            Assert.fail("able to set null attribute value");
        } catch (ConstraintViolationException e) {
            // expected this
        }

    }

    /** Test equality of two objects. */
    @Test public void equality() {
        EmptyAttributeValue value1 = new EmptyAttributeValue(EmptyType.NULL_VALUE);
        EmptyAttributeValue value2 = new EmptyAttributeValue(EmptyType.ZERO_LENGTH_VALUE);

        Assert.assertTrue(value1.equals(value1));
        Assert.assertTrue(value2.equals(value2));
        Assert.assertEquals(value1.hashCode(), value1.hashCode());
        Assert.assertEquals(value2.hashCode(), value2.hashCode());
        Assert.assertNotEquals(value1.hashCode(), value2.hashCode());

        Assert.assertFalse(value1.equals(value2));
        Assert.assertFalse(value1.equals(null));
        Assert.assertFalse(value1.equals(this));
        Assert.assertFalse(value2.equals(value1));
    }
}