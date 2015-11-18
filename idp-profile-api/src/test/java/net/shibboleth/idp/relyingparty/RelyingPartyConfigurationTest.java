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

package net.shibboleth.idp.relyingparty;

import java.util.ArrayList;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link RelyingPartyConfiguration}. */
public class RelyingPartyConfigurationTest {

    @Test public void testConstruction() {
        RelyingPartyConfiguration config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setDetailedErrors(true);
        Assert.assertEquals(config.getId(), "foo");
        Assert.assertEquals(config.getResponderId(), "http://idp.example.org");
        Assert.assertTrue(config.isDetailedErrors());
        Assert.assertTrue(config.getProfileConfigurations().isEmpty());

        config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setDetailedErrors(false);
        Assert.assertEquals(config.getId(), "foo");
        Assert.assertEquals(config.getResponderId(), "http://idp.example.org");
        Assert.assertFalse(config.isDetailedErrors());
        Assert.assertTrue(config.getProfileConfigurations().isEmpty());

        ArrayList<ProfileConfiguration> profileConfigs = new ArrayList<>();
        profileConfigs.add(new MockProfileConfiguration("foo"));
        profileConfigs.add(null);
        profileConfigs.add(new MockProfileConfiguration("bar"));

        config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setProfileConfigurations(profileConfigs);
        Assert.assertEquals(config.getId(), "foo");
        Assert.assertEquals(config.getResponderId(), "http://idp.example.org");
        Assert.assertEquals(config.getProfileConfigurations().size(), 2);

        try {
            config = new RelyingPartyConfiguration();
            config.setId(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            config = new RelyingPartyConfiguration();
            config.setId("");
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            config = new RelyingPartyConfiguration();
            config.setResponderId(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testProfileConfiguration() {
        ArrayList<ProfileConfiguration> profileConfigs = new ArrayList<>();
        profileConfigs.add(new MockProfileConfiguration("foo"));
        profileConfigs.add(new MockProfileConfiguration("bar"));

        RelyingPartyConfiguration config = new RelyingPartyConfiguration();
        config.setId("foo");
        config.setResponderId("http://idp.example.org");
        config.setProfileConfigurations(profileConfigs);
        Assert.assertNotNull(config.getProfileConfiguration("foo"));
        Assert.assertNotNull(config.getProfileConfiguration("bar"));
        Assert.assertNull(config.getProfileConfiguration("baz"));
    }
}