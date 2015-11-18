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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.AbstractSAML1NameIdentifierEncoder;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.profile.SAML1ObjectSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * {@link net.shibboleth.idp.saml.nameid.NameIdentifierAttributeEncoder} that encodes the first String value of an
 * {@link net.shibboleth.idp.attribute.IdPAttribute} to a SAML 1 {@link NameIdentifier}.
 */
public class SAML1StringNameIdentifierEncoder extends AbstractSAML1NameIdentifierEncoder {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML1StringNameIdentifierEncoder.class);

    /** Identifier builder. */
    @Nonnull private final SAMLObjectBuilder<NameIdentifier> identifierBuilder;

    /** The format of the name identifier. */
    @Nullable private String format;

    /** The security or administrative domain that qualifies the name identifier. */
    @Nullable private String qualifier;

    /** Constructor. */
    public SAML1StringNameIdentifierEncoder() {
        identifierBuilder =
                (SAMLObjectBuilder<NameIdentifier>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(
                        NameIdentifier.DEFAULT_ELEMENT_NAME);
        if (identifierBuilder == null) {
            throw new ConstraintViolationException("Builder unavailable for NameIdentifier objects");
        }
        format = NameIdentifier.UNSPECIFIED;
    }

    /**
     * Get the format of the name identifier.
     * 
     * @return format of the name identifier
     */
    @Nullable public final String getNameFormat() {
        return format;
    }

    /**
     * Set the format of the name identifier.
     * 
     * @param nameFormat format of the name identifier
     */
    public final void setNameFormat(@Nullable final String nameFormat) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        format = StringSupport.trimOrNull(nameFormat);
    }

    /**
     * Get the security or administrative domain that qualifies the name identifier.
     * 
     * @return security or administrative domain that qualifies the name identifier
     */
    @Nullable public final String getNameQualifier() {
        return qualifier;
    }

    /**
     * Set the security or administrative domain that qualifies the name identifier.
     * 
     * @param nameQualifier security or administrative domain that qualifies the name identifier
     */
    @Nullable public final void setNameQualifier(final String nameQualifier) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        qualifier = StringSupport.trimOrNull(nameQualifier);
    }

    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final String input) {
        return SAML1ObjectSupport.areNameIdentifierFormatsEquivalent(input, format);
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public NameIdentifier encode(@Nonnull final IdPAttribute attribute) throws AttributeEncodingException {
        final String attributeId = attribute.getId();

        final Collection<IdPAttributeValue<?>> attributeValues = attribute.getValues();
        if (attributeValues == null || attributeValues.isEmpty()) {
            throw new AttributeEncodingException("Attribute " + attribute.getId()
                    + " does not contain any values to encode");
        }

        NameIdentifier nameId = identifierBuilder.buildObject();
        nameId.setFormat(format);
        nameId.setNameQualifier(qualifier);

        for (final IdPAttributeValue attrValue : attributeValues) {
            if (attrValue == null || attrValue.getValue() == null) {
                log.debug("Skipping null value of attribute {}", attributeId);
                continue;
            }
            
            final Object value = attrValue.getValue();
            if (value instanceof String) {
                // Check for empty or all-whitespace, but don't trim.
                if (StringSupport.trimOrNull((String) value) == null) {
                    log.debug("Skipping all-whitespace value of attribute {}", attributeId);
                    continue;
                }
                nameId.setValue((String) value);
                return nameId;
            } else {
                log.debug("Skipping unsupported value of type {} of attribute {}", value.getClass().getName(),
                        attributeId);
                continue;
            }
        }
        throw new AttributeEncodingException("Attribute '" + attributeId + "' did not contain any encodable values");
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SAML1StringNameIdentifierEncoder)) {
            return false;
        }

        final SAML1StringNameIdentifierEncoder other = (SAML1StringNameIdentifierEncoder) obj;
        return java.util.Objects.equals(getNameFormat(), other.getNameFormat())
                && java.util.Objects.equals(getNameQualifier(), other.getNameQualifier());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(getNameFormat(), getNameQualifier(), getProtocol(),
                SAML1StringNameIdentifierEncoder.class);
    }
    
}