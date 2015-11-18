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

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.service.ReloadableSpringService;
import net.shibboleth.idp.relyingparty.RelyingPartyConfigurationResolver;
import net.shibboleth.utilities.java.support.component.IdentifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credential resolver whose purpose is to resolve configured IdP encryption credentials.
 */
public class EncryptionCredentialsResolver implements CredentialResolver, IdentifiableComponent {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(EncryptionCredentialsResolver.class);
    
    /** The reloading resolver which is the source of the credentials. */
    @Nonnull private ReloadableSpringService<RelyingPartyConfigurationResolver> service;
    
    /** Component ID. */
    @Nullable private String id;
    
    /**
     * Constructor.
     * 
     * @param resolverService the Spring service exposing the relying party configuration service
     */
    public EncryptionCredentialsResolver(ReloadableSpringService<RelyingPartyConfigurationResolver> resolverService) {
        service = Constraint.isNotNull(resolverService, 
                "ReloadableSpringService for RelyingPartyConfigurationResolver cannot be null");
    }
    
    /** {@inheritDoc} */
    @Nullable public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    public void setId(@Nonnull String componentId) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(componentId), "Component ID can not be null or empty");
    }
    
    /** {@inheritDoc} */
    @Nullable public Credential resolveSingle(@Nullable CriteriaSet criteriaSet) throws ResolverException {
        Iterable<Credential> creds = resolve(criteriaSet);
        if (creds.iterator().hasNext()) {
            return creds.iterator().next();
        } else {
            return null;
        }
    }
    
    /** {@inheritDoc} */
    @Nonnull public Iterable<Credential> resolve(@Nullable final CriteriaSet criteria) 
            throws ResolverException {
        ServiceableComponent<RelyingPartyConfigurationResolver> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("EncryptionCredentialsResolver '{}': error looking up relying party configuration service:"
                        + " Invalid configuration.", getId());
            } else {
                final RelyingPartyConfigurationResolver resolver = component.getComponent();
                if (resolver instanceof DefaultRelyingPartyConfigurationResolver) {
                    log.trace("Saw expected instance of DefaultRelyingPartyConfigurationResolver");
                    return ((DefaultRelyingPartyConfigurationResolver)resolver).getEncryptionCredentials();
                } else {
                    log.trace("Did NOT see expected instance of DefaultRelyingPartyConfigurationResolver");
                    return Collections.emptyList();
                }
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        return null;
    }

}
