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

package net.shibboleth.idp.saml.authn.principal;

import javax.annotation.Nonnull;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;

import net.shibboleth.idp.authn.principal.CloneablePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;

/** Principal based on a SAML AuthnContextClassRef. */
public final class AuthnContextClassRefPrincipal implements CloneablePrincipal {

    /** The class ref. */
    @Nonnull @NotEmpty private String authnContextClassRef;

    /**
     * Constructor.
     * 
     * @param classRef the class reference URI
     */
    public AuthnContextClassRefPrincipal(@Nonnull @NotEmpty final String classRef) {
        authnContextClassRef = Constraint.isNotNull(
                StringSupport.trimOrNull(classRef), "AuthnContextClassRef cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Nonnull @NotEmpty public String getName() {
        return authnContextClassRef;
    }
    
    /**
     * Returns the value as a SAML {@link AuthnContextClassRef}.
     * 
     * @return  the principal value in the form of an {@link AuthnContextClassRef}
     */
    @Nonnull public AuthnContextClassRef getAuthnContextClassRef() {
        AuthnContextClassRef ref = (AuthnContextClassRef) Constraint.isNotNull(
                XMLObjectSupport.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME),
                    "No builder for AuthnContextClassRef").buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextClassRef(getName());
        return ref;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return authnContextClassRef.hashCode();
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

        if (other instanceof AuthnContextClassRefPrincipal) {
            return authnContextClassRef.equals(((AuthnContextClassRefPrincipal) other).getName());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("authnContextClassRef", authnContextClassRef).toString();
    }

    /** {@inheritDoc} */
    public AuthnContextClassRefPrincipal clone() throws CloneNotSupportedException {
        AuthnContextClassRefPrincipal copy = (AuthnContextClassRefPrincipal) super.clone();
        copy.authnContextClassRef = authnContextClassRef;
        return copy;
    }
}