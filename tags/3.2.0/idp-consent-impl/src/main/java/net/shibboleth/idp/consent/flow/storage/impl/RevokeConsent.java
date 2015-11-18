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

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consent action which deletes a consent record from storage.
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#IO_ERROR}
 */
public class RevokeConsent extends AbstractConsentIndexedStorageAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RevokeConsent.class);

    /** Trap errors in the storage layer. */
    private boolean maskStorageErrors;

    /**
     * Set whether to trap and hide storage-related errors.
     * 
     * @param flag flag to set
     */
    public void setMaskStorageErrors(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        maskStorageErrors = flag;
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final String context = getStorageContext();
        final String key = getStorageKey();

        log.debug("{} Attempting to delete consent storage record with context '{}' and key '{}'", getLogPrefix(),
                context, key);
        try {
            final boolean success = getStorageService().delete(context, key);
            if (success) {
                log.debug("{} Deleted consent storage record with context '{}' and key '{}'", getLogPrefix(), context,
                        key);
            } else {
                log.debug("{} No consent storage record found with context '{}' and key '{}'", getLogPrefix(),
                        context, key);
            }
            
            removeKeyFromStorageIndex(key);
            
        } catch (final IOException e) {
            log.error("{} Unable to delete consent storage record with context '{}' and key '{}'", getLogPrefix(),
                    context, key, e);
            if (!maskStorageErrors) {
                ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
            }
        }
    }

}