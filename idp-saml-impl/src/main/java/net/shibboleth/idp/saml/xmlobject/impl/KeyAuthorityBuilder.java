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

import net.shibboleth.idp.saml.xmlobject.ExtensionsConstants;
import net.shibboleth.idp.saml.xmlobject.KeyAuthority;

import org.opensaml.core.xml.AbstractXMLObjectBuilder;

/** Builder of {@link KeyAuthority} objects. */
@ThreadSafe
public class KeyAuthorityBuilder extends AbstractXMLObjectBuilder<KeyAuthority> {

    /** {@inheritDoc} */
    public KeyAuthority buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new KeyAuthorityImpl(namespaceURI, localName, namespacePrefix);
    }

    /**
     * Build a KeyAuthority element with the default namespace prefix and element name.
     * 
     * @return a new instance of {@link KeyAuthority}
     */
    public KeyAuthority buildObject() {
        return buildObject(ExtensionsConstants.SHIB_MDEXT10_NS, KeyAuthority.DEFAULT_ELEMENT_LOCAL_NAME,
                ExtensionsConstants.SHIB_MDEXT10_PREFIX);
    }
}