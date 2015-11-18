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

package net.shibboleth.idp.cas.ticket;

import org.joda.time.Instant;

import javax.annotation.Nonnull;

/**
 * CAS service ticket.
 *
 * @author Marvin S. Addison
 */
public class ServiceTicket extends Ticket {

    /** Forced authentication flag. */
    private final boolean renew;

    /**
     * Creates a new authenticated ticket with an identifier, service, and expiration date.
     *
     * @param id Ticket ID.
     * @param sessionId IdP session ID used to create ticket.
     * @param service Service that requested the ticket.
     * @param expiration Expiration instant.
     * @param renew True if ticket was issued from forced authentication, false otherwise.
     */
    public ServiceTicket(
            @Nonnull final String id,
            @Nonnull final String sessionId,
            @Nonnull final String service,
            @Nonnull final Instant expiration,
            final boolean renew) {
        super(id, sessionId, service, expiration);
        this.renew = renew;
    }


    public boolean isRenew() {
        return renew;
    }
}
