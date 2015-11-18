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

package net.shibboleth.idp.attribute.resolver;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/** A data connector that just returns a static collection of attributes. */
@ThreadSafe
public class MockDataConnector extends AbstractDataConnector {

    /** Number of times {@link #destroy()} was called. */
    private int destroyCount;

    /** Number of times {@link #initialize()} was called. */
    private int initializeCount;

    /** Static collection of values returned by this connector. */
    private final Map<String, IdPAttribute> values;

    /** Exception thrown by resolution. */
    private ResolutionException resolutionException;

    /**
     * Constructor.
     * 
     * @param id unique ID for this data connector
     * @param connectorValues static collection of values returned by this connector
     * @throws ComponentInitializationException 
     */
    public MockDataConnector(String id, Map<String, IdPAttribute> connectorValues) throws ComponentInitializationException {
        setId(id);
        values = connectorValues;
    }

    /**
     * Constructor.
     *
     * @param id
     * @param connectorValues
     * @param newHashSet
     * @throws ComponentInitializationException 
     */
    public MockDataConnector(String id, Map<String, IdPAttribute> connectorValues, Set<ResolverPluginDependency> newHashSet) throws ComponentInitializationException {
        setDependencies(newHashSet);
        setId(id);
        values = connectorValues;
        initialize();
    }

    /** Fail resolutions */
    public void setFailure(boolean fail) {
        if (fail) {
            resolutionException = new ResolutionException();
        } else {
            resolutionException = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        if (resolutionException != null) {
            throw resolutionException;
        }

        return values;
    }

    /** {@inheritDoc} */
    @Override
    public void doDestroy() {
        super.doDestroy();
        destroyCount += 1;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isInitialized() {
        return initializeCount > 0;
    }

    /** {@inheritDoc} */
    @Override
    public void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        initializeCount += 1;
    }

    /**
     * Gets the number of times {@link #destroy()} was called.
     * 
     * @return number of times {@link #destroy()} was called
     */
    public int getDestroyCount() {
        return destroyCount;
    }

    /**
     * Gets the number of times {@link #initialize()} was called.
     * 
     * @return number of times {@link #initialize()} was called
     */
    public int getInitializeCount() {
        return initializeCount;
    }

}