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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.filter.AttributeFilterException;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.context.navigate.SubjectContextPrincipalLookupFunction;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.RelyingPartyIdLookupFunction;
import net.shibboleth.idp.profile.context.navigate.ResponderIdLookupFunction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.RootContextLookup;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.profile.context.navigate.InboundMessageContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Action that invokes the {@link AttributeFilter} for the current request.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link IdPEventIds#UNABLE_FILTER_ATTRIBS}
 * 
 * @post If resolution is successful, the relevant RelyingPartyContext.getSubcontext(AttributeContext.class, false) !=
 *       null
 */
public class FilterAttributes extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(FilterAttributes.class);

    /** Service used to get the engine used to filter attributes. */
    @Nonnull private final ReloadableService<AttributeFilter> attributeFilterService;

    /** Strategy used to locate the identity of the issuer associated with the attribute filtering. */
    @Nullable private Function<ProfileRequestContext,String> issuerLookupStrategy;

    /** Strategy used to locate the identity of the recipient associated with the attribute filtering. */
    @Nullable private Function<ProfileRequestContext,String> recipientLookupStrategy;
    
    /** Strategy used to locate or create the {@link AttributeFilterContext}. */
    @Nonnull private Function<ProfileRequestContext,AttributeFilterContext> filterContextCreationStrategy;

    /** Strategy used to locate the {@link AttributeContext} to filter. */
    @Nonnull private Function<ProfileRequestContext,AttributeContext> attributeContextLookupStrategy;
    
    /** Strategy used to locate the principal name associated with the attribute filtering. */
    @Nonnull private Function<ProfileRequestContext,String> principalNameLookupStrategy;

    /**
     * Strategy used to locate the {@link AuthenticationContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext,AuthenticationContext> authnContextLookupStrategy;

    /**
     * Strategy used to locate the {@link SAMLMetadataContext} associated with a given {@link ProfileRequestContext}.
     */
    @Nonnull private Function<ProfileRequestContext,SAMLMetadataContext> metadataContextLookupStrategy;
    
    /**
     * Strategy used to locate the {@link SAMLMetadataContext} associated with a given {@link AttributeFilterContext}.
     */
    @Nonnull private Function<AttributeFilterContext,SAMLMetadataContext> metadataFromFilterLookupStrategy;
    
    /** Whether to treat resolver errors as equivalent to resolving no attributes. */
    private boolean maskFailures;

    /** AuthenticationContext to work from (if any). */
    @Nullable private AuthenticationContext authenticationContext;

    /** AttributeContext to filter. */
    @Nullable private AttributeContext attributeContext;

    /**
     * Constructor.
     * 
     * @param filterService engine used to filter attributes
     */
    public FilterAttributes(@Nonnull final ReloadableService<AttributeFilter> filterService) {
        attributeFilterService = Constraint.isNotNull(filterService, "Service cannot be null");
        
        issuerLookupStrategy = new ResponderIdLookupFunction();
        recipientLookupStrategy = new RelyingPartyIdLookupFunction();
        
        attributeContextLookupStrategy = Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));

        principalNameLookupStrategy = Functions.compose(
                new SubjectContextPrincipalLookupFunction(),
                new ChildContextLookup<ProfileRequestContext,SubjectContext>(SubjectContext.class));
        
        authnContextLookupStrategy = new ChildContextLookup<>(AuthenticationContext.class);
        
        // Default: inbound msg context -> SAMLPeerEntityContext -> SAMLMetadataContext
        metadataContextLookupStrategy = Functions.compose(
                new ChildContextLookup<>(SAMLMetadataContext.class),
                Functions.compose(new ChildContextLookup<>(SAMLPeerEntityContext.class),
                        new InboundMessageContextLookup()));
        
        // This is always set to navigate to the root context and then apply the previous function.
        metadataFromFilterLookupStrategy = Functions.compose(
                new Function<ProfileRequestContext,SAMLMetadataContext>() {
                    @Override
                    public SAMLMetadataContext apply(ProfileRequestContext input) {
                        return metadataContextLookupStrategy.apply(input);
                    }
                },
                new RootContextLookup<AttributeFilterContext,ProfileRequestContext>());

        // Defaults to ProfileRequestContext -> RelyingPartyContext -> AttributeFilterContext.
        filterContextCreationStrategy = Functions.compose(new ChildContextLookup<>(AttributeFilterContext.class, true),
                new ChildContextLookup<ProfileRequestContext,RelyingPartyContext>(RelyingPartyContext.class));
        
        maskFailures = true;
    }
    
    /**
     * Set the strategy used to lookup the issuer for this attribute filtering.
     * 
     * @param strategy  lookup strategy
     */
    public void setIssuerLookupStrategy(@Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        issuerLookupStrategy = strategy;
    }

    /**
     * Set the strategy used to lookup the recipient for this attribute filtering.
     * 
     * @param strategy  lookup strategy
     */
    public void setRecipientLookupStrategy(@Nullable final Function<ProfileRequestContext,String> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        recipientLookupStrategy = strategy;
    }

    /**
     * Set the strategy used to locate or create the {@link AttributeFilterContext} to populate.
     * 
     * @param strategy lookup/creation strategy
     */
    public void setFilterContextCreationStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeFilterContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        filterContextCreationStrategy =
                Constraint.isNotNull(strategy, "AttributeContext creation strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the {@link AttributeContext} associated with a given
     * {@link ProfileRequestContext}.
     * 
     * @param strategy strategy used to locate the {@link AttributeContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,AttributeContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "AttributeContext lookup strategy cannot be null");
    }
    
    /**
     * Set the strategy used to locate the principal name for this attribute filtering.
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
     * Set the strategy used to locate the {@link SAMLMetadataContext} associated with a given
     * {@link ProfileRequestContext}.  Also sets the strategy to find the {@link SAMLMetadataContext}
     * from the {@link AttributeFilterContext};  
     * SAMLMetadataContext
     * @param strategy strategy used to locate the {@link AuthenticationContext} associated with a given
     *            {@link ProfileRequestContext}
     */
    public void setMetadataContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,SAMLMetadataContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        metadataContextLookupStrategy =
                Constraint.isNotNull(strategy, "MetadataContext lookup strategy cannot be null");
        metadataFromFilterLookupStrategy = Functions.compose(metadataContextLookupStrategy,
                new RootContextLookup<AttributeFilterContext,ProfileRequestContext>());
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
        
        attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
        if (attributeContext == null) {
            log.debug("{} No attribute context, no attributes to filter", getLogPrefix());
            return false;
        }

        if (attributeContext.getIdPAttributes().isEmpty()) {
            log.debug("{} No attributes to filter", getLogPrefix());
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

        // Get the filter context from the profile request
        // this may already exist but if not, auto-create it.
        final AttributeFilterContext filterContext = filterContextCreationStrategy.apply(profileRequestContext);
        if (filterContext == null) {
            log.error("{} Unable to locate or create AttributeFilterContext", getLogPrefix());
            if (maskFailures) {
                log.warn("Filter error masked, clearing resolved attributes");
                attributeContext.setIdPAttributes(null);
            } else {
                ActionSupport.buildEvent(profileRequestContext, IdPEventIds.UNABLE_FILTER_ATTRIBS);
            }
            return;
        }
        
        populateFilterContext(profileRequestContext, filterContext);

        ServiceableComponent<AttributeFilter> component = null;

        try {
            component = attributeFilterService.getServiceableComponent();
            if (null == component) {
                log.error("{} Error encountered while filtering attributes : Invalid Attribute Filter configuration",
                        getLogPrefix());
                if (maskFailures) {
                    log.warn("Filter error masked, clearing resolved attributes");
                    attributeContext.setIdPAttributes(null);
                } else {
                    ActionSupport.buildEvent(profileRequestContext, IdPEventIds.UNABLE_FILTER_ATTRIBS);
                }
            } else {
                final AttributeFilter filter = component.getComponent();
                filter.filterAttributes(filterContext);
                filterContext.getParent().removeSubcontext(filterContext);
                attributeContext.setIdPAttributes(filterContext.getFilteredIdPAttributes().values());
            }
        } catch (final AttributeFilterException e) {
            log.error("{} Error encountered while filtering attributes", getLogPrefix(), e);
            if (maskFailures) {
                log.warn("Filter error masked, clearing resolved attributes");
                attributeContext.setIdPAttributes(null);
            } else {
                ActionSupport.buildEvent(profileRequestContext, IdPEventIds.UNABLE_FILTER_ATTRIBS);
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }
    
    /**
     * Fill in the filter context data.
     * 
     * @param profileRequestContext current profile request context
     * @param filterContext context to populate
     */
    private void populateFilterContext(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AttributeFilterContext filterContext) {
        
        filterContext.setPrincipal(principalNameLookupStrategy.apply(profileRequestContext));

        filterContext.setPrincipalAuthenticationMethod(null);
        if (null != authenticationContext) {
            final AuthenticationResult result = authenticationContext.getAuthenticationResult();
            if (null != result) {
                filterContext.setPrincipalAuthenticationMethod(result.getAuthenticationFlowId());
            }
        }

        if (recipientLookupStrategy != null) {
            filterContext.setAttributeRecipientID(recipientLookupStrategy.apply(profileRequestContext));
        } else {
            filterContext.setAttributeRecipientID(null);
        }

        if (issuerLookupStrategy != null) {
            filterContext.setAttributeIssuerID(issuerLookupStrategy.apply(profileRequestContext));
        } else {
            filterContext.setAttributeIssuerID(null);
        }
                
        filterContext.setRequesterMetadataContextLookupStrategy(metadataFromFilterLookupStrategy);

        // If the filter context doesn't have a set of attributes to filter already
        // then look for them in the AttributeContext.
        if (filterContext.getPrefilteredIdPAttributes().isEmpty()) {
            filterContext.setPrefilteredIdPAttributes(attributeContext.getIdPAttributes().values());
        }
    }

}