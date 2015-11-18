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

import java.security.Principal;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/**
 * Interface for an authentication component that exposes custom {@link Principal} objects.
 * 
 * <p>Components may expose principals based on both the potential to support them, or because
 * they actively contain them, depending on the nature of a component. Calling components rely
 * on this interface to determine whether a supporting component applies to a given operation
 * or request based on the principals returned.</p>
 */
public interface PrincipalSupportingComponent {

    /**
     * Get an immutable set of supported custom principals that the component produces, supports, contains, etc.
     * 
     * @param <T> type of Principal to inquire on
     * @param c type of Principal to inquire on
     * 
     * @return a set of matching principals
     */
    @Nonnull @NonnullElements @Unmodifiable <T extends Principal> Set<T> getSupportedPrincipals(
            @Nonnull final Class<T> c);
}