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

package net.shibboleth.idp.security;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.service.AbstractServiceableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.AccessControl;
import net.shibboleth.utilities.java.support.security.AccessControlService;

/**
 * This class wraps an {@link AccessControlService} in a
 * {@link net.shibboleth.utilities.java.support.service.ServiceableComponent}.
 */
public class ReloadingAccessControlService extends AbstractServiceableComponent<AccessControlService>
        implements AccessControlService {

    /** The embedded service. */
    private final AccessControlService service;

    /**
     * Constructor.
     * 
     * @param svc the embedded service
     */
    public ReloadingAccessControlService(@Nonnull AccessControlService svc) {
        service = Constraint.isNotNull(svc, "AccessControlService cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        setId(service.getId());
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override
    public AccessControl getInstance(@Nonnull final String name) {
        return service.getInstance(name);
    }

    /** {@inheritDoc} */
    @Override
    public AccessControlService getComponent() {
        return this;
    }
    
}