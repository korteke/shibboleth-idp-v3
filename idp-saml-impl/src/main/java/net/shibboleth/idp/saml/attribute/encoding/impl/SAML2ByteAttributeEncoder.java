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
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.saml.attribute.encoding.AbstractSAML2AttributeEncoder;
import net.shibboleth.idp.saml.attribute.encoding.AttributeMapperProcessor;
import net.shibboleth.idp.saml.attribute.encoding.SAMLEncoderSupport;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeMapper;
import net.shibboleth.idp.saml.attribute.mapping.impl.ByteAttributeValueMapper;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;

/**
 * {@link net.shibboleth.idp.attribute.AttributeEncoder} that produces a SAML 2 Attribute from an {@link IdPAttribute}
 * that contains <code>byte[]</code> values.
 */
public class SAML2ByteAttributeEncoder extends AbstractSAML2AttributeEncoder<ByteAttributeValue> implements
        AttributeMapperProcessor<RequestedAttribute, IdPRequestedAttribute> {

    /** {@inheritDoc} */
    @Override protected boolean canEncodeValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final IdPAttributeValue value) {
        return value instanceof ByteAttributeValue;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected XMLObject encodeValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final ByteAttributeValue value) throws AttributeEncodingException {
        return SAMLEncoderSupport.encodeByteArrayValue(attribute,
                AttributeValue.DEFAULT_ELEMENT_NAME, value.getValue(), encodeType());
    }

    /** {@inheritDoc} */
    @Override @Nonnull public void populateAttributeMapper(final
            AbstractSAMLAttributeMapper<RequestedAttribute, IdPRequestedAttribute> mapper) {
        mapper.setAttributeFormat(getNameFormat());
        mapper.setId(getMapperId());
        mapper.setSAMLName(getName());
        mapper.setValueMapper(new ByteAttributeValueMapper());
    }

}