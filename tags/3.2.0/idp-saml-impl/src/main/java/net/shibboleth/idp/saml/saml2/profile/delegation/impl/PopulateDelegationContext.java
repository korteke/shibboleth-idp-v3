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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.profile.context.navigate.SAMLMetadataContextLookupFunction;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationContext;
import net.shibboleth.idp.saml.saml2.profile.delegation.DelegationRequest;
import net.shibboleth.utilities.java.support.annotation.Prototype;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * A profile action which determines whether issuance of a delegated 
 * {@link org.opensaml.saml.saml2.core.Assertion} token
 * is active, and populates a {@link DelegationContext} appropriately.
 * 
 * <p>
 * The output of 3 different evaluations is combined to produce the final result:
 * <ol>
 * 
 * <li>
 * Determination is made whether delegation is requested by the relying party, as a value of type 
 * {@link DelegationRequest}. Delegation may be requested via:
 * <ul>
 * <li>The inclusion of the IdP entityID as an {@link Audience} in the {@link AudienceRestriction} condition
 * of the inbound {@link AuthnRequest}.</li>
 * <li>The presence of a {@link RequestedAttribute} with name {@link LibertyConstants#SERVICE_TYPE_SSOS} in 
 * the relying party's metadata via {@link AttributeConsumingService}.
 * </ul>
 * </li>
 * 
 * <li>
 * Determination is made whether issuance of a delegated token is allowed for the relying party,
 * based on the (legacy) static value {@link BrowserSSOProfileConfiguration#isAllowingDelegation()}
 * or the predicate {@link BrowserSSOProfileConfiguration#getAllowDelegation()}.
 * </li>
 * 
 * <li>
 * Holder-of-key subject confirmation {@link Credential} instances are resolved for the relying party from 
 * its resolved metadata {@link RoleDescriptor}.
 * </li>
 * 
 * </ol>
 * </p>
 * 
 * <p>
 * If 1) delegation is allowed, 2) subject confirmation credentials were resolved, and 3) request status was either 
 * {@link DelegationRequest#REQUESTED_OPTIONAL} or {@link DelegationRequest#REQUESTED_REQUIRED}, 
 * a {@link DelegationContext} is populated indicating issuance of delegated token to be active, and containing the
 * resolved subject confirmation credentials.
 * </p>
 * 
 * <p>
 * If request status was {@link DelegationRequest#REQUESTED_REQUIRED} but delegation was not allowed and/or no
 * subject confirmation credentials could be resolved, a fatal event is produced.
 * </p>
 * 
 * <p>
 * Otherwise, issuance of a delegated token is not active and so no {@link DelegationContext} is populated.
 * </p>
 * 
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#MESSAGE_PROC_ERROR}
 * @event {@link EventIds#INVALID_SEC_CFG}
 */
@Prototype
public class PopulateDelegationContext extends AbstractProfileAction {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PopulateDelegationContext.class);
    
    // Configured data
    
    /** Strategy used to lookup the RelyingPartyContext. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Strategy used to lookup the SAMLMetadataContext. */
    @Nonnull private Function<ProfileRequestContext, SAMLMetadataContext> samlMetadataContextLookupStrategy;
    
    /** Strategy used to lookup the {@link DelegationContext. */
    @Nonnull private Function<ProfileRequestContext, DelegationContext> delegationContextLookupStrategy;
    
    /** Default delegation request value. */
    private DelegationRequest defaultDelegationRequested = DelegationRequest.NOT_REQUESTED;
    
    /** The credential resolver used to resolve HoK Credentials for the peer. */
    @Nonnull private CredentialResolver credentialResolver;
    
    
    // Runtime data
    
    /** The delegation requested state for the current request. */
    private DelegationRequest delegationRequested;
    
    /** The current RelyingPartyContext. */
    private RelyingPartyContext relyingPartyContext;
    
    /** Whether delegation is allowed for the current relying party. */
    private boolean delegationAllowed;
    
    /** The entityID of the local responder entity. */
    private String responderId;
    
    /** The entityID of the SAML relying party. */
    private String relyingPartyId;
    
    /** The RoleDescriptor for the SAML peer entity. */
    private RoleDescriptor roleDescriptor;
    
    /** The AttributeConsumingService for the SAML peer entity. */
    private AttributeConsumingService attributeConsumingService;
    
    /** The subject confirmation credentials. */
    private List<Credential> confirmationCredentials;
    
    
    /**
     * Constructor.
     */
    public PopulateDelegationContext() {
        super();
        
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        samlMetadataContextLookupStrategy = new SAMLMetadataContextLookupFunction();
        delegationContextLookupStrategy = new ChildContextLookup<>(DelegationContext.class, true);
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
     * Set the strategy used to locate the current {@link SAMLMetadataContext}.
     * 
     * @param strategy strategy used to locate the current {@link SAMLMetadataContext}
     */
    public void setSAMLMetadataContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, SAMLMetadataContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        samlMetadataContextLookupStrategy = Constraint.isNotNull(strategy, 
                "SAMLMetadataContext lookup strategy may not be null");
    }
    
    /**
     * Set the strategy used to locate the current {@link DelegationContext}.
     * 
     * @param strategy strategy used to locate the current {@link DelegationContext}
     */
    public void setDelegationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, DelegationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        delegationContextLookupStrategy = Constraint.isNotNull(strategy, 
                "DelegationContext lookup strategy may not be null");
    }
    
    /**
     * Set the {@link CredentialResolver} instance to use to resolve HoK {@link Credential}.
     * 
     * <p>
     * Typically this should be a metadata-based resolver which accepts input as the 
     * peer's {@link RoleDescriptor}.
     * </p>
     * 
     * @param resolver the resolver instance to use
     */
    public void setCredentialResolver(@Nonnull final CredentialResolver resolver) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        credentialResolver = Constraint.isNotNull(resolver, "CredentialResolver may not be null");
    }
    
    /**
     * Get the effective default value for whether request processing should proceed 
     * with issuance of a delegation token.
     * 
     * @return the default value
     */
    @Nonnull public DelegationRequest getDefaultDelegationRequested() {
        return defaultDelegationRequested;
    }
    
    /**
     * Set the effective default value for whether request processing should proceed 
     * with issuance of a delegation token.
     * 
     * @param delegationRequest the default delegation requested value
     */
    public void setDefaultDelegationRequested(@Nonnull final DelegationRequest delegationRequest) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        defaultDelegationRequested = 
                Constraint.isNotNull(delegationRequest, "Default DelegationRequest may not be null");
    }
    
    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (relyingPartyContextLookupStrategy == null) {
            throw new ComponentInitializationException("RelyingPartyContext lookup strategy may not be null");
        }
        if (samlMetadataContextLookupStrategy == null) {
            throw new ComponentInitializationException("SAMLMetadataContext lookup strategy may not be null");
        }
        if (credentialResolver == null) {
            throw new ComponentInitializationException("CredentialResolver may not be null");
        }
    }
    
    /** {@inheritDoc} */
    // Checkstyle: ReturnCount OFF -- already heavily refactored for return count and complexity
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        // Eval the activation condition first.  Don't bother with the rest if false, esp since
        // could terminate with a fatal error unnecessarily.
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteInbound(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteRelyingParty(profileRequestContext)) {
            return false;
        }
        
        if (!doPreExecuteMetadata(profileRequestContext)) {
            return false;
        }
        
        delegationRequested = getDelegationRequested(profileRequestContext);
        if (DelegationRequest.NOT_REQUESTED.equals(delegationRequested)) {
            log.debug("Issuance of a delegated Assertion is not in effect, skipping further processing");
            return false;
        }
        
        confirmationCredentials = resolveConfirmationCredentials(profileRequestContext);
        
        return true;
    }
    // Checkstyle: ReturnCount ON
    
    /**
     * Pre-execute actions on the inbound message.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    protected boolean doPreExecuteInbound(@Nonnull final ProfileRequestContext profileRequestContext) {
        if (profileRequestContext.getInboundMessageContext() == null 
                || profileRequestContext.getInboundMessageContext().getMessage() == null) {
            log.warn("No inbound message context or message found");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            return false;
        }
        
        if (!(profileRequestContext.getInboundMessageContext().getMessage() instanceof AuthnRequest)) {
            log.debug("Request is not a SAML 2 AuthnRequest");
            return false;
        }
        return true;
    }
    
    /**
     * Pre-execute actions on the relying party context info.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed
     */
    @SuppressWarnings("deprecation")
    protected boolean doPreExecuteRelyingParty(@Nonnull final ProfileRequestContext profileRequestContext) {
        relyingPartyContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (relyingPartyContext == null) {
            log.warn("No RelyingPartyContext was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        relyingPartyId = relyingPartyContext.getRelyingPartyId();
        if (relyingPartyId == null) {
            log.warn("No relying party ID was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false; 
        }
        
        if (relyingPartyContext.getProfileConfig() instanceof BrowserSSOProfileConfiguration) {
            BrowserSSOProfileConfiguration config = 
                    (BrowserSSOProfileConfiguration) relyingPartyContext.getProfileConfig();
            if (config.getAllowingDelegation() != null) {
                delegationAllowed = config.getAllowingDelegation();
            } else if (config.getAllowDelegation() != null) {
                delegationAllowed = config.getAllowDelegation().apply(profileRequestContext);
            }
        } else {
            log.debug("ProfileConfiguration does not support delegation: {}", 
                    relyingPartyContext.getProfileConfig().getClass().getName());
            return false; 
        }
        
        // This is @Nonnull
        responderId = relyingPartyContext.getConfiguration().getResponderId();
        
        return true;
    }
    
    /**
     * Pre-execute actions on the relying party metadata.
     * 
     * @param profileRequestContext the current profile request context
     * @return true iff {@link #doExecute(ProfileRequestContext)} should proceed, false otherwise
     */
    protected boolean doPreExecuteMetadata(@Nonnull final ProfileRequestContext profileRequestContext) {
        SAMLMetadataContext samlMetadataContext = samlMetadataContextLookupStrategy.apply(profileRequestContext);
        if (samlMetadataContext == null) {
            log.debug("No SAMLMetadataContext was available, skipping further delegation processing");
            return false;
        }
        
        roleDescriptor = samlMetadataContext.getRoleDescriptor();
        if (roleDescriptor == null) {
            log.debug("No RoleDescriptor was available, skipping further delegation processing");
            return false;
        }
        
        AttributeConsumingServiceContext acsContext = 
                samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class);
        if (acsContext != null) {
            attributeConsumingService = acsContext.getAttributeConsumingService();
        }
        if (attributeConsumingService == null) {
            log.debug("No AttributeConsumingService was resolved, won't be able to determine " 
                    + "delegation requested status via metadata");
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        switch (delegationRequested) {
            case NOT_REQUESTED:
                log.debug("Delegation was not requested");
                break;
                
            case REQUESTED_OPTIONAL:
                if (delegationAllowed) {
                    log.debug("Delegation token issuance was requested (optional) and allowed");
                    if (confirmationCredentials == null || confirmationCredentials.isEmpty()) {
                        log.warn("Issuance of delegated token was indicated, " 
                                + "but no confirmation credentials were available, skipping issuance");
                    } else {
                        createAndPopulateDelegationContext(profileRequestContext);
                    }
                } else {
                    log.debug("Delegation token issuance was requested (optional), but not allowed, " 
                            + "skipping delegated assertion issuance");
                    return;
                }
                break;
                
            case REQUESTED_REQUIRED:
                if (delegationAllowed) {
                    log.debug("Delegation token issuance was requested (required) and allowed");
                    if (confirmationCredentials == null || confirmationCredentials.isEmpty()) {
                        log.warn("Issuance of delegated token was indicated, " 
                                + "but no confirmation credentials were available");
                        ActionSupport.buildEvent(profileRequestContext, EventIds.MESSAGE_PROC_ERROR);
                    } else {
                        createAndPopulateDelegationContext(profileRequestContext);
                    }
                } else {
                    log.warn("Delegation token issuance was requested (required), but disallowed by policy");
                    ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
                }
                break;
                
            default:
                log.error("Unknown value '{}' for delegation request state", delegationRequested);
        }
    }

    /**
     * Create and populate the {@link DelegationContext} using the available information.
     * 
     * @param profileRequestContext the current request context
     */
    private void createAndPopulateDelegationContext(final ProfileRequestContext profileRequestContext) {
        DelegationContext delegationContext = 
                delegationContextLookupStrategy.apply(profileRequestContext);
        if (delegationContext == null) {
            log.warn("No DelegationContext was available");
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
        }
        delegationContext.setIssuingDelegatedAssertion(true);
        delegationContext.setDelegationRequested(delegationRequested);
        delegationContext.setSubjectConfirmationCredentials(confirmationCredentials);
    }
    
    /**
     * Resolve the subject confirmation credentials.
     * 
     * @param requestContext the current request context
     * @return the subject confirmation credentials, or null if not resolveable or there is an error
     */
    private List<Credential> resolveConfirmationCredentials(@Nonnull final ProfileRequestContext requestContext) {
        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new RoleDescriptorCriterion(roleDescriptor));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
        // Add an entityID criterion just in case don't have a MetadataCredentialResolver,
        // and want to resolve via entityID + usage only, e.g. from a CollectionCredentialResolver
        // or other more general resolver type.
        criteriaSet.add(new EntityIdCriterion(relyingPartyId));
        
        ArrayList<Credential> creds = new ArrayList<>();
        try {
            for (Credential cred : credentialResolver.resolve(criteriaSet)) {
                if (cred != null) {
                    creds.add(cred);
                }
            }
            return creds;
        } catch (ResolverException e) {
            log.warn("Error resolving subject confirmation credentials for relying party: {}", relyingPartyId, e);
            return null;
        }
    }
    
    /**
     * Check whether issuance of a delegated token has been requested.
     * 
     * @param requestContext the current request context
     * @return true if delegation is requested, false otherwise
     */
    private DelegationRequest getDelegationRequested(@Nonnull final ProfileRequestContext requestContext) {
        if (isDelegationRequestedByAudience(requestContext)) {
            log.debug("Delegation was requested via AuthnRequest Audience, treating as: {}", 
                    DelegationRequest.REQUESTED_REQUIRED);
            return DelegationRequest.REQUESTED_REQUIRED;
        }
        
        DelegationRequest requestedByMetadata = getDelegationRequestedByMetadata(requestContext);
        if (requestedByMetadata != DelegationRequest.NOT_REQUESTED) {
            log.debug("Delegation was requested via metadata: {}", requestedByMetadata);
            return requestedByMetadata;
        }
        
        log.debug("Delegation request was not explicitly indicated, using default value: {}", 
                getDefaultDelegationRequested());
        return getDefaultDelegationRequested();
    }
    
    /**
     * Determine whether a delegation token was requested via the SP's SPSSODescriptor AttributeConsumingService.
     * 
     * @param requestContext the current request context
     * @return DelegationRequest enum value as appropriate
     */
    @Nonnull private DelegationRequest getDelegationRequestedByMetadata(
            @Nonnull final ProfileRequestContext requestContext) {
        
        if (attributeConsumingService == null) {
            log.debug("No AttributeConsumingService was available");
            return DelegationRequest.NOT_REQUESTED;
        }
        
        for (RequestedAttribute requestedAttribute : attributeConsumingService.getRequestAttributes()) {
            if (Objects.equals(LibertyConstants.SERVICE_TYPE_SSOS, 
                    StringSupport.trimOrNull(requestedAttribute.getName()))) {
                log.debug("Saw requested attribute '{}' in metadata AttributeConsumingService for SP: {}",
                        LibertyConstants.SERVICE_TYPE_SSOS, relyingPartyId);
                if (requestedAttribute.isRequired()) {
                    log.debug("Metadata delegation request attribute indicated it was required");
                    return DelegationRequest.REQUESTED_REQUIRED;
                } else {
                    log.debug("Metadata delegation request attribute indicated it was NOT required");
                    return DelegationRequest.REQUESTED_OPTIONAL;
                }
            }
        }
        
        return DelegationRequest.NOT_REQUESTED;
    }

    /**
     * Determine whether a delegation token was requested via the inbound AuthnRequest's
     * Conditions' AudienceRestriction.
     * 
     * @param requestContext the current request context
     * @return true if the AudienceRestrictions condition contained the local entity Id, false otherwise
     */
    private boolean isDelegationRequestedByAudience(@Nonnull final ProfileRequestContext requestContext) {
        if (!(requestContext.getInboundMessageContext().getMessage() instanceof AuthnRequest)) {
            log.debug("Inbound SAML message was not an AuthnRequest: {}", 
                    requestContext.getInboundMessageContext().getMessage().getClass().getName());
            return false;
        }
        
        AuthnRequest authnRequest = (AuthnRequest) requestContext.getInboundMessageContext().getMessage();
        if (authnRequest.getConditions() != null) {
            Conditions conditions = authnRequest.getConditions();
            for (AudienceRestriction ar : conditions.getAudienceRestrictions()) {
                for (Audience audience : ar.getAudiences()) {
                    String audienceValue = StringSupport.trimOrNull(audience.getAudienceURI());
                    if (Objects.equals(audienceValue, responderId)) {
                        log.debug("Saw an AuthnRequest/Conditions/AudienceRestriction/Audience with value of '{}'",
                                responderId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
