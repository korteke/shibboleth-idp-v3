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

package net.shibboleth.idp.saml.attribute.mapping;

import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

import com.google.common.collect.Multimap;

/**
 * This attribute defines the mechanism to go from a list of objects into a collection (IdP) {@link IdPAttribute}, the
 * representation (a {@link Multimap}) being such as is useful to attribute filtering. <br/>
 * Implementations of this interface will be paired with implementations of {@link AttributeMapper}.
 * 
 * @param <InType> the type which is to be inspected and mapped
 * @param <OutType> some sort of representation of an IdP attribute
 */
public interface AttributesMapper<InType,OutType extends IdPAttribute> {

    /**
     * Map the input objects into IdP attributes.
     * 
     * @param prototypes the SAML attributes
     * 
     * @return a map from IdP AttributeId to RequestedAttributes
     */
    @Nonnull @NonnullElements Multimap<String,OutType> mapAttributes(
            @Nonnull @NonnullElements final List<InType> prototypes);
    
}