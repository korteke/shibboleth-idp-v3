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

package net.shibboleth.idp.saml.saml2.profile.config;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/** Unit test for {@link BrowserSSOProfileConfiguration}. */
public class BrowserSSOProfileConfigurationTest {

    @Test
    public void testProfileId() {
        Assert.assertEquals(BrowserSSOProfileConfiguration.PROFILE_ID, "http://shibboleth.net/ns/profiles/saml2/sso/browser");

        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertEquals(config.getId(), BrowserSSOProfileConfiguration.PROFILE_ID);
    }

    @Test
    public void testIncludeAttributeStatement() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertTrue(config.includeAttributeStatement());

        config.setIncludeAttributeStatement(false);
        Assert.assertFalse(config.includeAttributeStatement());
    }

    @Test
    public void testSkipEndpointValidationWhenSigned() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertFalse(config.skipEndpointValidationWhenSigned());

        config.setSkipEndpointValidationWhenSigned(true);
        Assert.assertTrue(config.skipEndpointValidationWhenSigned());
    }
    
    @Test
    public void testMaximumSpSessionLifeTime() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertEquals(config.getMaximumSPSessionLifetime(), 0);

        config.setMaximumSPSessionLifetime(1000);
        Assert.assertEquals(config.getMaximumSPSessionLifetime(), 1000);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAllowingDelegation() {
        // Note: testing the deprecated boolean value variant
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertFalse(config.isAllowingDelegation());
        Assert.assertNull(config.getAllowingDelegation());
        
        config.setAllowingDelegation(false);
        Assert.assertFalse(config.isAllowingDelegation());
        Assert.assertNotNull(config.getAllowingDelegation());
        Assert.assertEquals(config.getAllowingDelegation(), Boolean.FALSE);

        config.setAllowingDelegation(true);
        Assert.assertTrue(config.isAllowingDelegation());
        Assert.assertNotNull(config.getAllowingDelegation());
        Assert.assertEquals(config.getAllowingDelegation(), Boolean.TRUE);
    }
    
    @Test
    public void testAllowDelegation() {
        // Note: testing the newer predicate variant
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertNotNull(config.getAllowDelegation());
        
        Predicate<ProfileRequestContext> predicate = Predicates.alwaysTrue();
        config.setAllowDelegation(predicate);
        Assert.assertSame(config.getAllowDelegation(), predicate);
        
        try {
            config.setAllowDelegation(null);
            Assert.fail("Null predicate should not have been allowed");
        } catch (ConstraintViolationException e) {
            // expected, do nothing 
        }
    }
    
    @Test
    public void testMaximumTokenDelegationChainLength(){
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertEquals(config.getMaximumTokenDelegationChainLength(), 1);
        
        config.setMaximumTokenDelegationChainLength(10);
        Assert.assertEquals(config.getMaximumTokenDelegationChainLength(), 10);
    }
    
}