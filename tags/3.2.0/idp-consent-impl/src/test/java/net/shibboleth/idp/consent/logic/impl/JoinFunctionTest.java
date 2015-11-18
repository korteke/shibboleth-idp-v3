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
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/** {@link JoinFunction} unit test. */
public class JoinFunctionTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private Function<ProfileRequestContext, String> functionA;

    private Function<ProfileRequestContext, String> functionB;

    private JoinFunction function;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        functionA = FunctionSupport.<ProfileRequestContext, String> constant("a");
        functionB = FunctionSupport.<ProfileRequestContext, String> constant("b");
    }

    @Test public void testNullInput() {
        function = new JoinFunction(functionA, functionB);

        Assert.assertNull(function.apply(null));
    }

    @Test public void testJoin() {
        function = new JoinFunction(functionA, functionB);

        Assert.assertEquals(function.apply(prc), "a:b");
    }

    @Test public void testNullFirstFunctionJoin() {
        functionA = FunctionSupport.<ProfileRequestContext, String> constant(null);

        function = new JoinFunction(functionA, functionB);

        Assert.assertEquals(function.apply(prc), "b");
    }

    @Test public void testNullSecondFunctionJoin() {
        functionB = FunctionSupport.<ProfileRequestContext, String> constant(null);

        function = new JoinFunction(functionA, functionB);

        Assert.assertEquals(function.apply(prc), "a");
    }

    @Test public void testEmptyFirstFunctionJoin() {
        functionA = FunctionSupport.<ProfileRequestContext, String> constant("");

        function = new JoinFunction(functionA, functionB);

        Assert.assertEquals(function.apply(prc), ":b");
    }

    @Test public void testEmptySecondFunctionJoin() {
        functionB = FunctionSupport.<ProfileRequestContext, String> constant("");

        function = new JoinFunction(functionA, functionB);

        Assert.assertEquals(function.apply(prc), "a:");
    }

}
