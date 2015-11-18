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

package net.shibboleth.idp.test.flows.cas;

import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.idp.cas.ticket.ProxyTicket;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import org.joda.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Ticket service that support error handling tests. All operations throw {@link RuntimeException}.
 *
 * @author Marvin S. Addison
 */
public class ThrowingTicketService implements TicketService {
    @Nonnull
    @Override
    public ServiceTicket createServiceTicket(@Nonnull String id, @Nonnull Instant expiry, @Nonnull String sessionId, @Nonnull String service, boolean renew) {
        throw new RuntimeException("createServiceTicket failed for ticket " + id);
    }

    @Nullable
    @Override
    public ServiceTicket removeServiceTicket(@Nonnull String id) {
        throw new RuntimeException("removeServiceTicket failed for ticket " + id);
    }

    @Nonnull
    @Override
    public ProxyGrantingTicket createProxyGrantingTicket(@Nonnull String id, @Nonnull Instant expiry, @Nonnull ServiceTicket serviceTicket) {
        throw new RuntimeException("createProxyGrantingTicket failed for ticket " + id);
    }

    @Nonnull
    @Override
    public ProxyGrantingTicket createProxyGrantingTicket(@Nonnull String id, @Nonnull Instant expiry, @Nonnull ProxyTicket proxyTicket) {
        throw new RuntimeException("createProxyGrantingTicket failed for ticket " + id);
    }

    @Nullable
    @Override
    public ProxyGrantingTicket fetchProxyGrantingTicket(@Nonnull String id) {
        throw new RuntimeException("fetchProxyGrantingTicket failed for ticket " + id);
    }

    @Nullable
    @Override
    public ProxyGrantingTicket removeProxyGrantingTicket(@Nonnull String id) {
        throw new RuntimeException("removeProxyGrantingTicket failed for ticket " + id);
    }

    @Nonnull
    @Override
    public ProxyTicket createProxyTicket(@Nonnull String id, @Nonnull Instant expiry, @Nonnull ProxyGrantingTicket pgt, @Nonnull String service) {
        throw new RuntimeException("createProxyTicket failed for ticket " + id);
    }

    @Nullable
    @Override
    public ProxyTicket removeProxyTicket(@Nonnull String id) {
        throw new RuntimeException("removeProxyTicket failed for ticket " + id);
    }
}
