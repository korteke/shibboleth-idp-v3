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

package net.shibboleth.idp.saml.attribute.encoding;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeDesignatorMapper;

/**
 * An interface which is implemented by encoders which encode SAML 1 attributes and which can be reverse mapped.
 * 
 * @param <I> The IdP Type
 */
public interface AttributeDesignatorMapperProcessor<I extends IdPAttribute> {

    /**
     * This populates the attribute mapper to go from a SAML AttributeDesignator to an IdP Attribute (or
     * derivative) such that applying the output of the mapper to the encoder will produce the name attribute
     * (and vice versa).
     * 
     * <p>The populated mapper <em>must not</em> have any associated (IdP) Attribute names and <em>must</em> implement
     * {@link Object#equals(Object)} and {@link Object#hashCode()}.</p>
     * 
     * @param mapper the mapper.
     */
    @Nonnull void populateAttributeMapper(@Nonnull final AbstractSAMLAttributeDesignatorMapper<I> mapper);

}