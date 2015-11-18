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

import java.util.Arrays;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link ScopedStringAttributeValue}. */
public class ByteAttributeValueTest {

    static final byte[] DATA1 = {1,2,3,0xF};
    static final byte[] DATAEQUALS1 = {1,2,3,0xF};
    static final byte[] DATA2 = {2,3,1};
    
    /** Test proper instantiation of the object. */
    @Test public void instantiation() {
        ByteAttributeValue value = new ByteAttributeValue(DATA1);
        Assert.assertEquals(value.getValue(), DATA1);

        try {
            new ByteAttributeValue(null);
            Assert.fail("able to set null attribute value");
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    /** Test equality of two objects. */
    @Test public void equality() {
        ByteAttributeValue value1 = new ByteAttributeValue(DATA1);
        ByteAttributeValue value2 = new ByteAttributeValue(DATAEQUALS1);
        ByteAttributeValue value3 = new ByteAttributeValue(DATA2);

        Assert.assertTrue(value1.equals(value2));
        Assert.assertTrue(value1.equals(value2));
        Assert.assertTrue(value1.equals(value1));
        Assert.assertTrue(value2.equals(value1));
        Assert.assertEquals(value1.hashCode(), value2.hashCode());

        Assert.assertFalse(value1.equals(value3));
        Assert.assertFalse(value1.equals(null));
        Assert.assertFalse(value1.equals(this));
        Assert.assertFalse(value3.equals(value1));

        Assert.assertFalse(value2.equals(value3));
        Assert.assertFalse(value3.equals(value2));
    }

    @Test public void toHexStringBase64() {
        ByteAttributeValue value = new ByteAttributeValue(DATA1);
        
        value.toString();
        Assert.assertEquals(value.toHex(), "0102030f");
        
        Assert.assertTrue(Arrays.equals(Base64Support.decode(value.toBase64()), DATA1));
    }
}