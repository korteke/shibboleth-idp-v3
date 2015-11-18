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

package net.shibboleth.idp.attribute.resolver.dc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Base class for implementing {@link MappingStrategy} instances that includes
 * support for field renaming/aliasing. 
 * 
 *  @param <T> The type of result.
 */
public abstract class AbstractMappingStrategy<T> implements MappingStrategy<T> {

    /** A map to rename result fields. */
    @Nonnull @NonnullElements private Map<String,String> resultRenamingMap;
    
    /** Whether an empty result set is an error. */
    private boolean noResultAnError;

    /** Whether a result set with more than one result is an error. */
    private boolean multipleResultsAnError;

    /** Constructor. */
    public AbstractMappingStrategy() {
        resultRenamingMap = Collections.emptyMap();
    }
    
    /**
     * Get an unmodifiable view of a map of aliasing rules.
     * 
     * @return a map of result field names to alternate field names
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String,String> getResultRenamingMap() {
        return ImmutableMap.copyOf(resultRenamingMap);
    }
    
    /**
     * Set the map of aliasing rules.
     * 
     * @param map map of result field names to alternate field names
     */
    public void setResultRenamingMap(@Nonnull @NonnullElements final Map<String,String> map) {
        Constraint.isNotNull(map, "Renaming map cannot be null");
        
        resultRenamingMap = new HashMap<>(map.size());
        for (final Map.Entry<String,String> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                resultRenamingMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Gets whether an empty result set is treated as an error.
     * 
     * @return whether an empty result set is treated as an error
     */
    public boolean isNoResultAnError() {
        return noResultAnError;
    }

    /**
     * Sets whether an empty result set is treated as an error.
     * 
     * @param isAnError whether an empty result set is treated as an error
     */
    public void setNoResultAnError(final boolean isAnError) {
        noResultAnError = isAnError;
    }

    /**
     * Gets whether multiple results is treated as an error.
     * 
     * @return whether multiple results is treated as an error
     */
    public boolean isMultipleResultsAnError() {
        return multipleResultsAnError;
    }

    /**
     * Sets whether multiple results is treated as an error.
     * 
     * @param isAnError whether multiple results is treated as an error
     */
    public void setMultipleResultsAnError(final boolean isAnError) {
        multipleResultsAnError = isAnError;
    }
}