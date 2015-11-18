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

import net.shibboleth.idp.saml.xmlobject.Scope;

import org.opensaml.core.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing shibmd:Scope metadata extension.
 */
public class ScopeTest extends XMLObjectProviderBaseTestCase {

    private String expectedContent;

    private Boolean expectedRegexp;

    /** Constructor. */
    public ScopeTest() {
        singleElementFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibMDScope.xml";
        singleElementOptionalAttributesFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibMDScopeOptionalAttributes.xml";
    }

    @BeforeMethod
	protected void setUp() throws Exception {
        expectedContent = "ThisIsSomeScopeValue";
        expectedRegexp = Boolean.TRUE;
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementMarshall() {
        Scope scope = (Scope) buildXMLObject(Scope.DEFAULT_ELEMENT_NAME);

        scope.setValue(expectedContent);

        assertXMLEquals(expectedDOM, scope);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesMarshall() {
        Scope scope = (Scope) buildXMLObject(Scope.DEFAULT_ELEMENT_NAME);

        scope.setValue(expectedContent);
        scope.setRegexp(expectedRegexp);

        assertXMLEquals(expectedOptionalAttributesDOM, scope);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementUnmarshall() {
        Scope scope = (Scope) unmarshallElement(singleElementFile);

        Assert.assertNotNull(scope, "Unmarshalled object was null");
        Assert.assertEquals(scope.getValue(), expectedContent, "Scope value");
        Assert.assertEquals(Boolean.FALSE, scope.getRegexp(),"Regexp attribute value");
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesUnmarshall() {
        Scope scope = (Scope) unmarshallElement(singleElementOptionalAttributesFile);

        Assert.assertNotNull(scope, "Unmarshalled object was null");
        Assert.assertEquals(scope.getValue(), expectedContent, "Scope value");
        Assert.assertEquals(expectedRegexp, scope.getRegexp(), "Regexp attribute value");
    }

    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    @Test
	public void testXSBooleanAttributes() {
        Scope scope = (Scope) buildXMLObject(Scope.DEFAULT_ELEMENT_NAME);

        // regexp attribute
        scope.setRegexp(Boolean.TRUE);
        Assert.assertEquals(scope.getRegexp(), Boolean.TRUE, "Unexpected value for boolean attribute found");
        Assert.assertNotNull(scope.getRegexpXSBoolean(), "XSBooleanValue was null");
        Assert.assertEquals(scope.getRegexpXSBoolean(), new XSBooleanValue(Boolean.TRUE, false), 
                "XSBooleanValue was unexpected value");
        Assert.assertEquals(scope.getRegexpXSBoolean().toString(), "true", "XSBooleanValue string was unexpected value");

        scope.setRegexp(Boolean.FALSE);
        Assert.assertEquals(scope.getRegexp(), Boolean.FALSE, "Unexpected value for boolean attribute found");
        Assert.assertNotNull(scope.getRegexpXSBoolean(), "XSBooleanValue was null");
        Assert.assertEquals(scope.getRegexpXSBoolean(), new XSBooleanValue(Boolean.FALSE, false), 
                "XSBooleanValue was unexpected value");
        Assert.assertEquals(scope.getRegexpXSBoolean().toString(), "false",
                "XSBooleanValue string was unexpected value");

        scope.setRegexp((Boolean) null);
        Assert.assertEquals(scope.getRegexp(), Boolean.FALSE, "Unexpected default value for boolean attribute found");
        Assert.assertNull(scope.getRegexpXSBoolean(), "XSBooleanValue was not null");
    }
}