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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import net.shibboleth.idp.session.AbstractSPSessionSerializer;
import net.shibboleth.idp.session.SPSession;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * JSON serializer for {@link CASSPSession} class.
 *
 * @author Marvin S. Addison
 */
public class CASSPSessionSerializer extends AbstractSPSessionSerializer {

    /** Field name of CAS ticket. */
    @Nonnull @NotEmpty
    private static final String TICKET_FIELD = "st";


    /**
     * Constructor.
     *
     * @param offset milliseconds to subtract from record expiration to establish session expiration value
     */
    public CASSPSessionSerializer(@Duration @NonNegative long offset) {
        super(offset);
    }

    @Override
    protected void doSerializeAdditional(@Nonnull final SPSession instance, @Nonnull final JsonGenerator generator) {
        if (!(instance instanceof CASSPSession)) {
            throw new IllegalArgumentException("Expected instance of CASSPSession but got " + instance);
        }
        generator.write(TICKET_FIELD, ((CASSPSession) instance).getTicketId());
    }

    @Nonnull
    @Override
    protected SPSession doDeserialize(
            @Nonnull final JsonObject obj,
            @Nonnull @NotEmpty final String id,
            final long creation,
            final long expiration) throws IOException {
        return new CASSPSession(id, creation, expiration, obj.getString(TICKET_FIELD));
    }
}
