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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Advice;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Action which adds a {@link DelegationPolicy} element to the {@link Advice} of an {@link Assertion}.
 * 
 * <p>
 * The assertion to modify is determined by the strategy set by {@link #setAssertionLookupStrategy(Function)}.
 *</p>
 * 
 * <p>
 * The maximum chain delegation length value for the added policy element is as follows:
 * <ol>
 * <li>If an inbound assertion token is present as determined by the strategy set by
 * {@link #setAssertionTokenStrategy(Function)}, the value is obtained from the policy contained
 * within the first {@link DelegationPolicy} element of that assertion's {@link Advice} element.</li>
 * <li>Otherwise the request is assumed to be the initial SSO request, so the value is determined by
 * the requesting SP's profile configuration value
 * {@link BrowserSSOProfileConfiguration#getMaximumTokenDelegationChainLength()}.</li>
 * <li>If neither of these approaches produces a value, a default value is used 
 * {@link #DEFAULT_POLICY_MAX_CHAIN_LENGTH}</li>
 * </ol>
 * </p>
 */
@Prototype
public class AddDelegationPolicyToAssertion extends AbstractProfileAction {
    
    /** Default policy max chain length, when can't otherwise be derived. */
    public static final Long DEFAULT_POLICY_MAX_CHAIN_LENGTH = 1L;
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(AddDelegationPolicyToAssertion.class);
    
    /** Strategy used to locate the {@link Assertion} to operate on. */
    @Nonnull private Function<ProfileRequestContext,Assertion> assertionLookupStrategy;
    
    /** Function used to resolve the inbound assertion token to process. */
    @Nonnull private Function<ProfileRequestContext, Assertion> assertionTokenStrategy;
    
    /** Strategy used to lookup the RelyingPartyContext. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** The assertion to modify. */
    @Nullable private Assertion assertion;
    
    /** The inbound delegated Assertion that was attested. */
    @Nullable private Assertion attestedAssertion;
    
    /** The max token delegation chain length value to add. */
    @Nullable private Long maxChainLength;
    
    /** Constructor. */
    public AddDelegationPolicyToAssertion() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        assertionLookupStrategy = new AssertionStrategy();
        assertionTokenStrategy = new DelegatedAssertionLookupStrategy();
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
     * Set the strategy used to locate the current {@link RelyingPartyContext}.
     * 
     * @param strategy strategy used to locate the current {@link RelyingPartyContext}
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        relyingPartyContextLookupStrategy = Constraint.isNotNull(strategy, 
                "RelyingPartyContext lookup strategy may not be null");
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
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext))  {
            return false;
        }
        
        assertion = assertionLookupStrategy.apply(profileRequestContext);
        if (assertion == null) {
            log.debug("No assertion found, nothing to do");
            return false;
        }
        
        attestedAssertion = assertionTokenStrategy.apply(profileRequestContext);
        
        maxChainLength = resolveMaxChainLength(profileRequestContext);
        log.debug("Resolved token max delegation chain length: {}", maxChainLength);
        
        return true;
    }
    
    /** {@inheritDoc} */
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        DelegationPolicy delegationPolicy = (DelegationPolicy) XMLObjectSupport.buildXMLObject(
                DelegationPolicy.DEFAULT_ELEMENT_NAME);
        delegationPolicy.setMaximumTokenDelegationChainLength(maxChainLength);
        
        if (assertion.getAdvice() == null) {
            assertion.setAdvice((Advice) XMLObjectSupport.buildXMLObject(Advice.DEFAULT_ELEMENT_NAME));
        }
        
        assertion.getAdvice().getChildren().add(delegationPolicy);
    }

    /**
     * Resolve the max token delegation chain length value to add to the assertion.
     * 
     * @param profileRequestContext the current profile request context
     * @return the max chain length value
     */
    @Nonnull protected Long resolveMaxChainLength(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (attestedAssertion != null) {
            // If have an inbound assertion token, then this is N-tier, so we copy the value from the inbound token.
            log.debug("Saw inbound assertion token, attempting to extract max delegation chain length " 
                    + "from token's DelegationPolicy");
            if (attestedAssertion.getAdvice() != null) {
                List<XMLObject> inboundPolicies = 
                        attestedAssertion.getAdvice().getChildren(DelegationPolicy.DEFAULT_ELEMENT_NAME);
                if (inboundPolicies != null && !inboundPolicies.isEmpty()) {
                    return ((DelegationPolicy)inboundPolicies.get(0)).getMaximumTokenDelegationChainLength();
                }
            }
        } else {
            // If no inbound assertion token, this must be initial SSO, so pull from RP's IdP config.
            log.debug("Attempting to resolve max delegation chain length from RP profile config");
            RelyingPartyContext relyingPartyContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
            if (relyingPartyContext != null) {
                if (relyingPartyContext.getProfileConfig() instanceof BrowserSSOProfileConfiguration) {
                    return ((BrowserSSOProfileConfiguration) relyingPartyContext.getProfileConfig())
                            .getMaximumTokenDelegationChainLength();
                } else {
                    log.debug("Profile config was not BrowserSSOProfileConfiguration, can't evaluate: {}", 
                            relyingPartyContext.getProfileConfig() != null ? 
                                    relyingPartyContext.getProfileConfig().getClass().getName() : "null");
                }
            }
        }
        log.debug("Unable to resolve max delegation chain length from inbound token or profile config, " 
                + "returning default: {}", DEFAULT_POLICY_MAX_CHAIN_LENGTH);
        return DEFAULT_POLICY_MAX_CHAIN_LENGTH;
    }

    /**
     * Default strategy for obtaining assertion to modify.
     */
    private class AssertionStrategy implements Function<ProfileRequestContext,Assertion> {

        /** {@inheritDoc} */
        @Override
        @Nullable public Assertion apply(@Nullable final ProfileRequestContext input) {
            if (input != null && input.getOutboundMessageContext() != null) {
                final Object outboundMessage = input.getOutboundMessageContext().getMessage();
                if (outboundMessage instanceof Assertion) {
                    return (Assertion) outboundMessage;
                } else if (outboundMessage instanceof Response) {
                    Response response = (Response) outboundMessage;
                    if (response.getAssertions().isEmpty()) {
                        return null;
                    } else {
                        for (Assertion theAssertion : response.getAssertions()) {
                            if (!theAssertion.getAuthnStatements().isEmpty()) {
                                log.debug("Found Assertion with AuthnStatement to decorate in outbound Response");
                                return theAssertion;
                            }
                        }
                        log.debug("Found no Assertion with AuthnStatement in outbound Response, returning first");
                        return response.getAssertions().get(0);
                    } 
                }
            }
            
            return null;
        }
    }

}
