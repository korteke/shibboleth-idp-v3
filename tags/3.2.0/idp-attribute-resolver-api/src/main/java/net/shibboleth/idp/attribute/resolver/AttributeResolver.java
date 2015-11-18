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

package net.shibboleth.idp.attribute.resolver;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/**
 * A component that resolves the attributes for a particular subject.
 * 
 * <p><em>Note Well</em>This class is about <em>attribute resolution</em>, that is to say the summoning
 * up of attributes in response to the exigies of the provided context. It does <em>not</em> implement
 * net.shibboleth.utilities.java.support.resolver.Resolver which is about summoning up bits of
 * generic data from the configuration (e.g., metadata) in response to specific
 * net.shibboleth.utilities.java.support.resolver.Criterion.</p>
 * */
@ThreadSafe
public interface AttributeResolver extends IdentifiedComponent {

    /**
     * Gets the collection of attribute definitions for this resolver.
     * 
     * @return attribute definitions loaded in to this resolver
     */
    @Nonnull @NonnullElements @Unmodifiable Map<String, AttributeDefinition> getAttributeDefinitions();

    /**
     * Gets the unmodifiable collection of data connectors for this resolver.
     * 
     * @return data connectors loaded in to this resolver
     */
    @Nonnull @NonnullElements @Unmodifiable Map<String, DataConnector> getDataConnectors();

    /**
     * Resolves the attribute for the give request. Note, if attributes are requested,
     * {@link AttributeResolutionContext#getRequestedIdPAttributeNames()}, the resolver will <strong>not</strong> fail
     * if they can not be resolved. This information serves only as a hint to the resolver to, potentially, optimize the
     * resolution of attributes.
     * 
     * @param resolutionContext the attribute resolution context that identifies the request subject and accumulates the
     *            resolved attributes
     * 
     * @throws ResolutionException thrown if there is a problem resolving the attributes for the subject
     */
    void resolveAttributes(@Nonnull final AttributeResolutionContext resolutionContext) throws ResolutionException;

}