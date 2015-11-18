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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeValueMapper;
import net.shibboleth.utilities.java.support.codec.Base64Support;

import org.opensaml.core.xml.XMLObject;

/**
 * Mapping extract a {@link ByteAttributeValue} from an AttributeValue.
 */
public class ByteAttributeValueMapper extends AbstractSAMLAttributeValueMapper {

    /** {@inheritDoc} */
    @Nullable protected IdPAttributeValue<?> decodeValue(@Nonnull final XMLObject object) {
        final String value = getStringValue(object);
        if (null == value) {
            return null;
        }
        final byte[] decoded = Base64Support.decode(value);
        if (null == decoded) {
            return null;
        }
        return ByteAttributeValue.valueOf(decoded);
    }

    /** {@inheritDoc} */
    @Nonnull protected String getAttributeTypeName() {
        return "Base64Encoded";
    }
}
