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

package net.shibboleth.idp.authn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.messaging.context.navigate.ChildContextLookup;

import com.google.common.base.Function;

/**
 * A base class for authentication related actions.
 * 
 * In addition to the work performed by {@link AbstractProfileAction}, this action also looks up
 * and makes available the {@link AuthenticationContext}.
 * 
 * Authentication action implementations should override
 * {@link #doExecute(ProfileRequestContext, AuthenticationContext)}
 * 
 * @param <InboundMessageType> type of in-bound message
 * @param <OutboundMessageType> type of out-bound message
 * 
 * @event {@link AuthnEventIds#INVALID_AUTHN_CTX}
 */
public abstract class AbstractAuthenticationAction<InboundMessageType, OutboundMessageType>
        extends AbstractProfileAction<InboundMessageType, OutboundMessageType> {

    /**
     * Strategy used to extract, and create if necessary, the {@link AuthenticationContext} from the
     * {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext, AuthenticationContext> authnCtxLookupStrategy;
    
    /** AuthenticationContext to operate on. */
    @Nullable private AuthenticationContext authnContext;

    /** Constructor. */
    public AbstractAuthenticationAction() {
        authnCtxLookupStrategy = new ChildContextLookup<>(AuthenticationContext.class);
    }

    /**
     * Set the context lookup strategy.
     * 
     * @param strategy  lookup strategy function for {@link AuthenticationContext}.
     */
    public void setLookupStrategy(@Nonnull final Function<ProfileRequestContext, AuthenticationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        authnCtxLookupStrategy = Constraint.isNotNull(strategy, "Strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected final boolean doPreExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {

        if (super.doPreExecute(profileRequestContext)) {
            authnContext = authnCtxLookupStrategy.apply(profileRequestContext);
            if (authnContext == null) {
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_AUTHN_CTX);
                return false;
            }
    
            return doPreExecute(profileRequestContext, authnContext);
        } else {
            return false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected final void doExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {
        doExecute(profileRequestContext, authnContext);
    }

    /**
     * Performs this authentication action's pre-execute step. Default implementation just returns true.
     * 
     * @param profileRequestContext the current IdP profile request context
     * @param authenticationContext the current authentication context
     * 
     * @return true iff execution should continue
     */
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        return true;
    }
    
    /**
     * Performs this authentication action. Default implementation throws an exception.
     * 
     * @param profileRequestContext the current IdP profile request context
     * @param authenticationContext the current authentication context
     */
    protected void doExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
    }

}