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

import net.shibboleth.idp.profile.spring.logic.DummyPredicate;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactConfiguration;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("deprecation")
public class SAML2BrowserSSOProfileTest extends BaseSAMLProfileTest {

    @Test public void defaults() {

        BrowserSSOProfileConfiguration profile = getBean(BrowserSSOProfileConfiguration.class, "saml/saml2SSO.xml", "beans.xml");

        Assert.assertTrue(profile.includeAttributeStatement());
        Assert.assertFalse(profile.skipEndpointValidationWhenSigned());
        Assert.assertEquals(profile.getMaximumSPSessionLifetime(), 0);

        // defaults for AbstractSAML2ProfileConfiguration

        assertTruePredicate(profile.getEncryptAssertions());
        assertFalsePredicate(profile.getEncryptNameIDs());

        Assert.assertEquals(profile.getProxyCount(), 0);
        Assert.assertTrue(profile.getProxyAudiences().isEmpty());

        // defaults for AbstractSAMLProfileConfiguration
        assertFalsePredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertTruePredicate(profile.getSignResponses());
        assertFalsePredicate(profile.getEncryptAttributes());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertNull(profile.getAllowingDelegation());
        Assert.assertNotNull(profile.getAllowDelegation());
        assertFalsePredicate(profile.getAllowDelegation());
        Assert.assertEquals(profile.getMaximumTokenDelegationChainLength(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml2-sso");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());
        Assert.assertNull(profile.getSecurityConfiguration());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertNull(artifact.getArtifactType());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 1111);
    }

    @Test public void values() {
        BrowserSSOProfileConfiguration profile =
                getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/saml2SSOValues.xml");

        Assert.assertFalse(profile.includeAttributeStatement());
        Assert.assertTrue(profile.skipEndpointValidationWhenSigned());
        Assert.assertEquals(profile.getMaximumSPSessionLifetime(), 1);

        assertConditionalPredicate(profile.getEncryptAssertions());
        assertFalsePredicate(profile.getEncryptNameIDs());
        Assert.assertTrue(profile.isEncryptionOptional());
        Assert.assertNull(profile.getAllowingDelegation());
        Assert.assertNotNull(profile.getAllowDelegation());
        assertTruePredicate(profile.getAllowDelegation());
        Assert.assertEquals(profile.getMaximumTokenDelegationChainLength(), 3);

        Assert.assertEquals(profile.getProxyCount(), 0);
        Assert.assertTrue(profile.getProxyAudiences().isEmpty());

        // defaults for AbstractSAMLProfileConfiguration
        assertConditionalPredicate(profile.getSignRequests());
        assertTruePredicate(profile.getSignAssertions());
        assertFalsePredicate(profile.getSignResponses());
        assertTruePredicate(profile.getEncryptAttributes());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml2-sso");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());

        final SAMLArtifactConfiguration artifact = profile.getArtifactConfiguration();
        Assert.assertEquals(artifact.getArtifactType(), BigInteger.valueOf(7654).toByteArray());
        Assert.assertEquals(artifact.getArtifactResolutionServiceIndex().intValue(), 1111);

        Assert.assertEquals(profile.getDefaultAuthenticationMethods().size(), 1);
        final AuthnContextClassRefPrincipal authnMethod =
                (AuthnContextClassRefPrincipal) profile.getDefaultAuthenticationMethods().get(0);
        Assert.assertEquals(authnMethod.getAuthnContextClassRef().getAuthnContextClassRef(),
                "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

        final List<String> nameIDPrefs = profile.getNameIDFormatPrecedence();

        Assert.assertEquals(nameIDPrefs.size(), 2);
        Assert.assertTrue(nameIDPrefs.contains("one"));
        Assert.assertTrue(nameIDPrefs.contains("two"));
        
        Assert.assertNotNull(profile.getSecurityConfiguration());
        Assert.assertNotNull(profile.getSecurityConfiguration().getSignatureSigningConfiguration());
    }
    
    @Test public void allowDelegationPredicateRef() {
        BrowserSSOProfileConfiguration profile =
                getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/saml2SSOAllowDelegationPredicate.xml");

        Assert.assertNull(profile.getAllowingDelegation());
        Assert.assertNotNull(profile.getAllowDelegation());
        Assert.assertTrue(profile.getAllowDelegation() instanceof DummyPredicate);
    }
    
    @Test public void allowDelegationPropertyReplacement() {
        BrowserSSOProfileConfiguration profile =
                getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/saml2SSOAllowDelegationPropertyReplacement.xml");

        Assert.assertNull(profile.getAllowingDelegation());
        Assert.assertNotNull(profile.getAllowDelegation());
        assertTruePredicate(profile.getAllowDelegation());
    }
    
}