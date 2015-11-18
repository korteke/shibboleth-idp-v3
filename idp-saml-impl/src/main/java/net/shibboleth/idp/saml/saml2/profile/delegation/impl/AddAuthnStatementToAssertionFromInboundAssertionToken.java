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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.config.navigate.IdentifierGenerationStrategyLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.profile.SAML2ActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Action that builds an {@link AuthnStatement} and adds it to an {@link Assertion} returned by a lookup
 * strategy, by default in the {@link ProfileRequestContext#getOutboundMessageContext()}.
 * 
 * <p>This action is designed specifically to be used with SAML 2 delegation.
 * The {@link AuthnStatement} will be cloned directly from the inbound {@link Assertion} token obtained
 * from via the {@link #setAssertionTokenStrategy(Function)}.
 * </p>
 * 
 * <p>If no {@link Response} exists, then an {@link Assertion} directly in the outbound message context will
 * be used or created</p>
 * 
 * <p>A constructed {@link Assertion} will have its ID, IssueInstant, Issuer, and Version properties set.
 * The issuer is based on {@link net.shibboleth.idp.relyingparty.RelyingPartyConfiguration#getResponderId()}.</p>
 * 
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#MESSAGE_PROC_ERROR}
 */
@Prototype
public class AddAuthnStatementToAssertionFromInboundAssertionToken extends AbstractProfileAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = 
            LoggerFactory.getLogger(AddAuthnStatementToAssertionFromInboundAssertionToken.class);
    
    /**
     * Whether the generated authentication statement should be placed in its own assertion or added to one if it
     * exists.
     */
    private boolean statementInOwnAssertion;

    /** Strategy used to locate the {@link IdentifierGenerationStrategy} to use. */
    @Nonnull private Function<ProfileRequestContext,IdentifierGenerationStrategy> idGeneratorLookupStrategy;

    /** Strategy used to obtain the assertion issuer value. */
    @Nonnull private Function<ProfileRequestContext,String> issuerLookupStrategy;

    /** The generator to use. */
    @Nullable private IdentifierGenerationStrategy idGenerator;
    
    /** EntityID to populate as assertion issuer. */
    @Nullable private String issuerId;
    
    /** Strategy used to locate the {@link Assertion} to operate on. */
    @NonnullAfterInit private Function<ProfileRequestContext,Assertion> assertionLookupStrategy;
    
    /** Function used to resolve the inbound assertion token to process. */
    @Nonnull private Function<ProfileRequestContext, Assertion> assertionTokenStrategy;
    
    /** The authentication statement which is to be cloned into the new Assertion. */
    @Nullable private AuthnStatement sourceStatement;
    
    /** Constructor. */
    public AddAuthnStatementToAssertionFromInboundAssertionToken() {
        statementInOwnAssertion = false;

        idGeneratorLookupStrategy = new IdentifierGenerationStrategyLookupFunction();
        issuerLookupStrategy = new ResponderIdLookupFunction();
        assertionTokenStrategy = new DelegatedAssertionLookupStrategy();
    }
    
    /**
     * Set whether the generated statement should be placed in its own assertion or added to one if it exists.
     * 
     * @return whether the generated statement should be placed in its own assertion or added to one if it exists
     */
    public boolean isStatementInOwnAssertion() {
        return statementInOwnAssertion;
    }
    
    /**
     * Set whether the generated authentication statement should be placed in its own assertion or added to one if it
     * exists.
     * 
     * @param inOwnAssertion whether the generated authentication statement should be placed in its own assertion or
     *            added to one if it exists
     */
    public void setStatementInOwnAssertion(final boolean inOwnAssertion) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        statementInOwnAssertion = inOwnAssertion;
    }
    
    /**
     * Set the strategy used to locate the inbound assertion token to process.
     * 
     * @param strategy lookup strategy
     */
    public void setAssertionTokenStrategy(
            @Nonnull final Function<ProfileRequestContext,Assertion> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        assertionTokenStrategy = Constraint.isNotNull(strategy, "Assertion token strategy may not be null");
    }

    /**
     * Set the strategy used to locate the {@link IdentifierGenerationStrategy} to use.
     * 
     * @param strategy lookup strategy
     */
    public void setIdentifierGeneratorLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,IdentifierGenerationStrategy> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        idGeneratorLookupStrategy =
                Constraint.isNotNull(strategy, "IdentifierGenerationStrategy lookup strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the issuer value to use.
     * 
     * @param strategy lookup strategy
     */
    public void setIssuerLookupStrategy(@Nonnull final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        issuerLookupStrategy = Constraint.isNotNull(strategy, "Issuer lookup strategy cannot be null");
    }
    
    /**
     * Get the {@link IdentifierGenerationStrategy} to use if an assertion must be created.
     * 
     * @return the ID generation strategy
     */
    @Nonnull public IdentifierGenerationStrategy getIdGenerator() {
        Constraint.isNotNull(idGenerator, "IdentifierGenerationStrategy has not been initialized yet");
        return idGenerator;
    }

    /**
     * Get the issuer name to use if an assertion must be created.   
     *
     * @return the issuer name
     */
    @Nonnull public String getIssuerId() {
        Constraint.isNotNull(issuerId, "Issuer name has not been initialized yet");
        return issuerId;
    }
    
    /**
     * Set the strategy used to locate the {@link Assertion} to operate on.
     * 
     * @param strategy strategy used to locate the {@link Assertion} to operate on
     */
    public void setAssertionLookupStrategy(@Nonnull final Function<ProfileRequestContext,Assertion> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        assertionLookupStrategy = Constraint.isNotNull(strategy, "Assertion lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (assertionLookupStrategy == null) {
            assertionLookupStrategy = new AssertionStrategy();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }

        log.debug("{} Attempting to add an AuthnStatement to outgoing Assertion based on inbound Assertion token", 
                getLogPrefix());
        
        idGenerator = idGeneratorLookupStrategy.apply(profileRequestContext);
        if (idGenerator == null) {
            log.debug("{} No identifier generation strategy", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        issuerId = issuerLookupStrategy.apply(profileRequestContext);
        if (issuerId == null) {
            log.debug("{} No assertion issuer value", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        Assertion attestedToken = assertionTokenStrategy.apply(profileRequestContext);
        if (attestedToken == null) {
            log.debug("{} No inbound assertion token", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        if (!attestedToken.getAuthnStatements().isEmpty()) {
            sourceStatement = attestedToken.getAuthnStatements().get(0);
        }
        if (sourceStatement == null) {
            log.debug("{} Inbound assertion token contains no AuthnStatement", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        final Assertion assertion = assertionLookupStrategy.apply(profileRequestContext);
        if (assertion == null) {
            log.error("Unable to obtain Assertion to modify");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return;
        }

        final AuthnStatement statement = getNewAuthnStatement();
        if (statement == null) {
            log.error("Unable to obtain AuthnStatement to add");
            ActionSupport.buildEvent(profileRequestContext, EventIds.MESSAGE_PROC_ERROR);
            return;
        }
        assertion.getAuthnStatements().add(statement);

        log.debug("{} Added AuthenticationStatement to Assertion {}", getLogPrefix(), assertion.getID());
    }

    /**
     * Obtain the new {@link AuthnStatement} to add by cloning the inbound token's statement 
     * which was previously stored.
     * 
     * @return the cloned AuthnStatement, or null if an error is encountered
     */
    @Nullable protected AuthnStatement getNewAuthnStatement() {
        try {
            return XMLObjectSupport.cloneXMLObject(sourceStatement);
        } catch (MarshallingException | UnmarshallingException e) {
            log.error("{} Error cloning Assertion AuthnStatement", getLogPrefix(), e);
            return null;
        }
    }
    
    /**
     * Default strategy for obtaining assertion to modify.
     * 
     * <p>If the outbound context is empty, a new assertion is created and stored there. If the outbound
     * message is already an assertion, it's returned. If the outbound message is a response, then either
     * an existing or new assertion in the response is returned, depending on the action setting. If the
     * outbound message is anything else, null is returned.</p>
     */
    private class AssertionStrategy implements Function<ProfileRequestContext,Assertion> {

        /** {@inheritDoc} */
        @Override
        @Nullable public Assertion apply(@Nullable final ProfileRequestContext input) {
            if (input != null && input.getOutboundMessageContext() != null) {
                final Object outboundMessage = input.getOutboundMessageContext().getMessage();
                if (outboundMessage == null) {
                    final Assertion ret = SAML2ActionSupport.buildAssertion(
                            AddAuthnStatementToAssertionFromInboundAssertionToken.this,
                            getIdGenerator(), getIssuerId());
                    input.getOutboundMessageContext().setMessage(ret);
                    return ret;
                } else if (outboundMessage instanceof Assertion) {
                    return (Assertion) outboundMessage;
                } else if (outboundMessage instanceof Response) {
                    if (isStatementInOwnAssertion() || ((Response) outboundMessage).getAssertions().isEmpty()) {
                        return SAML2ActionSupport.addAssertionToResponse(
                                AddAuthnStatementToAssertionFromInboundAssertionToken.this,
                                (Response) outboundMessage, getIdGenerator(), getIssuerId());
                    } else {
                        return ((Response) outboundMessage).getAssertions().get(0);
                    } 
                }
            }
            
            return null;
        }
    }

}
