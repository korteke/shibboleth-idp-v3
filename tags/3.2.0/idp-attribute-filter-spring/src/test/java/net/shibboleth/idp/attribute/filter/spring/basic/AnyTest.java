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

package net.shibboleth.idp.attribute.filter.spring.basic;

import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for ANY matcher or policy Rule.
 */
public class AnyTest extends BaseAttributeFilterParserTest {
    
    @Test public void testMatcher() throws ComponentInitializationException {
        testMatcher("any.xml", true);
        testMatcher("any.xml", true);
    }
    
    public void testMatcher(String path, boolean isAfp) throws ComponentInitializationException {
        Matcher matcher = getMatcher(path, isAfp);
        
        Assert.assertEquals(Matcher.MATCHES_ALL.getClass(), matcher.getClass());
    }
    
    @Test public void testPolicy() throws ComponentInitializationException {
        testPolicy("any.xml", true);
        testPolicy("any.xml", false);
    }

    public void testPolicy(String path, boolean isAfp) throws ComponentInitializationException {
        PolicyRequirementRule policy = getPolicyRule(path, isAfp);
        Assert.assertEquals(PolicyRequirementRule.MATCHES_ALL.getClass(), policy.getClass());
    }

}
