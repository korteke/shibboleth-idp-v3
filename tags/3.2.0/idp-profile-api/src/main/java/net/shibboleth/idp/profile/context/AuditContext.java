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

package net.shibboleth.idp.profile.context;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link BaseContext} containing information to preserve for auditing/logging.
 */
public final class AuditContext extends BaseContext {

    /** Extensible map of arbitrary field to data mappings. */
    @Nonnull @NonnullElements private Multimap<String,String> fieldMap;
    
    /** Constructor. */
    public AuditContext() {
        fieldMap = ArrayListMultimap.create(20, 1);
    }
    
    /**
     * Get a live view of the map of field/data mappings.
     * 
     * @return field/data mappings
     */
    @Nonnull @NonnullElements @Live public Multimap<String,String> getFields() {
        return fieldMap;
    }
    
    /**
     * Get a live collection of values associated with a field.
     * 
     * @param field field to retrieve
     * 
     * @return the field's values
     */
    @Nonnull @NonnullElements @Live public Collection<String> getFieldValues(@Nonnull @NotEmpty final String field) {
        return fieldMap.get(field);
    }
    
}