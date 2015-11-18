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

package net.shibboleth.idp.saml.metadata.impl;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the service interface to implement {@link MetadataResolver}.
 * 
 */
public class ReloadingRelyingPartyMetadataProvider extends AbstractIdentifiableInitializableComponent implements
        MetadataResolver {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ReloadingRelyingPartyMetadataProvider.class);

    /** The service which managed the reloading. */
    private final ReloadableService<MetadataResolver> service;

    /**
     * Constructor.
     * 
     * @param resolverService the service which will manage the loading.
     */
    public ReloadingRelyingPartyMetadataProvider(@Nonnull ReloadableService<MetadataResolver> resolverService) {
        service = Constraint.isNotNull(resolverService, "MetadataResolver Service cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Iterable<EntityDescriptor> resolve(CriteriaSet criteria) throws ResolverException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ServiceableComponent<MetadataResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyMetadataProvider '{}': Error accessing underlying metadata source: "
                        + "Invalid configuration.", getId());
            } else {
                final MetadataResolver resolver = component.getComponent();
                return resolver.resolve(criteria);
            }
        } catch (final ResolverException e) {
            log.error("RelyingPartyMetadataProvider '{}': Error during resolution", getId(), e);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return Collections.EMPTY_SET;
    }

    /** {@inheritDoc} */
    @Override @Nullable public EntityDescriptor resolveSingle(CriteriaSet criteria) throws ResolverException {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ServiceableComponent<MetadataResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyMetadataProvider '{}': Error accessing underlying metadata source: "
                        + "Invalid configuration.", getId());
            } else {
                final MetadataResolver resolver = component.getComponent();
                return resolver.resolveSingle(criteria);
            }
        } catch (final ResolverException e) {
            log.error("RelyingPartyResolver '{}': Error during resolution", getId(), e);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean isRequireValidMetadata() {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ServiceableComponent<MetadataResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyMetadataProvider '{}': Error accessing underlying metadata source: "
                        + "Invalid configuration.", getId());
            } else {
                final MetadataResolver resolver = component.getComponent();
                return resolver.isRequireValidMetadata();
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        throw new IllegalAccessError("Could not find a valid MetadataResolver");
    }

    /** {@inheritDoc} */
    @Override public void setRequireValidMetadata(boolean requireValidMetadata) {
        throw new IllegalAccessError("Cannot set RequireValidMetadata");
    }

    /** {@inheritDoc} */
    @Override public MetadataFilter getMetadataFilter() {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ServiceableComponent<MetadataResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyMetadataProvider '{}': Error accessing underlying metadata source: "
                        + "Invalid configuration.", getId());
            } else {
                final MetadataResolver resolver = component.getComponent();
                return resolver.getMetadataFilter();
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        throw new IllegalAccessError("Could not find a valid MetadataResolver");
    }

    /** {@inheritDoc} */
    @Override public void setMetadataFilter(MetadataFilter newFilter) {
        throw new IllegalAccessError("Cannot set Metadata filter");
    }
    
}