/*
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/** unit tests for {@link AbstractComposedPolicyRule}. */
public class AbstractComposedPolicyRuleTest {

    @Test
    public void testInitDestroy() throws ComponentInitializationException {
        List<PolicyRequirementRule> firstList = new ArrayList<>(2);
        ComposedPolicyRule rule = new ComposedPolicyRule(Collections.EMPTY_LIST);
        
        for (int i = 0; i < 2;i++) {
            firstList.add(new TestMatcher());
        }
        
        rule.destroy();
        
        boolean thrown = false;
        try {
            rule.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        
        Assert.assertTrue(thrown, "Initialize after destroy");

        for (int i = 0; i < 2;i++) {
            firstList.add(new TestMatcher());
        }
        firstList.add(null);
        rule = new ComposedPolicyRule(firstList);
        
        Assert.assertEquals(firstList.size()-1, rule.getComposedRules().size());
        
        thrown = false;
        try {
            rule.getComposedRules().add(new TestMatcher());
        } catch (UnsupportedOperationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Set into the returned list");
        rule.setId("Test");
        
        rule.initialize();
        
        rule.destroy();
    }
    
    @Test
    public void testParams() throws ComponentInitializationException {
        ComposedPolicyRule rule = new ComposedPolicyRule(null);

        Assert.assertTrue(rule.getComposedRules().isEmpty(), "Initial state - no matchers");
        Assert.assertTrue(rule.getComposedRules().isEmpty(), "Add null - no matchers");
        
        List<PolicyRequirementRule> list = new ArrayList<>();
        
        for (int i = 0; i < 30; i++) {
            list.add(null);
        }
        
        rule = new ComposedPolicyRule(list);
        Assert.assertTrue(rule.getComposedRules().isEmpty(), "Add List<null> - no matchers");
        
        list.set(2, new TestMatcher());
        list.set(3, new TestMatcher());
        list.set(7, new TestMatcher());
        list.set(11, new TestMatcher());
        list.set(13, new TestMatcher());
        list.set(17, new TestMatcher());
        list.set(19, new TestMatcher());
        list.set(23, new TestMatcher());
        list.set(29, new TestMatcher());
        Assert.assertTrue(rule.getComposedRules().isEmpty(), "Change to input list - no matchers");

        rule = new ComposedPolicyRule(list);
        Assert.assertEquals(rule.getComposedRules().size(), 9, "Add a List with nulls");
        
        list.clear();
        Assert.assertEquals(rule.getComposedRules().size(), 9, "Change to input list");

        rule = new ComposedPolicyRule(list);
        Assert.assertTrue(rule.getComposedRules().isEmpty(), "Empty list");

        LoggerFactory.getLogger(AbstractComposedPolicyRuleTest.class).debug(rule.toString());
    }
    
    
    private class ComposedPolicyRule extends AbstractComposedPolicyRule {

        /**
         * Constructor.
         *
         * @param composedMatchers
         */
        public ComposedPolicyRule(Collection<PolicyRequirementRule> composedMatchers) {
            super(composedMatchers);
        }

        @Override
        public Tristate matches(@Nullable AttributeFilterContext arg0) {
            return Tristate.FALSE;
        }
    }
    
    public static class TestMatcher extends AbstractInitializableComponent implements  PolicyRequirementRule, DestructableComponent, InitializableComponent {

        @Override
        public Tristate matches(@Nullable AttributeFilterContext arg0) {
            return Tristate.FALSE;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable public String getId() {
            return "99";
        }
        
    }
}