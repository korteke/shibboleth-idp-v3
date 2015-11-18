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

package net.shibboleth.idp.consent.logic.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.flow.storage.impl.UpdateCounter;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * Function to order storage keys by least-used and oldest first during pruning of storage records.
 * 
 * For every storage key supplied as input, this function attempts to lookup the number of times the flow corresponding
 * to the storage key has been executed. As such, this function depends on the {@link UpdateCounter} action being
 * executed prior to this function.
 */
public class CounterStorageKeyFunction extends AbstractInitializableComponent implements
        Function<Pair<ProfileRequestContext, List<String>>, List<String>> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CounterStorageKeyFunction.class);

    /** Strategy used to find the {@link ProfileInterceptorContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ProfileInterceptorContext> interceptorContextlookupStrategy;

    /** Strategy used to determine the storage storageContext. */
    @Nonnull private Function<ProfileRequestContext, String> storageContextLookupStrategy;

    /** Constructor. */
    public CounterStorageKeyFunction() {
        setInterceptorContextLookupStrategy(new ChildContextLookup(ProfileInterceptorContext.class));
        setStorageContextLookupStrategy(new FlowIdLookupFunction());
    }

    /**
     * Set the profile interceptor context lookup strategy.
     * 
     * @param strategy the profile interceptor context lookup strategy
     */
    public void setInterceptorContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, ProfileInterceptorContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        interceptorContextlookupStrategy =
                Constraint.isNotNull(strategy, "Profile interceptor context lookup strategy cannot be null");
    }

    /**
     * Set the storage context lookup strategy.
     * 
     * @param strategy the storage context lookup strategy
     */
    public void setStorageContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        storageContextLookupStrategy = Constraint.isNotNull(strategy, "Storage context lookup strategy cannot be null");
    }

    /**
     * Get the storage service from the flow descriptor of the profile interceptor context. The profile interceptor
     * context is retrieved by applying the profile interceptor context lookup strategy to the profile request context.
     * The storage service is retrieved from the flow descriptor of the last interceptor flow which was attempted.
     * 
     * @param profileRequestContext the profile request context
     * @return the storage service
     * @throws net.shibboleth.utilities.java.support.logic.ConstraintViolationException if the lookup strategy returns
     *             <code>null</code>, the interceptor flow descriptor is <code>null</code>, or the storage service is
     *             <code>null</code>
     */
    @Nonnull protected StorageService getStorageService(@Nonnull final ProfileRequestContext profileRequestContext) {
        Constraint.isNotNull(profileRequestContext, "Profile request context cannot be null");

        final ProfileInterceptorContext interceptorContext =
                interceptorContextlookupStrategy.apply(profileRequestContext);
        Constraint.isNotNull(interceptorContext,
                "Profile interceptor context not available from profile request context");

        final ProfileInterceptorFlowDescriptor flowDescriptor = interceptorContext.getAttemptedFlow();
        Constraint.isNotNull(flowDescriptor,
                "Profile interceptor flow descriptor not available from profile interceptor context");

        return Constraint.isNotNull(flowDescriptor.getStorageService(),
                "Storage service not available from interceptor flow descriptor");
    }

    /**
     * Get the storage context by applying the storage context lookup strategy to the profile request context.
     * 
     * @param profileRequestContext the profile request context
     * @return the storage context
     * @throws net.shibboleth.utilities.java.support.logic.ConstraintViolationException if the lookup strategy returns
     *             <code>null</code>
     */
    @Nonnull protected String getStorageContext(@Nonnull final ProfileRequestContext profileRequestContext) {
        Constraint.isNotNull(profileRequestContext, "Profile request context cannot be null");

        return Constraint.isNotNull(storageContextLookupStrategy.apply(profileRequestContext),
                "Storage context not available from profile request context");
    }

    /**
     * Get the storage key for the storage record whose value is a counter.
     * 
     * @param storageKey the storage key
     * @return the storage key for the storage record whose value is a counter
     */
    @Nonnull protected String getCounterStorageKey(@Nonnull final String storageKey) {
        Constraint.isNotNull(storageKey, "Storage key cannot be null");

        return Joiner.on(JoinFunction.SEPARATOR).join(storageKey, UpdateCounter.COUNTER_KEY);
    }

    /**
     * Get the counter for the given storage key.
     * 
     * @param storageService the storage service
     * @param storageContext the storage context
     * @param storageKey the storage key
     * @return the counter for the given storage key
     * @throws IOException if a storage service error occurs
     * @throws NumberFormatException if the storage record value cannot be parsed as an integer
     */
    @Nullable protected Long getStorageKeyCounter(@Nonnull final StorageService storageService,
            @Nonnull final String storageContext, @Nonnull final String storageKey) throws IOException {
        Constraint.isNotNull(storageService, "Storage service cannot be null");
        Constraint.isNotNull(storageContext, "Storage context cannot be null");
        Constraint.isNotNull(storageKey, "Storage key cannot be null");

        final String counterStorageKey = getCounterStorageKey(storageKey);

        final StorageRecord storageRecord = storageService.read(storageContext, counterStorageKey);
        log.debug("Read storage record '{}' with context '{}' and key '{}'", storageRecord, storageContext,
                counterStorageKey);

        return (storageRecord == null) ? null : storageRecord.getVersion();
    }

    /**
     * Get the map of storage keys to counters.
     * 
     * @param profileRequestContext the profile request context
     * @param storageKeys the storage keys
     * @return map of storage keys to counters
     */
    @Nonnull protected Map<String, Long> getStorageKeyCounters(
            @Nonnull final ProfileRequestContext profileRequestContext, @Nonnull final List<String> storageKeys) {
        Constraint.isNotNull(profileRequestContext, "Profile request context cannot be null");
        Constraint.isNotNull(storageKeys, "Storage keys cannot be null");

        final StorageService storageService = getStorageService(profileRequestContext);
        final String storageContext = getStorageContext(profileRequestContext);

        final Map<String, Long> map = new LinkedHashMap<>();
        for (final String storageKey : storageKeys) {
            try {
                map.put(storageKey, getStorageKeyCounter(storageService, storageContext, storageKey));
            } catch (NumberFormatException | IOException e) {
                log.error("Unable to retrieve counter for storage key '{}'", storageKey, e);
            }
        }
        return map;
    }

    /** {@inheritDoc} */
    @Override @Nullable public List<String> apply(@Nullable final Pair<ProfileRequestContext, List<String>> input) {
        if (input == null || input.getFirst() == null || input.getSecond() == null) {
            return null;
        }

        try {
            final ProfileRequestContext profileRequestContext = input.getFirst();
            final List<String> storageKeys = input.getSecond();

            final Map<String, Long> keyToCounterMap = getStorageKeyCounters(profileRequestContext, storageKeys);
            final Comparator<String> comparator = new CounterStorageKeyComparator(storageKeys, keyToCounterMap);

            Collections.sort(storageKeys, comparator);

            return storageKeys;
        } catch (final ConstraintViolationException e) {
            log.warn("Unable to apply counter storage key function", e);
            return input.getSecond();
        }
    }

}
