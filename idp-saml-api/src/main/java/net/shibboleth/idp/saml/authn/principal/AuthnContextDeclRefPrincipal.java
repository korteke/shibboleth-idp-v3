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
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;

import net.shibboleth.idp.authn.principal.CloneablePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;

/** Principal based on a SAML AuthnContextDeclRef. */
public final class AuthnContextDeclRefPrincipal implements CloneablePrincipal {

    /** The decl ref. */
    @Nonnull @NotEmpty private String authnContextDeclRef;

    /**
     * Constructor.
     * 
     * @param declRef the decl ref URI
     */
    public AuthnContextDeclRefPrincipal(@Nonnull @NotEmpty final String declRef) {
        authnContextDeclRef = Constraint.isNotNull(
                StringSupport.trimOrNull(declRef), "AuthnContextDeclRef cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Nonnull @NotEmpty public String getName() {
        return authnContextDeclRef;
    }
    
    /**
     * Returns the value as a SAML {@link AuthnContextDeclRef}.
     * 
     * @return  the principal value in the form of an {@link AuthnContextDeclRef}
     */
    @Nonnull public AuthnContextDeclRef getAuthnContextDeclRef() {
        AuthnContextDeclRef ref = (AuthnContextDeclRef) Constraint.isNotNull(
                XMLObjectSupport.getBuilder(AuthnContextDeclRef.DEFAULT_ELEMENT_NAME),
                    "No builder for AuthnContextDeclRef").buildObject(AuthnContextDeclRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextDeclRef(getName());
        return ref;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return authnContextDeclRef.hashCode();
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

        if (other instanceof AuthnContextDeclRefPrincipal) {
            return authnContextDeclRef.equals(((AuthnContextDeclRefPrincipal) other).getName());
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("authnContextDeclRef", authnContextDeclRef).toString();
    }

    /** {@inheritDoc} */
    public AuthnContextDeclRefPrincipal clone() throws CloneNotSupportedException {
        AuthnContextDeclRefPrincipal copy = (AuthnContextDeclRefPrincipal) super.clone();
        copy.authnContextDeclRef = authnContextDeclRef;
        return copy;
    }
}