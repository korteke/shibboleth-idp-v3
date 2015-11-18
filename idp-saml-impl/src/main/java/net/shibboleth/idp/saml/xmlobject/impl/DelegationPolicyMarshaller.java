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

import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;

/** Marshaller for {@link DelegationPolicy}. */
@ThreadSafe
public class DelegationPolicyMarshaller extends AbstractSAMLObjectMarshaller {

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        DelegationPolicy delegationPolicy = (DelegationPolicy) xmlObject;

        if (delegationPolicy.getMaximumTokenDelegationChainLength() != null) {
            domElement.setAttributeNS(null, DelegationPolicy.MAX_DELEGATION_CHAIN_LENGTH_ATTRIB_NAME,
                    delegationPolicy.getMaximumTokenDelegationChainLength().toString());
        }

    }
}