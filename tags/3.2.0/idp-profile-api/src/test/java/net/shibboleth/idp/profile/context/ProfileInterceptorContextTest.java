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

package net.shibboleth.idp.profile.context;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link ProfileInterceptorContext} unit test. */
public class ProfileInterceptorContextTest {

    @Test public void testInstantiation() {
        final ProfileInterceptorContext context = new ProfileInterceptorContext();
        Assert.assertNull(context.getAttemptedFlow());
        Assert.assertTrue(context.getAvailableFlows().isEmpty());
    }

    /** Tests mutating attemptedFlow. */
    @Test public void testAttemptedFlow() {
        final ProfileInterceptorContext context = new ProfileInterceptorContext();
        final ProfileInterceptorFlowDescriptor descriptor = new ProfileInterceptorFlowDescriptor();
        context.setAttemptedFlow(descriptor);
        Assert.assertEquals(descriptor, context.getAttemptedFlow());
    }
    
}