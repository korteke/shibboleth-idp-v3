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

package net.shibboleth.idp.attribute.filter.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.MockMatcher;
import net.shibboleth.idp.attribute.filter.MockPolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeFilter}. */
public class AttributeFilterImplTest {

    /** Test that post-construction state is what is expected. */
    @Test public void testPostConstructionState() throws Exception {
        AttributeFilter filter = new AttributeFilterImpl("engine", null);
        Assert.assertNotNull(filter.getFilterPolicies());
        Assert.assertTrue(filter.getFilterPolicies().isEmpty());
        Assert.assertEquals(filter.getId(), "engine");

        try {
            new AttributeFilterImpl("  ", null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }

        try {
            new AttributeFilterImpl("", null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }

        try {
            new AttributeFilterImpl(null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }
    }

    /** Test setting and retrieving filter policies. */
    @Test public void testFilterPolicies() throws Exception {
        AttributeFilterPolicy policy1 = new AttributeFilterPolicy("policy1", PolicyRequirementRule.MATCHES_NONE, null);
        AttributeFilterPolicy policy2 = new AttributeFilterPolicy("policy2", PolicyRequirementRule.MATCHES_NONE, null);
        AttributeFilterPolicy policy3 = new AttributeFilterPolicy("policy3", PolicyRequirementRule.MATCHES_NONE, null);

        AttributeFilterImpl filter = new AttributeFilterImpl("engine", Arrays.asList(policy1, policy1, policy2));
        policy1.initialize();
        policy2.initialize();
        filter.initialize();

        Assert.assertTrue(filter.isInitialized());
        Assert.assertEquals(filter.getFilterPolicies().size(), 3);
        Assert.assertTrue(filter.getFilterPolicies().contains(policy1));
        Assert.assertTrue(policy1.isInitialized());
        Assert.assertTrue(filter.getFilterPolicies().contains(policy2));
        Assert.assertTrue(policy2.isInitialized());
        Assert.assertFalse(filter.getFilterPolicies().contains(policy3));
        Assert.assertFalse(policy3.isInitialized());

        filter = new AttributeFilterImpl("engine", Arrays.asList(policy1, policy2));
        filter.initialize();

        Assert.assertEquals(filter.getFilterPolicies().size(), 2);
        List<AttributeFilterPolicy> contents = filter.getFilterPolicies();
        Assert.assertEquals(contents.get(0).getId(), "policy1");
        Assert.assertEquals(contents.get(1).getId(), "policy2");

        try {
            filter.getFilterPolicies().add(policy1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Test filtering attributes. */
    @Test public void testFilterAttributes() throws Exception {
        MockMatcher attribute1Matcher = new MockMatcher();
        attribute1Matcher.setMatchingAttribute("attribute1");
        attribute1Matcher.setMatchingValues(null);

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(attribute1Matcher);
        attribute1Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        IdPAttribute attribute2 = new IdPAttribute("attribute2");
        attribute2.setValues(Arrays.asList(new StringAttributeValue("a"), new StringAttributeValue("b")));
        filterContext.getPrefilteredIdPAttributes().put(attribute2.getId(), attribute2);

        final AttributeFilterImpl filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        attribute1Policy.initialize();
        policy.initialize();
        ComponentSupport.initialize(filter);

        filter.filterAttributes(filterContext);
        final Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredIdPAttributes();
        Assert.assertEquals(resultAttrs.size(), 1);
        final List<IdPAttributeValue<?>> result = resultAttrs.get("attribute1").getValues();
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue("one")));
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }

    @Test public void testAllMatcher() throws Exception {

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(Matcher.MATCHES_ALL);
        attribute1Policy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        attribute1Policy.initialize();
        policy.initialize();
        AttributeFilterImpl filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        filter.initialize();

        filter.filterAttributes(filterContext);
        List<IdPAttributeValue<?>> result = filterContext.getFilteredIdPAttributes().get("attribute1").getValues();
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue("one")));
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }
    
    @Test public void testAllMatcherFails() throws Exception {

        AttributeRule attribute2Policy = new AttributeRule();
        attribute2Policy.setId("attribute2Policy");
        attribute2Policy.setAttributeId("attribute1");
        MockMatcher matcher = new MockMatcher();
        matcher.setFailValidate(true);
        attribute2Policy.setMatcher(matcher);
        attribute2Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute2Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        attribute2Policy.initialize();
        policy.initialize();
        AttributeFilterImpl filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        filter.initialize();

        filter.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredIdPAttributes().isEmpty());
    }


    @Test public void testNoneMatcher() throws Exception {

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(Matcher.MATCHES_NONE);
        attribute1Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        attribute1Policy.initialize();
        policy.initialize();
        ComponentSupport.initialize(filter);

        filter.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredIdPAttributes().isEmpty());
    }

