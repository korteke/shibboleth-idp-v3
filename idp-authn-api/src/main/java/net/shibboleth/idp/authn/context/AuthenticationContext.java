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

package net.shibboleth.idp.authn.context;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactory;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactoryRegistry;
import net.shibboleth.idp.authn.principal.PrincipalSupportingComponent;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.MoreObjects;

/**
 * A context representing the state of an authentication attempt, this is the primary
 * input/output context for the action flow responsible for authentication, and
 * within that flow, the individual flows that carry out a specific kind of
 * authentication.
 */
public final class AuthenticationContext extends BaseContext {

    /** Time, in milliseconds since the epoch, when the authentication process started. */
    @Positive private final long initiationInstant;

    /** Whether to require fresh subject interaction to succeed. */
    private boolean forceAuthn;

    /** Whether authentication must not involve subject interaction. */
    private boolean isPassive;
    
    /** A non-normative hint some protocols support to indicate who the subject might be. */
    @Nullable private String hintedName;
    
    /** Flows that could potentially be used to authenticate the user. */
    @Nonnull @NonnullElements private final Map<String,AuthenticationFlowDescriptor> potentialFlows;

    /** Authentication results associated with an active session and available for (re)use. */
    @Nonnull @NonnullElements private final Map<String,AuthenticationResult> activeResults;
        
    /** The registry of predicate factories for custom principal evaluation. */
    @Nonnull private PrincipalEvalPredicateFactoryRegistry evalRegistry;

    /** Previously attempted flows (could be failures or intermediate results). */
    @Nonnull @NonnullElements private final Map<String,AuthenticationFlowDescriptor> intermediateFlows;
    
    /** Authentication flow being attempted to authenticate the user. */
    @Nullable private AuthenticationFlowDescriptor attemptedFlow;
    
    /** Signals authentication flow to run next, to influence selection logic. */
    @Nullable private String signaledFlowId;

    /** Storage map for interflow communication. */
    @Nonnull private final Map<String,Object> stateMap;
    
    /** A successful "initial" authentication result from the current request's initial-authn phase. */
    @Nullable private AuthenticationResult initialAuthenticationResult;

    /** A successful authentication result (the output of the attempted flow, if any). */
    @Nullable private AuthenticationResult authenticationResult;

    /** Result may be cached for reuse in the normal way. */
    private boolean resultCacheable;
    
    /** Time, in milliseconds since the epoch, when authentication process completed. */
    @NonNegative private long completionInstant;

    /** Constructor. */
    public AuthenticationContext() {
        initiationInstant = System.currentTimeMillis();
        
        potentialFlows = new LinkedHashMap<>();
        activeResults = new HashMap<>();
        intermediateFlows = new HashMap<>();
        
        stateMap = new HashMap<>();
        
        evalRegistry = new PrincipalEvalPredicateFactoryRegistry();
        resultCacheable = true;
    }

    /**
     * Get the time, in milliseconds since the epoch, when the authentication process started.
     * 
     * @return time when the authentication process started
     */
    @Positive public long getInitiationInstant() {
        return initiationInstant;
    }

    /**
     * Get the authentication results currently active for the subject.
     * 
     * @return authentication results currently active for the subject
     */
    @Nonnull @NonnullElements @Live public Map<String,AuthenticationResult> getActiveResults() {
        return activeResults;
    }

    /**
     * Set the authentication results currently active for the subject.
     * 
     * @param results authentication results currently active for the subject
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setActiveResults(
            @Nonnull @NonnullElements final Iterable<AuthenticationResult> results) {
        Constraint.isNotNull(results, "AuthenticationResult collection cannot be null");

        activeResults.clear();
        for (AuthenticationResult result : results) {
            activeResults.put(result.getAuthenticationFlowId(), result);
        }

        return this;
    }
    
    /**
     * Get the set of flows that could potentially be used for user authentication.
     * 
     * @return the potential flows
     */
    @Nonnull @NonnullElements @Live public Map<String,AuthenticationFlowDescriptor> getPotentialFlows() {
        return potentialFlows;
    }

    
    /**
     * Get the set of flows that have been executed, successfully or otherwise, without producing a completed result.
     * 
     * @return the intermediately executed flows
     */
    @Nonnull @NonnullElements @Live public Map<String,AuthenticationFlowDescriptor> getIntermediateFlows() {
        return intermediateFlows;
    }
    
    /**
     * Get the registry of predicate factories for custom principal evaluation.
     * 
     * @return predicate factory registry
     */
    @Nonnull public PrincipalEvalPredicateFactoryRegistry getPrincipalEvalPredicateFactoryRegistry() {
        return evalRegistry;
    }

