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

package net.shibboleth.idp.attribute.filter.spring.policy;

import java.util.Map;
import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl.AttributeRequesterPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This tests not just the parsing of the rule, but also the construction of the complex tests.<br/>
 * <code>
 *  <PermitValueRule xsi:type="basic:AttributeRequesterString" value="https://service.example.edu/shibboleth-sp" />
 * </code><br/>
 * vs<br/>
 * <code>
 *  <afp:PolicyRequirementRule xsi:type="basic:AttributeRequesterString" value="https://service.example.edu/shibboleth-sp" />
 * </code><br/>
 */
public class AttributeRequesterRuleParserTest extends BaseAttributeFilterParserTest {

    private Map<String, IdPAttribute> epaUid;

    @BeforeClass public void setupAttributes() throws ComponentInitializationException, ResolutionException {

        epaUid = getAttributes("epa-uid.xml");
    }

    @Test public void policy() throws ComponentInitializationException {
        policy("attributeRequester.xml", true);
        policy("attributeRequester.xml", false);
    }

    public void policy(String path, boolean isAfp) throws ComponentInitializationException {
        final PolicyRequirementRule rule = getPolicyRule(path, isAfp);

        AttributeFilterContext filterContext = DataSources.populatedFilterContext("principal", "issuer", "http://example.org");
        Assert.assertEquals(rule.matches(filterContext), Tristate.FALSE);
        filterContext = DataSources.populatedFilterContext("principal", "issuer", "https://service.example.edu/shibboleth-sp");
        Assert.assertEquals(rule.matches(filterContext), Tristate.TRUE);

        final AttributeRequesterPolicyRule arRule = (AttributeRequesterPolicyRule) rule;
        Assert.assertEquals(arRule.getMatchString(), "https://service.example.edu/shibboleth-sp");
        Assert.assertFalse(arRule.isIgnoreCase());
    }
 
    @Test public void matcher() throws ComponentInitializationException {
        matcher("attributeRequester.xml", true);
        matcher("attributeRequester.xml", false);
    }

    public void matcher(String path, boolean isAfp) throws ComponentInitializationException {
        final Matcher matcher = getMatcher(path, isAfp);

        AttributeFilterContext filterContext = DataSources.populatedFilterContext("principal", "issuer", "http://example.org");
        filterContext.setPrefilteredIdPAttributes(epaUid.values());
        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(epaUid.get("uid"), filterContext);
        Assert.assertTrue(result.isEmpty());

        filterContext = DataSources.populatedFilterContext("principal", "issuer", "https://service.example.edu/shibboleth-sp");
        filterContext.setPrefilteredIdPAttributes(epaUid.values());
        result = matcher.getMatchingValues(epaUid.get("uid"), filterContext);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getValue(), "daffyDuck");
    }
}
