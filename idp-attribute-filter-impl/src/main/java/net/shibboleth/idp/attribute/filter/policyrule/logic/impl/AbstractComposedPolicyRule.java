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

package net.shibboleth.idp.attribute.filter.policyrule.logic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Base class for {@link PolicyRequirementRule} implementations that are compositions of other
 * {@link PolicyRequirementRule}s.
 */
public abstract class AbstractComposedPolicyRule extends AbstractIdentifiableInitializableComponent implements
        PolicyRequirementRule, UnmodifiableComponent {

    /** The composed matchers. */
    private final List<PolicyRequirementRule> rules;

    /**
     * Constructor.
     * 
     * @param theRules matchers being composed
     */
    public AbstractComposedPolicyRule(@Nullable @NullableElements final Collection<PolicyRequirementRule> theRules) {
        ArrayList<PolicyRequirementRule> checkedMatchers = new ArrayList<>();

        if (theRules != null) {
            CollectionSupport.addIf(checkedMatchers, theRules, Predicates.notNull());
        }

        rules = ImmutableList.copyOf(Iterables.filter(checkedMatchers, Predicates.notNull()));
    }

    /**
     * Get the composed matchers.
     * 
     * @return the composed matchers
     */
    @Nonnull @NonnullElements @Unmodifiable public List<PolicyRequirementRule> getComposedRules() {
        return rules;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("Composed Rules : ", getComposedRules()).toString();
    }
}