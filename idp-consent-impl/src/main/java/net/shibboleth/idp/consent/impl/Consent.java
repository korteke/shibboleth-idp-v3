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

package net.shibboleth.idp.consent.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;

/**
 * Represents consent.
 * 
 * A consent object consists of an identifier, a value (usually a hash) representing the thing being consented to, and a
 * boolean which indicates whether consent is approved or denied.
 */
public class Consent extends AbstractIdentifiableInitializableComponent {

    /** Wildcard used to represent consent to any ID or value. */
    @Nonnull @NotEmpty public static final String WILDCARD = "*";

    /** Consent value. */
    @Nullable private String value;

    /** Whether consent is approved. */
    @Nullable private boolean approved;

    /**
     * Get the consent value.
     * 
     * @return the consent value
     */
    @Nullable public String getValue() {
        return value;
    }

    /**
     * Whether consent is approved or denied.
     * 
     * @return true if consent is approved
     */
    @Nullable public boolean isApproved() {
        return approved;
    }

    /**
     * Set the consent value.
     * 
     * @param val the consent value
     */
    public void setValue(@Nonnull @NotEmpty final String val) {
        value = Constraint.isNotNull(StringSupport.trimOrNull(val), "The value cannot be null or empty");
    }

    /**
     * Set whether consent is approved.
     * 
     * @param flag true if consent is approved
     */
    public void setApproved(final boolean flag) {
        approved = flag;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Consent)) {
            return false;
        }

        final Consent other = (Consent) obj;

        return Objects.equals(getId(), other.getId()) && Objects.equals(getValue(), other.getValue())
                && Objects.equals(isApproved(), other.isApproved());
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return Objects.hash(getId(), getValue(), isApproved());
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("value", getValue())
                .add("isApproved", isApproved())
                .toString();
    }

}
