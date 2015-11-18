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

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.SpringRequestContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link FlowIdLookupFunction} unit test. */
public class FlowIdLookupFunctionTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private FlowIdLookupFunction function;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        function = new FlowIdLookupFunction();
    }

    @Test public void testNullInput() {
        Assert.assertNull(function.apply(null));
    }

    @Test public void testNullSpringRequestContext() {
        Assert.assertNull(prc.getSubcontext(SpringRequestContext.class));
        Assert.assertNull(function.apply(prc));
    }

    @Test public void testNullWebFlowRequestContext() {
        prc.getSubcontext(SpringRequestContext.class, true);
        Assert.assertNotNull(prc.getSubcontext(SpringRequestContext.class));
        Assert.assertNull(prc.getSubcontext(SpringRequestContext.class).getRequestContext());
        Assert.assertNull(function.apply(prc));
    }

    @Test(enabled = false) public void testNullFlowExecutionContext() {
        // TODO Need to override Web Flow mocks for a null FlowExecutionContext
    }

    @Test(enabled = false) public void testNonActiveFlowExecutionContext() {
        // TODO Need to override Web Flow mocks for a null FlowExecutionContext
    }

    @Test public void testFlowId() {
        prc.getSubcontext(SpringRequestContext.class, true).setRequestContext(src);
        Assert.assertNotNull(prc.getSubcontext(SpringRequestContext.class));
        Assert.assertNotNull(prc.getSubcontext(SpringRequestContext.class).getRequestContext());
        Assert.assertEquals(function.apply(prc), "mockFlow");
    }
}
