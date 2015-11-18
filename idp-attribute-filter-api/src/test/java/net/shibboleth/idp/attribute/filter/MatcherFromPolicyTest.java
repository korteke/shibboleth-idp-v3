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

import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link MatcherFromPolicy}.
 */
public class MatcherFromPolicyTest extends BaseBridgingClassTester {
    

    @Test public void all() {
        Matcher matcher = new MatcherFromPolicy(PolicyRequirementRule.MATCHES_ALL);
        
        AttributeFilterContext context = setUpCtx();
        
        Set<IdPAttributeValue<?>> values = matcher.getMatchingValues(context.getPrefilteredIdPAttributes().get(NAME1), context);
        
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(VALUE1));
        Assert.assertTrue(values.contains(VALUE2));
    }
    
    @Test public void none() {
        Matcher matcher = new MatcherFromPolicy(PolicyRequirementRule.MATCHES_NONE);
        
        AttributeFilterContext context = setUpCtx();
        
        Set<IdPAttributeValue<?>> values = matcher.getMatchingValues(context.getPrefilteredIdPAttributes().get(NAME1), context);
        
        Assert.assertTrue(values.isEmpty());
    }

    @Test public void fails() {
        Matcher matcher = new MatcherFromPolicy(PolicyRequirementRule.REQUIREMENT_RULE_FAILS);
        
        AttributeFilterContext context = setUpCtx();
        
        Set<IdPAttributeValue<?>> values = matcher.getMatchingValues(context.getPrefilteredIdPAttributes().get(NAME1), context);
        
        Assert.assertNull(values);
    }
}
