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

package net.shibboleth.idp.consent.logic.impl;

import net.shibboleth.idp.consent.flow.ar.impl.AttributeReleaseFlowDescriptor;
import net.shibboleth.idp.consent.flow.impl.ConsentFlowDescriptor;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * {@link FlowDescriptorLookupFunction} unit test.
 */
public class FlowDescriptorLookupFunctionTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private ProfileInterceptorContext pic;

    @BeforeMethod public void setUp() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        pic = new ProfileInterceptorContext();
        prc.addSubcontext(pic);
    }

    @Test public void testLookup() {
        pic.setAttemptedFlow(new ConsentFlowDescriptor());

        final FlowDescriptorLookupFunction<ConsentFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(ConsentFlowDescriptor.class);

        final ConsentFlowDescriptor flow = strategy.apply(prc);

        Assert.assertTrue(flow instanceof ConsentFlowDescriptor);
    }

    @Test public void testLookupChild() {
        pic.setAttemptedFlow(new ConsentFlowDescriptor());

        final FlowDescriptorLookupFunction<AttributeReleaseFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(AttributeReleaseFlowDescriptor.class);

        final AttributeReleaseFlowDescriptor flow = strategy.apply(prc);

        Assert.assertNull(flow);
    }

    @Test public void testLookupParent() {
        pic.setAttemptedFlow(new AttributeReleaseFlowDescriptor());

        final FlowDescriptorLookupFunction<ConsentFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(ConsentFlowDescriptor.class);

        final ConsentFlowDescriptor flow = strategy.apply(prc);

        Assert.assertTrue(flow instanceof ConsentFlowDescriptor);
    }

    @Test public void testNullInput() {

        final FlowDescriptorLookupFunction<ConsentFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(ConsentFlowDescriptor.class);

        final ConsentFlowDescriptor flow = strategy.apply(null);

        Assert.assertNull(flow);
    }

    @Test public void testNoInterceptorContext() {
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        final FlowDescriptorLookupFunction<ConsentFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(ConsentFlowDescriptor.class);

        final ConsentFlowDescriptor flow = strategy.apply(null);

        Assert.assertNull(flow);
    }

    @Test public void testNoAttemptedFlow() {

        final FlowDescriptorLookupFunction<ConsentFlowDescriptor> strategy =
                new FlowDescriptorLookupFunction(ConsentFlowDescriptor.class);

        final ConsentFlowDescriptor flow = strategy.apply(null);

        Assert.assertNull(flow);
    }
}
