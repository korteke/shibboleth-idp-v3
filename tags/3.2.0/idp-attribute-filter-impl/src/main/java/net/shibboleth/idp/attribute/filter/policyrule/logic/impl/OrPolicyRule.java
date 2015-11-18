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

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * {@link PolicyRequirementRule} that implements the disjunction of Policy Rules.  That
 * is to say {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#TRUE}
 * if any rule returns {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#TRUE},
 * {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#FAIL} as soon as a rule returns
 * {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#FAIL}, and
 * {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#FALSE} otherwise.<br/>
 * The standard "fail/succeed fast" optimization is implemented.
 */
@ThreadSafe
public class OrPolicyRule extends AbstractComposedPolicyRule {

    /**
     * Constructor.
     * 
     * @param composedMatchers matchers being composed
     */
    public OrPolicyRule(@Nullable @NullableElements final Collection<PolicyRequirementRule> composedMatchers) {
        super(composedMatchers);
    }


    /**
    * A given rule is considered to have matched as soon as TRUE is returned by any composed
    * rule. It is considered to have failed as soon as FAIL is returned by any composed
    * rule.
    * 
    * {@inheritDoc}
    */
    public Tristate matches(@Nonnull AttributeFilterContext filterContext) {
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final List<PolicyRequirementRule> rules = getComposedRules();
        
        for (PolicyRequirementRule rule:rules) {
            Tristate match = rule.matches(filterContext);
            if (Tristate.FAIL == match) {
                return Tristate.FAIL;
            } else if (Tristate.TRUE == match) {
                return Tristate.TRUE;
            }
        }
        return Tristate.FALSE;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (getComposedRules().isEmpty()) {
            throw new ComponentInitializationException("No policy rules supplied to OR");
        }
    }
}