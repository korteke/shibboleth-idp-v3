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
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/** An attribute value with an associated scope. */
@ThreadSafe
public class ScopedStringAttributeValue extends StringAttributeValue {

    /** Scope of the attribute value. */
    private final String scope;

    /**
     * Constructor.
     * 
     * @param attributeValue the value of the attribute
     * @param valueScope scope of the value
     */
    public ScopedStringAttributeValue(@Nonnull @NotEmpty final String attributeValue,
            @Nonnull @NotEmpty final String valueScope) {
        super(attributeValue);
        scope = Constraint.isNotNull(StringSupport.trimOrNull(valueScope), "Scope cannot be null or empty");
    }

    /**
     * Get the scope of the value.
     * 
     * @return scope of the value
     */
    @Nonnull @NotEmpty public final String getScope() {
        return scope;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String getDisplayValue() {
        return getValue() + '@' + scope;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(@Nullable final Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ScopedStringAttributeValue)) {
            return false;
        }

        final ScopedStringAttributeValue otherValue = (ScopedStringAttributeValue) obj;
        return java.util.Objects.equals(getValue(), otherValue.getValue())
                && java.util.Objects.equals(scope, otherValue.scope);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hashCode(getValue(), scope);
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NotEmpty public String toString() {
        return MoreObjects.toStringHelper(this).add("value", getValue()).add("scope", scope).toString();
    }

    /**
     * Returns an {@link EmptyAttributeValue} or {@link ScopedStringAttributeValue} as appropriate. This method should
     * be preferred over the constructor when the value may be null or empty.
     * 
     * @param value to inspect
     * @param scope of the value
     * @return {@link EmptyAttributeValue} or {@link ScopedStringAttributeValue}
     */
    public static IdPAttributeValue<?> valueOf(@Nullable final String value, @Nonnull @NotEmpty final String scope) {
        if (value == null) {
            return EmptyAttributeValue.NULL;
        } else if (value.length() == 0) {
            return EmptyAttributeValue.ZERO_LENGTH;
        } else {
            return new ScopedStringAttributeValue(value, scope);
        }
    }
}