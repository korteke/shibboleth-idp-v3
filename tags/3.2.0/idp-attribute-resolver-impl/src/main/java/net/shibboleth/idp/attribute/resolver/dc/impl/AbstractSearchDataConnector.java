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

package net.shibboleth.idp.attribute.resolver.dc.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractDataConnector;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.dc.MappingStrategy;
import net.shibboleth.idp.attribute.resolver.dc.Validator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

/**
 * A {@link net.shibboleth.idp.attribute.resolver.DataConnector} containing functionality common to data connectors that
 * retrieve attribute data by searching a data source.
 * 
 * @param <T> type of executable search
 */
public abstract class AbstractSearchDataConnector<T extends ExecutableSearch> extends AbstractDataConnector {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSearchDataConnector.class);

    /** Builder used to create executable searches. */
    private ExecutableSearchBuilder<T> searchBuilder;

    /** Validator for validating this data connector. */
    private Validator connectorValidator;

    /** Strategy for mapping search results to a collection of {@link IdPAttribute}s. */
    private MappingStrategy mappingStrategy;

    /** Query result cache. */
    private Cache<String, Map<String, IdPAttribute>> resultsCache;

    /**
     * Gets the builder used to create executable searches.
     * 
     * @return builder used to create the executable searches
     */
    public ExecutableSearchBuilder<T> getExecutableSearchBuilder() {
        return searchBuilder;
    }

    /**
     * Sets the builder used to create the executable searches.
     * 
     * @param builder builder used to create the executable searches
     */
    public void setExecutableSearchBuilder(@Nonnull final ExecutableSearchBuilder<T> builder) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        searchBuilder = Constraint.isNotNull(builder, "Executable search builder can not be null");
    }

    /**
     * Gets the validator used to validate this connector.
     * 
     * @return validator used to validate this connector
     */
    public Validator getValidator() {
        return connectorValidator;
    }

    /**
     * Sets the validator used to validate this connector.
     * 
     * @param validator used to validate this connector
     */
    public void setValidator(@Nonnull final Validator validator) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        connectorValidator = Constraint.isNotNull(validator, "Validator can not be null");
    }

    /**
     * Gets the strategy for mapping from search results to a collection of {@link IdPAttribute}s.
     * 
     * @return strategy for mapping from search results to a collection of {@link IdPAttribute}s
     */
    public MappingStrategy getMappingStrategy() {
        return mappingStrategy;
    }

    /**
     * Sets the strategy for mapping from search results to a collection of {@link IdPAttribute}s.
     * 
     * @param strategy strategy for mapping from search results to a collection of {@link IdPAttribute}s
     */
    public void setMappingStrategy(@Nonnull final MappingStrategy strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        mappingStrategy = Constraint.isNotNull(strategy, "Mapping strategy can not be null");
    }

    /**
     * Gets the cache used to cache search results.
     * 
     * @return cache used to cache search results
     */
    @Nonnull public Cache<String, Map<String, IdPAttribute>> getResultsCache() {
        return resultsCache;
    }

    /**
     * Sets the cache used to cache search results. Note, all entries in the cache are invalidated prior to use.
     * 
     * @param cache cache used to cache search results
     */
    public void setResultsCache(@Nullable final Cache<String, Map<String, IdPAttribute>> cache) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        if (cache != null) {
            cache.invalidateAll();
        }
        resultsCache = cache;
    }

    /**
     * Attempts to retrieve attributes from the data source.
     * 
     * @param executable used to retrieve data from the data source
     * 
     * @return attributes
     * 
     * @throws ResolutionException thrown if there is a problem retrieving data from the data source
     */
    @Nullable protected abstract Map<String, IdPAttribute> retrieveAttributes(@Nonnull final T executable)
            throws ResolutionException;

    /** {@inheritDoc} */
    @Override @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final Map<String, List<IdPAttributeValue<?>>> dependsAttributes =
                PluginDependencySupport.getAllAttributeValues(workContext, getDependencies());
        final T executable = searchBuilder.build(resolutionContext, dependsAttributes);
        Map<String, IdPAttribute> resolvedAttributes = null;
        if (resultsCache != null) {
            final String cacheKey = executable.getResultCacheKey();
            resolvedAttributes = resultsCache.getIfPresent(cacheKey);
            log.trace("{} Cache found, resolved attributes {} using cache {}", new Object[] {getLogPrefix(),
                    resolvedAttributes, resultsCache,});
            if (resolvedAttributes == null) {
                resolvedAttributes = retrieveAttributes(executable);
                log.trace("{} Resolved attributes {}", getLogPrefix(), resolvedAttributes);
                resultsCache.put(cacheKey, resolvedAttributes != null ? resolvedAttributes
                        : Collections.<String,IdPAttribute>emptyMap());
            }
        } else {
            resolvedAttributes = retrieveAttributes(executable);
            log.trace("{} Resolved attributes: {}", getLogPrefix(), resolvedAttributes);
        }

        return resolvedAttributes;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (searchBuilder == null) {
            throw new ComponentInitializationException(getLogPrefix() + " No executable search builder was configured");
        }
        if (connectorValidator == null) {
            throw new ComponentInitializationException(getLogPrefix() + " No validator was configured");
        }
        if (mappingStrategy == null) {
            throw new ComponentInitializationException(getLogPrefix() + " No mapping strategy was configured");
        }
    }
    
}