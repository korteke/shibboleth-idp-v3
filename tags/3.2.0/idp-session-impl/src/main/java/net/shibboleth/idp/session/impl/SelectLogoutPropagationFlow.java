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

package net.shibboleth.idp.session.impl;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.session.LogoutPropagationFlowDescriptor;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.context.LogoutPropagationContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A profile action that selects a logout propagation flow to invoke.
 * 
 * <p>This is the heart of the logout propagation processing sequence, and runs after the
 * {@link net.shibboleth.idp.session.context.LogoutContext} has been populated. It uses the potential flows,
 * and their associated activation conditions to decide how to proceed.</p>
 * 
 * <p>This is a rare case in that the standard default event,
 * {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}, cannot be returned,
 * because the action must either dispatch to a flow by name, or signal an error.</p>
 * 
 * @event {@link AuthnEventIds#NO_POTENTIAL_FLOW}
 * @event Selected flow ID to execute
 */
public class SelectLogoutPropagationFlow extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SelectLogoutPropagationFlow.class);

    /** Selection function to determine suitable LogoutPropagationFlowDescriptor for given SPSession. */
    @Nonnull private final Function<SPSession, LogoutPropagationFlowDescriptor> flowSelectorFunction;

    /** Function to retrieve LogoutPropagationContext from context tree. */
    @Nonnull private Function<ProfileRequestContext, LogoutPropagationContext> logoutPropagationContextFunction;

    /**
     * Constructor.
     * 
     * @param selector mapping function from session to flow descriptor
     */
    public SelectLogoutPropagationFlow(@Nonnull final Function<SPSession, LogoutPropagationFlowDescriptor> selector) {
        flowSelectorFunction = Constraint.isNotNull(selector, "Selector cannot be null");
        logoutPropagationContextFunction = new ChildContextLookup<>(LogoutPropagationContext.class);
    }

    /**
     * Sets the function used to retrieve the {@link LogoutPropagationContext} from the context tree.
     *
     * @param function Lookup function.
     */
    public void setLogoutPropagationContextFunction(
            @Nonnull Function<ProfileRequestContext, LogoutPropagationContext> function) {
        logoutPropagationContextFunction = Constraint.isNotNull(function, "Function cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        final LogoutPropagationContext logoutPropCtx = logoutPropagationContextFunction.apply(profileRequestContext);
        if (logoutPropCtx == null || logoutPropCtx.getSession() == null) {
            log.error("{} LogoutPropagationContext not found or found with null SPSession", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }

        final LogoutPropagationFlowDescriptor flow = flowSelectorFunction.apply(logoutPropCtx.getSession());
        if (flow == null) {
            log.error("{} No potential flows to choose from, logout propagation will fail", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_POTENTIAL_FLOW);
            return;
        }

        log.debug("{} Selecting logout propagation flow {}", getLogPrefix(), flow.getId());
        ActionSupport.buildEvent(profileRequestContext, flow.getId());
    }
    
}