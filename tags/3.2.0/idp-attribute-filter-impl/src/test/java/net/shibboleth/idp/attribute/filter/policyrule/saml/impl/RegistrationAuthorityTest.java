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

import java.util.Arrays;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.RegistrationAuthorityPolicyRule;

import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** {@link RegistrationAuthorityPolicyRule} unit test. */
public class RegistrationAuthorityTest extends BaseMetadataTests {

    private static final String REQUESTED_REG_INFO = "http://www.swamid.se/";

    private static final String INCOMMON_REG_INFO = "https://incommon.org";

    private static final String INCOMMON_SP = "https://wiki.ligo.org/shibboleth-sp";

    private static final String NO_REGINFO_SP = "https://issues.shibboleth.net/shibboleth";

    private final String SWAMID = "https://sp-test.swamid.se/shibboleth";

    private EntitiesDescriptor metadata;

    @BeforeClass(dependsOnMethods = "initXMLObjectSupport") public void initRATest() {
        metadata = unmarshallElement("/data/net/shibboleth/idp/filter/impl/saml/mdrpi-metadata.xml");
    }

    private EntityDescriptor getEntity(String entityID) {
        for (EntityDescriptor entity : metadata.getEntityDescriptors()) {
            if (entity.getEntityID().equals(entityID)) {
                return entity;
            }
        }
        Assert.fail("Could not find " + entityID);
        return null;
    }

    @Test public void swamid() throws Exception {

        AttributeFilterContext context = metadataContext(getEntity(SWAMID), "principal");
        final RegistrationAuthorityPolicyRule filter = new RegistrationAuthorityPolicyRule();
        String[] array = {REQUESTED_REG_INFO, "foo",};
        filter.setIssuers(Arrays.asList(array));

        Assert.assertEquals(filter.matches(context), Tristate.TRUE);
        array[0] = INCOMMON_REG_INFO;
        filter.setIssuers(Arrays.asList(array));
        Assert.assertEquals(filter.matches(context), Tristate.FALSE);
    }

    @Test public void ligo() {
        AttributeFilterContext context = metadataContext(getEntity(INCOMMON_SP), "principal");
        final RegistrationAuthorityPolicyRule filter = new RegistrationAuthorityPolicyRule();
        String[] array = {REQUESTED_REG_INFO, "foo",};
        filter.setIssuers(Arrays.asList(array));

        Assert.assertEquals(filter.matches(context), Tristate.FALSE);
        array[0] = INCOMMON_REG_INFO;
        filter.setIssuers(Arrays.asList(array));
        Assert.assertEquals(filter.matches(context), Tristate.TRUE);
    }

    @Test public void none()  {
        AttributeFilterContext context = metadataContext(getEntity(NO_REGINFO_SP), "principal");
        final RegistrationAuthorityPolicyRule filter = new RegistrationAuthorityPolicyRule();
        String[] array = {REQUESTED_REG_INFO, INCOMMON_REG_INFO, "foo",};
        filter.setIssuers(Arrays.asList(array));

        filter.setMatchIfMetadataSilent(true);
        Assert.assertEquals(filter.matches(context), Tristate.TRUE);
        filter.setMatchIfMetadataSilent(false);
        Assert.assertEquals(filter.matches(context), Tristate.FALSE);
    }
}