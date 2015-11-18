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

package net.shibboleth.idp.authn.context;

import java.util.Arrays;

import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.AuthenticationResult;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link AuthenticationContext} unit test. */
public class AuthenticationContextTest {

    /** Tests initiation instant instantiation. */
    @Test public void testInitiationInstant() throws Exception {
        long start = System.currentTimeMillis();
        // this is here to allow the event's creation time to deviate from the 'start' time
        Thread.sleep(50);

        AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertTrue(ctx.getInitiationInstant() > start);
    }

    /** Tests mutating forcing authentication. */
    @Test public void testForcingAuthentication() throws Exception {
        AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertFalse(ctx.isForceAuthn());

        ctx.setForceAuthn(true);
        Assert.assertTrue(ctx.isForceAuthn());
    }

    /** Tests active results. */
    @Test public void testActiveResults() throws Exception {
        final AuthenticationResult result = new AuthenticationResult("test", new Subject());

        final AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertTrue(ctx.getActiveResults().isEmpty());
        
        ctx.setActiveResults(Arrays.asList(result));

        Assert.assertEquals(ctx.getActiveResults().size(), 1);
        Assert.assertEquals(ctx.getActiveResults().get("test"), result);
    }
    
    /** Tests potential flow instantiation. */
    @Test public void testPotentialFlows() throws Exception {
        AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertTrue(ctx.getPotentialFlows().isEmpty());

        final AuthenticationFlowDescriptor descriptor = new AuthenticationFlowDescriptor();
        descriptor.setId("test");
        ctx = new AuthenticationContext();
        ctx.getPotentialFlows().put(descriptor.getId(), descriptor);
        Assert.assertEquals(ctx.getPotentialFlows().size(), 1);
        Assert.assertEquals(ctx.getPotentialFlows().get("test"), descriptor);
    }

    /** Tests mutating attempted flow. */
    @Test public void testAttemptedFlow() throws Exception {
        final AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertNull(ctx.getAttemptedFlow());

        final AuthenticationFlowDescriptor descriptor = new AuthenticationFlowDescriptor();
        descriptor.setId("test");
        ctx.setAttemptedFlow(descriptor);
        Assert.assertEquals(ctx.getAttemptedFlow(), descriptor);
    }

    /** Tests setting completion instant. */
    @Test public void testCompletionInstant() throws Exception {
        final AuthenticationContext ctx = new AuthenticationContext();
        Assert.assertEquals(ctx.getCompletionInstant(), 0);

        long now = System.currentTimeMillis();
        // this is here to allow the event's creation time to deviate from the 'start' time
        Thread.sleep(50);

        ctx.setCompletionInstant();
        Assert.assertTrue(ctx.getCompletionInstant() > now);
    }
    
}