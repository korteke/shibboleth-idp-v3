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

package net.shibboleth.idp.profile.interceptor;

import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;

/** {@link ProfileInterceptorFlowDescriptor} unit test. */
public class ProfileInterceptorFlowDescriptorTest {

    private ProfileInterceptorFlowDescriptor descriptor;

    private RequestContext src;

    private ProfileRequestContext prc;

    @BeforeMethod public void setUp() throws Exception {
        descriptor = new ProfileInterceptorFlowDescriptor();
        descriptor.setId("test");

        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
    }

    @Test public void testInstantation() throws Exception {
        Assert.assertEquals(descriptor.getId(), "test");
        Assert.assertTrue(descriptor.isNonBrowserSupported());
        Assert.assertNull(descriptor.getStorageService());
        Assert.assertTrue(descriptor.apply(prc));
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class)
    public void testUnmodifiableActivationCondition()
            throws Exception {
        descriptor.initialize();
        descriptor.setActivationCondition(Predicates.<ProfileRequestContext> alwaysFalse());
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class)
    public void testUnmodifiableStorageService()
            throws Exception {
        descriptor.initialize();
        descriptor.setStorageService(null);
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class)
    public void testUnmodifiableNonBrowserSupport()
            throws Exception {
        descriptor.initialize();
        descriptor.setNonBrowserSupported(true);
    }

    @Test public void testEquality() {
        final ProfileInterceptorFlowDescriptor descriptorWithSameId = new ProfileInterceptorFlowDescriptor();
        descriptorWithSameId.setId("test");
        Assert.assertTrue(descriptor.equals(descriptorWithSameId));

        final ProfileInterceptorFlowDescriptor descriptorWithDifferentId = new ProfileInterceptorFlowDescriptor();
        descriptorWithDifferentId.setId("differentId");
        Assert.assertFalse(descriptor.equals(descriptorWithDifferentId));
    }

    @Test public void testMutatingPredicate() throws Exception {
        descriptor.setActivationCondition(Predicates.<ProfileRequestContext> alwaysFalse());
        descriptor.initialize();

        Assert.assertFalse(descriptor.apply(prc));
    }

    @Test public void testMutatingNonBrowserSupport() {
        descriptor.setNonBrowserSupported(true);
        Assert.assertTrue(descriptor.isNonBrowserSupported());

        descriptor.setNonBrowserSupported(false);
        Assert.assertFalse(descriptor.isNonBrowserSupported());
    }

}
