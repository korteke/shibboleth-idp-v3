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

import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.BaseContext;

import javax.annotation.Nonnull;

/**
 * IdP context container for CAS service (i.e. relying party) metadata.
 * This context is typically a child of {@link org.opensaml.profile.context.ProfileRequestContext}.
 *
 * @author Marvin S. Addison
 */
public class ServiceContext extends BaseContext {
    @Nonnull
    private final Service service;

    /**
     * Creates a new instance.
     *
     * @param service Service metadata held by context.
     */
    public ServiceContext(@Nonnull final Service service) {
        Constraint.isNotNull(service, "Service cannot be null");
        this.service = service;
    }

    /** @return Service metadata held by this context. */
    @Nonnull
    public Service getService() {
        return service;
    }
}
