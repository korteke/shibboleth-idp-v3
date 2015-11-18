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

import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.AttributeRequesterNameIDFormatExactPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.saml.impl.AttributeRequesterNameIdFormatRuleParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeRequesterNameIdFormatRuleParser}.
 */
public class AttributeRequesterNameIdFormatRuleParserTest extends BaseAttributeFilterParserTest {

    @Test public void v2() throws ComponentInitializationException {
        final AttributeRequesterNameIDFormatExactPolicyRule rule =
                (AttributeRequesterNameIDFormatExactPolicyRule) getPolicyRule("requesterNameId.xml", false);

        Assert.assertEquals(rule.getNameIdFormat(), "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");

    }

    @Test public void basic() throws ComponentInitializationException {
        AttributeRequesterNameIDFormatExactPolicyRule rule =
                (AttributeRequesterNameIDFormatExactPolicyRule) getPolicyRule("requesterNameId2.xml", false);

        Assert.assertEquals(rule.getNameIdFormat(), "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");

        rule = (AttributeRequesterNameIDFormatExactPolicyRule) getPolicyRule("requesterNameId2.xml", true);

        Assert.assertEquals(rule.getNameIdFormat(), "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
    }
}
