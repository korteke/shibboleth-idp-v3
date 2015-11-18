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

package net.shibboleth.idp.attribute.resolver.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolvedAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.ResolvedDataConnector;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.MapConstraints;

/**
 * A context which carries and collects information through the attribute resolution process, and coordinates data
 * between the resolver implementation and the various resolver plugin implementations.
 *
 * <p>
 * This should be considered a private API limited to plugin implementations.
 * </p>
 */
@NotThreadSafe
public class AttributeResolverWorkContext extends BaseContext {

    /** Attribute definitions that have been resolved and the resultant attribute. */
    @Nonnull @NonnullElements private final Map<String, ResolvedAttributeDefinition> resolvedAttributeDefinitions;

    /** Data connectors that have been resolved and the resultant attributes. */
    @Nonnull @NonnullElements private final Map<String, ResolvedDataConnector> resolvedDataConnectors;

    /** Constructor. */
    public AttributeResolverWorkContext() {
        resolvedAttributeDefinitions =
                MapConstraints.constrainedMap(new HashMap<String, ResolvedAttributeDefinition>(),
                        MapConstraints.notNull());

        resolvedDataConnectors =
                MapConstraints.constrainedMap(new HashMap<String, ResolvedDataConnector>(), MapConstraints.notNull());
    }

    /**
     * Gets the resolved attribute definitions that been recorded.
     * 
     * @return resolved attribute definitions that been recorded
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, ResolvedAttributeDefinition>
            getResolvedIdPAttributeDefinitions() {
        return Collections.unmodifiableMap(resolvedAttributeDefinitions);
    }

    /**
     * Records the results of an attribute definition resolution.
     * 
     * @param definition the resolved attribute definition, must not be null
     * @param attribute the attribute produced by the given attribute definition, may be null
     * 
     * @throws ResolutionException thrown if a result of a resolution for the given attribute definition have already
     *             been recorded
     */
    public void recordAttributeDefinitionResolution(@Nonnull final AttributeDefinition definition,
            @Nullable final IdPAttribute attribute) throws ResolutionException {
        Constraint.isNotNull(definition, "Resolver attribute definition cannot be null");

        if (resolvedAttributeDefinitions.containsKey(definition.getId())) {
            throw new ResolutionException("The resolution of attribute definition " + definition.getId()
                    + " has already been recorded");
        }

        final ResolvedAttributeDefinition wrapper = new ResolvedAttributeDefinition(definition, attribute);
        resolvedAttributeDefinitions.put(definition.getId(), wrapper);
    }

    /**
     * Gets the resolved data connectors that been recorded.
     * 
     * @return resolved data connectors that been recorded
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, ResolvedDataConnector> getResolvedDataConnectors() {
        return Collections.unmodifiableMap(resolvedDataConnectors);
    }

    /**
     * Records the results of an data connector resolution.
     * 
     * @param connector the resolved data connector, must not be null
     * @param attributes the attribute produced by the given data connector, may be null
     * 
     * @throws ResolutionException thrown if a result of a resolution for the given data connector has already been
     *             recorded
     */
    public void recordDataConnectorResolution(@Nonnull final DataConnector connector,
            @Nullable final Map<String, IdPAttribute> attributes) throws ResolutionException {
        Constraint.isNotNull(connector, "Resolver data connector cannot be null");

        if (resolvedDataConnectors.containsKey(connector.getId())) {
            throw new ResolutionException("The resolution of data connector " + connector.getId()
                    + " has already been recorded");
        }

        final ResolvedDataConnector wrapper = new ResolvedDataConnector(connector, attributes);
        resolvedDataConnectors.put(connector.getId(), wrapper);
    }

    /**
     * Transfer the attributes from a failover dataconnector to a failed one. This allows up stream processing to
     * pretend that the failed connector worked OK. The inherent duplication is OK since the code which exploits this
     * does the dedupe.
     *
     * @param failedConnector the connector which failed and provoked the failover.
     * @param failoverConnector the failover connector which did resolve OK.
     * @throws ResolutionException if badness ocurrs
     */
    public void recordFailoverResolution(@Nonnull final DataConnector failedConnector,
            @Nonnull final DataConnector failoverConnector) throws ResolutionException {

        if (failoverConnector == null) {
            return;
        }

        if (resolvedDataConnectors.containsKey(failedConnector.getId())) {
            throw new ResolutionException("The resolution of data connector " + failedConnector.getId()
                    + " has already been recorded");
        }

        final ResolvedDataConnector resolvedFailoverConector = resolvedDataConnectors.get(failoverConnector.getId());
        if (null == resolvedFailoverConector) {
            throw new ResolutionException("The resolution of failover conector" + failoverConnector.getId()
                    + " was not recorded");
        }
        final ResolvedDataConnector wrapper =
                new ResolvedDataConnector(failedConnector, resolvedFailoverConector.getResolvedAttributes());
        resolvedDataConnectors.put(failedConnector.getId(), wrapper);
    }
}