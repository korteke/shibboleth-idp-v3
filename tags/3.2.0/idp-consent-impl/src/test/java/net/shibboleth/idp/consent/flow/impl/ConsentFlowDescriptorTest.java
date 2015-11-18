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

package net.shibboleth.idp.consent.flow.impl;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ConsentFlowDescriptor} unit test. */
public class ConsentFlowDescriptorTest {

    private ConsentFlowDescriptor descriptor;

    @BeforeMethod public void setUp() {
        descriptor = new ConsentFlowDescriptor();
        descriptor.setId("test");
    }

    @Test public void testInstantation() {
        Assert.assertEquals(descriptor.getId(), "test");
        Assert.assertFalse(descriptor.compareValues());
        Assert.assertEquals(Long.valueOf(DOMTypeSupport.durationToLong("P1Y")), descriptor.getLifetime());
    }

    @Test public void testCompareValues() {
        descriptor.setCompareValues(true);
        Assert.assertTrue(descriptor.compareValues());
        descriptor.setCompareValues(false);
        Assert.assertFalse(descriptor.compareValues());
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullLifetime() {
        descriptor.setLifetime(null);
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNegativeLifetime() {
        descriptor.setLifetime(new Long(-10));
    }

    @Test public void testLifetime() {
        Long lifetime = new Long(1000);
        descriptor.setLifetime(lifetime);
        Assert.assertEquals(descriptor.getLifetime(), lifetime);

        lifetime = new Long(0);
        descriptor.setLifetime(lifetime);
        Assert.assertEquals(descriptor.getLifetime(), lifetime);
    }

    @Test public void testMaxStoredRecords() {
        descriptor.setMaximumNumberOfStoredRecords(1024);
        Assert.assertEquals(descriptor.getMaximumNumberOfStoredRecords(), 1024);
        
        descriptor.setMaximumNumberOfStoredRecords(0);
        Assert.assertEquals(descriptor.getMaximumNumberOfStoredRecords(), 0);
        
        descriptor.setMaximumNumberOfStoredRecords(-1);
        Assert.assertEquals(descriptor.getMaximumNumberOfStoredRecords(), -1);
    }

}
