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

package net.shibboleth.idp.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.storage.StorageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A registry of mappings between a {@link SPSession} class and a corresponding {@link StorageSerializer}
 * for that type.
 */
public final class SPSessionSerializerRegistry extends AbstractInitializableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SPSessionSerializerRegistry.class);
    
    /** Storage for the registry mappings. */
    @Nonnull @NonnullElements
    private Map<Class<? extends SPSession>,StorageSerializer<? extends SPSession>> registry;

    /** Constructor. */
    public SPSessionSerializerRegistry() {
        registry = Collections.emptyMap();
    }
    
    /**
     * Set the mappings to use.
     * 
     * @param map  map to populate registry with
     */
    public void setMappings(@Nonnull @NonnullElements
            Map<Class<? extends SPSession>,StorageSerializer<? extends SPSession>> map) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(map, "Map cannot be null");
        
        registry = new HashMap<>(map.size());
        for (final Map.Entry<Class<? extends SPSession>,StorageSerializer<? extends SPSession>> entry
                : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                registry.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Get a registered {@link StorageSerializer} for a given {@link SPSession} type, if any.
     * 
     * @param type a type of SPSession
     * @return a corresponding StorageSerializer, or null
     */
    @Nullable public StorageSerializer<? extends SPSession> lookup(
            @Nonnull final Class<? extends SPSession> type) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        Constraint.isNotNull(type, "SPSession type cannot be null");
        
        final StorageSerializer<? extends SPSession> serializer = registry.get(type);
        if (serializer != null) {
            log.debug("Registry located StorageSerializer of type '{}' for SPSession type '{}'",
                    serializer.getClass().getName(), type);
            return serializer;
        } else {
            log.debug("Registry failed to locate StorageSerializer for SPSession type '{}'", type);
            return null;
        }
    }

}