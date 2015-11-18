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

import java.util.Arrays;
import java.util.Collections;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.matcher.impl.AbstractMatcherPolicyRuleTest;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.OrPolicyRule;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link OrPolicyRule} unit test. */
public class OrPolicyRuleTest extends AbstractMatcherPolicyRuleTest {

    @BeforeTest public void setup() throws Exception {
        super.setUp();
    }

    @Test public void testNullArguments() throws Exception {
        OrPolicyRule rule = new OrPolicyRule(Collections.singletonList(PolicyRequirementRule.MATCHES_ALL));
        rule.setId("test");
        rule.initialize();

        try {
            rule.matches(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

    }

    @Test(expectedExceptions = {ComponentInitializationException.class}) public void emptyInput()
            throws ComponentInitializationException {
        OrPolicyRule rule = new OrPolicyRule(null);
        rule.setId("test");
        rule.initialize();
    }

    @Test public void testMatches() throws ComponentInitializationException {
        OrPolicyRule rule = new OrPolicyRule(Arrays.asList(PolicyRequirementRule.MATCHES_NONE, PolicyRequirementRule.MATCHES_NONE));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FALSE);

        rule = new OrPolicyRule(Arrays.asList(PolicyRequirementRule.MATCHES_NONE, null, PolicyRequirementRule.MATCHES_NONE));

        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FALSE);

        rule = new OrPolicyRule(Arrays.asList(PolicyRequirementRule.MATCHES_NONE, null, PolicyRequirementRule.MATCHES_ALL));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.TRUE);
        
        rule = new OrPolicyRule(Arrays.asList(PolicyRequirementRule.MATCHES_NONE, null, PolicyRequirementRule.MATCHES_ALL,
                PolicyRequirementRule.REQUIREMENT_RULE_FAILS));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.TRUE);

        rule = new OrPolicyRule(Arrays.asList(PolicyRequirementRule.MATCHES_NONE, null, PolicyRequirementRule.MATCHES_NONE,
                PolicyRequirementRule.REQUIREMENT_RULE_FAILS));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FAIL);
    }
    
    @Test public void testSingletons() throws ComponentInitializationException {
        OrPolicyRule rule = new OrPolicyRule(Collections.singletonList(PolicyRequirementRule.MATCHES_NONE));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FALSE);

        rule = new OrPolicyRule(Collections.singletonList(PolicyRequirementRule.REQUIREMENT_RULE_FAILS));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.FAIL);

        rule = new OrPolicyRule(Collections.singletonList(PolicyRequirementRule.MATCHES_ALL));
        rule.setId("Test");
        rule.initialize();
        Assert.assertEquals(rule.matches(DataSources.unPopulatedFilterContext()), Tristate.TRUE);
    }


}