    /**
     * Set the registry of predicate factories for custom principal evaluation.
     * 
     * @param registry predicate factory registry
     */
    public void setPrincipalEvalPredicateFactoryRegistry(
            @Nonnull final PrincipalEvalPredicateFactoryRegistry registry) {
        evalRegistry = Constraint.isNotNull(registry, "PrincipalEvalPredicateFactoryRegistry cannot be null");
    }
    
    /**
     * Get whether subject interaction is allowed.
     * 
     * @return whether subject interaction may occur
     */
    public boolean isPassive() {
        return isPassive;
    }

    /**
     * Set whether subject interaction is allowed.
     * 
     * @param passive whether subject interaction may occur
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setIsPassive(final boolean passive) {
        isPassive = passive;
        return this;
    }
    
    /**
     * Get whether to require fresh subject interaction to succeed.
     * 
     * @return whether subject interaction must occur
     */
    public boolean isForceAuthn() {
        return forceAuthn;
    }

    /**
     * Set whether to require fresh subject interaction to succeed.
     * 
     * @param force whether subject interaction must occur
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setForceAuthn(final boolean force) {
        forceAuthn = force;
        return this;
    }
    
    /**
     * Get a non-normative hint provided by the request about the user's identity.
     * 
     * @return  the username hint
     */
    @Nullable public String getHintedName() {
        return hintedName;
    }
    
    /**
     * Set a non-normative hint provided by the request about the user's identity.
     * 
     * @param hint the username hint
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setHintedName(@Nullable final String hint) {
        hintedName = StringSupport.trimOrNull(hint);
        return this;
    }

    /**
     * Get the authentication flow that was attempted in order to authenticate the user.
     * 
     * <p>This is not set if an existing result was reused for SSO.</p>
     * 
     * @return authentication flow that was attempted in order to authenticate the user
     */
    @Nullable public AuthenticationFlowDescriptor getAttemptedFlow() {
        return attemptedFlow;
    }

    /**
     * Set the authentication flow that was attempted in order to authenticate the user.
     * 
     * <p>Do not set if an existing result was reused for SSO.</p>
     * 
     * @param flow authentication flow that was attempted in order to authenticate the user
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setAttemptedFlow(@Nullable final AuthenticationFlowDescriptor flow) {
        attemptedFlow = flow;
        return this;
    }
    
    /**
     * Get the flow ID signaled as the next selection.
     * 
     * @return  ID of flow to run next
     */
    @Nullable public String getSignaledFlowId() {
        return signaledFlowId;
    }
    
    /**
     * Set the flow ID signaled as the next selection.
     * 
     * @param id ID of flow to run next
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setSignaledFlowId(@Nullable final String id) {
        signaledFlowId = StringSupport.trimOrNull(id);
        return this;
    }    

    /**
     * Get the map of intermediate state that flows can use to pass information amongst themselves.
     * 
     * @return the state map
     */
    @Nonnull @Live public Map<String,Object> getAuthenticationStateMap() {
        return stateMap;
    }
    
    /**
     * Get the "initial" authentication result produced during this request's initial-authn phase.
     * 
     * <p>This is used to make a previous result available for SSO even if the "forced authentication"
     * feature is being used, since the result was produced as part of the same request.</p>
     * 
     * @return "initial" authentication result, if any
     */
    @Nullable public AuthenticationResult getInitialAuthenticationResult() {
        return initialAuthenticationResult;
    }

    /**
     * Set the "initial" authentication result produced during this request's initial-authn phase.
     * 
     * @param result "initial" authentication result, if any
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setInitialAuthenticationResult(
            @Nullable final AuthenticationResult result) {
        initialAuthenticationResult = result;
        return this;
    }
    
    /**
     * Get the authentication result produced by the attempted flow, or reused for SSO.
     * 
     * @return authentication result, if any
     */
    @Nullable public AuthenticationResult getAuthenticationResult() {
        return authenticationResult;
    }

    /**
     * Set the authentication result produced by the attempted flow, or reused for SSO.
     * 
     * @param result authentication result, if any
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setAuthenticationResult(@Nullable final AuthenticationResult result) {
        authenticationResult = result;
        return this;
    }

    /**
     * Get whether the result is suitable for caching (such as in a session) for reuse.
     * 
     * @return  true iff the result may be cached/reused, subject to other policy
     */
    public boolean isResultCacheable() {
        return resultCacheable;
    }
    
