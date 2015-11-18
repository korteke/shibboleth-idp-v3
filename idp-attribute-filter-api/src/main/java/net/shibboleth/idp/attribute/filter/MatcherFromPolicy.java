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

package net.shibboleth.idp.attribute.filter;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Bridging class to go from a {@link PolicyRequirementRule} to a {@link Matcher}.
 * <p>
 * 
 * If the rule is true then we return all values, else we return none. If the rule fails we return null.
 */
public class MatcherFromPolicy extends BaseBridgingClass implements Matcher, IdentifiedComponent,
        DestructableComponent {

    /** The rule we are shadowing. */
    private final PolicyRequirementRule rule;

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MatcherFromPolicy.class);

    /**
     * Constructor.
     * 
     * @param theRule the class we are bridging to
     */
    public MatcherFromPolicy(@Nonnull PolicyRequirementRule theRule) {
        super(theRule);
        rule = theRule;
    }
    
    /** Testing support.  Get the embedded PolicyRequirementRule.
     * @return the embedded matcher.
     */
    @Nonnull public PolicyRequirementRule getPolicyRequirementRule() {
        return rule;
    }


    /** {@inheritDoc} */
    @Override
    @Nullable public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull IdPAttribute attribute,
            @Nonnull AttributeFilterContext filterContext) {

        final Tristate result= rule.matches(filterContext);

        if (Tristate.FAIL == result) {
            log.warn("{} The rule returned FAIL, returning null", getLogPrefix());
            return null;
        } else if (Tristate.FALSE == result) {
            log.debug("{} The rule returned FALSE, no values returned", getLogPrefix());
            return Collections.EMPTY_SET;
        } else {
            log.debug("{} The rule returned TRUE, all values returned", getLogPrefix());
            return ImmutableSet.copyOf(attribute.getValues());
        }
    }
    
}