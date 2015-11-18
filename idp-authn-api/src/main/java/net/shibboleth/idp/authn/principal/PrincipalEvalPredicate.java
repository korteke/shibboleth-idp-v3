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

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

/**
 * A {@link Predicate} to evaluate a {@link Principal} that represents a requested form of
 * authentication against a set of principals supported by a {@link PrincipalSupportingComponent}.
 * 
 * <p>The predicate contains an additional method that makes available the actual
 * {@link Principal} object that satisfied the predicate. In concrete terms, this
 * represents the <strong>actual</strong> authentication method that was performed.</p>
 */
public interface PrincipalEvalPredicate extends Predicate<PrincipalSupportingComponent> {

    /**
     * Get the {@link Principal} object from the evaluated {@link PrincipalSupportingComponent}
     * that actually satisfied the predicate, if any.
     * 
     * @return  a custom principal, or null
     */
    @Nullable Principal getMatchingPrincipal(); 
}