    @Test public void testDenyFilterAttributes() throws Exception {
        MockMatcher deny = new MockMatcher();
        deny.setMatchingAttribute("attribute1");
        deny.setMatchingValues(Collections.singletonList(new StringAttributeValue("one")));

        AttributeRule denyPolicy = new AttributeRule();
        denyPolicy.setId("denyPolicy");
        denyPolicy.setAttributeId("attribute1");
        denyPolicy.setMatcher(deny);
        denyPolicy.setIsDenyRule(true);

        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Arrays.asList(denyPolicy, allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        AttributeFilterImpl filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        denyPolicy.initialize();
        allowPolicy.initialize();
        policy.initialize();
        filter.initialize();

        filter.filterAttributes(filterContext);
        Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredIdPAttributes();
        Assert.assertEquals(resultAttrs.size(), 1);
        List<IdPAttributeValue<?>> result = resultAttrs.get("attribute1").getValues();
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }

    @Test public void testNoPolicy() throws Exception {
        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_NONE,
                        Collections.singletonList(allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        policy.initialize();
        ComponentSupport.initialize(filter);

        filter.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredIdPAttributes().isEmpty());
    }

    @Test public void testDenyAllFilterAttributes() throws Exception {
        AttributeRule denyPolicy = new AttributeRule();
        denyPolicy.setId("denyPolicy");
        denyPolicy.setAttributeId("attribute1");
        denyPolicy.setMatcher(Matcher.MATCHES_ALL);
        denyPolicy.setIsDenyRule(true);

        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Arrays.asList(denyPolicy, allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));
        filterContext.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        allowPolicy.initialize();
        denyPolicy.initialize();
        policy.initialize();
        ComponentSupport.initialize(filter);

        filter.filterAttributes(filterContext);
        Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredIdPAttributes();
        Assert.assertTrue(resultAttrs.isEmpty());
    }

    @Test public void testInitDestroy() throws ComponentInitializationException {
        MockMatcher matcher = new MockMatcher();
        AttributeRule filterPolicy = new AttributeRule();
        filterPolicy.setId("filterPolicy");
        filterPolicy.setAttributeId("attribute1");
        filterPolicy.setMatcher(matcher);
        filterPolicy.setIsDenyRule(false);

        MockPolicyRequirementRule policyRule = new MockPolicyRequirementRule();
        AttributeFilterPolicy policy = new AttributeFilterPolicy("policy", policyRule, Collections.singletonList(filterPolicy));

        Assert.assertFalse(policyRule.isInitialized());
        Assert.assertFalse(policyRule.isDestroyed());
        Assert.assertFalse(matcher.isInitialized());
        Assert.assertFalse(matcher.isDestroyed());

        AttributeFilterImpl filter = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        policy.initialize();
        matcher.initialize();
        policyRule.initialize();
        ComponentSupport.initialize(filter);

        Assert.assertTrue(policyRule.isInitialized());
        Assert.assertFalse(policyRule.isDestroyed());
        Assert.assertTrue(matcher.isInitialized());
        Assert.assertFalse(matcher.isDestroyed());

        filter.destroy();
        policyRule.destroy();
        policy.destroy();
        matcher.destroy();
        Assert.assertTrue(policyRule.isInitialized());
        Assert.assertTrue(policyRule.isDestroyed());
        Assert.assertTrue(matcher.isInitialized());
        Assert.assertTrue(matcher.isDestroyed());
    }

}