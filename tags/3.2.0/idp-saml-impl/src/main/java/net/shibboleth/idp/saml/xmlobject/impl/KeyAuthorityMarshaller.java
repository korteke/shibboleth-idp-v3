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

package net.shibboleth.idp.saml.xmlobject.impl;

import java.util.Map.Entry;

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.xmlobject.KeyAuthority;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Marshaller for {@link KeyAuthority}. */
@ThreadSafe
public class KeyAuthorityMarshaller extends AbstractXMLObjectMarshaller {

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        KeyAuthority keyAuthority = (KeyAuthority) xmlObject;

        if (keyAuthority.getVerifyDepth() != null) {
            domElement.setAttributeNS(null, KeyAuthority.VERIFY_DEPTH_ATTRIB_NAME, keyAuthority.getVerifyDepth()
                    .toString());
        }

        Attr attr;
        for (Entry<QName, String> entry : keyAuthority.getUnknownAttributes().entrySet()) {
            attr = AttributeSupport.constructAttribute(domElement.getOwnerDocument(), entry.getKey());
            attr.setValue(entry.getValue());
            domElement.setAttributeNodeNS(attr);
            if (XMLObjectProviderRegistrySupport.isIDAttribute(entry.getKey())
                    || keyAuthority.getUnknownAttributes().isIDAttribute(entry.getKey())) {
                attr.getOwnerElement().setIdAttributeNode(attr, true);
            }
        }

    }
}