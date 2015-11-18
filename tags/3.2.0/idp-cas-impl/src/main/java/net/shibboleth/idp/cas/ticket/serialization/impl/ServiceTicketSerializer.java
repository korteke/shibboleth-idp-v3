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

package net.shibboleth.idp.cas.ticket.serialization.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import org.joda.time.Instant;

/**
 * Serializes service tickets in simple field-delimited form.
 *
 * @author Marvin S. Addison
 */
public class ServiceTicketSerializer extends AbstractTicketSerializer<ServiceTicket> {
    @Override
    @NotEmpty
    protected String[] extractFields(@Nonnull final ServiceTicket ticket) {
        return new String[] {
                ticket.getSessionId(),
                ticket.getService(),
                String.valueOf(ticket.getExpirationInstant().getMillis()),
                String.valueOf(ticket.isRenew()),
        };
    }

    @Override
    @Nonnull
    protected ServiceTicket createTicket(@Nonnull final String id, @NotEmpty final String[] fields) {
        if (fields.length != 4) {
            throw new IllegalArgumentException("Expected 4 fields but got " + fields.length);
        }
        return new ServiceTicket(
                id, fields[0], fields[1], new Instant(Long.valueOf(fields[2])), Boolean.parseBoolean(fields[3]));
    }
}
