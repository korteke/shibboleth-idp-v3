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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link CreateGlobalConsentResult} unit test. */
public class CreateGlobalConsentResultTest extends AbstractConsentIndexedStorageActionTest {

    @BeforeMethod public void setUpAction() throws Exception {
        action = new CreateGlobalConsentResult();
        populateAction();
    }

    @Test public void testCreateGlobalConsentResult() throws Exception {
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final ProfileInterceptorContext pic = prc.getSubcontext(ProfileInterceptorContext.class, false);
        Assert.assertNotNull(pic);
        Assert.assertEquals(pic.getResults().size(), 0);

        final Collection<String> keys = readStorageKeysFromIndex();
        Assert.assertEquals(keys, Arrays.asList("key"));

        final Map<String, Consent> consents = readConsentsFromStorage();
        Assert.assertEquals(consents.size(), 1);

        final Consent globalConsent = consents.values().iterator().next();
        Assert.assertNotNull(globalConsent);
        Assert.assertEquals(globalConsent.getId(), Consent.WILDCARD);
        Assert.assertNull(globalConsent.getValue());
        Assert.assertTrue(globalConsent.isApproved());
    }
}
