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

import java.util.List;

import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.config.ECPProfileConfiguration;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SAML2ECPProfileTest extends BaseSAMLProfileTest {

    @Test public void defaults() {

        ECPProfileConfiguration profile = getBean(ECPProfileConfiguration.class, "saml/ecp.xml", "beans.xml");

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
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml2-ecp");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());

        Assert.assertNull(profile.getArtifactConfiguration());
    }

    @Test public void values() {
        BrowserSSOProfileConfiguration profile =
                getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/ecpValues.xml");

        Assert.assertFalse(profile.includeAttributeStatement());
        Assert.assertTrue(profile.skipEndpointValidationWhenSigned());
        Assert.assertEquals(profile.getMaximumSPSessionLifetime(), 1);

        assertTruePredicate(profile.getEncryptAssertions());
        assertFalsePredicate(profile.getEncryptNameIDs());

        Assert.assertEquals(profile.getProxyCount(), 0);
        Assert.assertTrue(profile.getProxyAudiences().isEmpty());

        // defaults for AbstractSAMLProfileConfiguration
        assertFalsePredicate(profile.getSignRequests());
        assertFalsePredicate(profile.getSignAssertions());
        assertTruePredicate(profile.getSignResponses());
        Assert.assertEquals(profile.getAssertionLifetime(), 5 * 60 * 1000);
        Assert.assertTrue(profile.getAdditionalAudiencesForAssertion().isEmpty());
        Assert.assertTrue(profile.includeConditionsNotBefore());
        Assert.assertEquals(profile.getInboundInterceptorFlows().size(), 1);
        Assert.assertEquals(profile.getInboundInterceptorFlows().get(0), "security-policy/saml2-ecp");
        Assert.assertTrue(profile.getOutboundInterceptorFlows().isEmpty());

        Assert.assertNull(profile.getArtifactConfiguration());

        Assert.assertEquals(profile.getDefaultAuthenticationMethods().size(), 1);
        final AuthnContextClassRefPrincipal authnMethod =
                (AuthnContextClassRefPrincipal) profile.getDefaultAuthenticationMethods().get(0);
        Assert.assertEquals(authnMethod.getAuthnContextClassRef().getAuthnContextClassRef(),
                "urn:oasis:names:tc:SAML:2.0:ac:classes:Password");

        final List<String> nameIDPrefs = profile.getNameIDFormatPrecedence();

        Assert.assertEquals(nameIDPrefs.size(), 2);
        Assert.assertTrue(nameIDPrefs.contains("three"));
        Assert.assertTrue(nameIDPrefs.contains("four"));
    }

    @Test(expectedExceptions = {BeanDefinitionParsingException.class,}) public void localityAddress() {
        getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/ecpLocalityAddress.xml");
    }

    @Test(expectedExceptions = {BeanDefinitionParsingException.class,}) public void localityDnsname() {
        getBean(BrowserSSOProfileConfiguration.class, "beans.xml", "saml/ecpLocalityDNSName.xml");
    }
    
}