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

package net.shibboleth.idp.saml.saml2.profile.delegation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * A predicate which determines whether issuance of a delegated 
 * SAML 2 {@link org.opensaml.saml.saml2.core.Assertion} is active.
 */
public class IssuingDelegatedAssertionPredicate implements Predicate<ProfileRequestContext> {
    
    /** Strategy used to lookup the {@link DelegationContext. */
    @Nonnull private Function<ProfileRequestContext, DelegationContext> delegationContextLookupStrategy;
    
    /** Constructor. */
    public IssuingDelegatedAssertionPredicate() {
        delegationContextLookupStrategy = new ChildContextLookup<>(DelegationContext.class);
    }
    
    /**
     * Set the strategy used to locate the current {@link DelegationContext}.
     * 
     * @param strategy strategy used to locate the current {@link DelegationContext}
     */
    public void setDelegationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, DelegationContext> strategy) {
        delegationContextLookupStrategy = Constraint.isNotNull(strategy, 
                "DelegationContext lookup strategy may not be null");
    }
    
    /** {@inheritDoc} */
    public boolean apply(@Nullable ProfileRequestContext input) {
        if (input == null) {
            return false;
        }
        DelegationContext delegationContext = delegationContextLookupStrategy.apply(input);
        if (delegationContext == null) {
            return false;
        }
        return delegationContext.isIssuingDelegatedAssertion();
    }

}
