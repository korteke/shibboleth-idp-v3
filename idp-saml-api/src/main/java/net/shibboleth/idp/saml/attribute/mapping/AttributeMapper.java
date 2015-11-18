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

import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/**
 * This attribute defines the mechanism to go from something into an (IdP) {@link IdPAttribute}. <br/>
 * Implementations of this interface will be paired with implementations of {@link AttributesMapper}.
 * 
 * @param <InType> the type which is to be inspected and mapped
 * @param <OutType> some sort of representation of an IdP attribute
 */
public interface AttributeMapper<InType,OutType extends IdPAttribute> extends IdentifiedComponent{

    /**
     * Map the SAML attribute to the required output type.
     * 
     * <p>If the input contains values to be decoded, failure to decode any values results in a failure
     * to map the input at all.</p>
     * 
     * @param input the SAML attribute
     * @return the appropriate map of names to output type
     * 
     */
    @Nonnull @NonnullElements Map<String,OutType> mapAttribute(@Nonnull final InType input);

}