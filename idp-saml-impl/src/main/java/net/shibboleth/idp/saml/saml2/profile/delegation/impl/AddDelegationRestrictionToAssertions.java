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
import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.profile.context.navigate.OutboundMessageContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLPresenterEntityContext;
import org.opensaml.saml.ext.saml2delrestrict.Delegate;
import org.opensaml.saml.ext.saml2delrestrict.DelegationRestrictionType;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.profile.SAML2ActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Action which adds a {@link DelegationRestrictionType} {@link Condition} to each {@link Assertion}
 * contained within the outbound {@link Response}.
 * 
 * <p>If the inbound assertion token specified in {@link LibertySSOSContext} contains an existing 
 * {@link DelegationRestrictionType} condition, it is cloned, and the current SAML presenter entityID 
 * is added as a new {@link Delegate}. Otherwise a new instance of {@link DelegationRestrictionType} 
 * is created and a single new {@link Delegate} added.
 * </p>
 * 
 * <p>In both cases the new delegate entityID is obtained from the {@link SAMLPresenterEntityContext} located
 * using the corresponding lookup function.  The new delegate is augmented with the SAML subject confirmation method
 * obtained from the current {@link LibertySSOSContext}.
 * </p>
 * 
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#MESSAGE_PROC_ERROR}
 */
