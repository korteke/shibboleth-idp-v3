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

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.saml.xmlobject.ScopedValue;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.AbstractXMLObjectBuilder;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

/**
 * Test for {@link SAMLEncoderSupport}.
 */
public class SAMLEncoderSupportTest  extends OpenSAMLInitBaseTestCase {
    
    /** Test values. */
    private final static String QNAME_LOCALPART = "myQName";
    private final static QName QNAME = new QName(QNAME_LOCALPART);
    private final static String STRING_VALUE = "TestValue";
    private final static String STRING_SCOPE = "TestScope";
    private final static String SCOPE_ATTRIBUTE_NAME = "Scpe"; //sic
    private final static String DELIMITER = "@";
    private final static IdPAttribute ATTR = new IdPAttribute("attr");
    private final static byte[] BYTE_ARRAY_VALUE = {1, 2, 3, 4, 5};
    private final static ScopedStringAttributeValue SCOPEDVAL = new ScopedStringAttributeValue(STRING_VALUE, STRING_SCOPE);
    
    @Test public void encodeStringValue() {
        
        try {
            SAMLEncoderSupport.encodeStringValue(null, QNAME, STRING_VALUE, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeStringValue(ATTR, null, STRING_VALUE, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        Assert.assertNull(SAMLEncoderSupport.encodeStringValue(ATTR, QNAME, "", true));
        Assert.assertNull(SAMLEncoderSupport.encodeStringValue(ATTR, QNAME, null, true));
        
        XMLObject obj = SAMLEncoderSupport.encodeStringValue(ATTR, QNAME, STRING_VALUE, true);
        Assert.assertEquals(obj.getElementQName().getLocalPart(), QNAME_LOCALPART);
        Assert.assertTrue(obj instanceof XSString);
        XSString str = (XSString) obj;
        
        Assert.assertEquals(str.getValue(), STRING_VALUE);
    }
    
    @Test public void encodeByteArrayValue() {
        
        try {
            SAMLEncoderSupport.encodeByteArrayValue(null, QNAME, BYTE_ARRAY_VALUE, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeByteArrayValue(ATTR, null, BYTE_ARRAY_VALUE, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        Assert.assertNull(SAMLEncoderSupport.encodeByteArrayValue(ATTR, QNAME, null, true));
        Assert.assertNull(SAMLEncoderSupport.encodeByteArrayValue(ATTR, QNAME, new byte[] {}, true));
        
        XMLObject obj = SAMLEncoderSupport.encodeByteArrayValue(ATTR, QNAME, BYTE_ARRAY_VALUE, true);
        Assert.assertEquals(obj.getElementQName().getLocalPart(), QNAME_LOCALPART);
        Assert.assertTrue(obj instanceof XSBase64Binary);
        XSBase64Binary str = (XSBase64Binary) obj;
        
        Assert.assertEquals(Base64Support.decode(str.getValue()), BYTE_ARRAY_VALUE);
    }

    @Test public void encodeXmlObjectValue() {
        
        final NameID objToEncode= new NameIDBuilder().buildObject();
        objToEncode.setValue(STRING_VALUE);
        
        try {
            SAMLEncoderSupport.encodeXMLObjectValue(null, QNAME, objToEncode);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeXMLObjectValue(ATTR, null, objToEncode);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        Assert.assertNull(SAMLEncoderSupport.encodeXMLObjectValue(ATTR, QNAME, null));
        
        XMLObject obj = SAMLEncoderSupport.encodeXMLObjectValue(ATTR, QNAME, objToEncode);
        Assert.assertEquals(obj.getElementQName().getLocalPart(), QNAME_LOCALPART);
        Assert.assertTrue(obj instanceof XSAny);
        XSAny any = (XSAny) obj;
        List<XMLObject> what = any.getUnknownXMLObjects();
       
        Assert.assertEquals(what.size(),1);
        Assert.assertTrue(what.get(0) instanceof NameID);
        
        NameID other = (NameID) what.get(0);
        Assert.assertEquals(other.getValue(), STRING_VALUE);
    }

    @Test public void encodeScopedStringValueAttribute() {
        
        XMLObjectProviderRegistrySupport.registerObjectProvider(ScopedValue.TYPE_NAME, new ScopedValueBuilder(), new ScopedValueMarshaller(), new ScopedValueUnmarshaller());
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueAttribute(null, QNAME, SCOPEDVAL, SCOPE_ATTRIBUTE_NAME, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueAttribute(ATTR, null,  SCOPEDVAL, SCOPE_ATTRIBUTE_NAME, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueAttribute(ATTR, QNAME, SCOPEDVAL, null, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }

        Assert.assertNull(SAMLEncoderSupport.encodeScopedStringValueAttribute(ATTR, QNAME, null, SCOPE_ATTRIBUTE_NAME, true));
        
        XMLObject obj = SAMLEncoderSupport.encodeScopedStringValueAttribute(ATTR, QNAME, SCOPEDVAL, SCOPE_ATTRIBUTE_NAME, true);
        Assert.assertEquals(obj.getElementQName().getLocalPart(), QNAME_LOCALPART);
        Assert.assertTrue(obj instanceof ScopedValue);
        ScopedValue sv = (ScopedValue) obj;
        
        Assert.assertEquals(sv.getValue(), STRING_VALUE);
        Assert.assertEquals(sv.getScope(), STRING_SCOPE);
    }

    @Test public void encodeScopedStringValueInline() {
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueInline(null, QNAME, SCOPEDVAL, DELIMITER, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueInline(ATTR, null,  SCOPEDVAL, DELIMITER, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }
        
        try {
            SAMLEncoderSupport.encodeScopedStringValueInline(ATTR, QNAME, SCOPEDVAL, null, true);
            Assert.fail("Missed contraint");
        } catch (ConstraintViolationException ex) {
            //OK
        }

        Assert.assertNull(SAMLEncoderSupport.encodeScopedStringValueInline(ATTR, QNAME, null, DELIMITER, true));
        
        XMLObject obj = SAMLEncoderSupport.encodeScopedStringValueInline(ATTR, QNAME, SCOPEDVAL, DELIMITER, true);
        Assert.assertEquals(obj.getElementQName().getLocalPart(), QNAME_LOCALPART);
        Assert.assertTrue(obj instanceof XSString);
        XSString str = (XSString) obj;
        
        Assert.assertEquals(str.getValue(), STRING_VALUE + DELIMITER + STRING_SCOPE);
        
        
        }
    
    //
    // The rest of this function is cut and paste from the real providers in idp-saml-imp
    //
    
    private class ScopedValueBuilder extends AbstractXMLObjectBuilder<ScopedValue> {

        public ScopedValue buildObject(String namespaceURI, String localName, String namespacePrefix) {
            return new ScopedValueImpl(namespaceURI, localName, namespacePrefix);
        }
    }

    private class ScopedValueImpl extends XSAnyImpl implements ScopedValue {

        /** Scope of this string element. */
        private String scope;

        /** Scope attribute name for this element. */
        private String scopeAttributeName;

        /**
         * Constructor.
         * 
         * @param namespaceURI the namespace the element is in
         * @param elementLocalName the local name of the XML element this Object represents
         * @param namespacePrefix the prefix for the given namespace
         */
        protected ScopedValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
            super(namespaceURI, elementLocalName, namespacePrefix);
        }

        /** {@inheritDoc} */
        public String getScope() {
            return scope;
        }

        /** {@inheritDoc} */
        public String getScopeAttributeName() {
            return scopeAttributeName;
        }

        /** {@inheritDoc} */
        public void setScope(String newScope) {
            scope = prepareForAssignment(scope, newScope);
            if (scope != null && scopeAttributeName != null) {
                getUnknownAttributes().put(new QName(scopeAttributeName), scope);
            }
        }

        /** {@inheritDoc} */
        public void setScopeAttributeName(String newScopeAttributeName) {
            if (scopeAttributeName != null) {
                QName oldName = new QName(scopeAttributeName);
                if (getUnknownAttributes().containsKey(oldName)) {
                    getUnknownAttributes().remove(oldName);
                }
            }

            scopeAttributeName = prepareForAssignment(scopeAttributeName, newScopeAttributeName);

            if (scope != null) {
                getUnknownAttributes().put(new QName(scopeAttributeName), scope);
            }
        }

        /** {@inheritDoc} */
        public String getValue() {
            return getTextContent();
        }

        /** {@inheritDoc} */
        public void setValue(String newValue) {
            setTextContent(newValue);
        }
        
    }
    
    private class ScopedValueMarshaller extends AbstractXMLObjectMarshaller {

        /** {@inheritDoc} */
        protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
            ScopedValue scopedValue = (ScopedValue) xmlObject;

            if (null != scopedValue.getScopeAttributeName()) {
                domElement.setAttributeNS(null, scopedValue.getScopeAttributeName(), scopedValue.getScope());
            }

        }

        /** {@inheritDoc} */
        protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {
            ScopedValue scopedValue = (ScopedValue) xmlObject;

            ElementSupport.appendTextContent(domElement, scopedValue.getValue());
        }
    }
    
    private class ScopedValueUnmarshaller extends AbstractXMLObjectUnmarshaller {

        /** {@inheritDoc} */
        protected void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
            ScopedValue sv = (ScopedValue) xmlObject;

            if (Strings.isNullOrEmpty(sv.getScopeAttributeName())) {
                sv.setScopeAttributeName(attribute.getName());
                sv.setScope(attribute.getValue());
            }

        }

        /** {@inheritDoc} */
        protected void processElementContent(XMLObject xmlObject, String elementContent) {
            ScopedValue sv = (ScopedValue) xmlObject;

            sv.setValue(elementContent);
        }
    }
}
