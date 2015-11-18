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

package net.shibboleth.idp.attribute.resolver.dc.ldap.impl;

import javax.annotation.Nonnull;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.attribute.resolver.dc.impl.ExecutableSearchBuilder;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;

/**
 * Abstract executable search filter builder for implementations that can provide a search filter.
 */
public abstract class AbstractExecutableSearchFilterBuilder extends AbstractInitializableComponent 
    implements ExecutableSearchBuilder<ExecutableSearchFilter> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractExecutableSearchFilterBuilder.class);

    /**
     * Creates an executable search filter that performs a search with the search filter.
     * 
     * @param searchFilter to execute a search with
     * @return executable search filter
     */
    // Checkstyle: AnonInnerLength OFF
    protected ExecutableSearchFilter build(@Nonnull final SearchFilter searchFilter) {
        return new ExecutableSearchFilter() {

            /** {@inheritDoc} */
            @Nonnull public String getResultCacheKey() {
                return String.valueOf(searchFilter.hashCode());
            }

            /** {@inheritDoc} */
            @Nonnull public SearchResult execute(@Nonnull final SearchExecutor executor,
                    @Nonnull final ConnectionFactory factory) throws LdapException {
                final Response<SearchResult> response = executor.search(factory, searchFilter);
                log.trace("Search returned response {}", response);
                return response.getResult();
            }

            /** {@inheritDoc} */
            @Nonnull public SearchFilter getSearchFilter() {
                return searchFilter;
            }

            /** {@inheritDoc} */
            public String toString() {
                return searchFilter.toString();
            }
        };
    }
    // Checkstyle: AnonInnerLength ON
}