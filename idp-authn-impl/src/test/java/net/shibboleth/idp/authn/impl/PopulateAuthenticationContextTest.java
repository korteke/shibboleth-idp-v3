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

package net.shibboleth.idp.authn.impl;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import org.opensaml.profile.action.ActionTestingSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/** {@link PopulateAuthenticationContext} unit test and base class for further action tests. */
public class PopulateAuthenticationContextTest {

    protected RequestContext src;
    protected ProfileRequestContext prc;
    protected ImmutableList<AuthenticationFlowDescriptor> authenticationFlows;
    
    @BeforeMethod public void setUp() throws Exception {        
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
        prc.addSubcontext(new AuthenticationContext(), true);

        authenticationFlows = ImmutableList.of(new AuthenticationFlowDescriptor(),
                new AuthenticationFlowDescriptor(), new AuthenticationFlowDescriptor());
        authenticationFlows.get(0).setId("test1");
        authenticationFlows.get(1).setId("test2");
        authenticationFlows.get(2).setId("test3");

        PopulateAuthenticationContext action = new PopulateAuthenticationContext();
        action.setAvailableFlows(authenticationFlows);
        action.initialize();

        action.execute(src);
    }

    /** Test that the authentication context is properly added. */
    @Test public void testAction() throws Exception {
        
        ActionTestingSupport.assertProceedEvent(prc);
        AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class, false);
        Assert.assertNotNull(authCtx);

        Assert.assertEquals(authCtx.getPotentialFlows().size(), 3);
        Assert.assertNotNull(authCtx.getPotentialFlows().get("test1"));
        Assert.assertNotNull(authCtx.getPotentialFlows().get("test2"));
        Assert.assertNotNull(authCtx.getPotentialFlows().get("test3"));
    }
}