public class AddDelegationRestrictionToAssertions extends AbstractProfileAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AddDelegationRestrictionToAssertions.class);
    
    /** Strategy used to locate the Response to operate on. */
    @Nonnull private Function<ProfileRequestContext,Response> responseLookupStrategy;
    
    /** Strategy used to locate the SAMLPresenterEntityContext. */
    @Nonnull private Function<ProfileRequestContext,SAMLPresenterEntityContext> presenterContextLookupStrategy;
    
    /** Function used to resolve the Liberty context to populate. */
    @Nonnull private Function<ProfileRequestContext, LibertySSOSContext> libertyContextLookupStrategy;
    
    /** List of assertions to modify. */
    @Nullable private List<Assertion> assertions;
    
    /** The delegated Assertion that was attested. */
    @Nullable private Assertion attestedAssertion;
    
    /** The subject confirmation method successfully used to confirm the assertion by the presenter. */
    @Nullable private String attestedSubjectConfirmationMethod;
    
    /** The presenting entity which successfully attested the Assertion token. */
    @Nullable private String presenterEntityID;
    
    /** The instant of delegation. */
    @Nullable private DateTime delegationInstant;
    
    /**
     * Constructor.
     */
    public AddDelegationRestrictionToAssertions() {
        responseLookupStrategy =
                Functions.compose(new MessageLookup<>(Response.class), new OutboundMessageContextLookup());
        presenterContextLookupStrategy =
                Functions.compose(new ChildContextLookup<>(SAMLPresenterEntityContext.class), 
                        new InboundMessageContextLookup());
        
        libertyContextLookupStrategy = new ChildContextLookup<>(LibertySSOSContext.class);
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
     * Set the strategy used to locate the Response to operate on.
     * 
     * @param strategy lookup strategy
     */
    public void setResponseLookupStrategy(@Nonnull final Function<ProfileRequestContext,Response> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        responseLookupStrategy = Constraint.isNotNull(strategy, "Response lookup strategy cannot be null");
    }
    
    /**
     * Set the strategy used to locate the {@link SAMLPresenterEntityContext}.
     * 
     * @param strategy lookup strategy
     */
    public void setPresenterLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLPresenterEntityContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        presenterContextLookupStrategy = Constraint.isNotNull(strategy, "Response lookup strategy cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        Response response = responseLookupStrategy.apply(profileRequestContext);
        if (response == null) {
            log.debug("{} No SAML Response located in current profile request context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return false;
        }
        
        assertions = response.getAssertions();
        if (assertions.isEmpty()) {
            log.debug("{} No assertions to modify", getLogPrefix());
            return false;
        }
        
        log.debug("{} Attempting to add a DelegationRestrictionType Condition to {} Assertion(s) in Response",
                getLogPrefix(), assertions.size());
        
        SAMLPresenterEntityContext presenterContext = presenterContextLookupStrategy.apply(profileRequestContext);
        if (presenterContext == null || presenterContext.getEntityId() == null) {
            log.debug("{} No SAML presenter entityID", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        presenterEntityID = presenterContext.getEntityId();
        
        LibertySSOSContext libertyContext = libertyContextLookupStrategy.apply(profileRequestContext);
        if (libertyContext == null) {
            log.debug("{} No LibertySSOSContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        attestedAssertion = libertyContext.getAttestedToken();
        if (attestedAssertion == null) {
            log.debug("{} No attested SAML 2 Assertion", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        attestedSubjectConfirmationMethod = libertyContext.getAttestedSubjectConfirmationMethod();
        if (attestedSubjectConfirmationMethod == null) {
            log.debug("{} No attested SAML 2 SubjectConfirmation method", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        //TODO do we have a single harmonized "issue instant" for the outbound request data?
        delegationInstant = new DateTime();
        
        return super.doPreExecute(profileRequestContext);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        for (final Assertion assertion : assertions) {
            addDelegationRestriction(profileRequestContext,
                    SAML2ActionSupport.addConditionsToAssertion(this, assertion));
            log.debug("{} Added DelegationRestrictionType Condition to Assertion {}", 
                    getLogPrefix(), assertion.getID());
        }
    }

    /**
     * Add a delegation restriction condition to the specified conditions.
     * 
     * @param profileRequestContext the current profile request context
     * @param conditions the conditions instance to modify
     */
    protected void addDelegationRestriction(@Nonnull final ProfileRequestContext profileRequestContext, 
            @Nonnull final Conditions conditions) {
        DelegationRestrictionType drt = buildDelegationRestriction(profileRequestContext);
        if (drt != null) {
            conditions.getConditions().add(drt);
        } else {
            log.error("{} Unable to build DelegationRestriction Condition", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.MESSAGE_PROC_ERROR);
        }
    }
    
    /**
     * Using the existing attested Assertion from the presenter as a context, build the 
     * appropriate DelegationRestrictionType Condition.
     * 
     * @param profileRequestContext the current profile request context
     * 
     * @return new DelegationRestrictionType Condition, or null if the condition could not be build
     */
    @Nullable protected DelegationRestrictionType buildDelegationRestriction(
            @Nonnull final ProfileRequestContext profileRequestContext) {
        DelegationRestrictionType drt = null;
        
        Delegate newDelegate = buildDelegate(profileRequestContext);
        
        drt = getDelegationRestrictionCondition(attestedAssertion.getConditions());
        
        if (drt != null) {
            try {
                drt = XMLObjectSupport.cloneXMLObject(drt);
            } catch (MarshallingException | UnmarshallingException e) {
                log.error("{} Error cloning DelegationRestriction Condition", getLogPrefix(), e);
                return null;
            }
        } else {
            drt = (DelegationRestrictionType) XMLObjectSupport.getBuilder(DelegationRestrictionType.TYPE_NAME)
                    .buildObject(Condition.DEFAULT_ELEMENT_NAME, DelegationRestrictionType.TYPE_NAME);
        }
        
        drt.getDelegates().add(newDelegate);
        
        return drt;
    }
    
    /**
     * Get the DelegationRestrictionType Condition from the supplied Conditions, if present.
     * 
     * @param conditions the Assertion Conditions to process
     * @return the DelegationRestrictionType Condition object, or null if not present
     */
    @Nullable protected DelegationRestrictionType getDelegationRestrictionCondition(
            @Nullable final Conditions conditions) {
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
     * Build the Delegate child for the DelegationRestrictionType Condition,
     * based on the current request context.
     * 
     * @param profileRequestContext the 
     * 
     * @return the new Delegate instance
     */
    @Nonnull protected Delegate buildDelegate(@Nonnull final ProfileRequestContext profileRequestContext) {
        NameID delegateNameID = (NameID) XMLObjectSupport.buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        delegateNameID.setValue(presenterEntityID);
        delegateNameID.setFormat(NameID.ENTITY);
        
        Delegate newDelegate = (Delegate) XMLObjectSupport.buildXMLObject(Delegate.DEFAULT_ELEMENT_NAME);
        newDelegate.setNameID(delegateNameID);
        newDelegate.setConfirmationMethod(attestedSubjectConfirmationMethod);
        newDelegate.setDelegationInstant(delegationInstant);
        
        return newDelegate;
    }

}
