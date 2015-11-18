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

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.wssecurity.SAML20AssertionToken;
import org.opensaml.soap.wssecurity.messaging.Token;
import org.opensaml.soap.wssecurity.messaging.Token.ValidationStatus;
import org.opensaml.soap.wssecurity.messaging.WSSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;


/**
 * Locate a pre-validated {@link org.opensaml.saml.saml2.core.Assertion} WS-Security token,
 * and populate the {@link LibertySSOSContext}.
 * 
 * <p>
 * The default token strategy is to resolve the first instance of {@link SAML20AssertionToken} 
 * present in the inbound {@link WSSecurityContext} which has a validation status of 
 * {@link ValidationStatus#VALID} 
 * </p>
 * 
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @pre <pre>assertionTokenStrategy.apply() != null</pre>
 * @post <pre>profileRequestContext.getSubcontext(LibertySSOSContext.class) != null</pre>
 * @post <pre>LibertySSOSContext.getAttestedToken() != null</pre>
 * @post <pre>LibertySSOSContext.getAttestedSubjectConfirmationMethod != null</pre>
 */
@Prototype
public class PopulateLibertyContext extends AbstractProfileAction {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(PopulateLibertyContext.class);
    
    /** Function used to resolve the assertion token to process. */
    @Nonnull private Function<ProfileRequestContext, SAML20AssertionToken> assertionTokenStrategy;
    
    /** Function used to resolve the Liberty context to populate. */
    @Nonnull private Function<ProfileRequestContext, LibertySSOSContext> libertyContextLookupStrategy;
    
    /** The SAML 2 Assertion token being processed. */
    private SAML20AssertionToken assertionToken;
    
    /** Liberty context to populate. */
    private LibertySSOSContext ssosContext;
    
    /**
     * Constructor.
     */
    public PopulateLibertyContext() {
        assertionTokenStrategy = new TokenStrategy();
        libertyContextLookupStrategy = new ChildContextLookup<>(LibertySSOSContext.class, true);
    }
    
    /**
     * Set the strategy used to locate the {@link LibertySSOSContext} to populate.
     * 
     * @param strategy lookup strategy
     */
    public void setLibertyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,LibertySSOSContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        libertyContextLookupStrategy = Constraint.isNotNull(strategy, "Assertion token strategy may not be null");
    }
    
    /**
     * Set the strategy used to locate the requester ID for canonicalization.
     * 
     * @param strategy lookup strategy
     */
    public void setAssertionTokenStrategy(
            @Nonnull final Function<ProfileRequestContext,SAML20AssertionToken> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        assertionTokenStrategy = Constraint.isNotNull(strategy, "Assertion token strategy may not be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        assertionToken = assertionTokenStrategy.apply(profileRequestContext);
        
        if (assertionToken == null) {
            log.info("{} No valid SAML20AssertionToken available within inbound WSSecurityContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return false;
        }
        
        ssosContext = libertyContextLookupStrategy.apply(profileRequestContext);
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        // Populate Liberty context for use later.
        ssosContext = profileRequestContext.getSubcontext(LibertySSOSContext.class, true);
        ssosContext.setAttestedToken(assertionToken.getWrappedToken());
        ssosContext.setAttestedSubjectConfirmationMethod(assertionToken.getSubjectConfirmation().getMethod());
    }
    
    /**
     * Default strategy for resolving the assertion token to process.
     * 
     * <p>This impl just returns the first valid {@link SAML20AssertionToken} found
     * in the inbound {@link WSSecurityContext}.</p>
     */
    public class TokenStrategy implements Function<ProfileRequestContext, SAML20AssertionToken> {

        /** {@inheritDoc} */
        @Nullable
        public SAML20AssertionToken apply(@Nullable ProfileRequestContext input) {
            if (input == null) {
                return null;
            }
            WSSecurityContext wssContext = 
                    input.getInboundMessageContext().getSubcontext(WSSecurityContext.class);
            if (wssContext == null) {
                log.info("{} No WSSecurityContext available within inbound message context", getLogPrefix());
                return null;
            }
            
            for (Token token : wssContext.getTokens()) {
                if (token.getValidationStatus().equals(ValidationStatus.VALID) 
                        && token instanceof SAML20AssertionToken) {
                    return (SAML20AssertionToken) token;
                }
            }
            return null;
        }
        
    }
    
}
