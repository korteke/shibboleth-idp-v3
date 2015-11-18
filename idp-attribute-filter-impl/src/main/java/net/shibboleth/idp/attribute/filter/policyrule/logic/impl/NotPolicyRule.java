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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.MoreObjects;

/**
 * {@link PolicyRequirementRule} that implements the negation of a matcher. <br/>
 * <br/>
 * if FAIL then FAIL else if TRUE then FALSE else TRUE<br/>
 */
@ThreadSafe
public final class NotPolicyRule extends AbstractIdentifiableInitializableComponent implements PolicyRequirementRule {

    /** The matcher we are negating. */
    private final PolicyRequirementRule negatedRule;

    /**
     * Constructor.
     * 
     * @param rule attribute value matcher to be negated
     */
    public NotPolicyRule(@Nonnull final PolicyRequirementRule rule) {
        negatedRule = Constraint.isNotNull(rule, "Policy Requirement rule can not be null");
    }

    /**
     * Get the matcher that is being negated.
     * 
     * @return matcher that is being negated
     */
    @Nonnull public PolicyRequirementRule getNegatedRule() {
        return negatedRule;
    }

    /** {@inheritDoc} */
    @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        Tristate match = negatedRule.matches(filterContext);
        if (Tristate.FAIL == match) {
            return Tristate.FAIL;
        } else if (Tristate.FALSE == match) {
            return Tristate.TRUE;
        } else {
            return Tristate.FALSE;
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("Negated Policy Rule", negatedRule).toString();
    }
}