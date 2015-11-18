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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml1.core.Attribute;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
public class AbstractSAMLAttributeEncoderTest extends OpenSAMLInitBaseTestCase {
    
    private XSStringBuilder theBuilder;
    private QName theQName = new QName("LocalQNAME");
    private final String MY_NAME = "myName";
    private final String MY_NAMESPACE = "myNameSpace";
    private final String ATTRIBUTE_ID = "attrID";
    private final String ATTRIBUTE_VALUE_1 = "attrValOne";
    private final String ATTRIBUTE_VALUE_2 = "attrValeTwo";
    
    @BeforeClass public void initTest() throws ComponentInitializationException {
        theBuilder = new XSStringBuilder();
    }
    
    @Test public void initializeAndSetters() throws AttributeEncodingException, ComponentInitializationException {
        AbstractSAML1AttributeEncoder encoder = new mockEncoder(theBuilder, theQName);
        
        Assert.assertNull(encoder.getName());
        Assert.assertEquals(encoder.getNamespace(), "urn:mace:shibboleth:1.0:attributeNamespace:uri");
        
        try {
            encoder.setName("");
            Assert.fail();
        } catch (ConstraintViolationException e) {
            
        }
        
        encoder = new mockEncoder(theBuilder, theQName);
        try {
            encoder.setNamespace("");
            Assert.fail();
        } catch (ConstraintViolationException e) {
            
        }
        
        encoder = new mockEncoder(theBuilder, theQName);
        encoder.setNamespace(MY_NAMESPACE);
        encoder.setName(MY_NAME);
        
        try {
            encoder.encode(new IdPAttribute(ATTRIBUTE_ID));
            Assert.fail();
        } catch (UninitializedComponentException ex) {
            // OK
        }
        
        encoder.initialize();
        try {
            encoder.setName(" ");
            Assert.fail();
        } catch (UnmodifiableComponentException ex) {
            //
        }
        
        try {
            encoder.setNamespace(" ");
            Assert.fail();
        } catch (UnmodifiableComponentException ex) {
            //
        }

    }

    @Test public void encode() throws AttributeEncodingException, ComponentInitializationException {
        AbstractSAML1AttributeEncoder encoder = new mockEncoder(theBuilder, theQName);
        encoder.setNamespace(MY_NAMESPACE);
        encoder.setName(MY_NAME);
        encoder.initialize();
        IdPAttribute attr = new IdPAttribute(ATTRIBUTE_ID);
        
        try {
            encoder.encode(attr);
        } catch (AttributeEncodingException e) {
            // OK
        }
        
        final int[] intArray = {1, 2, 3, 4};
        final List<IdPAttributeValue<?>> values = new ArrayList<>(
                Arrays.asList(new ByteAttributeValue(new byte[] {1, 2, 3,}),
                        null,
                        new IdPAttributeValue<Object>() {
                            @Override
                            public Object getValue() {
                                return intArray;
                            }

                            @Override
                            public String getDisplayValue() {
                                return intArray.toString();
                            }
                        }
                        ));
        attr.setValues(values);
        try {
            encoder.encode(attr);
        } catch (AttributeEncodingException e) {
            // OK
        }
        values.add(new StringAttributeValue(ATTRIBUTE_VALUE_1));
        values.add(new StringAttributeValue(ATTRIBUTE_VALUE_2));
        attr.setValues(values);
        
        List<XMLObject> result = ((Attribute) encoder.encode(attr)).getAttributeValues();
        
        Assert.assertEquals(result.size(), 2);
        Set<String> resultSet = new HashSet<>(2); 
        for (XMLObject o: result) {
            Assert.assertTrue(o instanceof XSString);
            resultSet.add(((XSString) o).getValue());
        }
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_1));
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_2));

    }
    
    @Test public void equalsHash() {
        mockEncoder enc1 = new mockEncoder(theBuilder, theQName);
        Assert.assertEquals(enc1, enc1);
        Assert.assertNotSame(enc1, null);
        Assert.assertNotSame(enc1, this);

        mockEncoder enc2 = new mockEncoder(theBuilder, theQName);
        
        Assert.assertEquals(enc1, enc2);
        Assert.assertEquals(enc1.hashCode(), enc2.hashCode());
        enc1.setName(MY_NAME);
        enc1.setNamespace(MY_NAMESPACE);
        enc2.setName(MY_NAME);
        enc2.setNamespace(MY_NAMESPACE);
        Assert.assertEquals(enc1, enc2);
        Assert.assertEquals(enc1.hashCode(), enc2.hashCode());
        enc2.setName(MY_NAME + MY_NAME);
        Assert.assertNotSame(enc1,  enc2);
        Assert.assertNotSame(enc1.hashCode(),  enc2.hashCode());
        enc2.setName(MY_NAME);
        enc2.setNamespace(MY_NAME);
        Assert.assertNotSame(enc1,  enc2);
        Assert.assertNotSame(enc1.hashCode(),  enc2.hashCode());        
    }
 
    protected static class mockEncoder extends AbstractSAML1AttributeEncoder {
        
        @Nonnull private final XSStringBuilder builder;
        @Nonnull private final QName myQName;

        /**
         * Constructor.
         *
         * @param theBuilder
         * @param theQName
         */
        public mockEncoder(@Nonnull final XSStringBuilder theBuilder, @Nonnull final QName theQName) {
            builder = theBuilder;
            myQName = theQName;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean canEncodeValue(@Nonnull final IdPAttribute attribute,
                @Nonnull final IdPAttributeValue value) {
            return ! (value instanceof ByteAttributeValue);
        }

        /** {@inheritDoc} */
        @Override
        @Nullable protected XMLObject encodeValue(@Nonnull final IdPAttribute attribute,
                @Nonnull final IdPAttributeValue value) throws AttributeEncodingException {
            if (!(value instanceof StringAttributeValue)) {
                return null;
            }
            XSString result = builder.buildObject(myQName);
            result.setValue((String) value.getValue());
            return result;
        }
        
    }
}
