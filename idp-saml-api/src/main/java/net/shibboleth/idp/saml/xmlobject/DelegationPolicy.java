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

package net.shibboleth.idp.saml.xmlobject;

import javax.xml.namespace.QName;

import org.opensaml.saml.common.SAMLObject;

/** SAMLObject for the Shibboleth DelegationPolicy extension supporting SAML delegation. */
public interface DelegationPolicy extends SAMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "DelegationPolicy";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(ExtensionsConstants.SHIB_DELEXT10_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, ExtensionsConstants.SHIB_DELEXT10_PREFIX);

    /** VerifyDepth attribute name. */
    public static final String MAX_DELEGATION_CHAIN_LENGTH_ATTRIB_NAME = "MaximumTokenDelegationChainLength";

    /**
     * Get the MaximumTokenDelegationChainLength attribute value.
     * 
     * @return the MaximumTokenDelegationChainLength attribute value
     */
    public Long getMaximumTokenDelegationChainLength();

    /**
     * Set the MaximumTokenDelegationChainLength attribute value.
     * 
     * @param value the new MaximumTokenDelegationChainLength attribute value
     */
    public void setMaximumTokenDelegationChainLength(Long value);

}
