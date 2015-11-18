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

import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link XMLObjectAttributeValue}. */
public class XMLObjectAttributeValueTest {
    
    private XSString xsId1;
    private XSString xsId2;
    private XSString xsId3;
    
    public XMLObjectAttributeValueTest() {
        XSStringBuilder  builder = new XSStringBuilder();
        QName foo = new QName("foo");
        xsId1 = builder.buildObject(foo);
        xsId1.setValue("one");
        xsId2 = builder.buildObject(foo);
        xsId2.setValue("one");
        xsId3 = builder.buildObject(foo);
        xsId3.setValue("three");
    }

    /** Test proper instantiation of the object. */
    @Test public void instantiation() {
        XMLObjectAttributeValue value = new XMLObjectAttributeValue(xsId1);
        Assert.assertEquals(value.getValue(), xsId1);

        try {
            new XMLObjectAttributeValue(null);
            Assert.fail("able to set null attribute value");
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    /** Test equality of two objects. */
    @Test public void equality() {
        XMLObjectAttributeValue value1 = new XMLObjectAttributeValue(xsId1);
        XMLObjectAttributeValue value2 = new XMLObjectAttributeValue(xsId2);
        XMLObjectAttributeValue value3 = new XMLObjectAttributeValue(xsId3);

        Assert.assertTrue(value1.equals(value1));
        Assert.assertNotEquals(value1.hashCode(), value2.hashCode());

        Assert.assertFalse(value1.equals(value3));
        Assert.assertFalse(value1.equals(null));
        Assert.assertFalse(value1.equals(this));
        Assert.assertFalse(value3.equals(value1));

        Assert.assertFalse(value2.equals(value3));
        Assert.assertFalse(value3.equals(value2));
        value2.toString();
    }
}