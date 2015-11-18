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

package net.shibboleth.idp.authn.principal;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;

/**
 * Function that returns the first custom {@link Principal} of a particular type found on the
 * {@link net.shibboleth.idp.authn.AuthenticationResult} returned by
 * {@link AuthenticationContext#getAuthenticationResult()}.
 * 
 * <p>
 * The context is located using a lookup strategy, by default a child of the input context.
 * <p>
 * 
 * <p>
 * If for any reason a matching Principal can't be located, a default is returned.
 * </p>
 * 
 * @param <T> the custom Principal type to locate
 */
public class DefaultPrincipalDeterminationStrategy<T extends Principal> implements Function<ProfileRequestContext,T> {

    /** Type of Principal to return. */
    @Nonnull private final Class<T> principalType;

    /** Default Principal to return. */
    @Nonnull private final T defaultPrincipal;
    
    /** A map supplying weighted preference to particular Principals. */
    @Nonnull @NonnullElements private Map<T,Integer> weightMap;

    /** Authentication context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,AuthenticationContext> authnContextLookupStrategy;

    /**
     * Constructor.
     * 
     * @param type class type for Principal type
     * @param principal default Principal to return
     */
    public DefaultPrincipalDeterminationStrategy(@Nonnull final Class<T> type, @Nonnull final T principal) {
        principalType = Constraint.isNotNull(type, "Class type cannot be null");
        defaultPrincipal = Constraint.isNotNull(principal, "Default Principal cannot be null");
        weightMap = Collections.emptyMap();
        authnContextLookupStrategy = new ChildContextLookup<>(AuthenticationContext.class, false);
    }
    
    /**
     * Set the map of Principals to weight values to impose a sort order on any matching Principals
     * found in the authentication result.
     * 
     * @param map   map to set
     */
    public void setWeightMap(@Nullable @NonnullElements final Map<T,Integer> map) {
        if (map == null) {
            weightMap = Collections.emptyMap();
            return;
        }
        
        weightMap = new HashMap<>(map.size());
        for (final Map.Entry<T,Integer> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                weightMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Set lookup strategy for {@link AuthenticationContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setAuthenticationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AuthenticationContext> strategy) {
        authnContextLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable public T apply(@Nullable final ProfileRequestContext input) {
        final AuthenticationContext ac = authnContextLookupStrategy.apply(input);
        if (ac == null || ac.getAuthenticationResult() == null) {
            return defaultPrincipal;
        }

        final Set<T> principals = ac.getAuthenticationResult().getSupportedPrincipals(principalType);
        if (principals.isEmpty()) {
            return defaultPrincipal;
        } else if (principals.size() == 1 || weightMap.isEmpty()) {
            return principals.iterator().next();
        }
            
        Object[] principalArray = principals.toArray();
        Arrays.sort(principalArray, new WeightedComparator());
        return (T) principalArray[principalArray.length - 1];
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