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

package net.shibboleth.idp.authn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.config.navigate.AuthenticationFlowsLookupFunction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactoryRegistry;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * An action that populates an {@link AuthenticationContext} with the {@link AuthenticationFlowDescriptor}
 * objects configured into the IdP, filtered by flow IDs from a lookup function, and optionally a customized
 * {@link PrincipalEvalPredicateFactoryRegistry}.
 * 
 * <p>The flow IDs used for filtering must omit the {@link AuthenticationFlowDescriptor#FLOW_ID_PREFIX} prefix.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class) != null</pre>
 * @post The AuthenticationContext is modified as above.
 */
public class PopulateAuthenticationContext extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateAuthenticationContext.class);
    
    /** The flows to make available for possible use. */
    @Nonnull @NonnullElements private Collection<AuthenticationFlowDescriptor> availableFlows;
    
    /** Lookup function for the flow IDs to activate from within the available set. */
    @Nonnull private Function<ProfileRequestContext,Collection<String>> activeFlowsLookupStrategy;

    /** The registry of predicate factories for custom principal evaluation. */
    @Nullable private PrincipalEvalPredicateFactoryRegistry evalRegistry;
    
    /** Constructor. */
    PopulateAuthenticationContext() {
        availableFlows = Collections.emptyList();
        activeFlowsLookupStrategy = new AuthenticationFlowsLookupFunction();
    }
    
    /**
     * Set the flows available for possible use.
     * 
     * @param flows the flows available for possible use
     */
    public void setAvailableFlows(@Nonnull @NonnullElements final Collection<AuthenticationFlowDescriptor> flows) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(flows, "Flow collection cannot be null");
        
        availableFlows = new ArrayList<>(Collections2.filter(flows, Predicates.notNull()));
    }
    
    /**
     * Set the lookup strategy to use for the authentication flows to activate.
     * 
     * @param strategy lookup strategy
     */
    public void setActiveFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        activeFlowsLookupStrategy = Constraint.isNotNull(strategy, "Flow lookup strategy cannot be null");
    }
    
    /**
     * Get the registry of predicate factories for custom principal evaluation.
     * 
     * @return predicate factory registry
     */
    @Nonnull public PrincipalEvalPredicateFactoryRegistry getPrincipalPredicateFactoryEvalRegistry() {
        return evalRegistry;
    }
    
    /**
     * Set the registry of predicate factories for custom principal evaluation.
     * 
     * @param registry predicate factory registry
     */
    public void setPrincipalEvalPredicateFactoryRegistry(
            @Nonnull final PrincipalEvalPredicateFactoryRegistry registry) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        evalRegistry = Constraint.isNotNull(registry, "PrincipalEvalPredicateFactoryRegistry cannot be null");
    }

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        if (evalRegistry != null) {
            log.debug("{} Installing custom PrincipalEvalPredicateFactoryRegistry into AuthenticationContext",
                    getLogPrefix());
            authenticationContext.setPrincipalEvalPredicateFactoryRegistry(evalRegistry);
        }
        
        if (availableFlows.isEmpty()) {
            log.warn("{} No authentication flows are available for this request", getLogPrefix());
            return;
        }
        
        final Collection<String> activeFlows = activeFlowsLookupStrategy.apply(profileRequestContext);

        if (activeFlows != null && !activeFlows.isEmpty()) {
            for (final AuthenticationFlowDescriptor desc : availableFlows) {
                final String flowId = desc.getId().substring(desc.getId().indexOf('/') + 1);
                if (activeFlows.contains(flowId)) {
                    if (desc.apply(profileRequestContext)) {
                        authenticationContext.getPotentialFlows().put(desc.getId(), desc);
                    } else {
                        log.debug("{} Filtered out authentication flow {} due to attached condition", getLogPrefix(),
                                desc.getId());
                    }
                } else {
                    log.debug("{} Filtered out authentication flow {} due to profile configuration", getLogPrefix(),
                            desc.getId());
                }
            }
        } else {
            for (final AuthenticationFlowDescriptor desc : availableFlows) {
                if (desc.apply(profileRequestContext)) {
                    authenticationContext.getPotentialFlows().put(desc.getId(), desc);
                } else {
                    log.debug("{} Filtered out authentication flow {} due to attached condition", getLogPrefix(),
                            desc.getId());
                }
            }
        }

        if (authenticationContext.getPotentialFlows().isEmpty()) {
            log.warn("{} No authentication flows are active for this request", getLogPrefix());
        } else {
            log.debug("{} Installed {} authentication flows into AuthenticationContext", getLogPrefix(),
                    authenticationContext.getPotentialFlows().size());
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
}