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

package net.shibboleth.idp.attribute.filter.spring.saml;

import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.AttributeRequesterEntityAttributeRegexPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.saml.impl.AttributeRequesterEntityAttributeRegexRuleParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeRequesterEntityAttributeRegexRuleParser}.
 */
public class AttributeRequesterEntityAttributeRegexRuleParserTest extends  BaseAttributeFilterParserTest {

    @Test public void basic() throws ComponentInitializationException {
        AttributeRequesterEntityAttributeRegexPolicyRule rule = (AttributeRequesterEntityAttributeRegexPolicyRule) getPolicyRule("requesterEARegex2.xml", false);
     
        Assert.assertEquals(rule.getValueRegex().pattern(), "^urn:example\\.org:policy:[^:]*$");
        Assert.assertEquals(rule.getAttributeName(), "urn:example.org:policy");

        rule = (AttributeRequesterEntityAttributeRegexPolicyRule) getPolicyRule("requesterEARegex2.xml", true);
        
        Assert.assertEquals(rule.getValueRegex().pattern(), "^urn:example\\.org:policy:[^:]*$");
        Assert.assertEquals(rule.getAttributeName(), "urn:example.org:policy");
    }

    @Test public void v2() throws ComponentInitializationException {
        final AttributeRequesterEntityAttributeRegexPolicyRule rule = (AttributeRequesterEntityAttributeRegexPolicyRule) getPolicyRule("requesterEARegex.xml", false);
     
        Assert.assertEquals(rule.getValueRegex().pattern(), "^urn:example\\.org:policy:[^:]*$");
        Assert.assertEquals(rule.getAttributeName(), "urn:example.org:policy");
    }
}
