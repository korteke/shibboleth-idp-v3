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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiedInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * A policy describing if a set of attribute value filters is applicable.
 * 
 * Note, this filter policy operates on the {@link AttributeFilterContext#getFilteredIdPAttributes()} attribute set. The
 * idea being that as policies run they will retain or remove attributes and values for this collection. After all
 * policies run this collection will contain the final result.
 */
@ThreadSafe
public class AttributeFilterPolicy extends AbstractIdentifiedInitializableComponent implements
        UnmodifiableComponent {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeFilterPolicy.class);

    /** Criterion that must be met for this policy to be active for a given request. */
    private final PolicyRequirementRule rule;

    /** Filters to be used on attribute values. */
    private final List<AttributeRule> valuePolicies;

    /** Log prefix. */
    private String logPrefix;

    /**
     * Constructor.
     * 
     * @param policyId unique ID of this policy
     * @param requirementRule criterion used to determine if this policy is active for a given request
     * @param attributeRules value filtering policies employed if this policy is active
     */
    public AttributeFilterPolicy(@Nonnull @NotEmpty String policyId, @Nonnull PolicyRequirementRule requirementRule,
            @Nullable @NullableElements Collection<AttributeRule> attributeRules) {
        setId(policyId);

        rule = Constraint.isNotNull(requirementRule, "Attribute filter policy activiation criterion can not be null");

        ArrayList<AttributeRule> checkedPolicies = new ArrayList<>();
        CollectionSupport.addIf(checkedPolicies, attributeRules, Predicates.notNull());
        if (null != attributeRules) {
            valuePolicies = ImmutableList.copyOf(Iterables.filter(attributeRules, Predicates.notNull()));
        } else {
            valuePolicies = Collections.EMPTY_LIST;
        }
    }

    /**
     * Gets the MatchFunctor that must be met for this policy to be active for a given request.
     * 
     * @return MatchFunctor that must be met for this policy to be active for a given request
     */
    @Nonnull public PolicyRequirementRule getPolicyRequirementRule() {
        return rule;
    }

    /**
     * Gets the unmodifiable attribute rules that are in effect if this policy is in effect.
     * 
     * @return attribute rules that are in effect if this policy is in effect
     */
    @Nonnull @NonnullElements @Unmodifiable public List<AttributeRule> getAttributeRules() {
        return valuePolicies;
    }

    /**
     * Checks if the given filter context meets the requirements for this attribute filter policy as given by the
     * {@link PolicyRequirementRule}.
     * 
     * @param filterContext current filter context
     * 
     * @return true if this policy should be active for the given request, false otherwise
     * 
     */
    private boolean isApplicable(@Nonnull final AttributeFilterContext filterContext) {

        log.debug("{} Checking if attribute filter policy is active", getLogPrefix());

        Tristate isActive = rule.matches(filterContext);

        if (isActive == Tristate.FAIL) {
            log.warn("{} Policy requirement rule failed for this request", getLogPrefix());
        } else if (isActive == Tristate.TRUE) {
            log.debug("{} Policy is active for this request", getLogPrefix());
        } else {
            log.debug("{} Policy is not active for this request", getLogPrefix());
        }

        return isActive == Tristate.TRUE;
    }

    /**
     * Applies this filter policy to the given filter context if it is applicable.
     * 
     * @param filterContext current filter context
     * 
     * @throws AttributeFilterException thrown if there is a problem filtering out the attributes and values for this
     *             request
     */
    public void apply(@Nonnull final AttributeFilterContext filterContext) throws AttributeFilterException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");

        if (!isApplicable(filterContext)) {
            return;
        }

        final Map<String, IdPAttribute> attributes = filterContext.getPrefilteredIdPAttributes();
        log.debug("{} Applying attribute filter policy to current set of attributes: {}", getLogPrefix(),
                attributes.keySet());

        IdPAttribute attribute;
        for (AttributeRule valuePolicy : valuePolicies) {
            attribute = attributes.get(valuePolicy.getAttributeId());
            if (attribute != null) {
                if (!attribute.getValues().isEmpty()) {
                    valuePolicy.apply(attribute, filterContext);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        logPrefix = null;
    }

    /**
     * Get the prefix for logging.
     * 
     * @return Returns the logPrefix.
     */
    public String getLogPrefix() {
        String result;

        result = logPrefix;
        if (null == result) {
            result = new StringBuffer("Attribute Filter Policy '").append(getId()).append("' ").toString();
            logPrefix = result;
        }
        return result;
    }

}