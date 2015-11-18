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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.idp.saml.metadata.impl.AttributeMappingNodeProcessor;
import net.shibboleth.idp.saml.security.impl.KeyAuthorityNodeProcessor;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.service.ReloadableService;

import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.EntitiesDescriptorNameProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A {@link BeanPostProcessor} for {@link MetadataResolver} beans that ensures a {@link NodeProcessingMetadataFilter}
 * containing a pair of default {@link MetadataNodeProcessor} plugins is attached.
 * 
 * <p>
 * This is done to ensure that other components function correctly, such as the PKIX trust engine and predicates that
 * depend on group information.
 * </p>
 */
public class NodeProcessingAttachingBeanPostProcessor implements BeanPostProcessor {

    /** The attribute resolver we use to map attributes. */
    @Nullable private final ReloadableService<AttributeResolver> atributeResolverService;

    /**
     * Constructor.
     *
     * @param service the attribute resolver we use to map attributes
     */
    public NodeProcessingAttachingBeanPostProcessor(@Nullable ReloadableService<AttributeResolver> service) {
        atributeResolverService = service;
    }

    // Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (!(bean instanceof MetadataResolver) || bean instanceof ChainingMetadataResolver
                || bean instanceof RelyingPartyMetadataProvider) {
            // Do not attach to beans which just include other ones.
            return bean;
        }

        final MetadataResolver resolver = (MetadataResolver) bean;

        boolean filterAttached = false;

        final MetadataFilter filter = resolver.getMetadataFilter();
        if (filter != null) {
            if (filter instanceof NodeProcessingMetadataFilter) {
                filterAttached = true;
            } else if (filter instanceof MetadataFilterChain) {
                for (final MetadataFilter f : ((MetadataFilterChain) filter).getFilters()) {
                    if (f instanceof NodeProcessingMetadataFilter) {
                        filterAttached = true;
                        break;
                    }
                }
            }
        }

        if (!filterAttached) {
            final NodeProcessingMetadataFilter filterToAttach = new NodeProcessingMetadataFilter();
            final List<MetadataNodeProcessor> processors = new ArrayList<>(
                    Arrays.asList(new EntitiesDescriptorNameProcessor(), new KeyAuthorityNodeProcessor()));
            if (null != atributeResolverService) {
                processors.add(new AttributeMappingNodeProcessor(atributeResolverService));
            }
            filterToAttach.setNodeProcessors(processors);
            try {
                filterToAttach.initialize();
            } catch (final ComponentInitializationException e) {
                throw new BeanCreationException("Error initializing NodeProcessingMetadataFilter", e);
            }

            if (filter == null) {
                resolver.setMetadataFilter(filterToAttach);
            } else if (filter instanceof MetadataFilterChain) {
                ((MetadataFilterChain) filter).getFilters().add(filterToAttach);
            } else {
                final MetadataFilterChain chain = new MetadataFilterChain();
                chain.setFilters(Arrays.asList(filter, filterToAttach));
                resolver.setMetadataFilter(chain);
            }
        }

        return resolver;
    }

    // Checkstyle: CyclomaticComplexity ON

    /** {@inheritDoc} */
    @Override public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}