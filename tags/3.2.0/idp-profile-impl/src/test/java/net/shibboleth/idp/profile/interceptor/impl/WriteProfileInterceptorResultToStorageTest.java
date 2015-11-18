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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorResult;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;

import org.joda.time.DateTime;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.impl.MemoryStorageService;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link WriteProfileInterceptorResultToStorage} unit test. */
public class WriteProfileInterceptorResultToStorageTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private MemoryStorageService ss;

    private WriteProfileInterceptorResultToStorage action;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        ss = new MemoryStorageService();
        ss.setId("test");
        ss.initialize();

        final ProfileInterceptorFlowDescriptor descriptor = new ProfileInterceptorFlowDescriptor();
        descriptor.setStorageService(ss);

        final ProfileInterceptorContext pic = new ProfileInterceptorContext();
        pic.setAttemptedFlow(descriptor);
        prc.addSubcontext(pic);

        action = new WriteProfileInterceptorResultToStorage();
        action.initialize();
    }

    @Test public void testNoResults() {
        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);
    }

    @Test public void testNoAttemptedFlow() {
        prc.addSubcontext(new ProfileInterceptorContext(), true);
        Assert.assertNull(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow());

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);
    }

    @Test public void testNoStorageService() {
        prc.getSubcontext(ProfileInterceptorContext.class).setAttemptedFlow(new ProfileInterceptorFlowDescriptor());
        Assert.assertNull(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow().getStorageService());

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);
    }

    @Test public void testCreateStorageRecord() throws Exception {
        final MockProfileInterceptorResult result = new MockProfileInterceptorResult("context", "key", "value", null);
        prc.getSubcontext(ProfileInterceptorContext.class).getResults().add(result);

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final StorageRecord storageRecord = ss.read("context", "key");
        Assert.assertNotNull(storageRecord);
        Assert.assertEquals(storageRecord.getValue(), "value");
        Assert.assertEquals(storageRecord.getExpiration(), null);
    }

    @Test public void testCreateStorageRecordWithExpiration() throws Exception {
        final Long expiration = DateTime.now().getMillis() + 1000 * 60;
        final MockProfileInterceptorResult result =
                new MockProfileInterceptorResult("context", "key", "value", expiration);
        prc.getSubcontext(ProfileInterceptorContext.class).getResults().add(result);

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final StorageRecord storageRecord = ss.read("context", "key");
        Assert.assertNotNull(storageRecord);
        Assert.assertEquals(storageRecord.getValue(), "value");
        Assert.assertEquals(storageRecord.getExpiration(), expiration);
    }

    @Test public void testUpdateStorageRecord() throws Exception {
        final Long expiration = DateTime.now().getMillis() + 1000 * 60;
        MockProfileInterceptorResult result = new MockProfileInterceptorResult("context", "key", "value", null);
        prc.getSubcontext(ProfileInterceptorContext.class).getResults().add(result);

        Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        StorageRecord storageRecord = ss.read("context", "key");
        Assert.assertNotNull(storageRecord);
        Assert.assertEquals(storageRecord.getValue(), "value");
        Assert.assertEquals(storageRecord.getExpiration(), null);

        result = new MockProfileInterceptorResult("context", "key", "value2", expiration);
        prc.getSubcontext(ProfileInterceptorContext.class).getResults().clear();
        prc.getSubcontext(ProfileInterceptorContext.class).getResults().add(result);

        event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        storageRecord = ss.read("context", "key");
        Assert.assertNotNull(storageRecord);
        Assert.assertEquals(storageRecord.getValue(), "value2");
        Assert.assertEquals(storageRecord.getExpiration(), expiration);
    }

    private class MockProfileInterceptorResult extends AbstractProfileInterceptorResult {

        public MockProfileInterceptorResult(@Nonnull @NotEmpty final String context,
                @Nonnull @NotEmpty final String key, @Nonnull @NotEmpty final String value,
                @Nullable @Positive @Duration final Long expiration) {
            super(context, key, value, expiration);
        }
    }

}
