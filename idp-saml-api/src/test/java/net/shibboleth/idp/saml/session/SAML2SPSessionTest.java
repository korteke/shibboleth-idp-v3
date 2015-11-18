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

package net.shibboleth.idp.saml.session;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.NameID;
import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link SAML2SPSession} unit test. */
public class SAML2SPSessionTest extends OpenSAMLInitBaseTestCase {

    /** Tests that everything is properly initialized during object construction. */
    @Test public void testInstantiation() throws Exception {
        
        NameID nameID = (NameID) XMLObjectSupport.buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue("joe@example.org");
        nameID.setFormat(NameID.EMAIL);
        
        long start = System.currentTimeMillis();
        // this is here to allow the event's creation time to deviate from the 'start' time
        Thread.sleep(50);

        SAML2SPSession session = new SAML2SPSession("test", System.currentTimeMillis(),
                System.currentTimeMillis() + 60000L, nameID, "1234567890");
        Assert.assertEquals(session.getId(), "test");
        Assert.assertTrue(session.getCreationInstant() > start);
        Assert.assertTrue(session.getExpirationInstant() > session.getCreationInstant());
        Assert.assertSame(session.getNameID(), nameID);
        Assert.assertEquals(session.getSessionIndex(), "1234567890");
        Assert.assertEquals(session.getSPSessionKey(), "joe@example.org");

        try {
            new SAML2SPSession(null, 0, 0, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("", 0, 0, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("  ", 0, 0, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("foo", 0, 0, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("foo", start, 0, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("foo", start, start, null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new SAML2SPSession("foo", start, start, nameID, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }
    }

}