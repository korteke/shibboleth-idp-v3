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

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.AttributeExtensibleXMLObject;
import org.opensaml.xmlsec.signature.KeyInfo;

/** XMLObject for the Shibboleth KeyAuthority metadata extension. */
public interface KeyAuthority extends AttributeExtensibleXMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "KeyAuthority";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(ExtensionsConstants.SHIB_MDEXT10_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, ExtensionsConstants.SHIB_MDEXT10_PREFIX);

    /** VerifyDepth attribute name. */
    public static final String VERIFY_DEPTH_ATTRIB_NAME = "VerifyDepth";

    /**
     * Get the list of KeyInfo child elements.
     * 
     * @return the list of KeyInfo child elements
     */
    public List<KeyInfo> getKeyInfos();

    /**
     * Get the VerifyDepth attribute value.
     * 
     * @return the VerifyDepth attribute value
     */
    public Integer getVerifyDepth();

    /**
     * Set the VerifyDepth attribute value.
     * 
     * @param newVerifyDepth the new VerifyDepth attribute value
     */
    public void setVerifyDepth(Integer newVerifyDepth);

}
