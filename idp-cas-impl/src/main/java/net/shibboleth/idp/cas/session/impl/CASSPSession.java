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

package net.shibboleth.idp.cas.session.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.session.BasicSPSession;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Describes a CAS protocol-specific service provider session created in response to a successful ticket validation.
 *
 * @author Marvin S. Addison
 */
public class CASSPSession extends BasicSPSession {

    /** Validated ticket that started the SP session. */
    @Nonnull @NotEmpty
    private final String ticketId;


    /**
     * Creates a new CAS SP session.
     *
     * @param id         the identifier of the service associated with this session
     * @param creation   creation time of session, in milliseconds since the epoch
     * @param expiration expiration time of session, in milliseconds since the epoch
     * @param ticketId   ticket ID used to gain access to the service
     */
    public CASSPSession(
            @Nonnull @NotEmpty String id,
            @Duration @Positive long creation,
            @Duration @Positive long expiration,
            @Nonnull @NotEmpty String ticketId) {
        super(id, creation, expiration);
        this.ticketId = Constraint.isNotNull(StringSupport.trimOrNull(ticketId), "Ticket ID cannot be null or empty");
    }

    @Nonnull @NotEmpty
    public String getTicketId() {
        return ticketId;
    }

    @Override
    public String getSPSessionKey() {
        return ticketId;
    }

    @Override
    public String toString() {
        return "CASSPSession: " + getId() + " via " + ticketId;
    }
}
