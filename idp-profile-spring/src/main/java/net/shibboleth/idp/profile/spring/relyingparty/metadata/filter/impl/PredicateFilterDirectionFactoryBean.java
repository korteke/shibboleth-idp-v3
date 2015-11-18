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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import javax.annotation.Nullable;

import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter.Direction;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Factory bean allow property replacements of the direction going in to a {@link PredicateFilter}.
 */
public class PredicateFilterDirectionFactoryBean extends AbstractFactoryBean<PredicateFilter.Direction> {

    /** What we want to set the value from this should be "exclude" or "include".*/
    private final String value;
    
    /**
     * Constructor.
     * @param what the value to set
     */
    public PredicateFilterDirectionFactoryBean(@Nullable String what) {
        value = what;
    }
    
    /** {@inheritDoc} */
    @Override
    public Class<?> getObjectType() {
        return PredicateFilter.Direction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected Direction createInstance() throws Exception {
        if (value == null) {
            throw new BeanCreationException("Predicate filter requires 'direction' attribute");
        } else if ("exclude".equals(value)) {
            return PredicateFilter.Direction.EXCLUDE;
        } else if ("include".equals(value)) {
            return PredicateFilter.Direction.INCLUDE;
        } else {
            throw new BeanCreationException("Predicate filter direction must be 'include' or 'exclude'");
        }
    }

}