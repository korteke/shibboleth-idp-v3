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
import java.util.List;

import net.shibboleth.idp.saml.authn.principal.AuthenticationMethodPrincipal;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactConfiguration;
import net.shibboleth.idp.saml.saml1.profile.config.BrowserSSOProfileConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ShibbolethSSOProfileTest extends BaseSAMLProfileTest {

    @Test public void defaults() {

        BrowserSSOProfileConfiguration profile = getBean(BrowserSSOProfileConfiguration.class, "saml/shibbolethSSO.xml", "beans.xml");

        Assert.assertFalse(profile.includeAttributeStatement());

        assertFalsePredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertTruePredicate(profile.getSignResponses());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/shibboleth-sso");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertNull(artifact.getArtifactType());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 1111);
    }

    @Test public void values() {
        BrowserSSOProfileConfiguration profile =
                getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/shibbolethSSOValues.xml");

        Assert.assertTrue(profile.includeAttributeStatement());
        
        assertConditionalPredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertTruePredicate(profile.getSignResponses());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/shibboleth-sso");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertEquals(artifact.getArtifactType(), BigInteger.valueOf(32767).toByteArray());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 1111);

        Assert.assertEquals(profile.getDefaultAuthenticationMethods().size(), 1);
        final AuthenticationMethodPrincipal authnMethod =
                (AuthenticationMethodPrincipal) profile.getDefaultAuthenticationMethods().get(0);
        Assert.assertEquals(authnMethod.getName(),"urn:oasis:names:tc:SAML:2.0:ac:classes:Password");

        final List<String> nameIDPrefs = profile.getNameIDFormatPrecedence();

        Assert.assertEquals(nameIDPrefs.size(), 2);
        Assert.assertTrue(nameIDPrefs.contains("three"));
        Assert.assertTrue(nameIDPrefs.contains("four"));

    }
    
}