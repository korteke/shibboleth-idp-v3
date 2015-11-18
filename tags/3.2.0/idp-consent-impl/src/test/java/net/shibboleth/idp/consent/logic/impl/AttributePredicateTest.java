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

package net.shibboleth.idp.consent.logic.impl;

import java.util.Arrays;
import java.util.regex.Pattern;

import net.shibboleth.idp.attribute.IdPAttribute;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AttributePredicate} unit test. */
public class AttributePredicateTest {

    private AttributePredicate p;

    private IdPAttribute attribute1;

    private IdPAttribute attribute2;

    @BeforeMethod public void setUp() {
        attribute1 = new IdPAttribute("attribute1");
        attribute2 = new IdPAttribute("attribute2");
        p = new AttributePredicate();
    }

    @Test public void testWhitelist() {
        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        Assert.assertTrue(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));
    }

    @Test public void testBlacklist() {
        p.setBlacklistedAttributeIds(Arrays.asList("attribute1"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertTrue(p.apply(attribute2));
    }

    @Test public void testMatchExpression() {
        p.setAttributeIdMatchExpression(Pattern.compile(".*1"));
        Assert.assertTrue(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));
    }

    @Test public void testWhitelistAndBlacklist() {
        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setBlacklistedAttributeIds(Arrays.asList("attribute1"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));

        p.setBlacklistedAttributeIds(Arrays.asList("attribute2"));
        Assert.assertTrue(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));
    }

    @Test public void testWhitelistAndMatchExpression() {
        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*1"));
        Assert.assertTrue(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));

        p.setAttributeIdMatchExpression(Pattern.compile(".*2"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertTrue(p.apply(attribute2));
    }

    @Test public void testBlacklistAndMatchExpression() {
        p.setBlacklistedAttributeIds(Arrays.asList("attribute1"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*1"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));

        p.setAttributeIdMatchExpression(Pattern.compile(".*2"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertTrue(p.apply(attribute2));
    }

    @Test public void testWhitelistAndBlacklistAndMatchExpression() {
        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setBlacklistedAttributeIds(Arrays.asList("attribute1"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*1"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));

        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setBlacklistedAttributeIds(Arrays.asList("attribute2"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*1"));
        Assert.assertTrue(p.apply(attribute1));
        Assert.assertFalse(p.apply(attribute2));

        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setBlacklistedAttributeIds(Arrays.asList("attribute1"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*2"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertTrue(p.apply(attribute2));

        p.setWhitelistedAttributeIds(Arrays.asList("attribute1"));
        p.setBlacklistedAttributeIds(Arrays.asList("attribute2"));
        p.setAttributeIdMatchExpression(Pattern.compile(".*2"));
        Assert.assertFalse(p.apply(attribute1));
        Assert.assertTrue(p.apply(attribute2));
    }
}
