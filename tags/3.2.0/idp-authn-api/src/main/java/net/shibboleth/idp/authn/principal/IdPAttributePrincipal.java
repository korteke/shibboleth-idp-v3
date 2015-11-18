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

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.MoreObjects;

/** Principal that wraps an {@link IdPAttribute}. */
public class IdPAttributePrincipal implements Principal {

    /** The attributes. */
    @Nonnull private IdPAttribute attribute;

    /**
     * Constructor.
     * 
     * @param attr the attribute
     */
    public IdPAttributePrincipal(@Nonnull final IdPAttribute attr) {
        attribute = Constraint.isNotNull(attr, "IdPAttribute cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getName() {
        return attribute.getId();
    }
    
    /**
     * Get the {@link IdPAttribute}.
     * 
     * @return the attribute
     */
    @Nonnull public IdPAttribute getAttribute() {
        return attribute;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (other instanceof IdPAttributePrincipal) {
            return attribute.equals(((IdPAttributePrincipal) other).getAttribute());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("attribute", attribute).toString();
    }
    
}