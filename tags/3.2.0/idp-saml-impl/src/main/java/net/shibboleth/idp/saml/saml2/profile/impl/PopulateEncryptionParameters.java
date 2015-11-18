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

package net.shibboleth.idp.saml.saml2.profile.impl;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.profile.context.navigate.OutboundMessageContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.profile.context.EncryptionContext;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.EncryptionParameters;
import org.opensaml.xmlsec.EncryptionParametersResolver;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.criterion.EncryptionConfigurationCriterion;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.saml2.profile.config.SAML2ProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.MessageLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Action that resolves and populates {@link EncryptionParameters} on an {@link EncryptionContext}
 * created/accessed via a lookup function, by default on a {@link RelyingPartyContext} child of the
 * profile request context.
 * 
 * <p>The resolution process is contingent on the active profile configuration requesting encryption
 * of some kind, and an {@link EncryptionContext} is also created to capture these requirements.</p>
 * 
 * <p>The OpenSAML default, per-RelyingParty, and default per-profile {@link EncryptionConfiguration}
 * objects are input to the resolution process, along with the relying party's SAML metadata, which in
 * most cases will be the source of the eventual encryption key.</p>
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#INVALID_SEC_CFG}
 * @event {@link IdPEventIds#INVALID_RELYING_PARTY_CTX}
 * @event {@link IdPEventIds#INVALID_PROFILE_CONFIG}
 */
public class PopulateEncryptionParameters extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateEncryptionParameters.class);

    /** Strategy used to locate the {@link AuthnRequest} to operate on, if any. */
    @Nonnull private Function<ProfileRequestContext,AuthnRequest> requestLookupStrategy;
    
    /** Strategy used to look up a {@link RelyingPartyContext} for configuration options. */
    @Nonnull private Function<ProfileRequestContext,RelyingPartyContext> relyingPartyContextLookupStrategy;
    
    /** Strategy used to look up the {@link EncryptionContext} to store parameters in. */
    @Nonnull private Function<ProfileRequestContext,EncryptionContext> encryptionContextLookupStrategy;

    /** Strategy used to look up a SAML peer context. */
    @Nullable private Function<ProfileRequestContext,SAMLPeerEntityContext> peerContextLookupStrategy;
    
    /** Metadata protocolSupportEnumeration value to provide to resolver. */
    @Nullable private String samlProtocol;

    /** Metadata role type to provide to resolver. */
    @Nullable private QName peerRole;
    
    /** Strategy used to look up a per-request {@link EncryptionConfiguration} list. */
    @NonnullAfterInit private Function<ProfileRequestContext,List<EncryptionConfiguration>> configurationLookupStrategy;
    
    /** Resolver for parameters to store into context. */
    @NonnullAfterInit private EncryptionParametersResolver encParamsresolver;
    
    /** Active configurations to feed into resolver. */
    @Nullable @NonnullElements private List<EncryptionConfiguration> encryptionConfigurations;
    
    /** Is encryption optional in the case no parameters can be resolved? */
    private boolean encryptionOptional;
    
    /** Flag tracking whether assertion encryption is required. */
    private boolean encryptAssertions;

    /** Flag tracking whether assertion encryption is required. */
    private boolean encryptIdentifiers;

    /** Flag tracking whether assertion encryption is required. */
    private boolean encryptAttributes;

    /** Constructor. */
    public PopulateEncryptionParameters() {
        
        requestLookupStrategy =
                Functions.compose(new MessageLookup<>(AuthnRequest.class), new InboundMessageContextLookup());
        
        relyingPartyContextLookupStrategy = new ChildContextLookup<>(RelyingPartyContext.class);
        
        // Create context by default.
        encryptionContextLookupStrategy = Functions.compose(
                new ChildContextLookup<>(EncryptionContext.class, true),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));

        // Default: outbound msg context -> SAMLPeerEntityContext
        peerContextLookupStrategy =
                Functions.compose(new ChildContextLookup<>(SAMLPeerEntityContext.class),
                        new OutboundMessageContextLookup());
    }
    
    /**
     * Set the strategy used to locate the {@link AuthnRequest} to examine, if any.
     * 
     * @param strategy strategy used to locate the {@link AuthnRequest}
     */
    public void setRequestLookupStrategy(@Nonnull final Function<ProfileRequestContext,AuthnRequest> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        requestLookupStrategy = Constraint.isNotNull(strategy, "AuthnRequest lookup strategy cannot be null");
    }
    
    /**
     * Set the strategy used to return the {@link RelyingPartyContext} for configuration options.
     * 
     * @param strategy lookup strategy
     */
    public void setRelyingPartyContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,RelyingPartyContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        relyingPartyContextLookupStrategy =
                Constraint.isNotNull(strategy, "RelyingPartyContext lookup strategy cannot be null");
    }
    

    /**
     * Set the strategy used to look up the {@link EncryptionContext} to set the flags for.
     * 
     * @param strategy lookup strategy
     */
    public void setEncryptionContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,EncryptionContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        encryptionContextLookupStrategy = Constraint.isNotNull(strategy,
                "EncryptionContext lookup strategy cannot be null");
    }

    /**
     * Set the protocol constant to use during resolution.
     * 
     * @param protocol the protocol constant to set
     */
    public void setProtocol(@Nullable final String protocol) {
        samlProtocol = StringSupport.trimOrNull(protocol);
    }

    /**
     * Set the operational role to use during resolution.
     * 
     * @param role the operational role to set
     */
    public void setRole(@Nullable final QName role) {
        peerRole = role;
    }
    
    /**
     * Set the strategy used to look up a per-request {@link EncryptionConfiguration} list.
     * 
     * @param strategy lookup strategy
     */
    public void setConfigurationLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,List<EncryptionConfiguration>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        configurationLookupStrategy = Constraint.isNotNull(strategy,
                "EncryptionConfiguration lookup strategy cannot be null");
    }

    /**
     * Set lookup strategy for {@link SAMLPeerEntityContext} for input to resolution.
     * 
     * @param strategy  lookup strategy
     */
    public void setPeerContextLookupStrategy(
            @Nullable final Function<ProfileRequestContext,SAMLPeerEntityContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        peerContextLookupStrategy = strategy;
    }
    
    /**
     * Set the encParamsresolver to use for the parameters to store into the context.
     * 
     * @param newResolver   encParamsresolver to use
     */
    public void setEncryptionParametersResolver(
            @Nonnull final EncryptionParametersResolver newResolver) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        encParamsresolver = Constraint.isNotNull(newResolver, "EncryptionParametersResolver cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (encParamsresolver == null) {
            throw new ComponentInitializationException("EncryptionParametersResolver cannot be null");
        } else if (configurationLookupStrategy == null) {
            configurationLookupStrategy = new Function<ProfileRequestContext,List<EncryptionConfiguration>>() {
                public List<EncryptionConfiguration> apply(ProfileRequestContext input) {
                    return Collections.singletonList(SecurityConfigurationSupport.getGlobalEncryptionConfiguration());
                }
            };
        }
    }
    
// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        final RelyingPartyContext rpContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
        if (rpContext == null) {
            log.debug("{} Unable to locate RelyingPartyContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_RELYING_PARTY_CTX);
            return false;
        } else if (rpContext.getProfileConfig() == null) {
            log.debug("{} Unable to locate RelyingPartyContext", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, IdPEventIds.INVALID_PROFILE_CONFIG);
            return false;
        } else if (!(rpContext.getProfileConfig() instanceof SAML2ProfileConfiguration)) {
            log.debug("{} Not a SAML 2 profile configuration, nothing to do", getLogPrefix());
            return false;
        }
        
        final SAML2ProfileConfiguration profileConfiguration = (SAML2ProfileConfiguration) rpContext.getProfileConfig();
        
        final AuthnRequest request = requestLookupStrategy.apply(profileRequestContext);
        if (request != null && request.getNameIDPolicy() != null) {
            final String requestedFormat = request.getNameIDPolicy().getFormat();
            if (requestedFormat != null && NameID.ENCRYPTED.equals(requestedFormat)) {
                log.debug("{} Request asked for encrypted identifier, disregarding installed predicate");
                encryptIdentifiers = true;
            }
        }

        if (!encryptIdentifiers) {
            encryptIdentifiers = profileConfiguration.getEncryptNameIDs().apply(profileRequestContext);
            // Encryption can only be optional if the request didn't specify it above.
            encryptionOptional = profileConfiguration.isEncryptionOptional();
        }
        
        encryptAssertions = profileConfiguration.getEncryptAssertions().apply(profileRequestContext);
        encryptAttributes = profileConfiguration.getEncryptAttributes().apply(profileRequestContext);
        
        if (!encryptAssertions && !encryptIdentifiers && !encryptAttributes) {
            log.debug("{} No encryption requested, nothing to do", getLogPrefix());
            return false;
        }

        encryptionConfigurations = configurationLookupStrategy.apply(profileRequestContext);
        
        log.debug("{} Encryption for assertions ({}), identifiers ({}), attributes({})", getLogPrefix(),
                encryptAssertions, encryptIdentifiers, encryptAttributes);
        
        return super.doPreExecute(profileRequestContext);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        log.debug("{} Resolving EncryptionParameters for request", getLogPrefix());
        
        final EncryptionContext encryptCtx = encryptionContextLookupStrategy.apply(profileRequestContext);
        if (encryptCtx == null) {
            log.debug("{} No EncryptionContext returned by lookup strategy", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }
        
        try {
            if (encryptionConfigurations == null || encryptionConfigurations.isEmpty()) {
                throw new ResolverException("No EncryptionConfigurations returned by lookup strategy");
            }
            
            final EncryptionParameters params =
                    encParamsresolver.resolveSingle(buildCriteriaSet(profileRequestContext));
            log.debug("{} {} EncryptionParameters", getLogPrefix(),
                    params != null ? "Resolved" : "Failed to resolve");
            if (params != null) {
                if (encryptAssertions) {
                    encryptCtx.setAssertionEncryptionParameters(params);
                }
                if (encryptIdentifiers) {
                    encryptCtx.setIdentifierEncryptionParameters(params);
                }
                if (encryptAttributes) {
                    encryptCtx.setAttributeEncryptionParameters(params);
                }
            } else {
                log.warn("{} Resolver returned no EncryptionParameters", getLogPrefix());
                if (encryptionOptional) {
                    log.info("{} Encryption is optional, ignoring inability to encrypt", getLogPrefix());
                } else {
                    ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
                }
            }
        } catch (final ResolverException e) {
            log.error("{} Error resolving EncryptionParameters", getLogPrefix(), e);
            if (encryptionOptional) {
                log.info("{} Encryption is optional, ignoring inability to encrypt", getLogPrefix());
            } else {
                ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_SEC_CFG);
            }
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
    /**
     * Build the criteria used as input to the {@link EncryptionParametersResolver}.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return  the criteria set to use
     */
    @Nonnull private CriteriaSet buildCriteriaSet(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        final CriteriaSet criteria = new CriteriaSet(new EncryptionConfigurationCriterion(encryptionConfigurations));

        if (peerContextLookupStrategy != null) {
            final SAMLPeerEntityContext peerCtx = peerContextLookupStrategy.apply(profileRequestContext);
            if (peerCtx != null) {
                if (peerCtx.getEntityId() != null) {
                    log.debug("{} Adding entityID to resolution criteria", getLogPrefix());
                    criteria.add(new EntityIdCriterion(peerCtx.getEntityId()));
                    if (samlProtocol != null) {
                        criteria.add(new ProtocolCriterion(samlProtocol));
                    }
                    if (peerRole != null) {
                        criteria.add(new EntityRoleCriterion(peerRole));
                    }
                }
                final SAMLMetadataContext metadataCtx = peerCtx.getSubcontext(SAMLMetadataContext.class);
                if (metadataCtx != null && metadataCtx.getRoleDescriptor() != null) {
                    log.debug("{} Adding role metadata to resolution criteria", getLogPrefix());
                    criteria.add(new RoleDescriptorCriterion(metadataCtx.getRoleDescriptor()));
                }
            }
        }
        
        return criteria;
    }
    
}