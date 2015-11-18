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

package net.shibboleth.idp.relyingparty.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves a per-relying party configuration for a given profile request based on the request context. The
 * configuration is loaded via the supplied service.
 * 
 * <p>
 * Note that this resolver requires that none of the returned structures do any operations on receipt of
 * {@link #destroy()} since the returned value is not covered by the
 * </p>
 */
public class ReloadingRelyingPartyConfigurationResolver extends AbstractIdentifiableInitializableComponent implements
        RelyingPartyConfigurationResolver {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ReloadingRelyingPartyConfigurationResolver.class);

    /** The service which managed the reloading. */
    private final ReloadableService<RelyingPartyConfigurationResolver> service;

    /**
     * Constructor.
     * 
     * @param resolverService the service which will manage the loading.
     */
    public ReloadingRelyingPartyConfigurationResolver(
            @Nonnull ReloadableService<RelyingPartyConfigurationResolver> resolverService) {
        service = Constraint.isNotNull(resolverService, "RelyingParty Service cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Iterable<RelyingPartyConfiguration> resolve(
            @Nullable final ProfileRequestContext context) throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        ServiceableComponent<RelyingPartyConfigurationResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyResolver '{}': error looking up Relying Party: Invalid configuration.", getId());
            } else {
                final RelyingPartyConfigurationResolver resolver = component.getComponent();
                final List<RelyingPartyConfiguration> results = new ArrayList<>();
                for (final RelyingPartyConfiguration result : resolver.resolve(context)) {
                    results.add(result);
                }
                return results;
            }
        } catch (final ResolverException e) {
            log.error("RelyingPartyResolver '{}': error in resolution", getId(), e);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return Collections.emptySet();
    }

    /** {@inheritDoc} */
    @Override @Nullable public RelyingPartyConfiguration resolveSingle(@Nullable final ProfileRequestContext context)
            throws ResolverException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        ServiceableComponent<RelyingPartyConfigurationResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyResolver '{}': error looking up Relying Party: Invalid configuration.", getId());
            } else {
                final RelyingPartyConfigurationResolver resolver = component.getComponent();
                return resolver.resolveSingle(context);
            }
        } catch (final ResolverException e) {
            log.error("RelyingPartyResolver '{}': error in resolution", getId(), e);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override public SecurityConfiguration getDefaultSecurityConfiguration(String profileId) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        ServiceableComponent<RelyingPartyConfigurationResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("RelyingPartyResolver '{}': error looking up default security config:"
                        + " Invalid configuration.", getId());
            } else {
                final RelyingPartyConfigurationResolver resolver = component.getComponent();
                return resolver.getDefaultSecurityConfiguration(profileId);
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return null;
    }
    
}