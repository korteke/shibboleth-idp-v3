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

import java.util.Set;

import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.RegistrationAuthorityPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.saml.impl.RegistrationAuthorityRuleParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link RegistrationAuthorityRuleParser}.
 */
public class RegistrationAuthorityRuleParserTest extends BaseAttributeFilterParserTest {


    @Test public void silentTrue() throws ComponentInitializationException {
        silentTrue("registrationAuthorityOne.xml", false);
        silentTrue("registrationAuthorityOne.xml", true);
    }

    public void silentTrue(String path, boolean isAfp) throws ComponentInitializationException {

        final RegistrationAuthorityPolicyRule rule = (RegistrationAuthorityPolicyRule) getPolicyRule(path, isAfp);

        Assert.assertTrue(rule.isMatchIfMetadataSilent());
        final Set<String> issuers = rule.getIssuers();

        Assert.assertEquals(issuers.size(), 2);
        Assert.assertTrue(issuers.contains("https://example.org/SilentTrue/One"));
        Assert.assertTrue(issuers.contains("https://example.org/SilentTrue/Two"));
    }

    @Test public void silentFalse() throws ComponentInitializationException {
        silentFalse("registrationAuthorityTwo.xml", false);
        silentFalse("registrationAuthorityTwo.xml", true);
    }
    
    public void silentFalse(String path, boolean isAfp) throws ComponentInitializationException {
        final RegistrationAuthorityPolicyRule rule = (RegistrationAuthorityPolicyRule) getPolicyRule(path, isAfp);

        Assert.assertTrue(rule.isMatchIfMetadataSilent());
        final Set<String> issuers = rule.getIssuers();

        Assert.assertEquals(issuers.size(), 3);
        Assert.assertTrue(issuers.contains("https://example.org/SilentFalse/One"));
        Assert.assertTrue(issuers.contains("https://example.org/SilentFalse/Two"));
        Assert.assertTrue(issuers.contains("https://example.org/SilentFalse/Three"));
    }
}
