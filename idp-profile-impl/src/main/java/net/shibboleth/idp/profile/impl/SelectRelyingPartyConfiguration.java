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

package net.shibboleth.idp.profile.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * This action attempts to resolve a {@link RelyingPartyConfiguration} and adds it to the {@link RelyingPartyContext}
 * that was looked up.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#INVALID_RELYING_PARTY_CTX}
 * @event {@link IdPEventIds#INVALID_RELYING_PARTY_CONFIG}
 * 
 * @post If a {@link RelyingPartyContext} is located, it will be populated with a non-null result of applying
 * the supplied {@link RelyingPartyConfigurationResolver} to the {@link ProfileRequestContext}.
 */
public final class SelectRelyingPartyConfiguration extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SelectRelyingPartyConfiguration.class);

    /** Resolver used to look up relying party configurations. */
    @NonnullAfterInit private RelyingPartyConfigurationResolver rpConfigResolver;

    /**
     * Strategy used to locate the {@link RelyingPartyContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;

    /** The {@link RelyingPartyContext} to manipulate. */
    @Nullable private RelyingPartyContext relyingPartyCtx;
    
    /** Constructor. */
    public SelectRelyingPartyConfiguration() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }

    /**
     * Set the relying party config resolver to use.
     * 
     * @param resolver  the resolver to use
     */
    public void setRelyingPartyConfigurationResolver(@Nonnull final RelyingPartyConfigurationResolver resolver) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        rpConfigResolver = Constraint.isNotNull(resolver, "Relying party configuration resolver cannot be null");
    }
    
    /**
     * Set the strategy used to locate the {@link RelyingPartyContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link RelyingPartyContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (rpConfigResolver == null) {
            throw new ComponentInitializationException("RelyingPartyConfigurationResolver cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        relyingPartyCtx = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyCtx == null) {
            log.debug("{} No relying party context available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        }
        
        return super.doPreExecute(profileRequestContext);
    }
    
    /** {@inheritDoc} */
    @Override
    public void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        try {
            final RelyingPartyConfiguration config = rpConfigResolver.resolveSingle(profileRequestContext);
            if (config == null) {
                log.debug("{} No relying party configuration applies to this request", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CONFIG);
                return;
            }

            log.debug("{} Found relying party configuration {} for request", getLogPrefix(), config.getId());
            relyingPartyCtx.setConfiguration(config);
        } catch (ResolverException e) {
            log.error("{} Error trying to resolve relying party configuration", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CONFIG);
        }
    }
}