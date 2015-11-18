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

package net.shibboleth.idp.session;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link BasicSPSession} unit test. */
public class BasicSPSessionTest {

    /** Tests that everything is properly initialized during object construction. */
    @Test public void testInstantiation() throws Exception {
        long start = System.currentTimeMillis();
        // this is here to allow the event's creation time to deviate from the 'start' time
        Thread.sleep(50);

        BasicSPSession session = new BasicSPSession("test", System.currentTimeMillis(),
                System.currentTimeMillis() + 60000L);
        Assert.assertEquals(session.getId(), "test");
        Assert.assertTrue(session.getCreationInstant() > start);
        Assert.assertTrue(session.getExpirationInstant() > session.getCreationInstant());

        try {
            new BasicSPSession(null, 0, 0);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new BasicSPSession("", 0, 0);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new BasicSPSession("  ", 0, 0);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }
        try {
            new BasicSPSession("foo", 0, 0);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }

        try {
            new BasicSPSession("foo", start, 0);
            Assert.fail();
        } catch (ConstraintViolationException e) {

        }
    }

}