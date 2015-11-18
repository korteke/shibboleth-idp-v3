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

package net.shibboleth.idp.consent.flow.impl;

import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/** {@link PopulateConsentContext} unit test. */
public class PopulateConsentContextTest extends AbstractConsentActionTest {

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullCurrentConsentsFunction()
            throws Exception {
        action = new PopulateConsentContext(null);
        action.initialize();
    }

    @Test public void testCurrentConsentsFunction() throws Exception {
        action = new PopulateConsentContext(new MockCurrentConsentsFunction());
        action.initialize();

        final Event event = action.execute(src);

        ActionTestingSupport.assertProceedEvent(event);

        final ConsentContext consentContext = prc.getSubcontext(ConsentContext.class, false);
        Assert.assertNotNull(consentContext);
        Assert.assertEquals(consentContext.getCurrentConsents(), ConsentTestingSupport.newConsentMap());
    }

    /** Mock function which returns current consents. */
    private class MockCurrentConsentsFunction implements Function<ProfileRequestContext, Map<String, Consent>> {

        /** {@inheritDoc} */
        public Map<String, Consent> apply(@Nonnull final ProfileRequestContext input) {
            return ConsentTestingSupport.newConsentMap();
        }
    }

}
