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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ServiceException;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

/**
 * Strategy for summoning up a {@link MetadataResolver} from a populated {@link ApplicationContext}. <br/>
 * This is made somewhat complex by the need to chain multiple, top level Metadata Resolvers, but to not combine, non
 * top level resolvers. The parser will create a {@link RelyingPartyMetadataProvider} for each top level resolver. If we
 * encounter but one we are done (it is a {@link ServiceableComponent} already), otherwise we need to chain all the
 * children together and wrap them into a Serviceable Component.
 * 
 */
public class MetadataResolverServiceStrategy extends AbstractIdentifiableInitializableComponent implements
        Function<ApplicationContext, ServiceableComponent<MetadataResolver>> {

    /** {@inheritDoc} */
    @Override @Nullable public ServiceableComponent<MetadataResolver> apply(@Nullable ApplicationContext appContext) {
        final Collection<RelyingPartyMetadataProvider> resolvers =
                appContext.getBeansOfType(RelyingPartyMetadataProvider.class).values();

        if (resolvers.isEmpty()) {
            throw new ServiceException("Reload did not produce any bean of type"
                    + RelyingPartyMetadataProvider.class.getName());
        }
        if (1 == resolvers.size()) {
            // done
            return resolvers.iterator().next();
        }
        // initialize so we can sort
        for (RelyingPartyMetadataProvider resolver:resolvers) {
            try {
                resolver.initialize();
            } catch (ComponentInitializationException e) {
                throw new BeanCreationException("could not preinitialize , metadata provider " + resolver.getId(), e);
            }
        }
        
        final List<RelyingPartyMetadataProvider> resolverList = new ArrayList<>(resolvers.size());
        resolverList.addAll(resolvers);
        Collections.sort(resolverList); 
        final ChainingMetadataResolver chain = new ChainingMetadataResolver();
        try {
            chain.setResolvers(resolverList);
            chain.setId("MultiFileResolverFor:"+resolvers.size()+":Resources");
            chain.initialize();
            final RelyingPartyMetadataProvider result = new RelyingPartyMetadataProvider(chain);
            result.initialize();
            return result;
        } catch (ResolverException | ComponentInitializationException e) {
           throw new ServiceException("Chaining constructor create failed", e);
        }
    }
}