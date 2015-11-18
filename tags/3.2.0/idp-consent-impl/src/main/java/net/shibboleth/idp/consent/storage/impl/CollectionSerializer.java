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
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.storage.StorageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Serializes a {@link Collection} of strings. <code>Null</code> elements and non-string values are ignored.
 */
public class CollectionSerializer extends AbstractInitializableComponent implements
        StorageSerializer<Collection<String>> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CollectionSerializer.class);

    /** JSON generator factory. */
    @Nonnull private final JsonGeneratorFactory generatorFactory;

    /** JSON reader factory. */
    @Nonnull private final JsonReaderFactory readerFactory;

    /** Constructor. */
    public CollectionSerializer() {
        generatorFactory = Json.createGeneratorFactory(null);
        readerFactory = Json.createReaderFactory(null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String serialize(@Nonnull final Collection<String> instance) throws IOException {
        Constraint.isNotNull(instance, "Storage indexes cannot be null");

        final Collection<String> filteredInstance = Collections2.filter(instance, Predicates.notNull());

        final StringWriter sink = new StringWriter(128);
        final JsonGenerator gen = generatorFactory.createGenerator(sink);

        gen.writeStartArray();
        for (final String element : filteredInstance) {
            gen.write(element);
        }
        gen.writeEnd();
        gen.close();

        final String serialized = sink.toString();
        log.debug("Serialized '{}' as '{}'", instance, serialized);
        return serialized;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Collection<String> deserialize(final long version,
            @Nonnull @NotEmpty final String context, @Nonnull @NotEmpty final String key,
            @Nonnull @NotEmpty final String value, @Nullable final Long expiration) throws IOException {

        final JsonReader reader = readerFactory.createReader(new StringReader(value));
        final JsonStructure st = reader.read();
        if (!(st instanceof JsonArray)) {
            throw new IOException("Found invalid data structure");
        }

        final Collection<String> collection = new ArrayList<>();

        for (final JsonValue arrayValue : (JsonArray) st) {
            if (arrayValue.getValueType().equals(ValueType.STRING)) {
                collection.add(((JsonString) arrayValue).getString());
            }
        }

        log.debug("Deserialized context '{}' key '{}' value '{}' expiration '{}' as '{}'", new Object[] {context, key,
                value, expiration, collection,});
        return collection;
    }

}
