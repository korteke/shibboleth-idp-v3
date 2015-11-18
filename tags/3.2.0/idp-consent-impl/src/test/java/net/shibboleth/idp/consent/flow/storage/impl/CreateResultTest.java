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

package net.shibboleth.idp.consent.flow.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.consent.storage.impl.ConsentSerializer;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageRecord;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link CreateResult} unit test. */
public class CreateResultTest extends AbstractConsentIndexedStorageActionTest {

    protected CreateResult buildAction(@Nonnull final String key) throws Exception {
        final CreateResult action = new CreateResult();
        action.setStorageContextLookupStrategy(FunctionSupport.<ProfileRequestContext, String> constant("context"));
        action.setStorageKeyLookupStrategy(FunctionSupport.<ProfileRequestContext, String> constant(key));
        action.setStorageIndexKeyLookupStrategy(FunctionSupport.<ProfileRequestContext, String> constant("_index"));
        action.initialize();
        return action;
    }

    protected Map<String, Consent> readConsentFromStorage(@Nonnull final String key) throws Exception {
        final StorageRecord record = getMemoryStorageService().read("context", key);
        Assert.assertNotNull(record);
        final ConsentSerializer serializer = new ConsentSerializer();

        return serializer.deserialize(0, "context", key, record.getValue(), record.getExpiration());
    }

    @BeforeMethod public void setUpAction() throws Exception {
        action = new CreateResult();
        populateAction();
    }

    @Test public void testNoCurrentConsents() throws Exception {
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final ProfileInterceptorContext pic = prc.getSubcontext(ProfileInterceptorContext.class, false);
        Assert.assertNotNull(pic);
        Assert.assertEquals(pic.getResults().size(), 0);

        final StorageRecord record = getMemoryStorageService().read("context", "key");
        Assert.assertNull(record);
    }

    @Test public void testCreateResult() throws Exception {
        action.initialize();

        final ConsentContext consentCtx = prc.getSubcontext(ConsentContext.class);
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final ProfileInterceptorContext pic = prc.getSubcontext(ProfileInterceptorContext.class, false);
        Assert.assertNotNull(pic);
        Assert.assertEquals(pic.getResults().size(), 0);

        final Map<String, Consent> consents = readConsentsFromStorage();
        Assert.assertEquals(consents.size(), 2);
        Assert.assertEquals(consents, ConsentTestingSupport.newConsentMap());

        final Collection<String> keys = readStorageKeysFromIndex();
        Assert.assertEquals(keys, Arrays.asList("key"));
    }

    @Test public void testCreateResultWithSymbolics() throws Exception {
        ConsentSerializer serializer = new ConsentSerializer();
        serializer.setSymbolics(ConsentTestingSupport.newSymbolicsMap());
        serializer.initialize();
        ((AbstractConsentStorageAction) action).setStorageSerializer(serializer);

        testCreateResult();

        final StorageRecord record = getMemoryStorageService().read("context", "key");
        Assert.assertEquals(record.getValue(),
                "[{\"id\":101,\"v\":\"value1\",\"appr\":false},{\"id\":102,\"v\":\"value2\",\"appr\":false}]");
    }

    @Test public void testUpdateResult() throws Exception {
        action.initialize();

        final ConsentContext consentCtx = prc.getSubcontext(ConsentContext.class);
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());

        ActionTestingSupport.assertProceedEvent(action.execute(src));
        ActionTestingSupport.assertProceedEvent(action.execute(src));

        final ProfileInterceptorContext pic = prc.getSubcontext(ProfileInterceptorContext.class, false);
        Assert.assertNotNull(pic);
        Assert.assertEquals(pic.getResults().size(), 0);

        final Map<String, Consent> consents = readConsentsFromStorage();
        Assert.assertEquals(consents.size(), 2);
        Assert.assertEquals(consents, ConsentTestingSupport.newConsentMap());

        final Collection<String> keys = readStorageKeysFromIndex();
        Assert.assertEquals(keys, Arrays.asList("key"));
    }

    @Test public void testUpdateResultWithSymbolics() throws Exception {
        ConsentSerializer serializer = new ConsentSerializer();
        serializer.setSymbolics(ConsentTestingSupport.newSymbolicsMap());
        serializer.initialize();
        ((AbstractConsentStorageAction) action).setStorageSerializer(serializer);

        testUpdateResult();

        final StorageRecord record = getMemoryStorageService().read("context", "key");
        Assert.assertEquals(record.getValue(),
                "[{\"id\":101,\"v\":\"value1\",\"appr\":false},{\"id\":102,\"v\":\"value2\",\"appr\":false}]");
    }

    @Test public void testMaxStoredRecords() throws Exception {
        descriptor.setExpandedNumberOfStoredRecords(2);
        descriptor.setMaximumNumberOfStoredRecords(2);

        final ConsentContext consentCtx = prc.getSubcontext(ConsentContext.class);
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());

        // key1

        final CreateResult action1 = buildAction("key1");

        ActionTestingSupport.assertProceedEvent(action1.execute(src));

        final Map<String, Consent> consents1 = readConsentFromStorage("key1");
        Assert.assertEquals(consents1.size(), 2);
        Assert.assertEquals(consents1, ConsentTestingSupport.newConsentMap());

        Assert.assertEquals(readStorageKeysFromIndex(), Arrays.asList("key1"));

        // key2

        final CreateResult action2 = buildAction("key2");

        ActionTestingSupport.assertProceedEvent(action2.execute(src));

        final Map<String, Consent> consents2 = readConsentFromStorage("key2");
        Assert.assertEquals(consents2.size(), 2);
        Assert.assertEquals(consents2, ConsentTestingSupport.newConsentMap());

        Assert.assertEquals(readStorageKeysFromIndex(), Arrays.asList("key1", "key2"));

        // key3

        final CreateResult action3 = buildAction("key3");

        ActionTestingSupport.assertProceedEvent(action3.execute(src));

        final Map<String, Consent> consents3 = readConsentFromStorage("key3");
        Assert.assertEquals(consents3.size(), 2);
        Assert.assertEquals(consents3, ConsentTestingSupport.newConsentMap());

        Assert.assertEquals(readStorageKeysFromIndex(), Arrays.asList("key2", "key3"));
    }

    @Test public void testNoMaxStoredRecords() throws Exception {
        descriptor.setMaximumNumberOfStoredRecords(0);
        descriptor.setExpandedNumberOfStoredRecords(0);

        final ConsentContext consentCtx = prc.getSubcontext(ConsentContext.class);
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());

        // can't test unlimited, so test 10
        final List<String> keys = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            ActionTestingSupport.assertProceedEvent(buildAction("key" + Integer.toString(i)).execute(src));
            keys.add("key" + Integer.toString(i));
        }
        Assert.assertEquals(readStorageKeysFromIndex(), keys);
    }
}
