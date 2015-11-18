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
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeRequesterEntityAttributeExactPolicyRule}.
 */
public class AttributeRequesterEntityAttributeExactPolicyRuleTest extends BaseMetadataTests {

    private AttributeRequesterEntityAttributeExactPolicyRule getMatcher() throws ComponentInitializationException {
        return getMatcher("urn:example.org:policies", "urn:example.org:policy:1234", null);
    }

    private AttributeRequesterEntityAttributeExactPolicyRule getMatcher(String attributeName, String attributeValue,
            String attributeNameFormat) throws ComponentInitializationException {
        AttributeRequesterEntityAttributeExactPolicyRule matcher = new AttributeRequesterEntityAttributeExactPolicyRule();
        matcher.setId("matcher");
        matcher.setAttributeName(attributeName);
        matcher.setValue(attributeValue);
        matcher.setNameFormat(attributeNameFormat);
        matcher.initialize();
        return matcher;
    }

    @Test public void testValue() throws ComponentInitializationException {

        AttributeRequesterEntityAttributeExactPolicyRule matcher = getMatcher();
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.TRUE);

        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);

    }

    @Test public void testFormat() throws ComponentInitializationException {

        AttributeRequesterEntityAttributeExactPolicyRule matcher =
                getMatcher("urn:example.org:entitlements", "urn:example.org:entitlements:1234", null);
        Assert.assertEquals(matcher.getValue(), "urn:example.org:entitlements:1234");
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.TRUE);

        Assert.assertEquals(matcher.matches(metadataContext(wikiEntity, "Principal")), Tristate.FALSE);

        matcher = getMatcher("urn:example.org:entitlements", "urn:example.org:entitlements:1234", "foo");
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.FALSE);

        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);

        matcher =
                getMatcher("urn:example.org:entitlements", "urn:example.org:entitlements:1234",
                        "urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.TRUE);

        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);
    }

    @Test public void testNoMatch() throws ComponentInitializationException {

        AttributeRequesterEntityAttributeExactPolicyRule matcher =
                getMatcher("urn:example.org:policies", "urn:example.org:policy:1235", null);
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);

        matcher = getMatcher("urn:example.org:policiess", "urn:example.org:policy:1234", null);
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(metadataContext(noneEntity, "Principal")), Tristate.FALSE);
    }

    @Test public void testSplitAttribute() throws ComponentInitializationException {

        AttributeRequesterEntityAttributeExactPolicyRule matcher =
                getMatcher("urn:example.org:policies", "urn:example.org:policy:1234", null);
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.TRUE);
        Assert.assertEquals(matcher.matches(metadataContext(jiraEntity, "Principal")), Tristate.FALSE);

        matcher = getMatcher("urn:example.org:policies", "urn:example.org:policy:5678", null);
        Assert.assertEquals(matcher.matches(metadataContext(idpEntity, "Principal")), Tristate.TRUE);
        Assert.assertEquals(matcher.matches(metadataContext(noneEntity, "Principal")), Tristate.FALSE);
    }

    @Test public void testUnpopulated()
            throws ComponentInitializationException {
        Assert.assertEquals(getMatcher().matches(DataSources.unPopulatedFilterContext()), Tristate.FALSE);
    }

    @Test public void testNoMetadata()
            throws ComponentInitializationException {
        Assert.assertEquals(getMatcher().matches(metadataContext(null, "Principal")), Tristate.FALSE);
    }
}
