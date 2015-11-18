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

package net.shibboleth.idp.saml.profile.impl;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RefreshableMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that refreshes a {@link MetadataResolver} manually.
 * 
 * <p>The {@link MetadataResolver} to reload is indicated by supplying {@link #RESOLVER_ID} as a query parameter.</p>
 * 
 * <p>On success, a 200 HTTP status with a simple response body is returned. On failure, a non-successful
 * HTTP status is returned.</p>
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_MESSAGE}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link EventIds#IO_ERROR}
 */
public class ReloadMetadata extends AbstractProfileAction {

    /** Query parameter indicating ID of metadata provider bean to reload. */
    @Nonnull @NotEmpty public static final String RESOLVER_ID = "id";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ReloadMetadata.class);

    /** The service that contains the metadata. */
    @NonnullAfterInit private ReloadableService<MetadataResolver> metadataResolverService;
    
    /** Identifies bean to refresh. */
    @Nullable private String id;

    /**
     * Set the service that describes the metadata.
     * 
     * @param service what to set.
     */
    public void setMetadataResolver(@Nonnull final ReloadableService<MetadataResolver> service) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        metadataResolverService = Constraint.isNotNull(service, "MetadataResolver service cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (metadataResolverService == null) {
            throw new ComponentInitializationException("MetadataResolver service cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        
        if (!super.doPreExecute(profileRequestContext)) {
            return false;
        } else if (getHttpServletResponse() == null) {
            log.debug("{} No HttpServletResponse available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        id = getHttpServletRequest() != null ? getHttpServletRequest().getParameter(RESOLVER_ID) : null;
        if (id == null) {
            log.warn("{} No 'id' parameter found in request", getLogPrefix());
            try {
                getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "Metadata source not found.");
            } catch (final IOException e) {
                ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
            }
            return false;
        }
        
        return true;
    }

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
        log.debug("{} Reloading metadata from '{}'", getLogPrefix(), id);

        final ServiceableComponent<MetadataResolver> component = metadataResolverService.getServiceableComponent();
        try {
            RefreshableMetadataResolver toRefresh = null;
            
            MetadataResolver rootResolver = component.getComponent();
            
            // Step down into wrapping component.
            if (rootResolver instanceof RelyingPartyMetadataProvider) {
                rootResolver = ((RelyingPartyMetadataProvider) rootResolver).getEmbeddedResolver(); 
            }
            
            if (Objects.equals(id, rootResolver.getId()) && rootResolver instanceof RefreshableMetadataResolver) {
                toRefresh = (RefreshableMetadataResolver) rootResolver;
            } else if (rootResolver instanceof ChainingMetadataResolver) {
                for (final MetadataResolver childResolver : ((ChainingMetadataResolver) rootResolver).getResolvers()) {
                    if (Objects.equals(id, childResolver.getId())
                            && childResolver instanceof RefreshableMetadataResolver) {
                        toRefresh = (RefreshableMetadataResolver) childResolver;
                        break;
                    }
                }
            }
            
            if (toRefresh != null) {
                toRefresh.refresh();
                log.debug("{} Reloaded metadata from '{}'", getLogPrefix(), id);
                getHttpServletResponse().setStatus(HttpServletResponse.SC_OK);
                getHttpServletResponse().getWriter().println("Metadata reloaded.");
            } else {
                log.warn("{} Unable to locate refreshable metadata source '{}'", getLogPrefix(), id);
                getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "Metadata source not found.");
            }
            
        } catch (final ResolverException e) {
            log.error("{} Metadata source '{}': Error during refresh", getLogPrefix(), id, e);
            try {
                getHttpServletResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (final IOException e2) {
                log.error("{} I/O error responding to request", getLogPrefix(), e2);
                ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
            }
        } catch (final IOException e) {
            log.error("{} I/O error responding to request", getLogPrefix(), e);
            ActionSupport.buildEvent(profileRequestContext, EventIds.IO_ERROR);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
}