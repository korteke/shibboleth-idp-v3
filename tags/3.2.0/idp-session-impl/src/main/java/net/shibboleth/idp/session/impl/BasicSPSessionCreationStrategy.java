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

package net.shibboleth.idp.session.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Function;

/**
 * A function to create a {@link BasicSPSession} based on profile execution state.
 * 
 * <p>This strategy is a default approach that uses a lookup strategy for a {@link RelyingPartyContext}
 * to obtain an issuer value, used as the {@link SPSession}'s relying party ID. The authentication flow ID
 * comes from the {@link net.shibboleth.idp.authn.AuthenticationResult}
 * in the {@link net.shibboleth.idp.authn.context.AuthenticationContext}. The session has a 
 * creation time based on the time of execution, and the expiration is based on a configurable lifetime.</p> 
 */
public class BasicSPSessionCreationStrategy implements Function<ProfileRequestContext, SPSession> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BasicSPSessionCreationStrategy.class);
    
    /** Lifetime of sessions to create. */
    @Positive @Duration private final long sessionLifetime;

    /** RelyingPartyContext lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /**
     * Constructor.
     * 
     * @param lifetime lifetime in milliseconds, determines expiration of {@link SPSession} to be created
     */
    public BasicSPSessionCreationStrategy(@Positive @Duration final long lifetime) {
        sessionLifetime = Constraint.isGreaterThan(0, lifetime, "Lifetime must be greater than 0");
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
    }

    /**
     * Set the strategy used to locate the {@link RelyingPartyContext} to operate on.
     * 
     * @param strategy lookup strategy
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,RelyingPartyContext> strategy) {
        relyingPartyContextLookupStrategy = Constraint.isNotNull(strategy,
                "RelyingPartyContext lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nullable public SPSession apply(@Nullable final ProfileRequestContext input) {
        
        final RelyingPartyContext rpCtx = relyingPartyContextLookupStrategy.apply(input);
        if (rpCtx == null) {
            log.debug("No RelyingPartyContext, no SPSession created");
            return null;
        }
        
        final String issuer = rpCtx.getRelyingPartyId();
        if (issuer == null) {
            log.debug("No relying party ID, no SPSession created");
            return null;
        }
        
        final long now = System.currentTimeMillis();
        return new BasicSPSession(issuer, now, now + sessionLifetime);
    }

}