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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.flow.impl.AbstractConsentAction;
import net.shibboleth.idp.consent.logic.impl.FlowIdLookupFunction;
import net.shibboleth.idp.consent.storage.impl.ConsentSerializer;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageSerializer;
import org.opensaml.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Base class for consent actions which interact with a {@link StorageService}.
 * 
 * This action ensures that the storage service, serializer, storageContext, and storageKey are available.
 * 
 * The storage service is provided by the profile interceptor flow descriptor.
 * 
 * The storage serializer defaults to a {@link ConsentSerializer}.
 * 
 * The storage context defaults to the flow ID provided by a {@link FlowIdLookupFunction}.
 * 
 * @pre <pre>InterceptorContext.getAttemptedFlow() != null</pre>
 * @pre <pre>FlowDescriptor.getStorageService() != null</pre>
 * @pre <pre>StorageSerializer != null</pre>
 * @pre <pre>StorageContextLookupStrategy != null</pre>
 * @pre <pre>StorageKeyLookupStrategy != null</pre>
 * @pre <pre>storageContextLookupStrategy.apply(profileRequestContext) != null</pre>
 * @pre <pre>storageKeyLookupStrategy.apply(profileRequestContext) != null</pre>
 */
public abstract class AbstractConsentStorageAction extends AbstractConsentAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractConsentStorageAction.class);

    /** Strategy used to determine the storage context. */
    @NonnullAfterInit private Function<ProfileRequestContext, String> storageContextLookupStrategy;

    /** Strategy used to determine the storage key. */
    @NonnullAfterInit private Function<ProfileRequestContext, String> storageKeyLookupStrategy;

    /** Storage serializer. */
    @NonnullAfterInit private StorageSerializer storageSerializer;

    /** Storage service from the {@link ProfileInterceptorFlowDescriptor}. */
    @Nullable private StorageService storageService;

    /** Storage context resulting from lookup strategy. */
    @Nullable private String storageContext;

    /** Storage key resulting from lookup strategy. */
    @Nullable private String storageKey;

    /** Constructor. */
    public AbstractConsentStorageAction() {
        setStorageContextLookupStrategy(new FlowIdLookupFunction());
        setStorageSerializer(new ConsentSerializer());
    }

    /**
     * Get the storage context lookup strategy.
     * 
     * @return the storage context lookup strategy
     */
    @NonnullAfterInit public Function<ProfileRequestContext, String> getStorageContextLookupStrategy() {
        return storageContextLookupStrategy;
    }

    /**
     * Get the storage key lookup strategy.
     * 
     * @return the storage key lookup strategy
     */
    @NonnullAfterInit public Function<ProfileRequestContext, String> getStorageKeyLookupStrategy() {
        return storageKeyLookupStrategy;
    }

    /**
     * Get the storage serializer.
     * 
     * @return the storage serializer
     */
    @NonnullAfterInit public StorageSerializer getStorageSerializer() {
        return storageSerializer;
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
     * Set the storage key lookup strategy.
     * 
     * @param strategy the storage key lookup strategy
     */
    public void setStorageKeyLookupStrategy(@Nonnull final Function<ProfileRequestContext, String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        storageKeyLookupStrategy = Constraint.isNotNull(strategy, "Storage key lookup strategy cannot be null");
    }

    /**
     * Set the storage serializer.
     * 
     * @param serializer storage serializer
     */
    public void setStorageSerializer(@Nonnull final StorageSerializer serializer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        storageSerializer = Constraint.isNotNull(serializer, "Storage serializer cannot be null");
    }

    /**
     * Get the storage service from the {@link ProfileInterceptorFlowDescriptor}.
     * 
     * @return the storage service
     */
    @Nullable public StorageService getStorageService() {
        return storageService;
    }

    /**
     * Get the storage context resulting from applying the storage context lookup strategy.
     * 
     * @return the storage context
     */
    @Nullable public String getStorageContext() {
        return storageContext;
    }

    /**
     * Get the storage key resulting from applying the storage key lookup strategy.
     * 
     * @return the storage key
     */
    @Nullable public String getStorageKey() {
        return storageKey;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (storageSerializer == null) {
            throw new ComponentInitializationException("Storage serializer cannot be null");
        }

        if (storageContextLookupStrategy == null) {
            throw new ComponentInitializationException("Storage context lookup strategy cannot be null");
        }

        if (storageKeyLookupStrategy == null) {
            throw new ComponentInitializationException("Storage key lookup strategy cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        if (!super.doPreExecute(profileRequestContext, interceptorContext)) {
            return false;
        }

        final ProfileInterceptorFlowDescriptor flowDescriptor = interceptorContext.getAttemptedFlow();
        log.trace("{} Flow descriptor '{}'", getLogPrefix(), flowDescriptor);
        if (flowDescriptor == null) {
            log.warn("{} No flow descriptor available from interceptor context", getLogPrefix());
            return false;
        }

        storageService = flowDescriptor.getStorageService();
        log.trace("{} Storage service '{}'", getLogPrefix(), storageService);
        if (storageService == null) {
            log.debug("{} No storage service available from interceptor context", getLogPrefix());
            return false;
        }

        storageContext = storageContextLookupStrategy.apply(profileRequestContext);
        log.trace("{} Storage context '{}'", getLogPrefix(), storageContext);
        if (storageContext == null) {
            log.debug("{} No storage context", getLogPrefix());
            return false;
        }

        storageKey = storageKeyLookupStrategy.apply(profileRequestContext);
        log.trace("{} Storage key '{}'", getLogPrefix(), storageKey);
        if (storageKey == null) {
            log.debug("{} No storage key", getLogPrefix());
            return false;
        }

        return true;
    }
}
