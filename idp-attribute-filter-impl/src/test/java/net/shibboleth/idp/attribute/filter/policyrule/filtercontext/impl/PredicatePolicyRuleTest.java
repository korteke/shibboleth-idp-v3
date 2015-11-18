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

package net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * test for {@link PredicatePolicyRule}.
 */
public class PredicatePolicyRuleTest {

    @Test public void trueTest() throws ComponentInitializationException {
        PredicatePolicyRule rule = new PredicatePolicyRule();
        rule.setId("id");
        rule.setProfileContextStrategy(new Func());
        rule.setRulePredicate(new TestPred(true));
        rule.initialize();
        Assert.assertEquals(rule.matches(null), Tristate.TRUE);
    }

    @Test public void falseTest() throws ComponentInitializationException {
        PredicatePolicyRule rule = new PredicatePolicyRule();
        rule.setId("id");
        rule.setRulePredicate(new TestPred(false));
        rule.initialize();
        ProfileRequestContext pc = new ProfileRequestContext<>();
        RelyingPartyContext rpc = pc.getSubcontext(RelyingPartyContext.class, true);
        Assert.assertEquals(rule.matches(rpc.getSubcontext(AttributeFilterContext.class, true)), Tristate.FALSE);
    }

    @Test public void navigateFail() throws ComponentInitializationException {
        PredicatePolicyRule rule = new PredicatePolicyRule();
        rule.setId("id");
        rule.setRulePredicate(new TestPred(true));
        rule.initialize();
        Assert.assertEquals(rule.matches(null), Tristate.FAIL);
    }
    
    @Test public void throwFail() throws ComponentInitializationException {
        PredicatePolicyRule rule = new PredicatePolicyRule();
        rule.setId("id");
        rule.setProfileContextStrategy(new Func());
        rule.setRulePredicate(new ThrowPred());
        rule.initialize();
        Assert.assertEquals(rule.matches(null), Tristate.FAIL);
    }


    static class TestPred implements Predicate<ProfileRequestContext> {

        private final boolean what;

        /** Constructor. */
        public TestPred(boolean value) {
            what = value;
        }

        /** {@inheritDoc} */
        @Override public boolean apply(@Nullable ProfileRequestContext input) {

            return what;
        }
    }

    static class ThrowPred implements Predicate<ProfileRequestContext> {

        /** {@inheritDoc} */
        @Override public boolean apply(@Nullable ProfileRequestContext input) {
            throw new RuntimeException();
        }
    }
    static class Func implements Function<AttributeFilterContext, ProfileRequestContext> {

        /** {@inheritDoc} */
        @Override @Nullable public ProfileRequestContext apply(@Nullable AttributeFilterContext input) {
            return new ProfileRequestContext<>();
        }

    }
}
