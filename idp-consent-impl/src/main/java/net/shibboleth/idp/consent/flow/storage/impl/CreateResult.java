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

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.storage.impl.ConsentResult;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorResult;

import org.joda.time.DateTime;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consent action to create a consent result representing the result of a consent flow. The result is added to the
 * profile interceptor context for eventual storage by a storage service. The result of the consent flow is created from
 * the current consents of the consent context.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @pre The current consents in the consent context must not be empty.
 * @post A {@link ConsentResult} will be created representing the current consents and will be added to the
 *       {@link ProfileInterceptorContext}.
 */
public class CreateResult extends AbstractConsentIndexedStorageAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CreateResult.class);

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        if (!super.doPreExecute(profileRequestContext, interceptorContext)) {
            return false;
        }

        if (getConsentContext().getCurrentConsents().isEmpty()) {
            log.debug("{} No result will be created because there are no current consents", getLogPrefix());
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        try {
            final Map<String, Consent> currentConsents = getConsentContext().getCurrentConsents();
            final String value = getStorageSerializer().serialize(currentConsents);

            final long expiration = DateTime.now().plus(getConsentFlowDescriptor().getLifetime()).getMillis();

            final ProfileInterceptorResult result =
                    new ConsentResult(getStorageContext(), getStorageKey(), value, expiration);

            log.debug("{} Created consent result '{}'", getLogPrefix(), result);

            storeResultWithIndex(profileRequestContext, result);

        } catch (final IOException e) {
            log.debug("{} Unable to serialize consent", getLogPrefix(), e);
        }
    }

}