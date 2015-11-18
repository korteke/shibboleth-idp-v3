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

package net.shibboleth.idp.attribute.resolver.spring.impl;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.saml.attribute.principalconnector.impl.PrincipalConnector;
import net.shibboleth.idp.saml.attribute.principalconnector.impl.PrinicpalConnectorCanonicalizer;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.service.ServiceException;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

/**
 * Strategy for summoning up an {@link AttributeResolverImpl} from a populated {@link ApplicationContext}. We do this by
 * finding all the configured {@link AttributeDefinition}, {@link DataConnector} and {@link PrincipalConnector} beans
 * and bunging them into the Attribute Resolver which we then initialize.
 */
public class AttributeResolverServiceStrategy extends AbstractIdentifiableInitializableComponent implements
        Function<ApplicationContext,ServiceableComponent<AttributeResolver>> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeResolverServiceStrategy.class);

    /** {@inheritDoc} */
    @Override @Nullable public ServiceableComponent<AttributeResolver> apply(
            @Nullable final ApplicationContext appContext) {

        final Collection<PrincipalConnector> pcs = appContext.getBeansOfType(PrincipalConnector.class).values();
        final PrinicpalConnectorCanonicalizer pcc = new PrinicpalConnectorCanonicalizer(pcs);

        final Collection<AttributeDefinition> definitions =
                appContext.getBeansOfType(AttributeDefinition.class).values();

        final Collection<DataConnector> connectors = appContext.getBeansOfType(DataConnector.class).values();

        log.debug("Creating Attribute Resolver {} with {} Attribute Definition(s), {} Data Connector(s)"
                + " and {} Principal Connector(s)", getId(), definitions.size(), connectors.size(), pcs.size());

        final AttributeResolverImpl resolver = new AttributeResolverImpl(getId(), definitions, connectors, pcc);
        resolver.setApplicationContext(appContext);

        try {
            resolver.initialize();
        } catch (final ComponentInitializationException e) {
            throw new ServiceException("Unable to initialize attribute resolver for " + appContext.getDisplayName(), e);
        }
        return resolver;
    }
    
}