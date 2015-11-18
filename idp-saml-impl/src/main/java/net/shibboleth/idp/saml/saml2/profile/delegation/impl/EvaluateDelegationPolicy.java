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

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;
import net.shibboleth.idp.saml.xmlobject.DelegationPolicy;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.ext.saml2delrestrict.DelegationRestrictionType;
import org.opensaml.saml.saml2.core.Advice;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Action which implements policy controls to decide whether an SSO request based
 * on a delegated {@link Assertion} token is allowed to proceed.
 * 
 * <p>
 * Two policy checks are performed:
 * <ol>
 * <li>
 * The active {@link SSOSProfileConfiguration} is resolved and the predicate 
 * {@link SSOSProfileConfiguration#getDelegationPredicate()} is applied.  If the predicate evaluates to false,
 * the request is not allowed.  An example predicate commonly used here is 
 * {@link net.shibboleth.idp.saml.profile.config.logic.AllowedSAMLPresentersPredicate}.
 * </li>
 * <li>
 * The length of the delegation chain as indicated in the inbound assertion token's {@link DelegationRestrictionType}
 * condition is evaluated against a policy maximum resolved via the strategy set by 
 * {@link #setPolicyMaxChainLengthStrategy(Function)}, or from {@link #DEFAULT_POLICY_MAX_CHAIN_LENGTH} if no value 
 * can otherwise be resolved. If the chain of {@link org.opensaml.saml.ext.saml2delrestrict.Delegate} 
 * child elements is greater than or equal to the resolved policy max chain length, the request is not allowed.
 * The default policy resolution strategy is to look at the first {@link DelegationPolicy} contained within the 
 * inbound assertion token's {@link Advice}.
 * </li>
 * </ol>
 * </p>
 * 
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#INVALID_SEC_CFG}
 */
@Prototype
public class EvaluateDelegationPolicy extends AbstractProfileAction {
    
    /** Default policy max chain length, when can't otherwise be derived. */
    public static final Long DEFAULT_POLICY_MAX_CHAIN_LENGTH = 1L;
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(EvaluateDelegationPolicy.class);
    
    // Configured data
    
    /** Strategy used to lookup the RelyingPartyContext. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Function used to resolve the assertion token to process. */
    @Nonnull private Function<ProfileRequestContext, Assertion> assertionTokenStrategy;
    
    /** Function used to resolve the policy maximum delegation chain length. */
    @Nonnull private Function<ProfileRequestContext, Long> policyMaxChainLengthStrategy;
    
    // Runtime data
    
    /** The inbound delegated assertion token being evaluated. */
    private Assertion assertionToken;
    
    /** The policy maximum token delegation chain length. */
    private Long policyMaxChainLength;
    
    /** The actual token delegation chain length. */
    private Long tokenChainLength;
    
    /** The predicate used to determine whether the request is allowed to proceed. */
    private Predicate<ProfileRequestContext> delegationPredicate;
    
    /** Constructor. */
    public EvaluateDelegationPolicy() {
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        assertionTokenStrategy = new DelegatedAssertionLookupStrategy();
        policyMaxChainLengthStrategy = new PolicyMaxChainLengthStrategy();
    }
    
    /**
     * Set the strategy used to resolve the policy maximum delegation chain length.
     * 
     * @param strategy the strategy
     */
    public void setPolicyMaxChainLengthStrategy(@Nonnull final Function<ProfileRequestContext, Long> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        policyMaxChainLengthStrategy = Constraint.isNotNull(strategy, 
                "Policy max chain length strategy may not be null");
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

    /** {@inheritDoc} */
    protected boolean doPreExecute(ProfileRequestContext profileRequestContext) {
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteInbound(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteRelyingParty(profileRequestContext)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Pre-execute actions on the inbound message.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    protected boolean doPreExecuteInbound(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        assertionToken = assertionTokenStrategy.apply(profileRequestContext);
        if (assertionToken == null) {
            log.warn("{} No valid SAML 2 Assertion available within the request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return false;
        }
        
        tokenChainLength = getTokenDelegationChainLength(assertionToken);
        
        policyMaxChainLength = getPolicyMaxDelegationChainLength(profileRequestContext);
        
        return true;
    }
    
    /**
     * Pre-execute actions on the relying party context info.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    protected boolean doPreExecuteRelyingParty(@Nonnull final ProfileRequestContext profileRequestContext) {
        RelyingPartyContext relyingPartyContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyContext == null) {
            log.warn("No RelyingPartyContext was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        if (relyingPartyContext.getProfileConfig() instanceof SSOSProfileConfiguration) {
            delegationPredicate = ((SSOSProfileConfiguration)relyingPartyContext.getProfileConfig())
                    .getDelegationPredicate();
        } else {
            log.warn("Relying party profile configuration was not SSOSProfileConfiguration");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        return true;
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!checkAllowedDelegate(profileRequestContext)) {
            return;
        }
        checkTokenDelegationChainLength(profileRequestContext);
    }
    
    /**
     * Apply policy control {@link SSOSProfileConfiguration#getDelegationPredicate()}.
     * 
     * @param profileRequestContext the current request context
     * 
     * @return true if check passes, false if not
     */
    protected boolean checkAllowedDelegate(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (!delegationPredicate.apply(profileRequestContext)) {
            log.warn("Delegation predicate eval indicates delegated token use NOT allowed");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        log.debug("Delegation predicate eval indicates delegated token use is allowed");
        return true;
    }
    
    /**
     * Apply policy control which checks the actual token chain length against
     * the policy maximum chain length.
     * 
     * @param profileRequestContext the current request context
     * 
     * @return true if check passes, false if not
     */
    protected boolean checkTokenDelegationChainLength(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (tokenChainLength == null || tokenChainLength <= 0) {
            log.debug("Token did not have delegation chain, this must be initial delegation request, check passes");
            return true;
        }
        
        log.debug("Token delegation chain length was '{}', policy max was '{}'",
                tokenChainLength, policyMaxChainLength);
        
        if (tokenChainLength < policyMaxChainLength) {
            log.debug("Token delegation chain length is OK");
            return true;
        }
        
        if (tokenChainLength > policyMaxChainLength) {
            log.warn("Presented token delegation chain length exceeds policy max, and fails acceptance");
            //TODO right event ID?
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        
        if (tokenChainLength.equals(policyMaxChainLength)) {
            log.warn("Token delegation chain length is equal to policy max, can't issue a new token from this token");
            //TODO right event ID?
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            return false;
        }
        
        return true;
    }

    /**
     * Get the length of the delegation chain in the presented token.
     * 
     * @param token the token to evaluate
     * @return the token delegation chain length
     */
    protected Long getTokenDelegationChainLength(@Nonnull final Assertion token) {
        DelegationRestrictionType delRestrict = getDelegationRestrictionCondition(token.getConditions());
        
        if (delRestrict != null && delRestrict.getDelegates() != null) {
            return (long) delRestrict.getDelegates().size();
        }
        return null;
    }
    
    /**
     * Get the DelegationRestrictionType Condition from the supplied Conditions, if present.
     * 
     * @param conditions the Assertion Conditions to process
     * @return the DelegationRestrictionType Condition object, or null if not present
     */
    protected DelegationRestrictionType getDelegationRestrictionCondition(@Nullable final Conditions conditions) {
        if (conditions == null) {
            return null;
        }
        
        for (Condition conditionChild : conditions.getConditions()) {
            if (DelegationRestrictionType.TYPE_NAME.equals(conditionChild.getSchemaType())) {
                if (conditionChild instanceof DelegationRestrictionType) {
                    return (DelegationRestrictionType) conditionChild;
                } else {
                    log.warn("Saw Condition of xsi:type DelegationRestrictionType, but incorrect class instance: {}",
                            conditionChild.getClass().getName());
                }
            }
        }
        return null;
    }

    /**
     * Get the effective maximum delegation chain length allowed by policy.
     * 
     * @param profileRequestContext the current request context
     * @return the policy max delegation chain policy length
     */
    @Nonnull protected Long getPolicyMaxDelegationChainLength(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        
        Long value = policyMaxChainLengthStrategy.apply(profileRequestContext);
        if (value != null) {
            log.debug("Strategy resolved policy max token delegation chain length: {}", value);
            return value;
        } else {
            log.debug("Returning default policy max token delegation chain length: {}", 
                    DEFAULT_POLICY_MAX_CHAIN_LENGTH);
            return DEFAULT_POLICY_MAX_CHAIN_LENGTH;
        }
        
    }
    
    /**
     * Default strategy used to resolve the policy maximum token delegation chain length.
     * 
     * <p>
     * This strategy evaluates the extension element value
     * {@link DelegationPolicy#getMaximumTokenDelegationChainLength()} present in the {@link Advice}
     * of the presented {@link Assertion} token.
     * </p>
     */
    public class PolicyMaxChainLengthStrategy implements Function<ProfileRequestContext, Long> {

        /** {@inheritDoc} */
        @Nullable
        public Long apply(@Nullable ProfileRequestContext input) {
            if (assertionToken == null || assertionToken.getAdvice() == null) {
                return null;
            }
            Advice inboundAdvice = assertionToken.getAdvice();
            List<XMLObject> inboundPolicies = inboundAdvice.getChildren(DelegationPolicy.DEFAULT_ELEMENT_NAME);
            if (inboundPolicies != null && !inboundPolicies.isEmpty()) {
                return ((DelegationPolicy)inboundPolicies.get(0)).getMaximumTokenDelegationChainLength();
            }
            return null;
        }
        
    }

}
