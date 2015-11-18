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

import java.util.Collections;

import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.profile.ActionTestingSupport;

import org.opensaml.storage.impl.MemoryStorageService;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link RevokeConsent} unit test. */
public class RevokeConsentTest extends AbstractConsentIndexedStorageActionTest {

    @BeforeMethod public void setUpAction() throws Exception {
        action = new RevokeConsent();
        populateAction();
    }

    @Test public void testRevokeConsent() throws Exception {
        final MemoryStorageService ss = getMemoryStorageService();
        ss.create("context", "key", ConsentTestingSupport.newConsentMap(),
                ((AbstractConsentStorageAction) action).getStorageSerializer(), null);
        Assert.assertNotNull(ss.read("context", "key"));
        ss.create("context", "_index", Collections.singletonList("key"),
                ((AbstractConsentIndexedStorageAction) action).getStorageKeysSerializer(), null);

        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertNull(ss.read("context", "key"));
        Assert.assertTrue(readStorageKeysFromIndex().isEmpty());
    }

    @Test public void testRevokeEmptyStorage() throws Exception {
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);
    }
}
