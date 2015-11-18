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

package net.shibboleth.idp.saml.session;

import javax.annotation.Nonnull;

import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;

/**
 * Marker subtype for a SAML 1 session, adds no actual information other than its identity as
 * a SAML 1 session. 
 */
public class SAML1SPSession extends BasicSPSession {
    
    /**
     * Constructor.
     *
     * @param id the identifier of the service associated with this session
     * @param creation creation time of session, in milliseconds since the epoch
     * @param expiration expiration time of session, in milliseconds since the epoch
     */
    public SAML1SPSession(@Nonnull @NotEmpty final String id, @Duration @Positive final long creation,
            @Duration @Positive final long expiration) {
        super(id, creation, expiration);
    }

}