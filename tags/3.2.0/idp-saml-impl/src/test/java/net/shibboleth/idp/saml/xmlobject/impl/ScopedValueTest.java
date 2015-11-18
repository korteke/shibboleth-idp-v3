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

import net.shibboleth.idp.saml.xmlobject.ScopedValue;

import org.opensaml.core.xml.XMLObjectProviderBaseTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing shib:ScopedValue encoder extension.
 */
public class ScopedValueTest extends XMLObjectProviderBaseTestCase {

    /**
     * Expected attribute value.
     */
    private String expectedValue;

    /**
     * Expected scope value.
     */
    private String expectedScope;

    /**
     * Name of the scope attribute.
     */
    private String scopeAttribute;

    /**
     * Scope delimiter.
     */
    private String scopeDelimiter;

    /** Constructor. */
    public ScopedValueTest() {
        singleElementFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibScopedValue.xml";
        singleElementOptionalAttributesFile = "/net/shibboleth/idp/saml/impl/xmlobject/ShibScopedValueOptionalAttributes.xml";
    }

    @BeforeMethod
	protected void setUp() throws Exception {
        expectedValue = "member";
        expectedScope = "example.edu";
        scopeAttribute = "scope";
        scopeDelimiter = "@";
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementMarshall() {
        ScopedValue sv = (ScopedValue) buildXMLObject(ScopedValue.TYPE_NAME);

        sv.setValue(expectedValue + scopeDelimiter + expectedScope);

        assertXMLEquals(expectedDOM, sv);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesMarshall() {
        ScopedValue sv = (ScopedValue) buildXMLObject(ScopedValue.TYPE_NAME);

        sv.setValue(expectedValue);
        sv.setScopeAttributeName(scopeAttribute);
        sv.setScope(expectedScope);

        assertXMLEquals(expectedOptionalAttributesDOM, sv);
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementUnmarshall() {
        ScopedValue sv = (ScopedValue) unmarshallElement(singleElementFile);

        Assert.assertNotNull(sv, "Unmarshalled object was null");
        Assert.assertEquals(expectedValue + scopeDelimiter + expectedScope, sv.getValue(), "Scoped value");
    }

    /** {@inheritDoc} */
    @Test
	public void testSingleElementOptionalAttributesUnmarshall() {
        ScopedValue sv = (ScopedValue) unmarshallElement(singleElementOptionalAttributesFile);
        sv.setScopeAttributeName(scopeAttribute);
        Assert.assertNotNull(sv, "Unmarshalled object was null");
        Assert.assertEquals(expectedValue, sv.getValue(), "Scoped value");
        Assert.assertEquals(expectedScope, sv.getScope(), "Scope value");
        Assert.assertEquals(scopeAttribute, sv.getScopeAttributeName(), "Scope attribute name");
    }
}