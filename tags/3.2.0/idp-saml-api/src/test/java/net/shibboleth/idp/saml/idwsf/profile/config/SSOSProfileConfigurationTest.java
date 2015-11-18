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

package net.shibboleth.idp.saml.idwsf.profile.config;

import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/** Unit test for {@link SSOSProfileConfiguration}. */
public class SSOSProfileConfigurationTest {

    @Test
    public void testProfileId() {
        Assert.assertEquals(SSOSProfileConfiguration.PROFILE_ID, "http://shibboleth.net/ns/profiles/liberty/ssos");

        SSOSProfileConfiguration config = new SSOSProfileConfiguration();
        Assert.assertEquals(config.getId(), SSOSProfileConfiguration.PROFILE_ID);
    }
    
    @Test
    public void testDelegationPredicate() {
        ProfileRequestContext prc = new ProfileRequestContext<>();
        
        SSOSProfileConfiguration config = new SSOSProfileConfiguration();
        Assert.assertNotNull(config.getDelegationPredicate());
        Assert.assertFalse(config.getDelegationPredicate().apply(prc));
        
        config.setDelegationPredicate(Predicates.<ProfileRequestContext>alwaysTrue());
        Assert.assertNotNull(config.getDelegationPredicate());
        Assert.assertTrue(config.getDelegationPredicate().apply(prc));
        
    }
    
}