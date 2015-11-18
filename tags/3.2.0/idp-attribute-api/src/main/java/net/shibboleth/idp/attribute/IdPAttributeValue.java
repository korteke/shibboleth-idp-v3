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

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Interface for values of an {@link IdPAttribute}. This interface provides an explicit method for getting the value of
 * an attribute as opposed to any other data that may be associated with the value, as well as a displayable value.
 * <p>
 * Implementations of this interface <strong>MUST</strong> implement an appropriate {@link Object#equals(Object)} and
 * {@link Object#hashCode()} method. They should also implement {@link Object#toString()} such that useful
 * representations may be written out in log messages.
 * </p>
 * 
 * @param <ValueType> the type of value
 */
public interface IdPAttributeValue<ValueType> {

    /**
     * Get the value of this attribute.
     * 
     * @return the attribute value
     */
    @Nonnull ValueType getValue();
    
    /**
     * Get a displayable form of the value for user interfaces and similar purposes.
     * 
     * @return  a displayable value
     */
    @Nonnull @NotEmpty String getDisplayValue();
    
}