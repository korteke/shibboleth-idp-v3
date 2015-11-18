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

package net.shibboleth.idp.attribute.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

import org.opensaml.messaging.context.BaseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * A {@link BaseContext} that tracks a set of attributes. Usually the tracked attributes are about a particular user and
 * associated with a particular service request.
 */
@NotThreadSafe
public class AttributeContext extends BaseContext {

    /** The attributes tracked by this context. */
    @Nonnull private Map<String, IdPAttribute> attributes;
    
    /** The attributes tracked by this context prior to filtering. */
    @Nullable private Map<String, IdPAttribute> unfilteredAttributes;
    
    /** Log. */
    private final Logger log;
    
    /** Constructor. */
    public AttributeContext() {
        attributes = Collections.emptyMap();
        log = LoggerFactory.getLogger(AttributeContext.class);
    }

    /**
     * Gets the map of attributes, indexed by attribute ID, tracked by this context.
     * 
     * @return the collection of attributes indexed by attribute ID
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, IdPAttribute> getIdPAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes tracked by this context.
     * 
     * @param newAttributes the attributes
     */
    public void setIdPAttributes(@Nullable @NullableElements final Collection<IdPAttribute> newAttributes) {
        if (newAttributes == null) {
            attributes = Collections.emptyMap();
            return;
        }

        final HashMap<String,IdPAttribute> checkedAttributes = new HashMap<>();
        for (final IdPAttribute attribute : newAttributes) {
            if (attribute != null) {
                checkedAttributes.put(attribute.getId(), attribute);
            }
        }

        attributes = ImmutableMap.copyOf(checkedAttributes);
    }
    
    
    /**
     * Gets the map of unfiltered attributes, indexed by attribute ID, tracked by this context.
     * 
     * @return the collection of attributes indexed by attribute ID
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, IdPAttribute> getUnfilteredIdPAttributes() {
        if (null == unfilteredAttributes) {
            log.error("No Attributes have been set in this flow.");
            return Collections.emptyMap();
        }
        return unfilteredAttributes;
    }

    /**
     * Sets the unfiltered attributes tracked by this context.
     * 
     * @param newAttributes the attributes
     */
    public void setUnfilteredIdPAttributes(@Nullable @NullableElements final Collection<IdPAttribute> newAttributes) {
        if (null != unfilteredAttributes) {
            log.error("Unfiltered attributes have already been set in this flow.");
        }
        if (newAttributes == null) {
            unfilteredAttributes = Collections.emptyMap();
            return;
        }

        final HashMap<String,IdPAttribute> checkedAttributes = new HashMap<>();
        for (final IdPAttribute attribute : newAttributes) {
            if (attribute != null) {
                checkedAttributes.put(attribute.getId(), attribute);
            }
        }

        unfilteredAttributes = ImmutableMap.copyOf(checkedAttributes);
    }
}