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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/** An attribute definition that simply returns a static value. */
@ThreadSafe
public class MockAttributeDefinition extends AbstractAttributeDefinition {

    /** Number of times {@link #destroy()} was called. */
    private int destroyCount;

    /** Number of times {@link #initialize()} was called. */
    private int initializeCount;

    /** Static value returned by this definition. */
    private IdPAttribute staticValue;

    /** Exception thrown by resolution. */
    private ResolutionException resolutionException;

    /**
     * Constructor.
     * 
     * @param id unique ID of this attribute definition
     * @param value static value returned by this definition
     * @throws ComponentInitializationException 
     */
    public MockAttributeDefinition(final String id, final IdPAttribute value) throws ComponentInitializationException {
        setId(id);
        staticValue = value;
    }

    /**
     * Constructor.
     * 
     * @param id id of the data connector
     * @param exception exception thrown by resolution
     */
    public MockAttributeDefinition(final String id, final ResolutionException exception) {
        setId(id);
        resolutionException = exception;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        if (resolutionException != null) {
            throw resolutionException;
        }

        return staticValue;
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