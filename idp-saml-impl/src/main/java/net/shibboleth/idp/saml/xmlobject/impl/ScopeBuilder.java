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
import net.shibboleth.idp.saml.xmlobject.Scope;

import org.opensaml.core.xml.AbstractXMLObjectBuilder;

/** Builder of {@link Scope} objects. */
@ThreadSafe
public class ScopeBuilder extends AbstractXMLObjectBuilder<Scope> {

    /** {@inheritDoc} */
    public Scope buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new ScopeImpl(namespaceURI, localName, namespacePrefix);
    }

    /**
     * Build a Scope element with the default namespace prefix and element name.
     * 
     * @return a new instance of {@link Scope}
     */
    public Scope buildObject() {
        return buildObject(ExtensionsConstants.SHIB_MDEXT10_NS, Scope.DEFAULT_ELEMENT_LOCAL_NAME,
                ExtensionsConstants.SHIB_MDEXT10_PREFIX);
    }
}