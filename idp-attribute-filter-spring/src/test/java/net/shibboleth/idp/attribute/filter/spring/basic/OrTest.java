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

import java.util.List;

import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.NotMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.OrMatcher;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.NotPolicyRule;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.OrPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.basic.impl.OrMatcherParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link OrMatcherParser}.
 */
public class OrTest extends BaseAttributeFilterParserTest {

    @Test public void matcher() throws ComponentInitializationException {
        matcher( "or.xml", true);
        matcher( "or.xml", false);
    }

    public void matcher(String path, boolean isAfp) throws ComponentInitializationException {

        OrMatcher what = (OrMatcher) getMatcher(path,isAfp);

        final List<Matcher> children = what.getComposedMatchers();

        Assert.assertEquals(children.size(), 2);
        Assert.assertEquals(children.get(0).getClass(), NotMatcher.class);
        Assert.assertEquals(children.get(1).getClass(), Matcher.MATCHES_ALL.getClass());
    }

    @Test public void policy() throws ComponentInitializationException {
        policy("or.xml", true);
        policy("or.xml", false);
    }
    
    public void policy(String path, boolean isAfp) throws ComponentInitializationException {
        OrPolicyRule what = (OrPolicyRule) getPolicyRule(path,isAfp );

        final List<PolicyRequirementRule> children = what.getComposedRules();
        Assert.assertEquals(children.size(), 2);
        Assert.assertEquals(children.get(0).getClass(), NotPolicyRule.class);
        Assert.assertEquals(children.get(1).getClass(), PolicyRequirementRule.MATCHES_ALL.getClass());
    }
    
    @Test public void matcherSingleton() throws ComponentInitializationException {
        matcherSingleton( "orSingleton.xml", true);
        matcherSingleton( "orSingleton.xml", false);
    }

    public void matcherSingleton(String path, boolean isAfp) throws ComponentInitializationException {

        OrMatcher what = (OrMatcher) getMatcher( path,isAfp);

        final List<Matcher> children = what.getComposedMatchers();

        Assert.assertEquals(children.size(), 1);
        Assert.assertEquals(children.get(0).getClass(), NotMatcher.class);
    }

    @Test public void policySingleton() throws ComponentInitializationException {
        policySingleton( "orSingleton.xml", true);
        policySingleton( "orSingleton.xml", false);
    }

    public void policySingleton(String path, boolean isAfp) throws ComponentInitializationException {
        OrPolicyRule what = (OrPolicyRule) getPolicyRule(path,isAfp);

        final List<PolicyRequirementRule> children = what.getComposedRules();
        Assert.assertEquals(children.size(), 1);
        Assert.assertEquals(children.get(0).getClass(), PolicyRequirementRule.MATCHES_ALL.getClass());
    }

}
