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

package net.shibboleth.idp.attribute.resolver.dc;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;

/**
 * Strategy for mapping from an arbitrary result type to a collection of {@link IdPAttribute}s.
 * 
 *  @param <T> The type of result.
 */
public interface MappingStrategy<T> {

    /**
     * Maps the given results to a collection of {@link IdPAttribute} indexed by the attribute's ID.
     * 
     * @param results to map
     * 
     * @return the mapped attributes or null if none exist
     * 
     * @throws ResolutionException thrown if there is a problem reading data or mapping it
     */
    @Nullable Map<String,IdPAttribute> map(@Nonnull final T results) throws ResolutionException;
    
}