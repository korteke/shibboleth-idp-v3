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

import com.google.common.base.MoreObjects;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An {@link IdPAttributeValue} that is empty. This class defines an enum to represent the various types of empty values
 * that can occur.
 */
public class EmptyAttributeValue implements IdPAttributeValue<EmptyAttributeValue.EmptyType> {

    /** Instance of null empty attribute value. */
    public static final EmptyAttributeValue NULL = new EmptyAttributeValue(EmptyType.NULL_VALUE);

    /** Instance of zero length attribute value. */
    public static final EmptyAttributeValue ZERO_LENGTH = new EmptyAttributeValue(EmptyType.ZERO_LENGTH_VALUE);

    /** Types of empty values. */
    public enum EmptyType {
        /** Value that is the Java null reference. */
        NULL_VALUE,

        /** Value with zero length. */
        ZERO_LENGTH_VALUE
    }

    /** Value of the attribute. */
    @Nonnull @NotEmpty private final EmptyType value;

    /**
     * Constructor.
     * 
     * @param attributeValue value of the attribute
     */
    public EmptyAttributeValue(@Nonnull final EmptyType attributeValue) {
        value = Constraint.isNotNull(attributeValue, "Empty value enumeration cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public EmptyType getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getDisplayValue() {
        return value.toString();
    }

    /** {@inheritDoc} */
    @Override public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof EmptyAttributeValue)) {
            return false;
        }

        final EmptyAttributeValue other = (EmptyAttributeValue) obj;
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
    
}