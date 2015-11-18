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

package net.shibboleth.idp.profile.impl;

import java.util.Collections;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.profile.impl.SelectProfileConfiguration;
import net.shibboleth.idp.relyingparty.MockProfileConfiguration;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link SelectProfileConfiguration} unit test. */
public class SelectProfileConfigurationTest {

    private RequestContext src;
    
    private ProfileRequestContext prc;

    private SelectProfileConfiguration action;
    
    @BeforeMethod
    public void setUp() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        action = new SelectProfileConfiguration();
        action.initialize();        
    }
    
    /** Test that the action errors out properly if there is no relying party context. */
    @Test public void testNoRelyingPartyContext() throws Exception {
        prc.removeSubcontext(RelyingPartyContext.class);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CTX);
    }

    /** Test that the action errors out properly if there is no relying party configuration. */
    @Test public void testNoRelyingPartyConfiguration() throws Exception {
        prc.getSubcontext(RelyingPartyContext.class).setConfiguration(null);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CONFIG);
    }

    /** Test that the action errors out properly if the desired profile configuration is not configured. */
    @Test public void testInvalidProfileConfiguration() throws Exception {
        src = new RequestContextBuilder().setRelyingPartyProfileConfigurations(
                Collections.<ProfileConfiguration>singleton(new MockProfileConfiguration("mock"))).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_PROFILE_CONFIG);
    }

    /** Test that the action selects the appropriate profile configuration and proceeds properly. */
    @Test public void testSelectProfileConfiguration() throws Exception {
        src = new RequestContextBuilder().setRelyingPartyProfileConfigurations(
                Collections.<ProfileConfiguration>singleton(new MockProfileConfiguration("mock"))).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        prc.setProfileId("mock");

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertNotNull(prc.getSubcontext(RelyingPartyContext.class).getProfileConfig());
        Assert.assertEquals(prc.getSubcontext(RelyingPartyContext.class).getProfileConfig().getId(), "mock");
    }
    
}