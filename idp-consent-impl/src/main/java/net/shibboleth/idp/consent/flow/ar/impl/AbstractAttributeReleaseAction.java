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

package net.shibboleth.idp.consent.flow.ar.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.consent.context.impl.AttributeReleaseContext;
import net.shibboleth.idp.consent.flow.impl.AbstractConsentAction;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Base class for attribute release consent actions.
 * 
 * Ensures that
 * <ul>
 * <li>an {@link AttributeReleaseContext} is available from the {@link ProfileRequestContext}</li>
 * <li>the interceptor attempted flow is an {@link AttributeReleaseFlowDescriptor}</li>
 * <li>an {@link AttributeContext} is available from the {@link ProfileRequestContext}</li>
 * </ul>
 * 
 * @pre See above.
 */
public abstract class AbstractAttributeReleaseAction extends AbstractConsentAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractAttributeReleaseAction.class);

    /** The {@link AttributeReleaseContext} to operate on. */
    @Nullable private AttributeReleaseContext attributeReleaseContext;

    /** Strategy used to find the {@link AttributeReleaseContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, AttributeReleaseContext> attributeReleaseContextLookupStrategy;

    /** The attribute consent flow descriptor. */
    @Nullable private AttributeReleaseFlowDescriptor attributeReleaseFlowDescriptor;

    /** The {@link AttributeContext} to operate on. */
    @Nullable private AttributeContext attributeContext;

    /** Strategy used to find the {@link AttributeContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;

    /** Constructor. */
    public AbstractAttributeReleaseAction() {
        attributeReleaseContextLookupStrategy = new ChildContextLookup<>(AttributeReleaseContext.class, false);

        attributeContextLookupStrategy =
                Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                        new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
    }

    /**
     * Set the attribute context lookup strategy.
     * 
     * @param strategy the attribute context lookup strategy
     */
    public void setAttributeContextLookupStrategy(Function<ProfileRequestContext, AttributeContext> strategy) {
        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "Attribute context lookup strategy cannot be null");
    }

    /**
     * Set the attribute release context lookup strategy.
     * 
     * @param strategy the attribute release context lookup strategy
     */
    public void setAttributeReleaseContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeReleaseContext> strategy) {
        attributeReleaseContextLookupStrategy =
                Constraint.isNotNull(strategy, "Attribute release context lookup strategy cannot be null");
    }

    /**
     * Get the attribute release context.
     * 
     * @return the attribute release context
     */
    @Nullable public AttributeReleaseContext getAttributeReleaseContext() {
        return attributeReleaseContext;
    }

    /**
     * Get the attribute release flow descriptor.
     * 
     * @return the attribute release flow descriptor
     */
    @Nullable public AttributeReleaseFlowDescriptor getAttributeReleaseFlowDescriptor() {
        return attributeReleaseFlowDescriptor;
    }

    /**
     * Get the attribute context.
     * 
     * @return the attribute context
     */
    @Nullable public AttributeContext getAttributeContext() {
        return attributeContext;
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        attributeReleaseContext = attributeReleaseContextLookupStrategy.apply(profileRequestContext);
        if (attributeReleaseContext == null) {
            log.debug("{} Unable to locate attribute release context within profile request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        if (!(interceptorContext.getAttemptedFlow() instanceof AttributeReleaseFlowDescriptor)) {
            log.debug("{} ProfileInterceptorFlowDescriptor is not an AttributeReleaseFlowDescriptor", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        attributeReleaseFlowDescriptor = (AttributeReleaseFlowDescriptor) interceptorContext.getAttemptedFlow();

        attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        log.debug("{} Found attributeContext '{}'", getLogPrefix(), attributeContext);
        if (attributeContext == null) {
            log.error("{} Unable to locate attribute context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return super.doPreExecute(profileRequestContext, interceptorContext);
    }
}
