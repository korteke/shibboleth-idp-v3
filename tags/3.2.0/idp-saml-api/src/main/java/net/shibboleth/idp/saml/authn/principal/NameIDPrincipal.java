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

import net.shibboleth.idp.authn.principal.CloneablePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.core.xml.util.XMLObjectSupport.CloneOutputOption;
import org.opensaml.saml.saml2.core.NameID;

/**
 * Principal based on the SAML2 {@link NameID}.
 */
public class NameIDPrincipal implements CloneablePrincipal {

    /** The NameID. */
    @Nonnull private NameID nameID;

    /** Serialized form of NameID . */
    @Nonnull @NotEmpty private String name;
    
    /** Constructor. 
     * @param theNameID the NameID which is wrapped.
     */
    public NameIDPrincipal(@Nonnull final NameID theNameID) {
        nameID = Constraint.isNotNull(theNameID, "Supplied NameID cannot be null");
        try {
            name = SerializeSupport.nodeToString(Constraint.isNotNull(XMLObjectSupport.getMarshaller(theNameID),
                    "No marshaller for NameId").marshall(theNameID));
        } catch (MarshallingException e) {
            throw new XMLRuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return name;
    }
    
    /** Get the {@link NameID}. 
     * @return the nameID
     */
    @Nonnull public NameID getNameID() {
        return nameID;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return nameID.hashCode();
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

        if (other instanceof NameIDPrincipal) {
            return nameID.equals(((NameIDPrincipal) other).getNameID());
        }

        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public NameIDPrincipal clone() throws CloneNotSupportedException {
        NameIDPrincipal copy = (NameIDPrincipal) super.clone();
        try {
            copy.nameID = XMLObjectSupport.cloneXMLObject(nameID, CloneOutputOption.RootDOMInNewDocument);
            copy.name = SerializeSupport.nodeToString(
                    Constraint.isNotNull(XMLObjectSupport.getMarshaller(copy.nameID),
                            "No marshaller for nameID").marshall(copy.nameID));
        } catch (MarshallingException | UnmarshallingException e) {
            throw new XMLRuntimeException(e);
        }
        return copy;
    }

}
