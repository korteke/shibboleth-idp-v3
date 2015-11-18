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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.Attribute;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for {@link AbstractSAML2AttributeEncoder}.
 */
public class AbstractSAML2AttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    private XSStringBuilder theBuilder;

    private QName theQName = new QName("LocalQNAME");

    private final String MY_NAME = "myName";

    private final String MY_NAMESPACE = "myNameSpace";

    private final String ATTRIBUTE_ID = "attrID";

    private final String ATTRIBUTE_VALUE_1 = "attrValOne";

    private final String ATTRIBUTE_VALUE_2 = "attrValueTwo";

    private final String FRIENDLY_NAME = "friendly";
    
    @BeforeClass public void initTest() throws ComponentInitializationException {
        theBuilder = new XSStringBuilder();
    }

    @Test public void abstractSAML2AttributeEncoder() throws ComponentInitializationException, AttributeEncodingException {
        AbstractSAML2AttributeEncoder encoder = new mockEncoder();
        
        encoder.setName(MY_NAME);
        encoder.setNameFormat(MY_NAMESPACE);
        encoder.setFriendlyName(FRIENDLY_NAME);
        encoder.initialize();
        // Use literal here to catch things being edited by mistake
        Assert.assertEquals(encoder.getProtocol(), "urn:oasis:names:tc:SAML:2.0:protocol");

                
        IdPAttribute attr = new IdPAttribute(ATTRIBUTE_ID);
        attr.setValues(Arrays.asList(new StringAttributeValue(ATTRIBUTE_VALUE_1),
                new StringAttributeValue(ATTRIBUTE_VALUE_2)));
        
        XMLObject obj = encoder.encode(attr);
        
        Assert.assertTrue(obj instanceof Attribute);
        
        List<XMLObject> result = ((Attribute) obj).getAttributeValues();
        
        Assert.assertEquals(result.size(), 2);
        Set<String> resultSet = new HashSet<>(2); 
        for (XMLObject o: result) {
            Assert.assertTrue(o instanceof XSString);
            resultSet.add(((XSString) o).getValue());
        }
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_1));
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_2));
    }
    
    @Test public void friendlyName() throws ComponentInitializationException {
        AbstractSAML2AttributeEncoder encoder = new mockEncoder();
        Assert.assertNull(encoder.getFriendlyName());
        encoder.setName(MY_NAME);
        encoder.setNameFormat(MY_NAMESPACE);
        encoder.initialize();
        
        encoder = new mockEncoder();
        Assert.assertNull(encoder.getFriendlyName());
        encoder.setName(MY_NAME);
        encoder.setFriendlyName(FRIENDLY_NAME);
        encoder.setNameFormat(MY_NAMESPACE);
        Assert.assertEquals(encoder.getFriendlyName(), FRIENDLY_NAME);
        encoder.initialize();
        Assert.assertEquals(encoder.getFriendlyName(), FRIENDLY_NAME);
        try {
            encoder.setFriendlyName(FRIENDLY_NAME);
            Assert.fail();
        } catch (UnmodifiableComponentException ex) {
            //OK
        }
    }
    
    protected class mockEncoder extends AbstractSAML2AttributeEncoder {
        
        /** {@inheritDoc} */
        protected boolean canEncodeValue(IdPAttribute attribute, IdPAttributeValue value) {
            return ! (value instanceof ByteAttributeValue);
        }

        /** {@inheritDoc} */
        protected XMLObject encodeValue(IdPAttribute attribute, IdPAttributeValue value) throws AttributeEncodingException {
            if (!(value instanceof StringAttributeValue)) {
                return null;
            }
            XSString result = theBuilder.buildObject(theQName);
            result.setValue((String) value.getValue());
            return result;
        }
        
    }

}
