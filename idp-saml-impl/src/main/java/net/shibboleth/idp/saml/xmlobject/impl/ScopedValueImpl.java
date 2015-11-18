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

import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.xmlobject.ScopedValue;

import org.opensaml.core.xml.schema.impl.XSAnyImpl;

/** Concrete implementation of {@link ScopedValue}. */
@NotThreadSafe
public class ScopedValueImpl extends XSAnyImpl implements ScopedValue {

    /** Scope of this string element. */
    private String scope;

    /** Scope attribute name for this element. */
    private String scopeAttributeName;

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected ScopedValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getScope() {
        return scope;
    }

    /** {@inheritDoc} */
    public String getScopeAttributeName() {
        return scopeAttributeName;
    }

    /** {@inheritDoc} */
    public void setScope(String newScope) {
        scope = prepareForAssignment(scope, newScope);
        if (scope != null && scopeAttributeName != null) {
            getUnknownAttributes().put(new QName(scopeAttributeName), scope);
        }
    }

    /** {@inheritDoc} */
    public void setScopeAttributeName(String newScopeAttributeName) {
        if (scopeAttributeName != null) {
            QName oldName = new QName(scopeAttributeName);
            if (getUnknownAttributes().containsKey(oldName)) {
                getUnknownAttributes().remove(oldName);
            }
        }

        scopeAttributeName = prepareForAssignment(scopeAttributeName, newScopeAttributeName);

        if (scope != null) {
            getUnknownAttributes().put(new QName(scopeAttributeName), scope);
        }
    }

    /** {@inheritDoc} */
    public String getValue() {
        return getTextContent();
    }

    /** {@inheritDoc} */
    public void setValue(String newValue) {
        setTextContent(newValue);
    }
}