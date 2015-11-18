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

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.xmlobject.KeyAuthority;
import net.shibboleth.utilities.java.support.xml.QNameSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;

/** Unmarshaller for {@link KeyAuthority}. */
@ThreadSafe
public class KeyAuthorityUnmarshaller extends AbstractXMLObjectUnmarshaller {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(KeyAuthorityUnmarshaller.class);

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
        KeyAuthority authority = (KeyAuthority) xmlObject;

        if (attribute.getLocalName().equals(KeyAuthority.VERIFY_DEPTH_ATTRIB_NAME)) {
            authority.setVerifyDepth(Integer.valueOf(attribute.getValue()));
        } else {
            QName attribQName = QNameSupport.getNodeQName(attribute);
            if (attribute.isId()) {
                authority.getUnknownAttributes().registerID(attribQName);
            }
            authority.getUnknownAttributes().put(attribQName, attribute.getValue());
        }
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject)
            throws UnmarshallingException {
        KeyAuthority authority = (KeyAuthority) parentXMLObject;

        if (childXMLObject instanceof KeyInfo) {
            authority.getKeyInfos().add((KeyInfo) childXMLObject);
        } else {
            log.debug("Ignorning unknown child element {}", childXMLObject.getElementQName());
        }
    }
}