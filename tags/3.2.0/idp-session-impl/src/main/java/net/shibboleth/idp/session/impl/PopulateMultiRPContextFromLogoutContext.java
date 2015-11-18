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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.MultiRelyingPartyContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.idp.session.context.LogoutContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Profile action that populates a {@link MultiRelyingPartyContext} with the relying party
 * information from a {@link LogoutContext}, and extends each {@link RelyingPartyContext}
 * created with a {@link SAMLMetadataContext} based on metadata lookup.
 * 
 * <p>Any existing {@link MultiRelyingPartyContext} will be replaced.</p>
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post If (ProfileRequestContext.getSubcontext(LogoutContext.class) != null,
 *  then ProfileRequestContext.getSubcontext(MultiRelyingPartyContext.class) != null
 */
public class PopulateMultiRPContextFromLogoutContext extends AbstractProfileAction {
    
    /** Label for {@link MultiRelyingPartyContext} entries. */
    @Nonnull @NotEmpty private static final String LABEL = "logout";
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateMultiRPContextFromLogoutContext.class);
    
    /** Resolver used to look up SAML metadata. */
    @NonnullAfterInit private RoleDescriptorResolver metadataResolver;
    
    /** Lookup function for {@link LogoutContext}. */
    @Nonnull private Function<ProfileRequestContext,LogoutContext> logoutContextLookupStrategy;
    
    /** Map of {@link SPSession} subtypes to SAML metadata protocol constants. */
    @Nonnull private Map<Class<? extends SPSession>,String> sessionTypeProtocolMap;
    
    /** Role to resolve metadata for. */
    @NonnullAfterInit private QName role; 
    
    /** {@link LogoutContext} to process. */
    @Nullable private LogoutContext logoutCtx;
    
    /** Constructor. */
    public PopulateMultiRPContextFromLogoutContext() {
        logoutContextLookupStrategy = new ChildContextLookup<>(LogoutContext.class);
        sessionTypeProtocolMap = Collections.emptyMap();
        role = SPSSODescriptor.DEFAULT_ELEMENT_NAME;
    }

    /**
     * Set the {@link RoleDescriptorResolver} to use.
     * 
     * @param resolver  the resolver to use
     */
    public void setRoleDescriptorResolver(@Nonnull final RoleDescriptorResolver resolver) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        metadataResolver = Constraint.isNotNull(resolver, "RoleDescriptorResolver cannot be null");
    }

    /**
     * Set the lookup strategy for the LogoutContext to process.
     * 
     * @param strategy  lookup strategy
     */
    public void setLogoutContextLookupStrategy(@Nonnull final Function<ProfileRequestContext,LogoutContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        logoutContextLookupStrategy = Constraint.isNotNull(strategy, "LogoutContext lookup strategy cannot be null");
    }
    
    /**
     * Set the map of {@link SPSession} subtypes to SAML metadata protocol constants.
     * 
     * @param map map to set
     */
    public void setSessionTypeProtocolMap(@Nonnull final Map<Class<? extends SPSession>,String> map) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(map, "Session type to protocol map cannot be null");
        
        sessionTypeProtocolMap = new HashMap<>(map.size());
        
        for (final Map.Entry<Class<? extends SPSession>,String> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                final String trimmed = StringSupport.trimOrNull(entry.getValue());
                if (trimmed != null) {
                    sessionTypeProtocolMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    /**
     * Set the metadata role to lookup.
     * 
     * <p>Defaults to {@link SPSSODescriptor#DEFAULT_ELEMENT_NAME}.</p>
     * 
     * @param theRole the role element or type
     */
    public void setRole(@Nonnull final QName theRole) {
        role = Constraint.isNotNull(theRole, "Role cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (metadataResolver == null) {
            throw new ComponentInitializationException("RoleDescriptorResolver cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        logoutCtx = logoutContextLookupStrategy.apply(profileRequestContext);
        if (logoutCtx == null) {
            log.debug("{} No LogoutContext found, nothing to do", getLogPrefix());
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        final MultiRelyingPartyContext multiCtx = new MultiRelyingPartyContext();
        profileRequestContext.addSubcontext(multiCtx, true);
        
        for (final String relyingPartyId : logoutCtx.getSessionMap().keySet()) {
            final RelyingPartyContext rpCtx = new RelyingPartyContext();
            rpCtx.setRelyingPartyId(relyingPartyId);
            multiCtx.addRelyingPartyContext(LABEL, rpCtx);
            
            final EntityIdCriterion entityIdCriterion = new EntityIdCriterion(rpCtx.getRelyingPartyId());
            final EntityRoleCriterion roleCriterion = new EntityRoleCriterion(role);
            
            ProtocolCriterion protocolCriterion = null;
            final SPSession spSession = logoutCtx.getSessions(relyingPartyId).iterator().next();
            final String protocol = sessionTypeProtocolMap.get(spSession.getClass());
            if (protocol != null) {
                protocolCriterion = new ProtocolCriterion(protocol);
            }
            
            final CriteriaSet criteria = new CriteriaSet(entityIdCriterion, protocolCriterion, roleCriterion);
            try {
                final RoleDescriptor roleMetadata = metadataResolver.resolveSingle(criteria);
                if (roleMetadata == null) {
                    if (protocolCriterion != null) {
                        log.info("{} No metadata returned for {} in role {} with protocol {}",
                                new Object[]{getLogPrefix(), entityIdCriterion.getEntityId(), role, protocol,});
                    } else {
                        log.info("{} No metadata returned for {} in role {}",
                                new Object[]{getLogPrefix(), entityIdCriterion.getEntityId(), role,});
                    }
                    continue;
                }

                final SAMLMetadataContext metadataCtx = rpCtx.getSubcontext(SAMLMetadataContext.class, true);
                metadataCtx.setEntityDescriptor((EntityDescriptor) roleMetadata.getParent());
                metadataCtx.setRoleDescriptor(roleMetadata);

                log.debug("{} SAMLMetadataContext added to RelyingPartyContext for {}", getLogPrefix(),
                        rpCtx.getRelyingPartyId());
            } catch (final ResolverException e) {
                log.error("{} ResolverException thrown during metadata lookup", getLogPrefix(), e);
            }
        }
    }
    
}