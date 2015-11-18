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

package net.shibboleth.idp.attribute.filter.policyrule.logic.impl;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.matcher.impl.AbstractMatcherPolicyRuleTest;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.NotPolicyRule;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** Test the {@link NotPolicyRule} matcher. */
public class NotPolicyRuleTest extends AbstractMatcherPolicyRuleTest {

    @BeforeTest public void setup() throws Exception {
        super.setUp();
    }

    @Test public void testNullArguments() throws Exception {
        NotPolicyRule rule = new NotPolicyRule(PolicyRequirementRule.MATCHES_ALL);
        rule.setId("NullArgs");
        rule.initialize();
        
        Assert.assertEquals(rule.getNegatedRule(), PolicyRequirementRule.MATCHES_ALL);
        LoggerFactory.getLogger(AbstractComposedPolicyRuleTest.class).debug(rule.toString());

        try {
            rule.matches(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new NotPolicyRule(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testInitDestroy() throws ComponentInitializationException {
        AbstractComposedPolicyRuleTest.TestMatcher inMatcher = new AbstractComposedPolicyRuleTest.TestMatcher();
        NotPolicyRule rule = new NotPolicyRule(inMatcher);

        try {
            rule.matches(filterContext);
            Assert.fail();
        } catch (UninitializedComponentException e) {
            // expect this
        }
        Assert.assertFalse(inMatcher.isInitialized());
        Assert.assertFalse(inMatcher.isDestroyed());

        rule.setId("test");
        rule.initialize();

        rule.destroy();

        try {
            rule.initialize();
        } catch (DestroyedComponentException e) {
            // OK
        }

    }

    @Test public void testPredicate() throws ComponentInitializationException {
        NotPolicyRule rule = new NotPolicyRule(PolicyRequirementRule.MATCHES_ALL);
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FALSE);

        rule = new NotPolicyRule(PolicyRequirementRule.MATCHES_NONE);
        rule.setId("test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.TRUE);

        rule = new NotPolicyRule(PolicyRequirementRule.REQUIREMENT_RULE_FAILS);
        rule.setId("test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FAIL);
    } 

}
