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

import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.xmlobject.KeyAuthority;

import org.opensaml.core.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing shibmd:KeyAuthority metadata extension.
 */
public class KeyAuthorityTest extends XMLObjectProviderBaseTestCase {

    private Integer expectedVerifyDepth;

    private int expectedNumKeyInfos;

    private QName unknownAttribName;

    private String unknownAttribValue;

    /** Constructor. */
    public KeyAuthorityTest() {
        singleElementFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibMDKeyAuthority.xml";
        singleElementOptionalAttributesFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibMDKeyAuthorityOptionalAttributes.xml";
        childElementsFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibMDKeyAuthorityChildElements.xml";
    }

    @BeforeMethod
	protected void setUp() throws Exception {
        expectedVerifyDepth = new Integer(5);
        expectedNumKeyInfos = 4;

        unknownAttribName = new QName("http://www.example.org/testObjects", "UnknownAttrib", "test");
        unknownAttribValue = "FooBar";
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementMarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) buildXMLObject(KeyAuthority.DEFAULT_ELEMENT_NAME);

        assertXMLEquals(expectedDOM, keyAuthority);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesMarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) buildXMLObject(KeyAuthority.DEFAULT_ELEMENT_NAME);

        keyAuthority.setVerifyDepth(expectedVerifyDepth);
        keyAuthority.getUnknownAttributes().put(unknownAttribName, unknownAttribValue);

        assertXMLEquals(expectedOptionalAttributesDOM, keyAuthority);
    }

    /** {@inheritDoc} */
    @Test
	public void testChildElementsMarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) buildXMLObject(KeyAuthority.DEFAULT_ELEMENT_NAME);

        for (int i = 0; i < expectedNumKeyInfos; i++) {
            keyAuthority.getKeyInfos().add((KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
        }

        assertXMLEquals(expectedChildElementsDOM, keyAuthority);
    }

    @Test
    public void testSingleElementUnmarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) unmarshallElement(singleElementFile);

        Assert.assertNotNull(keyAuthority, "Unmarshalled object was null");
        Assert.assertNull(keyAuthority.getVerifyDepth(), "VerifyDepth attribute value");

        Assert.assertTrue(keyAuthority.getUnknownAttributes().isEmpty(), "Extension attribute map was not empty");

        Assert.assertTrue(keyAuthority.getKeyInfos().isEmpty(), "KeyInfo list was not empty");
    }

    @Test
    public void testSingleElementOptionalAttributesUnmarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) unmarshallElement(singleElementOptionalAttributesFile);

        Assert.assertNotNull(keyAuthority, "Unmarshalled object was null");
        Assert.assertEquals(expectedVerifyDepth, keyAuthority.getVerifyDepth(), "VerifyDepth attribute value");

        Assert.assertFalse(keyAuthority.getUnknownAttributes().isEmpty(), "Extension attribute map was empty");
        Assert.assertTrue(keyAuthority.getUnknownAttributes().containsKey(unknownAttribName),
                "AttributeMap did not contain expected unknown attribute name");
        Assert.assertEquals(unknownAttribValue, keyAuthority.getUnknownAttributes().get(unknownAttribName),
                "AttributeMap did not contain expected unknown attribute value");

        Assert.assertTrue(keyAuthority.getKeyInfos().isEmpty(), "KeyInfo list was not empty");
    }

    @Test
    public void testChildElementsUnmarshall() {
        KeyAuthority keyAuthority = (KeyAuthority) unmarshallElement(childElementsFile);

        Assert.assertNotNull(keyAuthority, "Unmarshalled object was null");
        Assert.assertNull(keyAuthority.getVerifyDepth(), "VerifyDepth attribute value");

        Assert.assertTrue(keyAuthority.getUnknownAttributes().isEmpty(), "Extension attribute map was not empty");

        Assert.assertFalse(keyAuthority.getKeyInfos().isEmpty(), "KeyInfo list was empty");
        Assert.assertEquals(expectedNumKeyInfos, keyAuthority.getKeyInfos().size(), "# of KeyInfo child elements");
    }
}