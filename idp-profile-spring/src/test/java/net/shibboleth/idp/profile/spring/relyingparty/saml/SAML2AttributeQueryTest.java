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

package net.shibboleth.idp.profile.spring.relyingparty.saml;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;

import net.shibboleth.idp.saml.profile.config.SAMLArtifactConfiguration;
import net.shibboleth.idp.saml.saml2.profile.config.AttributeQueryProfileConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SAML2AttributeQueryTest extends BaseSAMLProfileTest {

    @Test public void defaults() {

        AttributeQueryProfileConfiguration profile =
                getBean(AttributeQueryProfileConfiguration.class, "saml/saml2attributeQuery.xml", "beans.xml");

        // defaults for AbstractSAML2ProfileConfiguration

        assertConditionalPredicate(profile.getEncryptAssertions());
        assertFalsePredicate(profile.getEncryptNameIDs());

        Assert.assertEquals(profile.getProxyCount(), 0);
        Assert.assertTrue(profile.getProxyAudiences().isEmpty());

        // defaults for AbstractSAMLProfileConfiguration
        assertFalsePredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertConditionalPredicate(profile.getSignResponses());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml-soap");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());
        Assert.assertNull(profile.getSecurityConfiguration());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertNull(artifact.getArtifactType());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 2143);
    }

    @Test public void values() {
        AttributeQueryProfileConfiguration profile =
                getBean(AttributeQueryProfileConfiguration.class, "beans.xml", "saml/saml2attributeQueryValues.xml");

        assertFalsePredicate(profile.getEncryptAssertions());
        assertTruePredicate(profile.getEncryptNameIDs());

        Assert.assertEquals(profile.getProxyCount(), 99);
        final Collection<String> proxyAudiences = profile.getProxyAudiences();
        Assert.assertEquals(proxyAudiences.size(), 2);
        Assert.assertTrue(proxyAudiences.contains("ProxyAudience1"));
        Assert.assertTrue(proxyAudiences.contains("NibbleAHappyWarthog"));
        
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml-soap");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());
        Assert.assertNull(profile.getSecurityConfiguration());

        assertFalsePredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertConditionalPredicate(profile.getSignResponses());

        Assert.assertEquals(profile.getAssertionLifetime(), 10 * 60 * 1000);

        final Set<String> audiences = profile.getAdditionalAudiencesForAssertion();
        Assert.assertEquals(audiences.size(), 2);
        Assert.assertTrue(audiences.contains("NibbleAHappyWarthog"));
        Assert.assertTrue(audiences.contains("Audience2"));

        Assert.assertFalse(profile.includeConditionsNotBefore());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertEquals(artifact.getArtifactType(), BigInteger.valueOf(765).toByteArray());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 2143);

    }

}