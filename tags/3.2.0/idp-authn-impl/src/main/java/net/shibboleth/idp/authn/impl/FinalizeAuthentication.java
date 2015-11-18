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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicate;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactory;
import net.shibboleth.idp.authn.principal.PrincipalSupportingComponent;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An authentication action that runs after a completed authentication flow (or the reuse
 * of an active result) and transfers information from other contexts into a {@link SubjectContext}
 * child of the {@link ProfileRequestContext}.
 * 
 * <p>The action also cross-checks {@link RequestedPrincipalContext#getMatchingPrincipal()}, if set,
 * against the {@link AuthenticationResult} to ensure that the result produced actually satisfies the
 * request. This is redundant when reusing active results, but is necessary to prevent a flow from running
 * that can return different results and having it produce a result that doesn't actually satisfy the
 * request. Such a flow would be buggy, but this guards against a mistake from leaving the subsystem.</p>
 * 
 * <p>If no matching Principal is established, or if the match is no longer valid, the request is
 * evaluated in conjunction with the {@link AuthenticationResult} to establish a Principal that
 * does satisfy the request and it is recorded via {@link RequestedPrincipalContext#setMatchingPrincipal()}.</p>
 * 
 * <p>The context is populated based on the presence of a canonical principal name in either
 * a {@link SubjectCanonicalizationContext} or {@link SessionContext}, and also includes
 * the completed {@link AuthenticationResult} and any other active results found in the
 * {@link AuthenticationContext}.</p>
 * 
 * <p>Any {@link SubjectCanonicalizationContext} found will be removed.</p>
 * 
 * <p>If a {@link SubjectContext} already exists, then this action will validate that
 * the same principal name is represented by it, and signal a mismatch otherwise. This
 * is used in protocols that indicate normatively what the authenticated identity is
 * required to be.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#INVALID_SUBJECT_CTX}
 * @event {@link AuthnEventIds#REQUEST_UNSUPPORTED}
 * 
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class) != null</pre>
 * 
 * @post If SubjectCanonicalizationContext.getCanonicalPrincipalName() != null
 * || SessionContext.getIdPSession() != null
 * then ProfileRequestContext.getSubcontext(SubjectContext.class) != null 
 * @post AuthenticationContext.setCompletionInstant() was called
 * @post <pre>ProfileRequestContext.getSubcontext(SubjectCanonicalizationContext.class) == null</pre>
 */
public class FinalizeAuthentication extends AbstractAuthenticationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FinalizeAuthentication.class);
    
    /** A map supplying weighted preference to particular Principals. */
    @Nonnull @NonnullElements private Map<Principal,Integer> weightMap;
    
    /** The principal name extracted from the context tree. */
    @Nullable private String canonicalPrincipalName;
    
    /** Constructor. */
    public FinalizeAuthentication() {
        weightMap = Collections.emptyMap();
    }
    
    /**
     * Set the map of Principals to weight values to impose a sort order on any matching Principals
     * found in the authentication result.
     * 
     * @param map   map to set
     */
    public void setWeightMap(@Nullable @NonnullElements final Map<Principal,Integer> map) {
        if (map == null) {
            weightMap = Collections.emptyMap();
            return;
        }
        
        weightMap = new HashMap<>(map.size());
        for (final Map.Entry<Principal,Integer> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                weightMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        final SubjectCanonicalizationContext c14nCtx =
                profileRequestContext.getSubcontext(SubjectCanonicalizationContext.class);
        if (c14nCtx != null) {
            canonicalPrincipalName = c14nCtx.getPrincipalName();
            profileRequestContext.removeSubcontext(c14nCtx);
            log.debug("{} Canonical principal name was established as '{}'", getLogPrefix(), canonicalPrincipalName);
        }
        
        if (canonicalPrincipalName == null) {
            final SessionContext sessionCtx = profileRequestContext.getSubcontext(SessionContext.class);
            if (sessionCtx != null && sessionCtx.getIdPSession() != null) {
                canonicalPrincipalName = sessionCtx.getIdPSession().getPrincipalName();
                log.debug("{} Canonical principal name established from session as '{}'", getLogPrefix(),
                        canonicalPrincipalName);
            }
        }
        
        // Check for requested Principal criteria and make sure the result accomodates the criteria.
        // This is required because flow selection is based (generally) on statically-defined information
        // and the actual result produced may be a subset (and therefore could be an inadequate subset).
        final RequestedPrincipalContext requestedPrincipalCtx =
                authenticationContext.getSubcontext(RequestedPrincipalContext.class);
        if (requestedPrincipalCtx != null) {
            final AuthenticationResult latest = authenticationContext.getAuthenticationResult();
            if (latest == null) {
                log.warn("{} Authentication result missing from context?", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.REQUEST_UNSUPPORTED);
                return false;
            }
            
            // If a matching principal is set, re-verify it. Normally this will work.
            final Principal match = requestedPrincipalCtx.getMatchingPrincipal();
            if (match != null) {
                if (!latest.getSupportedPrincipals(match.getClass()).contains(match)) {
                    log.debug("{} Authentication result lacks originally projected matching principal '{}',"
                            + " reevaluating", getLogPrefix(), match.getName());
                    requestedPrincipalCtx.setMatchingPrincipal(null);
                }
            }
            
            // It didn't work, so we have to run the machinery over the request principals and
            // evaluate the result more fully.
            requestedPrincipalCtx.setMatchingPrincipal(
                    findMatchingPrincipal(authenticationContext, requestedPrincipalCtx));

            // If it's still null, then the result didn't meet our needs.
            if (requestedPrincipalCtx.getMatchingPrincipal() == null) {
                log.warn("{} Authentication result for flow {} did not satisfy the request", getLogPrefix(),
                        latest.getAuthenticationFlowId());
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.REQUEST_UNSUPPORTED);
                return false;
            }
            
        } else {
            log.debug("{} Request did not have explicit authentication requirements, result is accepted",
                    getLogPrefix());
        }
        
        return super.doPreExecute(profileRequestContext, authenticationContext);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
    
        if (canonicalPrincipalName != null) {
            final SubjectContext sc = profileRequestContext.getSubcontext(SubjectContext.class, true);
            
            // Check for an existing value.
            if (sc.getPrincipalName() != null && !canonicalPrincipalName.equals(sc.getPrincipalName())) {
                log.warn("{} Result of authentication ({}) does not match existing subject in context ({})",
                        getLogPrefix(), canonicalPrincipalName, sc.getPrincipalName());
                ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_SUBJECT_CTX);
                return;
            }
            
            sc.setPrincipalName(canonicalPrincipalName);
    
            final Map scResults = sc.getAuthenticationResults();
            scResults.putAll(authenticationContext.getActiveResults());
            
            final AuthenticationResult latest = authenticationContext.getAuthenticationResult();
            if (latest != null && !scResults.containsKey(latest.getAuthenticationFlowId())) {
                scResults.put(latest.getAuthenticationFlowId(), latest);
            }
        }
        
        authenticationContext.setCompletionInstant();
    }

    /**
     * Evaluate request criteria and the {@link AuthenticationResult} to locate a {@link Principal} in the
     * result that satisfies the request criteria.
     * 
     * <p>If a weighting map is supplied, the {@link Principal} returned is the one that both satisfies
     * the request and is highest weighted.</p>
     * 
     * @param authenticationContext authentication context
     * @param requestedPrincipalCtx request criteria
     * 
     * @return matching Principal, or null
     */
    @Nullable protected Principal findMatchingPrincipal(@Nonnull final AuthenticationContext authenticationContext,
            @Nonnull final RequestedPrincipalContext requestedPrincipalCtx) {
        
        // Maintain a list of each Principal that matches the request.
        final ArrayList<Principal> matches = new ArrayList<>();
        
        for (final Principal p : requestedPrincipalCtx.getRequestedPrincipals()) {
            log.debug("{} Checking result for compatibility with operator '{}' and principal '{}'",
                    getLogPrefix(), requestedPrincipalCtx.getOperator(), p.getName());
            final PrincipalEvalPredicateFactory factory =
                    authenticationContext.getPrincipalEvalPredicateFactoryRegistry().lookup(
                            p.getClass(), requestedPrincipalCtx.getOperator());
            if (factory != null) {
                final PrincipalEvalPredicate predicate = factory.getPredicate(p);

                // For unweighted results, we'd just apply the predicate to the AuthenticationResult, but
                // we won't be able to honor weighting, so we have to walk the supported principals one
                // at a time, wrap it to apply the predicate, and then record it if it succeeds.
                
                matches.clear();
                for (final Principal candidate
                        : authenticationContext.getAuthenticationResult().getSupportedPrincipals(p.getClass())) {
                    if (predicate.apply(new PrincipalSupportingComponent() {
                        public <T extends Principal> Set<T> getSupportedPrincipals(Class<T> c) {
                            return Collections.<T>singleton((T) candidate);
                        }
                    })) {
                        log.debug("{} Principal '{}' in authentication result satisfies request for principal '{}'",
                                getLogPrefix(), candidate.getName(), p.getName());
                        matches.add(candidate);
                    }
                }
                
                // The first non-empty match set satisfies the request, so that's what we use.
                // That honors the precedence order of the input criteria.
                if (!matches.isEmpty()) {
                    break;
                }
            } else {
                log.warn("{} Configuration does not support requested principal evaluation with "
                        + "operator '{}' and type '{}'", getLogPrefix(), requestedPrincipalCtx.getOperator(),
                        p.getClass());
            }
        }
        
        if (matches.isEmpty()) {
            return null;
        } else if (matches.size() == 1 || weightMap.isEmpty()) {
            return matches.get(0);
        } else {
            Object[] principalArray = matches.toArray();
            Arrays.sort(principalArray, new WeightedComparator());
            return (Principal) principalArray[principalArray.length - 1];
        }
    }
    
    /**
     * A {@link Comparator} that compares the mapped weights of the two operands, using a weight of zero
     * for any unmapped values.
     */
    private class WeightedComparator implements Comparator {

        /** {@inheritDoc} */
        @Override
        public int compare(Object o1, Object o2) {
            
            int weight1 = weightMap.containsKey(o1) ? weightMap.get(o1) : 0;
            int weight2 = weightMap.containsKey(o2) ? weightMap.get(o2) : 0;
            if (weight1 < weight2) {
                return -1;
            } else if (weight1 > weight2) {
                return 1;
            }
            
            return 0;
        }
        
    }

}