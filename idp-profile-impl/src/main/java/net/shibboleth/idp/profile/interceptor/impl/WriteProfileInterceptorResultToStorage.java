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

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorResult;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A profile interceptor action that writes a {@link ProfileInterceptorResult} to a {@link StorageService}.
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#IO_ERROR}
 */
public class WriteProfileInterceptorResultToStorage extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(WriteProfileInterceptorResultToStorage.class);

    /** Flow descriptor. */
    @Nullable private ProfileInterceptorFlowDescriptor flowDescriptor;

    /** Results to be stored. */
    @Nullable private List<ProfileInterceptorResult> results;

    /** Storage service. */
    @Nullable private StorageService storageService;

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        if (!super.doPreExecute(profileRequestContext, interceptorContext)) {
            return false;
        }

        results = interceptorContext.getResults();
        if (results.isEmpty()) {
            log.debug("{} No results available from interceptor context, nothing to store", getLogPrefix());
            return false;
        }

        flowDescriptor = interceptorContext.getAttemptedFlow();
        if (flowDescriptor == null) {
            log.warn("{} No flow descriptor within interceptor context", getLogPrefix());
            return false;
        }

        storageService = flowDescriptor.getStorageService();
        if (storageService == null) {
            log.warn("{} No storage service available from interceptor flow descriptor", getLogPrefix());
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        try {
            for (final ProfileInterceptorResult result : results) {
                store(result);
            }
        } catch (final IOException e) {
            log.error("{} Unable to write results '{}' to storage", getLogPrefix(), results, e);
            ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
        }
    }

    /**
     * Store a profile interceptor result.
     * 
     * @param result the profile interceptor result to be stored
     * @throws IOException if an error occurs
     */
    protected void store(@Nonnull final ProfileInterceptorResult result) throws IOException {
        final String context = result.getStorageContext();
        final String key = result.getStorageKey();
        final String value = result.getStorageValue();
        final Long expiration = result.getStorageExpiration();

        // Create / update loop until we succeed or exhaust attempts.
        int attempts = 10;
        boolean success = false;
        do {
            success = storageService.create(context, key, value, expiration);
            if (!success) {
                // The record already exists, so we need to overwrite via an update.
                success = storageService.update(context, key, value, expiration);
            }
        } while (!success && attempts-- > 0);

        if (!success) {
            log.error("{} Exhausted retry attempts storing result '{}'", getLogPrefix(), result);
        }

    }

}
