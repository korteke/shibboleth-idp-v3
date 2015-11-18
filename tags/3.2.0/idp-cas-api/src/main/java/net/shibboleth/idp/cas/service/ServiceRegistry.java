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

package net.shibboleth.idp.cas.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Registry for explicitly verified CAS services (relying parties).
 *
 * @author Marvin S. Addison
 */
public interface ServiceRegistry {
    /**
     * Looks up a service entry from a service URL.
     *
     * @param serviceURL Non-null CAS service URL.
     *
     * @return Service found in registry or null if no match found.
     */
    @Nullable
    Service lookup(@Nonnull String serviceURL);
}
