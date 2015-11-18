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

import net.shibboleth.utilities.java.support.primitive.StringSupport;
import org.joda.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * CAS proxy-granting ticket.
 *
 * @author Marvin S. Addison
 */
public class ProxyGrantingTicket extends Ticket {

    /** The ID of the parent proxy-granting ticket. */
    @Nullable
    private String parentId;

    /**
     * Creates a proxy-granting ticket with the given values.
     *
     * @param id Ticket ID.
     * @param sessionId IdP session ID used to create ticket.
     * @param service Service that requested the ticket.
     * @param expiration Expiration instant.
     * @param parentId ID of parent proxy-granting ticket or null if this is first proxy in chain.
     */
    public ProxyGrantingTicket(
            @Nonnull final String id,
            @Nonnull final String sessionId,
            @Nonnull final String service,
            @Nonnull final Instant expiration,
            @Nullable final String parentId) {
        super(id, sessionId, service, expiration);
        this.parentId = StringSupport.trimOrNull(parentId);
    }

    @Nullable
    public String getParentId() {
        return parentId;
    }

    /**
     * Determines whether this proxy-granting ticket is the root of a proxy chain.
     *
     * @return True if this proxy-granting ticket has no parent, false otherwise.
     */
    public boolean isRoot() {
        return getParentId() == null;
    }
}
