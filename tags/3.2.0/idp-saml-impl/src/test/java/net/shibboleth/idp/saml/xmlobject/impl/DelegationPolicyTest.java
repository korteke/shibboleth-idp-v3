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

import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;

import org.opensaml.core.xml.XMLObjectProviderBaseTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing shibdel:DelegationPolicy extension.
 */
public class DelegationPolicyTest extends XMLObjectProviderBaseTestCase {

    private Long expectedMaxChainLength;

    /** Constructor. */
    public DelegationPolicyTest() {
        singleElementFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibDelegationPolicy.xml";
        singleElementOptionalAttributesFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibDelegationPolicyOptionalAttributes.xml";
    }

    @BeforeMethod
	protected void setUp() throws Exception {
        expectedMaxChainLength = new Long(5);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementMarshall() {
        DelegationPolicy delegationPolicy = (DelegationPolicy) buildXMLObject(DelegationPolicy.DEFAULT_ELEMENT_NAME);

        assertXMLEquals(expectedDOM, delegationPolicy);
    }
    
    @Test
    public void testSingleElementUnmarshall() {
        DelegationPolicy delegationPolicy = (DelegationPolicy) unmarshallElement(singleElementFile);

        Assert.assertNotNull(delegationPolicy, "Unmarshalled object was null");
        Assert.assertNull(delegationPolicy.getMaximumTokenDelegationChainLength(), "MaximumTokenDelegationChainLength attribute value");
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesMarshall() {
        DelegationPolicy delegationPolicy = (DelegationPolicy) buildXMLObject(DelegationPolicy.DEFAULT_ELEMENT_NAME);

        delegationPolicy.setMaximumTokenDelegationChainLength(expectedMaxChainLength);

        assertXMLEquals(expectedOptionalAttributesDOM, delegationPolicy);
    }

    @Test
    public void testSingleElementOptionalAttributesUnmarshall() {
        DelegationPolicy delegationPolicy = (DelegationPolicy) unmarshallElement(singleElementOptionalAttributesFile);

        Assert.assertNotNull(delegationPolicy, "Unmarshalled object was null");
        Assert.assertEquals(delegationPolicy.getMaximumTokenDelegationChainLength(), expectedMaxChainLength,
                "VerifyDepth attribute value");
    }

}