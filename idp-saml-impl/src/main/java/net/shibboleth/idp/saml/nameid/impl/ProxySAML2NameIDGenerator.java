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

package net.shibboleth.idp.saml.nameid.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.nameid.NameIdentifierGenerationService;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.profile.SAML2NameIDGenerator;

/**
 * A compound implementation of the {@link SAML2NameIDGenerator} interface that wraps a sequence of
 * candidate generators along with a default to try if no format-specific options are available.
 */
public class ProxySAML2NameIDGenerator implements SAML2NameIDGenerator {
    
    /** Service used to get the generator to proxy. */
    @Nonnull private final ReloadableService<NameIdentifierGenerationService> generatorService;
    
    /**
     * Constructor.
     *
     * @param service the service providing the generator to proxy
     */
    public ProxySAML2NameIDGenerator(
            @Nonnull final ReloadableService<NameIdentifierGenerationService> service) {
        generatorService = Constraint.isNotNull(service, "NameIdentifierGenerationService cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public NameID generate(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull @NotEmpty final String format) throws SAMLException {
        
        ServiceableComponent<NameIdentifierGenerationService> component = null;
        try {
            component = generatorService.getServiceableComponent();
            if (component == null) {
                throw new SAMLException("Invalid NameIdentifierGenerationService configuration");
            }
            return component.getComponent().getSAML2NameIDGenerator().generate(profileRequestContext, format);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }

}