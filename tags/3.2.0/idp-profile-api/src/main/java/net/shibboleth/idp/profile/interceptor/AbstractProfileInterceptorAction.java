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

package net.shibboleth.idp.profile.interceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * A base class for profile interceptor actions.
 * 
 * In addition to the work performed by {@link AbstractProfileAction}, this action also looks up and makes available the
 * {@link ProfileInterceptorContext}.
 * 
 * Interceptor action implementations should override
 * {@link #doExecute(ProfileRequestContext, ProfileInterceptorContext)}
 * 
 * @param <InboundMessageType> type of in-bound message
 * @param <OutboundMessageType> type of out-bound message
 */
public abstract class AbstractProfileInterceptorAction<InboundMessageType, OutboundMessageType> extends
        AbstractProfileAction<InboundMessageType, OutboundMessageType> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractProfileInterceptorAction.class);

    /** Strategy used to find the {@link ProfileInterceptorContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ProfileInterceptorContext> interceptorContextlookupStrategy;

    /** The {@link ProfileInterceptorContext} to operate on. */
    @Nullable private ProfileInterceptorContext profileInterceptorContext;

    /** Constructor. */
    public AbstractProfileInterceptorAction() {
        interceptorContextlookupStrategy = new ChildContextLookup(ProfileInterceptorContext.class, true);
    }

    /**
     * Set the context lookup strategy for {@link ProfileInterceptorContext}.
     * 
     * @param strategy lookup strategy function
     */
    public void setLookupStrategy(@Nonnull final Function<ProfileRequestContext, ProfileInterceptorContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        interceptorContextlookupStrategy = Constraint.isNotNull(strategy, "Strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        profileInterceptorContext = interceptorContextlookupStrategy.apply(profileRequestContext);
        if (profileInterceptorContext == null) {
            log.error("{} Unable to create or locate profile interceptor context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return doPreExecute(profileRequestContext, profileInterceptorContext)
                && super.doPreExecute(profileRequestContext);
    }

    /**
     * Performs this profile interceptor action's pre-execute step. Default implementation returns true.
     * 
     * @param profileRequestContext the current profile request context
     * @param interceptorContext the current profile interceptor context
     * 
     * @return true iff execution should continue
     */
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        return true;
    }

    /** {@inheritDoc} */
    @Override protected final void doExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {
        doExecute(profileRequestContext, profileInterceptorContext);
    }

    /**
     * Performs this profile interceptor action. Default implementation does nothing.
     * 
     * @param profileRequestContext the current profile request context
     * @param interceptorContext the current profile interceptor context
     */
    protected void doExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

    }

}
