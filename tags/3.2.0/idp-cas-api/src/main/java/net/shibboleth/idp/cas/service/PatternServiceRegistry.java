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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.service.AbstractServiceableComponent;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.IdentifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service registry that evaluates a candidate service URL against one or more defined services, where each
 * definition contains a service URL regular expression pattern.
 *
 * <p>NOTE: This class will become an implementation component in the next major software version.</p>
 *
 * @author Marvin S. Addison
 */
public class PatternServiceRegistry extends AbstractServiceableComponent<ServiceRegistry>
        implements IdentifiableComponent, ServiceRegistry {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PatternServiceRegistry.class);

    /** Map of service definitions to compiled patterns. */
    @Nonnull
    @NonnullElements
    private Map<ServiceDefinition, Pattern> definitions = Collections.emptyMap();

    @Override
    public void setId(@Nonnull final String componentId) {
        super.setId(componentId);
    }

    /**
     * Sets the list of service definitions that back the registry.
     * @param definitions List of service definitions, each of which defines a match pattern to evaluate a candidate
     *                    service URL.
     */
    public void setDefinitions(@Nonnull @NonnullElements List<ServiceDefinition> definitions) {
        Constraint.noNullItems(definitions, "Definitions cannot be null or contain null items");
        // Preserve order of services in map
        this.definitions = new LinkedHashMap<>(definitions.size());
        for (ServiceDefinition definition : definitions) {
            this.definitions.put(definition, Pattern.compile(definition.getId()));
        }
    }

    @Nonnull
    @Override
    public ServiceRegistry getComponent() {
        return this;
    }

    @Override
    @Nullable
    public Service lookup(@Nonnull String serviceURL) {
        Constraint.isNotNull(serviceURL, "Service URL cannot be null");
        for (ServiceDefinition def : definitions.keySet()) {
            log.debug("Evaluating whether {} matches {}", serviceURL, def);
            if (definitions.get(def).matcher(serviceURL).matches()) {
                log.debug("Found match");
                return new Service(serviceURL, def.getGroup(), def.isAuthorizedToProxy(),
                        def.isSingleLogoutParticipant());
            }
        }
        return null;
    }
}
