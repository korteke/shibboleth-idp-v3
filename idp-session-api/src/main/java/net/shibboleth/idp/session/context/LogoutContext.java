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

package net.shibboleth.idp.session.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.session.SPSession;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A {@link BaseContext} that holds a multimap of {@link SPSession} objects.
 * 
 * <p>This context is used primarily to expose the {@link SPSession} objects for which logout is implicated.
 * It also tracks the processing of actions that populate a {@link MultiRelyingPartyContext} tree containing
 * detailed information on each relying party, typically for user interface considerations.</p>
 */
public final class LogoutContext extends BaseContext {

    /** SP sessions needing logout. */
    @Nonnull @NonnullElements private final Multimap<String,SPSession> sessionMap;

    /** An index of the session objects by an externally assigned key. */
    @Nonnull @NonnullElements private final Map<String,SPSession> keyedSessionMap;
    
    /** Constructor. */
    public LogoutContext() {
        sessionMap = ArrayListMultimap.create(10, 1);
        keyedSessionMap = new HashMap<>();
    }

    /**
     * Get a live view of the map of service ID/session mappings.
     * 
     * @return service ID/session mappings
     */
    @Nonnull @NonnullElements @Live public Multimap<String,SPSession> getSessionMap() {
        return sessionMap;
    }

    /**
     * Get a live view of the map of sessions keyed by an external value.
     * 
     * <p>This map can be used to index the sessions in the context according to a particular use case.</p>
     * 
     * @return keyed session mappings
     */
    @Nonnull @NonnullElements @Live public Map<String,SPSession> getKeyedSessionMap() {
        return keyedSessionMap;
    }

    /**
     * Get a live collection of sessions associated with a service.
     * 
     * @param id name of service to retrieve
     * 
     * @return the sessions for the service
     */
    @Nonnull @NonnullElements @Live public Collection<SPSession> getSessions(@Nonnull @NotEmpty final String id) {
        return sessionMap.get(id);
    }
    
}