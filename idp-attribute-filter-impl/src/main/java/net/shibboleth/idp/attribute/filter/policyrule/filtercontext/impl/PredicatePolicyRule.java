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

package net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.impl.AbstractPolicyRule;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;

/**
 * Call out to an externally define predicate.
 */
public class PredicatePolicyRule extends AbstractPolicyRule {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PredicatePolicyRule.class);

    /** The predicate to use. */
    @NonnullAfterInit private Predicate<ProfileRequestContext> rulePredicate;

    /** How to get to the {@link ProfileRequestContext} from the {@link AttributeFilterContext}. */
    @Nonnull private Function<AttributeFilterContext,ProfileRequestContext> profileContextStrategy;

    /** Constructor. */
    public PredicatePolicyRule() {
        profileContextStrategy =
                Functions.compose(new ParentContextLookup<RelyingPartyContext,ProfileRequestContext>(),
                        new ParentContextLookup<AttributeFilterContext,RelyingPartyContext>());
    }

    /**
     * Get the Predicate we'll use.
     * 
     * @return Returns the Predicate.
     */
    public Predicate<ProfileRequestContext> getRulePredicate() {
        return rulePredicate;
    }

    /**
     * set the Predicate we'll use.
     * 
     * @param predicate what to set.
     */
    public void setRulePredicate(Predicate<ProfileRequestContext> predicate) {
        rulePredicate = predicate;
    }

    /**
     * Set the context location strategy we'll use.
     * 
     * @return Returns the strategy.
     */
    public Function<AttributeFilterContext, ProfileRequestContext> getProfileContextStrategy() {
        return profileContextStrategy;
    }

    /**
     * Get the context location strategy we'll use.
     * 
     * @param strategy what to set.
     */
    public void setProfileContextStrategy(Function<AttributeFilterContext,ProfileRequestContext> strategy) {
        profileContextStrategy = Constraint.isNotNull(strategy, "ProfileContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == getRulePredicate()) {
            log.error("{} Provided Rule Predicate was null", getLogPrefix());
            throw new ComponentInitializationException("Provided Rule Predicate was null");
        }
    }

    /**
     * Compare the issuer from the context with the provided string.
     * 
     * @param filterContext the context
     * @return whether it matches. All failure and navigation issues return
     *      {@link net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate#FAIL}.
     * 
     * {@inheritDoc}
     */
    @Override public Tristate matches(@Nonnull AttributeFilterContext filterContext) {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        final ProfileRequestContext pc = profileContextStrategy.apply(filterContext);

        if (null == pc) {
            log.warn("{} Could not locate profile context", getLogPrefix());
            return Tristate.FAIL;
        }

        try {
            if (rulePredicate.apply(pc)) {
                log.trace("{} Predicate returned false", getLogPrefix());
                return Tristate.TRUE;
            }
            log.trace("{} Predicate returned false", getLogPrefix());
            return Tristate.FALSE;
        } catch (final Throwable ex) {
            log.warn("{} Applying the predicated failed", getLogPrefix(), ex);
            return Tristate.FAIL;
        }
    }
}