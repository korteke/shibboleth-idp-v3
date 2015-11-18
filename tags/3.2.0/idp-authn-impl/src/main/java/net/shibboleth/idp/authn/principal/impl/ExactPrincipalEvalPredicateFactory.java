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

package net.shibboleth.idp.authn.principal.impl;

import java.security.Principal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.principal.PrincipalEvalPredicate;
import net.shibboleth.idp.authn.principal.PrincipalEvalPredicateFactory;
import net.shibboleth.idp.authn.principal.PrincipalSupportingComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * {@link PrincipalEvalPredicateFactory} that implements exact matching of principals,
 * and works for any type.
 */
public class ExactPrincipalEvalPredicateFactory implements PrincipalEvalPredicateFactory {

    /** {@inheritDoc} */
    @Nonnull public PrincipalEvalPredicate getPredicate(@Nonnull final Principal candidate) {
        return new ExactMatchPredicate(candidate);
    }

    /** Implementation of an exact-matching predicate. */
    private class ExactMatchPredicate implements PrincipalEvalPredicate {

        /** The principal object to compare against. */
        @Nonnull private final Principal principal;
        
        /**
         * Constructor.
         *
         * @param candidate principal to compare against
         */
        public ExactMatchPredicate(@Nonnull final Principal candidate) {
            principal = Constraint.isNotNull(candidate, "Principal cannot be null");
        }

        /** {@inheritDoc} */
        public boolean apply(PrincipalSupportingComponent input) {
            return input != null && input.getSupportedPrincipals(principal.getClass()).contains(principal);
        }

        /** {@inheritDoc} */
        @Nullable public Principal getMatchingPrincipal() {
            return principal;
        }
    }
    
}