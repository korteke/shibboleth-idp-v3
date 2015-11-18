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

package net.shibboleth.idp.attribute.filter;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link PolicyFromMatcher}.
 */
public class PolicyFromMatcherTest extends BaseBridgingClassTester {
    

    @Test public void all() {
        PolicyRequirementRule rule = new PolicyFromMatcher(Matcher.MATCHES_ALL);
        
        AttributeFilterContext context = setUpCtx();
        
        Assert.assertTrue(rule.matches(context)==Tristate.TRUE);
    }
    
    @Test public void none() {
        PolicyRequirementRule rule = new PolicyFromMatcher(Matcher.MATCHES_NONE);
        
        AttributeFilterContext context = setUpCtx();
        
        Assert.assertTrue(rule.matches(context)==Tristate.FALSE);
    }

    @Test public void fails() {
        PolicyRequirementRule rule = new PolicyFromMatcher(Matcher.MATCHER_FAILS);
        
        AttributeFilterContext context = setUpCtx();
        
        Assert.assertTrue(rule.matches(context)==Tristate.FAIL);
    }
}
