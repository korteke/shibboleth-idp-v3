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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.MoreObjects;

/** Base class for {@link IdPAttribute} values that are strings. */
public class StringAttributeValue implements IdPAttributeValue<String> {

    /** The attribute value. */
    @Nonnull @NotEmpty private final String value;

    /**
     * Constructor.
     * 
     * @param attributeValue the attribute value
     */
    public StringAttributeValue(@Nonnull @NotEmpty final String attributeValue) {
        value = Constraint.isNotEmpty(attributeValue, "Attribute value cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public final String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getDisplayValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof StringAttributeValue)) {
            return false;
        }

        final StringAttributeValue other = (StringAttributeValue) obj;
        return Objects.equals(value, other.value);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return value.hashCode();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }

    /**
     * Returns an {@link EmptyAttributeValue} or {@link StringAttributeValue} as appropriate. This method should be
     * preferred over the constructor when the value may be null or empty.
     * 
     * @param value to inspect
     * @return {@link EmptyAttributeValue} or {@link StringAttributeValue}
     */
    @Nonnull public static IdPAttributeValue<?> valueOf(@Nullable final String value) {
        if (value == null) {
            return EmptyAttributeValue.NULL;
        } else if (value.length() == 0) {
            return EmptyAttributeValue.ZERO_LENGTH;
        } else {
            return new StringAttributeValue(value);
        }
    }
}