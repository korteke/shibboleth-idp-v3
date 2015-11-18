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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;

/**
 * Function that returns a profile interceptor flow descriptor from a profile request context using a lookup strategy.
 *
 * @param <T> the profile interceptor flow descriptor type to locate
 */
public class FlowDescriptorLookupFunction<T extends ProfileInterceptorFlowDescriptor> implements
        Function<ProfileRequestContext, T> {

    /** Profile interceptor flow descriptor type to look up. */
    @Nonnull private final Class<T> interceptorFlowDescriptorType;

    /** Profile interceptor context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, ProfileInterceptorContext> interceptorContextlookupStrategy;

    /**
     * Constructor.
     *
     * @param type profile interceptor flow descriptor type to look up
     */
    public FlowDescriptorLookupFunction(@Nonnull final Class<T> type) {
        interceptorFlowDescriptorType = Constraint.isNotNull(type, "Interceptor flow descriptor type cannot be null");
        
        interceptorContextlookupStrategy = new ChildContextLookup<>(ProfileInterceptorContext.class);
    }

    /**
     * Set the interceptor context lookup strategy.
     * 
     * @param strategy interceptor context lookup strategy
     */
    public void setInterceptorContextlookupStrategy(
            @Nonnull final Function<ProfileRequestContext, ProfileInterceptorContext> strategy) {
        interceptorContextlookupStrategy =
                Constraint.isNotNull(strategy, "Profile interceptor context lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public T apply(ProfileRequestContext input) {
        if (input == null) {
            return null;
        }

        final ProfileInterceptorContext interceptorContext = interceptorContextlookupStrategy.apply(input);
        if (interceptorContext == null) {
            return null;
        }

        final ProfileInterceptorFlowDescriptor interceptorFlowDescriptor = interceptorContext.getAttemptedFlow();
        if (interceptorFlowDescriptor == null) {
            return null;
        }

        if (!(interceptorFlowDescriptorType.isInstance(interceptorFlowDescriptor))) {
            return null;
        }

        return (T) interceptorFlowDescriptor;
    }

}
