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

package net.shibboleth.idp.attribute;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Attribute encoders convert an {@link IdPAttribute} into a protocol specific representation. Implementations must take
 * into account that an {@link IdPAttribute} may contain values of multiple types. An implementation encountering
 * a value type it does not understand may either decide to ignore it or throw an {@link AttributeEncodingException}.
 * 
 * <p>Encoders implement a {@link Predicate} interface to determine their applicability to a request.</p>
 * 
 * <p>Encoders <strong>MUST</strong> be thread-safe and stateless and <strong>MUST</strong> implement appropriate
 * {@link Object#equals(Object)} and {@link Object#hashCode()} methods.</p>
 * 
 * @param <EncodedType> the type of object created by encoding the attribute
 */
@ThreadSafe
public interface AttributeEncoder<EncodedType> {

    /**
     * Get the identifier of the protocol targeted by this encoder. Note, some protocols may have different types of
     * encoders that are used to encode attributes in to different parts of the protocol message. This identifier should
     * not be used to distinguish between the different message structure, it should only identify the protocol itself.
     * 
     * @return identifier of the protocol targeted by this encounter
     */
    @Nonnull @NotEmpty String getProtocol();
    
    /**
     * Get an activation condition for this encoder.
     * 
     * @return  a predicate indicating whether the encoder should be applied
     */
    @Nonnull Predicate<ProfileRequestContext> getActivationCondition();

    /**
     * Encode the supplied attribute into a protocol specific representation.
     * 
     * @param attribute the attribute to encode
     * 
     * @return the Object the attribute was encoded into
     * 
     * @throws AttributeEncodingException if unable to successfully encode attribute
     */
    @Nonnull EncodedType encode(@Nonnull final IdPAttribute attribute) throws AttributeEncodingException;
}