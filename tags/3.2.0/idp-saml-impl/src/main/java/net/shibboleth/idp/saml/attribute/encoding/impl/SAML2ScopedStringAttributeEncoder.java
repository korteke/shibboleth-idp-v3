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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.AbstractSAML2AttributeEncoder;
import net.shibboleth.idp.saml.attribute.encoding.AttributeMapperProcessor;
import net.shibboleth.idp.saml.attribute.encoding.SAMLEncoderSupport;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeMapper;
import net.shibboleth.idp.saml.attribute.mapping.impl.ScopedStringAttributeValueMapper;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link net.shibboleth.idp.attribute.AttributeEncoder} that produces a SAML 2 Attribute from an
 * {@link net.shibboleth.idp.attribute.IdPAttribute} that contains scoped string values.
 */
public class SAML2ScopedStringAttributeEncoder extends AbstractSAML2AttributeEncoder<ScopedStringAttributeValue>
        implements AttributeMapperProcessor<RequestedAttribute, IdPRequestedAttribute> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML2ScopedStringAttributeEncoder.class);

    /** Type of scoping to use. */
    @Nullable private String scopeType;

    /** Delimiter used for "inline" scopeType. */
    @Nullable private String scopeDelimiter;

    /** Attribute name used for "attribute" scopeType. */
    @Nullable private String scopeAttributeName;
    
    /**
     * Constructor.
     *
     */
    public SAML2ScopedStringAttributeEncoder() {
        scopeDelimiter = "@";
        scopeAttributeName = "Scope";
        scopeType = "inline";
    }

    /**
     * Get the name of the non-inline scope-carrying attribute.
     * 
     * @return the name of the scope-carrying attribute
     */
    @Nullable public String getScopeAttributeName() {
        return scopeAttributeName;
    }

    /**
     * Get the scope delimiter.
     * 
     * @return the scope delimiter
     */
    @Nullable public String getScopeDelimiter() {
        return scopeDelimiter;
    }

    /**
     * Get the scope syntax type.
     * 
     * @return the type of scope syntax to use
     */
    @Nullable public String getScopeType() {
        return scopeType;
    }

    /**
     * Set the name of the non-inline scope-carrying attribute.
     * 
     * @param newScopeAttribute the name to set
     */
    public void setScopeAttributeName(@Nullable final String newScopeAttribute) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scopeAttributeName = StringSupport.trimOrNull(newScopeAttribute);
    }

    /**
     * Set the scope delimiter.
     * 
     * @param newScopeDelimiter delimiter to set
     */
    public void setScopeDelimiter(@Nullable final String newScopeDelimiter) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scopeDelimiter = StringSupport.trimOrNull(newScopeDelimiter);
    }

    /**
     * Set the scope syntax type.
     * 
     * @param newScopeType the scope syntax type
     */
    public void setScopeType(@Nullable final String newScopeType) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scopeType = StringSupport.trimOrNull(newScopeType);
    }

    /**
     * Ensures that the values we will need are all there.
     * 
     * {@inheritDoc}
     */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == getScopeType()) {
            log.debug("Scope type not set, assuming \"inline\"");
            scopeType = "inline";
        }

        if ("attribute".equals(getScopeType())) {
            if (null == getScopeAttributeName()) {
                throw new ComponentInitializationException(
                        "Encoder of type \"attribute\" must specify a scope attribute name");
            }
        } else if ("inline".equals(getScopeType())) {
            if (null == getScopeDelimiter()) {
                throw new ComponentInitializationException("Encoder of type \"inline\" must specify a scope delimiter");
            }
        } else {
            throw new ComponentInitializationException("Encoder scope type must be set to \"inline\" or \"attribute\"");
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean canEncodeValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final IdPAttributeValue value) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        return value instanceof ScopedStringAttributeValue;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected XMLObject encodeValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final ScopedStringAttributeValue value) throws AttributeEncodingException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if ("attribute".equals(getScopeType())) {
            return SAMLEncoderSupport.encodeScopedStringValueAttribute(attribute,
                    AttributeValue.DEFAULT_ELEMENT_NAME, value, getScopeAttributeName(), encodeType());
        } else {
            return SAMLEncoderSupport.encodeScopedStringValueInline(
                    attribute, AttributeValue.DEFAULT_ELEMENT_NAME, value, getScopeDelimiter(), encodeType());
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull public void populateAttributeMapper(
            final AbstractSAMLAttributeMapper<RequestedAttribute, IdPRequestedAttribute> mapper) {
        mapper.setAttributeFormat(getNameFormat());
        mapper.setId(getMapperId());
        mapper.setSAMLName(getName());
        final ScopedStringAttributeValueMapper foo = new ScopedStringAttributeValueMapper();
        foo.setDelimiter(getScopeDelimiter());
        mapper.setValueMapper(foo);
    }

}