    /**
     * Set whether the result is suitable for caching (such as in a session) for reuse.
     * 
     * @param flag  flag to set
     */
    public void setResultCacheable(final boolean flag) {
        resultCacheable = flag;
    }
        
    /**
     * Get the time, in milliseconds since the epoch, when the authentication process ended. A value of 0 indicates
     * that authentication has not yet completed.
     * 
     * @return time when the authentication process ended
     */
    @NonNegative public long getCompletionInstant() {
        return completionInstant;
    }

    /**
     * Set the completion time of the authentication attempt to the current time.
     * 
     * @return this authentication context
     */
    @Nonnull public AuthenticationContext setCompletionInstant() {
        completionInstant = System.currentTimeMillis();
        return this;
    }

    /**
     * Helper method that evaluates a {@link PrincipalSupportingComponent} against a
     * {@link RequestedPrincipalContext} child of this context, if present, to determine
     * if the input is compatible with it.
     * 
     * @param component component to evaluate
     * 
     * @return true iff the input is compatible with the requested authentication requirements or if
     *  no such requirements have been imposed
     */
    public boolean isAcceptable(@Nonnull final PrincipalSupportingComponent component) {
        final RequestedPrincipalContext rpCtx = getSubcontext(RequestedPrincipalContext.class);
        if (rpCtx != null) {
            for (final Principal requestedPrincipal : rpCtx.getRequestedPrincipals()) {
                final PrincipalEvalPredicateFactory factory =
                        evalRegistry.lookup(requestedPrincipal.getClass(), rpCtx.getOperator());
                if (factory != null) {
                    if (factory.getPredicate(requestedPrincipal).apply(component)) {
                        return true;
                    }
                }
            }
            
            // Nothing matched the candidate.
            return false;
            
        } else {
            // No requirements so anything is acceptable.
            return true;
        }
    }
        
    /**
     * Helper method that evaluates {@link Principal} objects against a {@link RequestedPrincipalContext} child
     * of this context, if present, to determine if the input is compatible with them.
     * 
     * @param principals principal(s) to evaluate
     * 
     * @return true iff the input is compatible with the requested authentication requirements or if
     *  no such requirements have been imposed
     */
    public boolean isAcceptable(@Nonnull @NonnullElements final Collection<Principal> principals) {
        final RequestedPrincipalContext rpCtx = getSubcontext(RequestedPrincipalContext.class);
        if (rpCtx != null) {
            return isAcceptable(new PrincipalSupportingComponent() {
                public <T extends Principal> Set<T> getSupportedPrincipals(Class<T> c) {
                    final HashSet set = new HashSet<>();
                    for (final Principal p : principals) {
                        if (c.isAssignableFrom(p.getClass())) {
                            set.add(p);
                        }
                    }
                    return set;
                }
            });
        } else {
            // No requirements so anything is acceptable.
            return true;
        }
    }

    /**
     * Helper method that evaluates a {@link Principal} object against a {@link RequestedPrincipalContext} child
     * of this context, if present, to determine if the input is compatible with it.
     * 
     * @param <T> type of principal
     * @param principal principal to evaluate
     * 
     * @return true iff the input is compatible with the requested authentication requirements or if
     *  no such requirements have been imposed
     */
    public <T extends Principal> boolean isAcceptable(@Nonnull final T principal) {
        final RequestedPrincipalContext rpCtx = getSubcontext(RequestedPrincipalContext.class);
        if (rpCtx != null) {
            return isAcceptable(new PrincipalSupportingComponent() {
                public <TT extends Principal> Set<TT> getSupportedPrincipals(Class<TT> c) {
                    if (c.isAssignableFrom(principal.getClass())) {
                        return Collections.singleton((TT) principal);
                    } else {
                        return Collections.emptySet();
                    }
                }
            });
        } else {
            // No requirements so anything is acceptable.
            return true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("initiationInstant", new DateTime(initiationInstant))
                .add("isPassive", isPassive)
                .add("forceAuthn", forceAuthn)
                .add("hintedName", hintedName)
                .add("potentialFlows", potentialFlows.keySet())
                .add("activeResults", activeResults.keySet())
                .add("attemptedFlow", attemptedFlow)
                .add("signaledFlowId", signaledFlowId)
                .add("authenticationStateMap", stateMap)
                .add("resultCacheable", resultCacheable)
                .add("initialAuthenticationResult", initialAuthenticationResult)
                .add("authenticationResult", authenticationResult)
                .add("completionInstant", new DateTime(completionInstant))
                .toString();
    }

}