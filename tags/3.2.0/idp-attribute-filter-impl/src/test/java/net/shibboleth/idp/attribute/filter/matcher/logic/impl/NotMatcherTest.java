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

package net.shibboleth.idp.attribute.filter.matcher.logic.impl;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.or;

import java.util.Arrays;
import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.matcher.impl.AbstractMatcherPolicyRuleTest;
import net.shibboleth.idp.attribute.filter.matcher.impl.MockValuePredicateMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.NotMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.OrMatcher;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** Test the {@link NotMatcher} matcher. */
public class NotMatcherTest extends AbstractMatcherPolicyRuleTest {

    @BeforeTest public void setup() throws Exception {
        super.setUp();
    }

    @Test public void testNullArguments() throws Exception {

        Matcher valuePredicate = Matcher.MATCHES_ALL;
        NotMatcher matcher = new NotMatcher(valuePredicate);
        matcher.setId("NullArgs");
        matcher.initialize();

        try {
            matcher.getMatchingValues(null, filterContext);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            matcher.getMatchingValues(attribute, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            matcher.getMatchingValues(null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new NotMatcher(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testInitDestroy() throws ComponentInitializationException {
        AbstractComposedMatcherTest.TestMatcher inMatcher = new AbstractComposedMatcherTest.TestMatcher();
        NotMatcher matcher = new NotMatcher(inMatcher);

        try {
            matcher.getMatchingValues(attribute, filterContext);
            Assert.fail();
        } catch (UninitializedComponentException e) {
            // expect this
        }
        Assert.assertFalse(inMatcher.isInitialized());
        Assert.assertFalse(inMatcher.isDestroyed());

        matcher.setId("test");
        matcher.initialize();

        matcher.destroy();
        
        try {
            matcher.initialize();
        } catch (DestroyedComponentException e) {
            // OK
        }

    }

    @Test public void testGetMatchingValues() throws Exception {
        NotMatcher matcher = new NotMatcher(new MockValuePredicateMatcher(or(equalTo(value1), equalTo(value2))));
        matcher.setId("test");
        matcher.initialize();

        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(value3));
        matcher.destroy();
        try {
            matcher.getMatchingValues(attribute, filterContext);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expect this
        }

        OrMatcher orMatcher =
                new OrMatcher(Arrays.<Matcher>asList(new MockValuePredicateMatcher(equalTo(value1)),
                        new MockValuePredicateMatcher(equalTo(value2)), new MockValuePredicateMatcher(equalTo(value3))));

        orMatcher.setId("or");
        matcher = new NotMatcher(orMatcher);

        matcher.setId("Test");
        matcher.initialize();
        orMatcher.initialize();

        result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 0);
    }

    @Test public void testFails() throws Exception {
        NotMatcher matcher = new NotMatcher( Matcher.MATCHER_FAILS);
        matcher.setId("test");
        matcher.initialize();

        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNull(result);
    }
}
