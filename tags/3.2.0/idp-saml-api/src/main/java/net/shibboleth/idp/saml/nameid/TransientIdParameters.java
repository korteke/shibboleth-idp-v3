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

package net.shibboleth.idp.saml.nameid;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonGenerator;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Parameters we need to store in, and get out of a transient ID, namely the attribute recipient (aka the SP) and
 * the principal. Having this as a separate class allows streamlining of the encoding/decoding.
 */
public class TransientIdParameters {
    /** Context label for storage of IDs. */
    @Nonnull @NotEmpty public static final String CONTEXT = "TransientId";
    
    /** Field name of creation instant. */
    private static final String ATTRIBUTE_RECIPIENT_FIELD = "sp";

    /** Field name of principal name. */
    private static final String PRINCIPAL_FIELD = "princ";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(TransientIdParameters.class);

    /** The Attribute Recipient. */
    private final String attributeRecipient;

    /** The principal. */
    private final String principal;

    /**
     * Constructor for the attribute definition.
     * 
     * @param recipient the SP
     * @param thePrincipal the user
     */
    public TransientIdParameters(@Nullable final String recipient, @Nullable final String thePrincipal) {
        attributeRecipient = recipient;
        principal = thePrincipal;
    }

    /**
     * Constructor for the decoding definitions.
     * 
     * @param encoded the JSON encoded data
     * @throws IOException if decoding failed
     */
    public TransientIdParameters(@Nonnull @NotEmpty final String encoded) throws IOException {
        Constraint.isNotNull(StringSupport.trimOrNull(encoded), "encoded data must not be null or empty");

        final JsonReader reader = Json.createReader(new StringReader(encoded));
        final JsonStructure st = reader.read();

        if (!(st instanceof JsonObject)) {
            throw new IOException("Found invalid data structure while parsing IdPSession");
        }
        final JsonObject jsonObj = (JsonObject) st;

        principal = jsonObj.getString(PRINCIPAL_FIELD);
        attributeRecipient = jsonObj.getString(ATTRIBUTE_RECIPIENT_FIELD);
    }

    /**
     * Get the SP.
     * 
     * @return the sp.
     */
    @Nullable public String getAttributeRecipient() {
        return attributeRecipient;
    }

    /**
     * Get the Principal.
     * 
     * @return the principal
     */
    @Nullable public String getPrincipal() {
        return principal;
    }

    /**
     * Encode up for storing.
     * 
     * @return the encoded string.
     * @throws IOException if encoding failed
     */
    @Nonnull public String encode() throws IOException {
        try {
            final StringWriter sink = new StringWriter(128);
            final JsonGenerator gen = Json.createGenerator(sink);
            gen.writeStartObject().write(ATTRIBUTE_RECIPIENT_FIELD, getAttributeRecipient())
                    .write(PRINCIPAL_FIELD, getPrincipal());
            gen.writeEnd().close();

            return sink.toString();
        } catch (final JsonException e) {

            log.error("Exception while serializing IdPSession", e);
            throw new IOException("Exception while serializing IdPSession", e);
        }
    }
}
