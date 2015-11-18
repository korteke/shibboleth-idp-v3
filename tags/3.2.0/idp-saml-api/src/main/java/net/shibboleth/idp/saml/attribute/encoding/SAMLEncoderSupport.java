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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.saml.xmlobject.ScopedValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/** Support class for encoding IdP Attributes and their value. */
public final class SAMLEncoderSupport {

    /** Class logger. */
    @Nonnull private static final Logger LOG = LoggerFactory.getLogger(SAMLEncoderSupport.class);

    /** Constructor. */
    private SAMLEncoderSupport() {

    }

    /**
     * Encodes a String value into a SAML attribute value element.
     * 
     * @param attribute attribute to be encoded
     * @param attributeValueElementName the element name to create
     * @param value value to encoded
     * @param withType whether to include xsi:type
     * 
     * @return the attribute value element or null if the given value was null or empty
     */
    @Nullable public static XMLObject encodeStringValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final QName attributeValueElementName, @Nullable final String value, final boolean withType) {
        Constraint.isNotNull(attribute, "Attribute cannot be null");
        Constraint.isNotNull(attributeValueElementName, "Attribute Element Name cannot be null");

        if (Strings.isNullOrEmpty(value)) {
            LOG.debug("Skipping empty value for attribute {}", attribute.getId());
            return null;
        }

        LOG.debug("Encoding value {} of attribute {}", value, attribute.getId());
        
        if (withType) {
            final XMLObjectBuilder<XSString> stringBuilder = (XMLObjectBuilder<XSString>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().<XSString>getBuilderOrThrow(
                            XSString.TYPE_NAME);
            final XSString samlAttributeValue =
                    stringBuilder.buildObject(attributeValueElementName, XSString.TYPE_NAME);
            samlAttributeValue.setValue(value);
            return samlAttributeValue;
        } else {
            final XMLObjectBuilder<XSAny> anyBuilder = (XMLObjectBuilder<XSAny>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().<XSAny>getBuilderOrThrow(XSAny.TYPE_NAME);
            final XSAny samlAttributeValue = anyBuilder.buildObject(attributeValueElementName);
            samlAttributeValue.setTextContent(value);
            return samlAttributeValue;
        }
    }

    /**
     * Base64 encodes a <code>byte[]</code> into a SAML attribute value element.
     * 
     * @param attribute attribute to be encoded
     * @param attributeValueElementName the element name to create
     * @param value value to encoded
     * @param withType whether to include xsi:type
     * 
     * @return the attribute value element or null if the given value was null or empty
     */
    @Nullable public static XMLObject encodeByteArrayValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final QName attributeValueElementName, @Nullable final byte[] value, final boolean withType) {
        Constraint.isNotNull(attribute, "Attribute cannot be null");
        Constraint.isNotNull(attributeValueElementName, "Attribute Element Name cannot be null");

        if (value == null || value.length == 0) {
            LOG.debug("Skipping empty value for attribute {}", attribute.getId());
            return null;
        }

        if (withType) {
            final XMLObjectBuilder<XSBase64Binary> binaryBuilder = (XMLObjectBuilder<XSBase64Binary>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().<XSBase64Binary>getBuilderOrThrow(
                            XSBase64Binary.TYPE_NAME);
            final XSBase64Binary samlAttributeValue =
                    binaryBuilder.buildObject(attributeValueElementName, XSBase64Binary.TYPE_NAME);
            samlAttributeValue.setValue(Base64Support.encode(value, Base64Support.UNCHUNKED));
            return samlAttributeValue;
        } else {
            final XMLObjectBuilder<XSAny> anyBuilder = (XMLObjectBuilder<XSAny>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().<XSAny>getBuilderOrThrow(XSAny.TYPE_NAME);
            final XSAny samlAttributeValue = anyBuilder.buildObject(attributeValueElementName);
            samlAttributeValue.setTextContent(Base64Support.encode(value, Base64Support.UNCHUNKED));
            return samlAttributeValue;
        }
    }

    /**
     * Encodes an {@link XMLObject} value in to a {@link XSAny} SAML attribute value.
     * 
     * @param attribute attribute to be encoded
     * @param attributeValueElementName the SAML 1 or SAML 1 attribute name
     * @param value value to encoded
     * 
     * @return the attribute value element or null if the given value was null or empty
     */
    @Nullable public static XMLObject encodeXMLObjectValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final QName attributeValueElementName, @Nullable final XMLObject value) {
        if (value == null) {
            LOG.debug("Skipping empty value for attribute {}", attribute.getId());
            return null;
        }
        Constraint.isNotNull(attribute, "Attribute cannot be null");
        Constraint.isNotNull(attributeValueElementName, "Attribute Element Name cannot be null");

        final XMLObjectBuilder<XSAny> attributeValueBuilder = (XMLObjectBuilder<XSAny>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSAny.TYPE_NAME);
        final XSAny samlAttributeValue = attributeValueBuilder.buildObject(attributeValueElementName);
        samlAttributeValue.getUnknownXMLObjects().add(value);

        return samlAttributeValue;
    }

    /**
     * Encode a {@link ScopedStringAttributeValue} value in to an SAML attribute value element using the
     * (older Shibboleth) sytnax where the scope is inside an XML attribute.
     * 
     * @param attribute attribute to be encoded
     * @param attributeValueElementName the element name to create
     * @param value value to encoded
     * @param scopeAttributeName the name that the attribute will be given
     * @param withType whether to include xsi:type
     * 
     * @return the attribute value element or null if the given value was null or empty
     */
    public static XMLObject encodeScopedStringValueAttribute(@Nonnull final IdPAttribute attribute,
            @Nonnull final QName attributeValueElementName, @Nullable final ScopedStringAttributeValue value,
            @Nonnull @NotEmpty final String scopeAttributeName, final boolean withType) {
        Constraint.isNotNull(attribute, "Attribute cannot be null");
        Constraint.isNotNull(attributeValueElementName, "Attribute Element Name cannot be null");
        Constraint.isNotNull(scopeAttributeName, "Scope Attribute Name cannot be null");

        if (null == value || Strings.isNullOrEmpty(value.getScope()) || Strings.isNullOrEmpty(value.getValue())) {
            LOG.debug("Skipping empty value (or contents) for attribute {}", attribute.getId());
            return null;
        }
        
        final XMLObjectBuilder<ScopedValue> scopedValueBuilder = (XMLObjectBuilder<ScopedValue>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(ScopedValue.TYPE_NAME);
        final ScopedValue scopedValue =
                withType ? scopedValueBuilder.buildObject(attributeValueElementName, ScopedValue.TYPE_NAME)
                        : scopedValueBuilder.buildObject(attributeValueElementName);

        scopedValue.setScopeAttributeName(scopeAttributeName);
        scopedValue.setScope(value.getScope());
        scopedValue.setValue(value.getValue());

        return scopedValue;
    }

    /**
     * Encode a {@link ScopedStringAttributeValue} value into a SAML attribute value element using
     * the "inline" syntax where the scope and value are combined into a string.
     * 
     * @param attribute attribute to be encoded
     * @param attributeValueElementName the element name to create
     * @param value value to encoded
     * @param scopeDelimiter the delimiter to put between the value and the scope
     * @param withType whether to include xsi:type
     * 
     * @return the attribute value element or null if the given value was null or empty
     */
    public static XMLObject encodeScopedStringValueInline(@Nonnull final IdPAttribute attribute,
            @Nonnull final QName attributeValueElementName, @Nullable final ScopedStringAttributeValue value,
            @Nonnull String scopeDelimiter, final boolean withType) {
        Constraint.isNotNull(attribute, "Attribute cannot be null");
        Constraint.isNotNull(attributeValueElementName, "Attribute Element Name cannot be null");
        Constraint.isNotNull(scopeDelimiter, "Scope delimiter cannot be null");

        if (null == value || Strings.isNullOrEmpty(value.getScope()) || Strings.isNullOrEmpty(value.getValue())) {
            LOG.debug("Skipping empty value (or contents) for attribute {}", attribute.getId());
            return null;
        }

        final StringBuilder builder =
                new StringBuilder(value.getValue()).append(scopeDelimiter).append(value.getScope());

        return encodeStringValue(attribute, attributeValueElementName, builder.toString(), withType);
    }
}