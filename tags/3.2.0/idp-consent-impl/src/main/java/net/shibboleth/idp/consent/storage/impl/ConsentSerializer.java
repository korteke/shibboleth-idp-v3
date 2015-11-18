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

package net.shibboleth.idp.consent.storage.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.storage.StorageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Serializes {@link Consent}.
 */
public class ConsentSerializer extends AbstractInitializableComponent implements
        StorageSerializer<Map<String, Consent>> {

    /** Field name of consent identifier. */
    @Nonnull @NotEmpty private static final String ID_FIELD = "id";

    /** Field name of consent value. */
    @Nonnull @NotEmpty private static final String VALUE_FIELD = "v";

    /** Field name of whether consent is approved. */
    @Nonnull @NotEmpty private static final String IS_APPROVED_FIELD = "appr";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ConsentSerializer.class);

    /** JSON generator factory. */
    @Nonnull private final JsonGeneratorFactory generatorFactory;

    /** JSON reader factory. */
    @Nonnull private final JsonReaderFactory readerFactory;

    /** Shrink consent IDs into symbolic numbers. */
    @Nonnull @NonnullElements private BiMap<String, Integer> symbolics;

    /** Constructor. */
    public ConsentSerializer() {
        generatorFactory = Json.createGeneratorFactory(null);
        readerFactory = Json.createReaderFactory(null);
        symbolics = ImmutableBiMap.of();
    }

    /**
     * Set mappings of string constants to symbolic constants.
     * 
     * @param mappings string to symbolic mappings
     */
    public void setSymbolics(@Nonnull @NonnullElements final Map<String, Integer> mappings) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        symbolics = HashBiMap.create(Constraint.isNotNull(mappings, "Symbolic mappings cannot be null"));
        
        log.debug("symbolics '{}'", symbolics);
    }

    /**
     * Map a field value to a string, either directly or via the symbolic map.
     * 
     * @param field the object field to examine
     * 
     * @return the resulting string, or null if invalid
     */
    @Nullable protected String desymbolize(@Nonnull final JsonValue field) {
        if (field == null) {
            return null;
        }
        switch (field.getValueType()) {
            case STRING:
                return ((JsonString) field).getString();

            case NUMBER:
                return symbolics.inverse().get(((JsonNumber) field).intValueExact());

            default:
                return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Map<String, Consent>
            deserialize(final long version, @Nonnull @NotEmpty final String context,
                    @Nonnull @NotEmpty final String key, @Nonnull @NotEmpty final String value,
                    @Nullable final Long expiration) throws IOException {

        try (final JsonReader reader = readerFactory.createReader(new StringReader(value))) {
            final JsonStructure st = reader.read();

            if (!(st instanceof JsonArray)) {
                throw new IOException("Found invalid data structure while parsing consent");
            }
            final JsonArray array = (JsonArray) st;

            final Map<String, Consent> consents = new LinkedHashMap<>();

            for (final JsonValue a : array) {
                if (a.getValueType().equals(ValueType.OBJECT)) {
                    final JsonObject o = (JsonObject) a;

                    final String consentId = desymbolize(o.get(ID_FIELD));
                    if (consentId == null) {
                        log.warn("Unparseable consent id in structure");
                        continue;
                    }

                    final Consent consent = new Consent();
                    consent.setId(consentId);
                    if (o.containsKey(VALUE_FIELD)) {
                        consent.setValue(o.getString(VALUE_FIELD));
                    }
                    consent.setApproved(o.getBoolean(IS_APPROVED_FIELD, true));
                    consents.put(consent.getId(), consent);
                }
            }

            log.debug("Deserialized context '{}' key '{}' value '{}' expiration '{}' as '{}'", new Object[] {context,
                    key, value, expiration, consents,});
            return consents;
        } catch (final NullPointerException | ClassCastException | ArithmeticException | JsonException e) {
            log.error("Exception while parsing consent", e);
            throw new IOException("Found invalid data structure while parsing consent", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String serialize(@Nonnull final Map<String, Consent> consents) throws IOException {
        Constraint.isNotNull(consents, "Consents cannot be null");

        final Collection<Consent> filteredConsents = Collections2.filter(consents.values(), Predicates.notNull());
        Constraint.isNotEmpty(filteredConsents, "Consents cannot be empty");

        final StringWriter sink = new StringWriter(128);
        final JsonGenerator gen = generatorFactory.createGenerator(sink);

        gen.writeStartArray();
        for (final Consent consent : filteredConsents) {
            gen.writeStartObject();
            final Integer symbol = symbolics.get(consent.getId());
            if (symbol != null) {
                gen.write(ID_FIELD, symbol);
            } else {
                gen.write(ID_FIELD, consent.getId());
            }
            if (consent.getValue() != null) {
                gen.write(VALUE_FIELD, consent.getValue());
            }
            if (!consent.isApproved()) {
                gen.write(IS_APPROVED_FIELD, false);
            }
            gen.writeEnd();
        }
        gen.writeEnd();
        gen.close();

        final String serialized = sink.toString();
        log.debug("Serialized '{}' as '{}'", consents, serialized);
        return serialized;
    }

}
