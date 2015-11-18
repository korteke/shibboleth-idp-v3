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


import javax.annotation.Nullable;

import net.shibboleth.idp.session.IdPSession;

import org.opensaml.messaging.context.BaseContext;

/** A {@link BaseContext} that holds an {@link IdPSession}. */
public class SessionContext extends BaseContext {

    /** IdP session wrapped by this adapter. */
    private IdPSession session;

    /** Constructor. */
    public SessionContext() {
        
    }

    /**
     * Get the IdP session.
     * 
     * @return the IdP session
     */
    @Nullable public IdPSession getIdPSession() {
        return session;
    }

    /**
     * Set the IdP session.
     * 
     * @param theSession the IdP session
     */
    public void setIdPSession(@Nullable final IdPSession theSession) {
        session = theSession;
    }
    
}