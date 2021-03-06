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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.NameIDType;

import com.google.common.base.MoreObjects;

/** A {@link XMLObjectAttributeValue} value for an {@link net.shibboleth.idp.attribute.IdPAttribute}. */
public class XMLObjectAttributeValue implements IdPAttributeValue<XMLObject> {

    /** Value of the attribute. */
    private final XMLObject value;

    /**
     * Constructor.
     * 
     * @param attributeValue value of the attribute
     */
    public XMLObjectAttributeValue(@Nonnull final XMLObject attributeValue) {
        value = Constraint.isNotNull(attributeValue, "Attribute value cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    public final XMLObject getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getDisplayValue() {
        if (value instanceof NameIDType) {
            return ((NameIDType) value).getValue();
        }
        return "(XML data)";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof XMLObjectAttributeValue)) {
            return false;
        }

        XMLObjectAttributeValue other = (XMLObjectAttributeValue) obj;
        return value.equals(other.value);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }
}