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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.cas.ticket.Ticket;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.storage.StorageSerializer;

/**
 * Base class for ticket serializers that use a simple field-delimited serialization strategy.
 * Tickets are expected to be stored using the ticket ID as a key, so the ticket ID is not contained as part
 * of the serialized form.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractTicketSerializer<T extends Ticket> implements StorageSerializer<T> {
    /** Field delimiter. */
    private static final String DELIMITER = "::";

    /** Pattern used to extract fields from delimited form. */
    private static final Pattern SPLIT_PATTERN = Pattern.compile(DELIMITER);

    @Override
    public void initialize() throws ComponentInitializationException {}

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    @Nonnull
    public String serialize(@Nonnull final T ticket) throws IOException {
        final String[] fields = extractFields(ticket);
        if (fields.length == 0) {
            throw new IllegalStateException("Ticket has no fields to serialize.");
        }
        final StringBuilder sb = new StringBuilder(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(DELIMITER).append(fields[i]);
        }
        return sb.toString();
    }

    @Override
    @Nonnull
    public T deserialize(
            final long version,
            @Nonnull @NotEmpty final String context,
            @Nonnull @NotEmpty final String key,
            @Nonnull @NotEmpty final String value,
            @Nullable final Long expiration) throws IOException {
        return createTicket(key, SPLIT_PATTERN.split(value));
    }

    @NotEmpty
    protected abstract String[] extractFields(@Nonnull T ticket);

    @Nonnull protected abstract T createTicket(@Nonnull String id, @NotEmpty String[] fields);
}
