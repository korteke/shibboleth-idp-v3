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

package net.shibboleth.idp.authn.principal;

import java.security.Principal;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;

/** Test Principal for testing requested authentication behavior. */
public final class TestPrincipal implements Principal {

    /** The class ref. */
    @Nonnull @NotEmpty private String value;

    /**
     * Constructor.
     * 
     * @param newValue the principal name
     */
    public TestPrincipal(@Nonnull @NotEmpty final String newValue) {
        value = Constraint.isNotNull(StringSupport.trimOrNull(newValue), "Value cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Nonnull @NotEmpty public String getName() {
        return value;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return value.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (other instanceof TestPrincipal) {
            return value.equals(((TestPrincipal) other).getName());
        }

        return false;
    }

    /** {@inheritDoc} */
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }

}