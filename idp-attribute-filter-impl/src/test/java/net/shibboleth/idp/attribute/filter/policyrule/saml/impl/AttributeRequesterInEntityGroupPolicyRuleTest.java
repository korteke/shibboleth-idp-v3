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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeRequesterInEntityGroupPolicyRule}.
 */
public class AttributeRequesterInEntityGroupPolicyRuleTest extends BaseMetadataTests {

    private AttributeRequesterInEntityGroupPolicyRule getMatcher(String group) throws ComponentInitializationException {
        AttributeRequesterInEntityGroupPolicyRule matcher = new AttributeRequesterInEntityGroupPolicyRule();
        matcher.setId("matcher");
        matcher.setEntityGroup(group);
        matcher.initialize();
        return matcher;
    }


    @Test public void parent() throws ComponentInitializationException {
        AttributeRequesterInEntityGroupPolicyRule matcher = getMatcher("http://shibboleth.net");

        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.TRUE);
        Assert.assertEquals(matcher.matches(metadataContext(null, null)), Tristate.FALSE);

        matcher = getMatcher("urn:otherstuff");
        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);
    }

    @Test public void getter() throws ComponentInitializationException {
        Assert.assertEquals(getMatcher("http://shibboleth.net").getEntityGroup(), "http://shibboleth.net");
    }

    @Test public void noGroup() throws ComponentInitializationException {
        AttributeRequesterInEntityGroupPolicyRule matcher = new AttributeRequesterInEntityGroupPolicyRule();
        matcher.setId("matcher");
        matcher.initialize();
        Assert.assertEquals(matcher.matches(metadataContext(null, null)), Tristate.FALSE);
    }
}
