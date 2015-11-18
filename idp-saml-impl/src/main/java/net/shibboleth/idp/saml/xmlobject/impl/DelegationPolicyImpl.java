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

import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObject;

/** Implementation of {@link DelegationPolicy}. */
@NotThreadSafe
public class DelegationPolicyImpl extends AbstractSAMLObject implements DelegationPolicy {

    /** The VerifyDepth attribute. */
    private Long maximumTokenDelegationChainLength;

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected DelegationPolicyImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    @Override
    public Long getMaximumTokenDelegationChainLength() {
        return maximumTokenDelegationChainLength;
    }

    /** {@inheritDoc} */
    @Override
    public void setMaximumTokenDelegationChainLength(Long value) {
        maximumTokenDelegationChainLength = prepareForAssignment(maximumTokenDelegationChainLength, value);
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {
        return Collections.emptyList();
    }
}