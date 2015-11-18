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

package net.shibboleth.idp.authn.principal;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

/**
 * Interface for the serialization/deserialization of principals.
 *
 * @param <Type> the type of object handled
 */
public interface PrincipalSerializer<Type> extends InitializableComponent {

    /**
     * Whether the supplied principal can be serialized.
     *
     * @param principal to examine
     * @return whether principal can be serialized
     */
    boolean supports(@Nonnull final Principal principal);

    /**
     * Serialize the supplied principal.
     *
     * @param principal to serialize
     * @return serialized value
     * @throws IOException if an error occurs during serialization
     */
    @Nonnull @NotEmpty Type serialize(@Nonnull final Principal principal) throws IOException;

    /**
     * Whether the supplied value can be deserialized.
     *
     * @param value to examine
     * @return whether value can be deserialized
     */
    boolean supports(@Nonnull final Type value);

    /**
     * Deserialize the supplied value.
     *
     * @param value to deserialize
     * @return principal
     * @throws IOException if an error occurs during deserialization
     */
    @Nullable Principal deserialize(@Nonnull final Type value) throws IOException;
}