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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Component that manages sessions between the IdP and client devices.
 */
@ThreadSafe
public interface SessionManager {

    /**
     * Create and return a new {@link IdPSession} object for a subject.
     * 
     * <p>Implementations may perform additional work to persist or associate the session
     * with the client.</p>
     * 
     * @param principalName canonical name of the subject of the session
     * 
     * @return  the newly created session
     * @throws SessionException if the session cannot be created
     */
    @Nonnull IdPSession createSession(@Nonnull @NotEmpty final String principalName) throws SessionException;
    
    /**
     * Invalidates or otherwise removes a session from persistent storage and/or unbinds it
     * from a client.
     * 
     * <p>After calling this method, no further method calls on a corresponding {@link IdPSession}
     * object that may be in hand are guaranteed to function correctly. Their behavior is unspecified.</p>
     * 
     * @param sessionId the unique ID of the session to destroy
     * @param unbind whether the session should be unbound from the client
     * 
     * @throws SessionException if the session cannot be destroyed
     */
    void destroySession(@Nonnull @NotEmpty final String sessionId, final boolean unbind) throws SessionException;

}