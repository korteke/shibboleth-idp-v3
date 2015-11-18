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

package net.shibboleth.idp.profile.impl;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.context.navigate.SubjectContextPrincipalLookupFunction;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Action that invokes the {@link AttributeResolver} for the current request.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#UNABLE_RESOLVE_ATTRIBS}
 * 
 * @post If resolution is successful, an AttributeContext is created with the results.
 */
public final class ResolveAttributes extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ResolveAttributes.class);

    /** Service used to get the resolver used to fetch attributes. */
    @Nonnull private final ReloadableService<AttributeResolver> attributeResolverService;

    /** Strategy used to locate the identity of the issuer associated with the attribute resolution. */
    @Nullable private Function<ProfileRequestContext,String> issuerLookupStrategy;

    /** Strategy used to locate the identity of the recipient associated with the attribute resolution. */
    @Nullable private Function<ProfileRequestContext,String> recipientLookupStrategy;
    
    /** Strategy used to locate the principal name associated with the attribute resolution. */
    @Nonnull private Function<ProfileRequestContext,String> principalNameLookupStrategy;

    /**
     * Strategy used to locate an {@link AuthenticationContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext,AuthenticationContext> authnContextLookupStrategy;

    /** Strategy used to locate or create the {@link AttributeContext} to populate. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextCreationStrategy;
    
    /** Attribute IDs to pass into resolver. */
    @Nonnull @NonnullElements private Collection<String> attributesToResolve;
    
    /** Whether to treat resolver errors as equivalent to resolving no attributes. */
    private boolean maskFailures;

    /** AuthenticationContext to work from (if any). */
    @Nullable private AuthenticationContext authenticationContext;

    /**
     * Constructor.
     * 
     * @param resolverService resolver used to fetch attributes
     */
    public ResolveAttributes(@Nonnull final ReloadableService<AttributeResolver> resolverService) {
        attributeResolverService = Constraint.isNotNull(resolverService, "AttributeResolver cannot be null");
        
        issuerLookupStrategy = new ResponderIdLookupFunction();
        recipientLookupStrategy = new RelyingPartyIdLookupFunction();
        
        principalNameLookupStrategy = Functions.compose(
                new SubjectContextPrincipalLookupFunction(),
                new ChildContextLookup<ProfileRequestContext,SubjectContext>(SubjectContext.class));
        
        authnContextLookupStrategy = new ChildContextLookup<>(AuthenticationContext.class);
        
        // Defaults to ProfileRequestContext -> RelyingPartyContext -> AttributeContext.
        attributeContextCreationStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class, true),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        
        attributesToResolve = Collections.emptyList();
        
        maskFailures = true;
    }
    
    /**
     * Set the strategy used to lookup the issuer for this attribute resolution.
     * 
     * @param strategy  lookup strategy
     */
    public void setIssuerLookupStrategy(@Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        issuerLookupStrategy = strategy;
    }

    /**
     * Set the strategy used to lookup the recipient for this attribute resolution.
     * 
     * @param strategy  lookup strategy
     */
    public void setRecipientLookupStrategy(@Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        recipientLookupStrategy = strategy;
    }
    
    /**
     * Set the strategy used to locate the principal name for this attribute resolution.
     * 
     * @param strategy lookup strategy
     */
    public void setPrincipalNameLookupStrategy(@Nonnull final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        principalNameLookupStrategy = Constraint.isNotNull(strategy, "Principal name lookup strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the {@link AuthenticationContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link AuthenticationContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setAuthenticationContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,AuthenticationContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        authnContextLookupStrategy =
                Constraint.isNotNull(strategy, "AuthenticationContext lookup strategy cannot be null");
    }
    
    /**
     * Set the strategy used to locate or create the {@link AttributeContext} to populate.
     * 
     * @param strategy lookup/creation strategy
     */
    public void setAttributeContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        attributeContextCreationStrategy =
                Constraint.isNotNull(strategy, "AttributeContext creation strategy cannot be null");
    }
    
    /**
     * Set the attribute IDs to pass into the resolver.
     * 
     * @param attributeIds  attribute ID collection
     */
    public void setAttributesToResolve(@Nonnull @NonnullElements final Collection<String> attributeIds) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        Constraint.isNotNull(attributeIds, "Attribute ID collection cannot be null");
        attributesToResolve = StringSupport.normalizeStringCollection(attributeIds);
    }
    
    /**
     * Set whether to treat resolution failure as equivalent to resolving no attributes.
     * 
     * <p>This matches the behavior of V2.</p>
     * 
     * @param flag flag to set
     */
    public void setMaskFailures(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        maskFailures = flag;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        }
        
        authenticationContext = authnContextLookupStrategy.apply(profileRequestContext);
        if (authenticationContext == null) {
            log.debug("{} No authentication context available.", getLogPrefix());
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {

        // Get the resolution context from the profile request
        // this may already exist but if not, auto-create it
        final AttributeResolutionContext resolutionContext =
                profileRequestContext.getSubcontext(AttributeResolutionContext.class, true);
        
        populateResolutionContext(profileRequestContext, resolutionContext);

        ServiceableComponent<AttributeResolver> component = null;
        try {
            component = attributeResolverService.getServiceableComponent();
            if (null == component) {
                log.error("{} Error resolving attributes: Invalid Attribute resolver configuration", getLogPrefix());
                if (!maskFailures) {
                    ActionSupport.buildEvent(profileRequestContext, IdPEventIds.UNABLE_RESOLVE_ATTRIBS);
                }
            } else {
                final AttributeResolver attributeResolver = component.getComponent();
                attributeResolver.resolveAttributes(resolutionContext);
                profileRequestContext.removeSubcontext(resolutionContext);

                final AttributeContext attributeCtx = attributeContextCreationStrategy.apply(profileRequestContext);
                if (null == attributeCtx) {
                    throw new ResolutionException("Unable to create or locate AttributeContext to populate");
                }
                attributeCtx.setIdPAttributes(resolutionContext.getResolvedIdPAttributes().values());
                attributeCtx.setUnfilteredIdPAttributes(resolutionContext.getResolvedIdPAttributes().values());
            }
        } catch (final ResolutionException e) {
            log.error("{} Error resolving attributes", getLogPrefix(), e);
            if (!maskFailures) {
                ActionSupport.buildEvent(profileRequestContext, IdPEventIds.UNABLE_RESOLVE_ATTRIBS);
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }

    /**
     * Fill in the resolution context data.
     * 
     * @param profileRequestContext current profile request context
     * @param resolutionContext context to populate
     */
    private void populateResolutionContext(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AttributeResolutionContext resolutionContext) {
        
        // Populate requested attributes, if not already set.
        if (resolutionContext.getRequestedIdPAttributeNames() == null
                || resolutionContext.getRequestedIdPAttributeNames().isEmpty()) {
            resolutionContext.setRequestedIdPAttributeNames(attributesToResolve);
        }
        
        resolutionContext.setPrincipal(principalNameLookupStrategy.apply(profileRequestContext));
        
        resolutionContext.setPrincipalAuthenticationMethod(null);
        if (null != authenticationContext) {
            final AuthenticationResult result = authenticationContext.getAuthenticationResult();
            if (null != result) {
                resolutionContext.setPrincipalAuthenticationMethod(result.getAuthenticationFlowId());
            }
        }

        if (recipientLookupStrategy != null) {
            resolutionContext.setAttributeRecipientID(recipientLookupStrategy.apply(profileRequestContext));
        } else {
            resolutionContext.setAttributeRecipientID(null);
        }

        if (issuerLookupStrategy != null) {
            resolutionContext.setAttributeIssuerID(issuerLookupStrategy.apply(profileRequestContext));
        } else {
            resolutionContext.setAttributeIssuerID(null);
        }
    }
    
}