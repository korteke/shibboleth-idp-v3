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

package net.shibboleth.idp.saml.session.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.OutboundMessageContextLookup;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.session.SAML2SPSession;
import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * A function to create a {@link SAML2SPSession} based on profile execution state.
 * 
 * <p>This strategy is a default approach that uses a {@link RelyingPartyContext} via lookup strategy
 * to obtain a requester value, used as the {@link SPSession}'s relying party ID. The authentication flow ID
 * comes from the {@link net.shibboleth.idp.authn.AuthenticationResult} in the
 * {@link net.shibboleth.idp.authn.context.AuthenticationContext}.
 * The session has a creation time based on the time of execution, and the expiration is based on a configurable
 * lifetime, bounded by the per-SP lifetime setting for the profile.</p>
 * 
 * <p>The SAML 2 specific data is extracted from the first assertion containing an authn statement
 * found in a {@link Response} message located via a lookup strategy, by default the outbound
 * message context. Failure to locate any of this data will cause a null return value.</p>
 */
public class SAML2SPSessionCreationStrategy implements Function<ProfileRequestContext, SPSession> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML2SPSessionCreationStrategy.class);
    
    /** Lifetime of sessions to create. */
    @Positive @Duration private final long sessionLifetime;
    
    /** RelyingPartyContext lookup strategy. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Response lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, Response> responseLookupStrategy;
    
    /**
     * Constructor.
     * 
     * @param lifetime lifetime in milliseconds, determines upper bound for expiration of
     * {@link SAML2SPSession} to be created
     */
    public SAML2SPSessionCreationStrategy(@Positive @Duration final long lifetime) {
        sessionLifetime = Constraint.isGreaterThan(0, lifetime, "Lifetime must be greater than 0");
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        responseLookupStrategy =
                Functions.compose(new MessageLookup<>(Response.class), new OutboundMessageContextLookup());
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
        
    /**
     * Set the strategy used to locate the {@link Response} to operate on.
     * 
     * @param strategy strategy used to locate the {@link Response} to operate on
     */
    public void setResponseLookupStrategy(@Nonnull final Function<ProfileRequestContext, Response> strategy) {
        responseLookupStrategy = Constraint.isNotNull(strategy, "Response lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nullable public SPSession apply(@Nullable final ProfileRequestContext input) {
        
        final RelyingPartyContext rpCtx = relyingPartyContextLookupStrategy.apply(input);
        if (rpCtx == null) {
            log.debug("No RelyingPartyContext, no SAML2SPSession created");
            return null;
        }
        
        final String issuer = rpCtx.getRelyingPartyId();
        if (issuer == null) {
            log.debug("No relying party ID, no SAML2SPSession created");
            return null;
        }
        
        final Pair<Assertion, AuthnStatement> result = getAssertionAndStatement(input);
        if (result == null) {
            log.info("Creating BasicSPSession in the absence of necessary information");
            final long now = System.currentTimeMillis();
            return new BasicSPSession(issuer, now, now + sessionLifetime);
        }
        
        final long now = System.currentTimeMillis();
        final DateTime sessionBound = result.getSecond().getSessionNotOnOrAfter();
        long expiration;
        if (sessionBound != null) {
            expiration = sessionBound.getMillis(); 
        } else {
            expiration = now + sessionLifetime;
        }
        
        return new SAML2SPSession(issuer, now, expiration, result.getFirst().getSubject().getNameID(),
                result.getSecond().getSessionIndex());
    }

    /**
     * Locate the first assertion and authentication statement, such that the assertion subject
     * contains a name identifier and the statement contains a session index.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return the suitable objects, or null
     */
    @Nullable private Pair<Assertion, AuthnStatement> getAssertionAndStatement(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        
        final Response response = responseLookupStrategy.apply(profileRequestContext);
        if (response == null) {
            log.debug("No Response message or Assertions found");
            return null;
        }
        
        for (final Assertion assertion : response.getAssertions()) {
            if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
                for (final AuthnStatement statement : assertion.getAuthnStatements()) {
                    if (statement.getSessionIndex() != null) {
                        return new Pair<>(assertion, statement);
                    }
                }
            }
        }
        
        log.debug("No suitable Assertion/AuthnStatement found");
        return null;
    }
    
}