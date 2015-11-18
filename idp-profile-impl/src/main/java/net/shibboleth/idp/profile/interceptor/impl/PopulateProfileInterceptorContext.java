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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.idp.profile.interceptor.ProfileInterceptorFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * An profile interceptor action that populates a {@link ProfileInterceptorContext} with
 * {@link ProfileInterceptorFlowDescriptor} objects based on flow IDs from a lookup function.
 * 
 * <p>The flow IDs used for filtering must omit the {@link ProfileInterceptorFlowDescriptor#FLOW_ID_PREFIX} prefix.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#INVALID_PROFILE_CONFIG}
 * @post The ProfileInterceptorContext is modified as above.
 */
public class PopulateProfileInterceptorContext extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateProfileInterceptorContext.class);

    /** The flows to make available for possible use. */
    @Nonnull @NonnullElements private Collection<ProfileInterceptorFlowDescriptor> availableFlows;

    /** Lookup function for the flow IDs to activate from within the available set. */
    @NonnullAfterInit private Function<ProfileRequestContext,Collection<String>> activeFlowsLookupStrategy;
    
    /** Constructor. */
    public PopulateProfileInterceptorContext() {
        availableFlows = Collections.emptyList();
    }

    /**
     * Set the flows available for possible use.
     * 
     * @param flows the flows available for possible use
     */
    public void setAvailableFlows(@Nonnull @NonnullElements final Collection<ProfileInterceptorFlowDescriptor> flows) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(flows, "Flow collection cannot be null");

        availableFlows = new ArrayList<>(Collections2.filter(flows, Predicates.notNull()));
    }
    
    /**
     * Set the lookup strategy to use for the interceptor flows to activate.
     * 
     * @param strategy lookup strategy
     */
    public void setActiveFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        activeFlowsLookupStrategy = Constraint.isNotNull(strategy, "Flow lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (activeFlowsLookupStrategy == null) {
            throw new ComponentInitializationException("Flow lookup strategy cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {
        
        interceptorContext.getAvailableFlows().clear();
        interceptorContext.setAttemptedFlow(null);
        
        final Collection<String> activeFlows = activeFlowsLookupStrategy.apply(profileRequestContext);
        if (activeFlows != null) {
            for (final String id : activeFlows) {
                final String flowId = ProfileInterceptorFlowDescriptor.FLOW_ID_PREFIX + id;
                final Optional<ProfileInterceptorFlowDescriptor> flow = Iterables.tryFind(availableFlows,
                        new Predicate<ProfileInterceptorFlowDescriptor>() {
                            public boolean apply(ProfileInterceptorFlowDescriptor input) {
                                return input.getId().equals(flowId);
                            }
                });
                
                if (flow.isPresent()) {
                    log.debug("{} Installing flow {} into interceptor context", getLogPrefix(), flowId);
                    interceptorContext.getAvailableFlows().put(flow.get().getId(), flow.get());
                } else {
                    log.error("{} Configured interceptor flow {} not available for use", getLogPrefix(), flowId);
                    ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_PROFILE_CONFIG);
                    return;
                }
            }
        }
    }
    
}