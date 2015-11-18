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

package net.shibboleth.idp.profile.interceptor.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/** {@link PopulateProfileInterceptorContext} unit test. */
public class PopulateProfileInterceptorContextTest {

    protected ImmutableList<ProfileInterceptorFlowDescriptor> interceptorFlows;

    protected RequestContext src;

    protected ProfileRequestContext prc;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        interceptorFlows =
                ImmutableList.of(new ProfileInterceptorFlowDescriptor(), new ProfileInterceptorFlowDescriptor(),
                        new ProfileInterceptorFlowDescriptor());
        interceptorFlows.get(0).setId("intercept/test1");
        interceptorFlows.get(1).setId("intercept/test2");
        interceptorFlows.get(2).setId("intercept/test3");

        final PopulateProfileInterceptorContext action = new PopulateProfileInterceptorContext();
        action.setAvailableFlows(interceptorFlows);
        action.setActiveFlowsLookupStrategy(FunctionSupport.<ProfileRequestContext, Collection<String>> constant(Arrays
                .asList("test1", "test2", "test3")));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    /** Test that the context is properly added. */
    @Test public void testAction() throws Exception {
        final ProfileInterceptorContext interceptorContext = prc.getSubcontext(ProfileInterceptorContext.class);
        Assert.assertNotNull(interceptorContext);
        final List<ProfileInterceptorFlowDescriptor> availableFlows =
                ImmutableList.copyOf(interceptorContext.getAvailableFlows().values());
        Assert.assertEquals(availableFlows.size(), 3);
        Assert.assertEquals(availableFlows.get(0).getId(), "intercept/test1");
        Assert.assertEquals(availableFlows.get(1).getId(), "intercept/test2");
        Assert.assertEquals(availableFlows.get(2).getId(), "intercept/test3");
    }

    /** Test that the context is properly added. */
    @Test public void testError() throws Exception {
        final PopulateProfileInterceptorContext action = new PopulateProfileInterceptorContext();
        action.setAvailableFlows(interceptorFlows);
        action.setActiveFlowsLookupStrategy(FunctionSupport
                .<ProfileRequestContext, Collection<String>> constant(Collections.singletonList("test4")));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_PROFILE_CONFIG);
    }

}