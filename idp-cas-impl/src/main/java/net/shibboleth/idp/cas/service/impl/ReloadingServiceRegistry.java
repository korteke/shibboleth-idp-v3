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

package net.shibboleth.idp.cas.service.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.cas.service.Service;
import net.shibboleth.idp.cas.service.ServiceRegistry;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service registry wrapper around a {@link net.shibboleth.utilities.java.support.service.ReloadableService}.
 *
 * @author Marvin S. Addison
 */
public class ReloadingServiceRegistry extends AbstractIdentifiableInitializableComponent implements ServiceRegistry {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ReloadingServiceRegistry.class);

    /** The service that manages the reloading. */
    private final ReloadableService<ServiceRegistry> service;

    /**
     * Creates a new instance.
     *
     * @param delegate The service to which operations are delegated.
     */
    public ReloadingServiceRegistry(@Nonnull ReloadableService<ServiceRegistry> delegate) {
        service = Constraint.isNotNull(delegate, "ReloadableService cannot be null");
    }

    @Nullable
    @Override
    public Service lookup(@Nonnull String serviceURL) {
        ServiceableComponent<ServiceRegistry> component = null;
        try {
            component = service.getServiceableComponent();
            if (null == component) {
                log.error("ServiceRegistry '{}': error looking up service registry: Invalid configuration.", getId());
                return null;
            }
            return component.getComponent().lookup(serviceURL);
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
    }
}
