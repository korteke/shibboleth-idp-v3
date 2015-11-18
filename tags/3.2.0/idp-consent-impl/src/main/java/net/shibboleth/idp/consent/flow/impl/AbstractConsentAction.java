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

package net.shibboleth.idp.consent.flow.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Base class for consent actions.
 * 
 * Ensures that :
 * <ul>
 * <li>a consent context exists in the profile request context</li>
 * <li>the flow descriptor is a {@link ConsentFlowDescriptor}</li>
 * </ul>
 */
public abstract class AbstractConsentAction extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractConsentAction.class);

    /** Consent context. */
    @Nullable private ConsentContext consentContext;

    /** Consent flow descriptor. */
    @Nullable private ConsentFlowDescriptor consentFlowDescriptor;

    /** Strategy used to find the {@link ConsentContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ConsentContext> consentContextLookupStrategy;

    /** Constructor. */
    public AbstractConsentAction() {
        consentContextLookupStrategy = new ChildContextLookup<>(ConsentContext.class, false);
    }

    /**
     * Get the consent context.
     * 
     * @return the consent context
     */
    @Nullable public ConsentContext getConsentContext() {
        return consentContext;
    }

    /**
     * Get the consent flow descriptor.
     * 
     * @return the consent flow descriptor
     */
    @Nullable public ConsentFlowDescriptor getConsentFlowDescriptor() {
        return consentFlowDescriptor;
    }

    /**
     * Set the consent context lookup strategy.
     * 
     * @param strategy the consent context lookup strategy
     */
    public void
            setConsentContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, ConsentContext> strategy) {
        consentContextLookupStrategy = Constraint.isNotNull(strategy, "Consent context lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        consentContext = consentContextLookupStrategy.apply(profileRequestContext);
        if (consentContext == null) {
            log.debug("{} Unable to locate consent context within profile request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        if (!(interceptorContext.getAttemptedFlow() instanceof ConsentFlowDescriptor)) {
            log.debug("{} ProfileInterceptorFlowDescriptor is not a ConsentFlowDescriptor", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        consentFlowDescriptor = (ConsentFlowDescriptor) interceptorContext.getAttemptedFlow();

        return super.doPreExecute(profileRequestContext, interceptorContext);
    }
}
