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

package net.shibboleth.idp.saml.authn.principal;

import net.shibboleth.idp.saml.authn.principal.AuthnContextDeclRefPrincipal;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link AuthnContextDeclRefPrincipal} unit test. */
public class AuthnContextDeclRefPrincipalTest extends XMLObjectBaseTestCase {

    /**
     * Tests that everything is properly initialized during object construction.
     *  
     * @throws MarshallingException
     * @throws CloneNotSupportedException 
     */
    @Test public void testInstantiation() throws MarshallingException, CloneNotSupportedException {
        AuthnContextDeclRefPrincipal principal = new AuthnContextDeclRefPrincipal("http://opensaml.org/mydecl");
        Assert.assertEquals(principal.getName(), "http://opensaml.org/mydecl");

        AuthnContextDeclRef ref = buildXMLObject(AuthnContextDeclRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextDeclRef("http://opensaml.org/mydecl");
        Element xml = getMarshaller(AuthnContextDeclRef.DEFAULT_ELEMENT_NAME).marshall(ref);
        assertXMLEquals(xml.getOwnerDocument(), principal.getAuthnContextDeclRef());
        
        AuthnContextDeclRefPrincipal principal2 = principal.clone();
        assertXMLEquals(xml.getOwnerDocument(), principal2.getAuthnContextDeclRef());
        
        try {
            new AuthnContextDeclRefPrincipal(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new AuthnContextDeclRefPrincipal("");
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new AuthnContextDeclRefPrincipal("   ");
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }
        
    }
}