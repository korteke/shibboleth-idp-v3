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

package net.shibboleth.idp.authn.principal;

import java.io.Reader;
import java.io.Writer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;

/**
 * Base class for {@link PrincipalSerializer} implementations.
 * 
 * @param <Type> generic type of serialization
 */
@ThreadSafe
public abstract class AbstractPrincipalSerializer<Type> extends AbstractInitializableComponent
        implements PrincipalSerializer<Type> {

    /** JSON generator factory. */
    @Nonnull private final JsonGeneratorFactory generatorFactory;

    /** JSON reader factory. */
    @Nonnull private final JsonReaderFactory readerFactory;

    /**
     * Constructor.
     */
    public AbstractPrincipalSerializer() {
        generatorFactory = Json.createGeneratorFactory(null);
        readerFactory = Json.createReaderFactory(null);
    }

    /**
     * Get a {@link JsonGenerator}, synchronized for thread-safety.
     * 
     * @param writer destination for output
     * 
     * @return a generator
     */
    @Nonnull protected synchronized JsonGenerator getJsonGenerator(@Nonnull final Writer writer) {
        return generatorFactory.createGenerator(writer);
    }

    /**
     * Get a {@link JsonReader}, synchronized for thread-safety.
     * 
     * @param reader source of input
     * 
     * @return a reader
     */
    @Nonnull protected synchronized JsonReader getJsonReader(@Nonnull final Reader reader) {
        return readerFactory.createReader(reader);
    }

}