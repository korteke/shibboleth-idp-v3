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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A registry of mappings between a custom {@link Principal} subtype with a matching operator
 * and a corresponding {@link PrincipalEvalPredicateFactory} that returns predicates enforcing
 * a particular set of matching rules for that operator and subtype.
 */
public final class PrincipalEvalPredicateFactoryRegistry {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrincipalEvalPredicateFactoryRegistry.class);
    
    /** Storage for the registry mappings. */
    @Nonnull @NonnullElements
    private Map<Pair<Class<? extends Principal>, String>, PrincipalEvalPredicateFactory> registry;

    /** Constructor. */
    public PrincipalEvalPredicateFactoryRegistry() {
        registry = new ConcurrentHashMap();
    }
    
    /**
     * Constructor.
     * 
     * @param fromMap  map to populate registry with
     */
    public PrincipalEvalPredicateFactoryRegistry(@Nonnull @NonnullElements
            Map<Pair<Class<? extends Principal>, String>, PrincipalEvalPredicateFactory> fromMap) {
        registry = new ConcurrentHashMap(Constraint.isNotNull(fromMap, "Source map cannot be null"));
    }
    
    /**
     * Get a registered predicate factory for a given principal type and operator string, if any.
     * 
     * @param principalType a principal subtype
     * @param operator  an operator string
     * @return a corresponding predicate factory, or null
     */
    @Nullable public PrincipalEvalPredicateFactory lookup(@Nonnull final Class<? extends Principal> principalType,
            @Nonnull @NotEmpty final String operator) {
        Constraint.isNotNull(principalType, "Principal subtype cannot be null");
        String trimmed = Constraint.isNotNull(StringSupport.trimOrNull(operator), "Operator cannot be null or empty");
        
        Pair key = new Pair(principalType, trimmed);
        PrincipalEvalPredicateFactory factory = registry.get(key);
        if (factory != null) {
            log.debug("Registry located predicate factory of type '{}' for principal type '{}' and operator '{}'",
                    factory.getClass().getName(), principalType, trimmed);
            return factory;
        } else {
            log.debug("Registry failed to locate predicate factory for principal type '{}' and operator '{}'",
                    principalType, trimmed);
            return null;
        }
    }
    
    /**
     * Register a predicate factory for a given operator string.
     * 
     * @param principalType a principal subtype
     * @param operator  an operator string
     * @param factory   the predicate factory to register
     */
    public void register(@Nonnull final Class<? extends Principal> principalType,
            @Nonnull @NotEmpty final String operator, @Nonnull PrincipalEvalPredicateFactory factory) {
        Constraint.isNotNull(principalType, "Principal subtype cannot be null");
        String trimmed = Constraint.isNotNull(StringSupport.trimOrNull(operator), "Operator cannot be null or empty");
        Constraint.isNotNull(factory, "PrincipalEvalPredicateFactory cannot be null");
        
        log.debug("Registering predicate factory of type '{}' for principal type '{}' and operator '{}'",
                factory.getClass().getName(), principalType, operator);
        registry.put(new Pair(principalType, trimmed), factory);
    }
    
    /**
     * Deregister a predicate factory for a given operator string.
     * 
     * @param principalType a principal subtype
     * @param operator  an operator string
     */
    public void deregister(@Nonnull final Class<? extends Principal> principalType,
            @Nonnull @NotEmpty final String operator) {
        Constraint.isNotNull(principalType, "Principal subtype cannot be null");
        String trimmed = Constraint.isNotNull(StringSupport.trimOrNull(operator), "Operator cannot be null or empty");
        
        log.debug("Deregistering predicate factory for principal type '{}' and operator '{}'", principalType, operator);
        registry.remove(new Pair(principalType, trimmed));
    }
}