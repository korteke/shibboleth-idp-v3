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

import javax.annotation.Nonnull;

/**
 * Generates a {@link com.google.common.base.Predicate} to evaluate a {@link PrincipalSupportingComponent}
 * against a requested form of authentication expressed in terms of a {@link Principal}.
 * 
 * <p>The predicate is constructed around a {@link Principal} to compare in an
 * implementation-specific manner against the candidate component. A given factory
 * produces predicates that implement a particular set of matching rules.
 * Factories are accessed via a {@link PrincipalEvalPredicateFactoryRegistry}.</p>
 */
public interface PrincipalEvalPredicateFactory {

    /**
     * Get a predicate to compare a candidate {@link Principal} object against a
     * {@link PrincipalSupportingComponent} for a "match", where the definition of
     * a match is implementation-specific. 
     * 
     * @param candidate a {@link Principal} object to evaluate
     * 
     * @return a {@link com.google.common.base.Predicate} implementing custom matching rules
     */
    @Nonnull PrincipalEvalPredicate getPredicate(@Nonnull final Principal candidate);
}