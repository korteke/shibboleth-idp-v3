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

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.ticket.ProxyGrantingTicket;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import org.joda.time.Instant;

/**
 * Serializes proxy-granting tickets in simple field-delimited form.
 *
 * @author Marvin S. Addison
 */
public class ProxyGrantingTicketSerializer extends AbstractTicketSerializer<ProxyGrantingTicket> {
    @Override
    @NotEmpty
    protected String[] extractFields(@Nonnull final ProxyGrantingTicket ticket) {
        final ArrayList<String> fields = new ArrayList<>(4);
        fields.add(ticket.getSessionId());
        fields.add(ticket.getService());
        fields.add(String.valueOf(ticket.getExpirationInstant().getMillis()));
        if (ticket.getParentId() != null) {
            fields.add(ticket.getParentId());
        }
        return fields.toArray(new String[fields.size()]);
    }

    @Override
    @Nonnull
    protected ProxyGrantingTicket createTicket(@Nonnull final String id, @NotEmpty final String[] fields) {
        if (fields.length < 3) {
            throw new IllegalArgumentException("Expected at least 3 fields but got " + fields.length);
        }
        return new ProxyGrantingTicket(
                id,
                fields[0],
                fields[1],
                new Instant(Long.valueOf(fields[2])),
                fields.length > 3 ? fields[3] : null);
    }
}
