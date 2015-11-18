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

package net.shibboleth.idp.authn.principal.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Principal;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.shibboleth.idp.authn.principal.AbstractPrincipalSerializer;
import net.shibboleth.idp.authn.principal.PasswordPrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

/**
 * Principal serializer for {@link PasswordPrincipal} that encrypts the password.
 */
@ThreadSafe
public class PasswordPrincipalSerializer extends AbstractPrincipalSerializer<String> {

    /** Field name of password. */
    @Nonnull @NotEmpty private static final String PASSWORD_FIELD = "PW";

    /** Pattern used to determine if input is supported. */
    private static final Pattern JSON_PATTERN = Pattern.compile("^\\{\"PW\":.*\\}$");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PasswordPrincipalSerializer.class);

    /** Data sealer. */
    @NonnullAfterInit private DataSealer sealer;
    
    /** JSON object bulder factory. */
    @Nonnull private final JsonBuilderFactory objectBuilderFactory;

    /** Constructor. */
    public PasswordPrincipalSerializer() {
        objectBuilderFactory = Json.createBuilderFactory(null);
    }
    
    /**
     * Set the {@link DataSealer} to use.
     * 
     * @param theSealer encrypting component to use
     */
    public void setDataSealer(@Nonnull final DataSealer theSealer) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        sealer = Constraint.isNotNull(theSealer, "DataSealer cannot be null");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (sealer == null) {
            throw new ComponentInitializationException("DataSealer cannot be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean supports(@Nonnull final Principal principal) {
        return principal instanceof PasswordPrincipal;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String serialize(@Nonnull final Principal principal) throws IOException {
        final StringWriter sink = new StringWriter(32);
        final JsonGenerator gen = getJsonGenerator(sink);
        try {
            gen.writeStartObject()
               .write(PASSWORD_FIELD, sealer.wrap(principal.getName(),
                       System.currentTimeMillis() + DOMTypeSupport.durationToLong("P1Y")))
               .writeEnd();
        } catch (final DataSealerException e) {
            throw new IOException(e);
        }
        gen.close();
        return sink.toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean supports(@Nonnull @NotEmpty final String value) {
        return JSON_PATTERN.matcher(value).matches();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public PasswordPrincipal deserialize(@Nonnull @NotEmpty final String value) throws IOException {
        final JsonReader reader = getJsonReader(new StringReader(value));
        JsonStructure st = null;
        try {
            st = reader.read();
        } finally {
            reader.close();
        }
        if (!(st instanceof JsonObject)) {
            throw new IOException("Found invalid data structure while parsing PasswordPrincipal");
        }
        final JsonObject obj = (JsonObject) st;
        final JsonString str = obj.getJsonString(PASSWORD_FIELD);
        if (str != null) {
            if (!Strings.isNullOrEmpty(str.getString())) {
                try {
                    return new PasswordPrincipal(sealer.unwrap(str.getString()));
                } catch (final DataSealerException e) {
                    throw new IOException(e);
                }
            } else {
                log.warn("Skipping null/empty PasswordPrincipal");
            }
        }
        return null;
    }

    /**
     * Get a {@link JsonObjectBuilder} in a thread-safe manner.
     * 
     * @return  an object builder
     */
    @Nonnull private synchronized JsonObjectBuilder getJsonObjectBuilder() {
        return objectBuilderFactory.createObjectBuilder();
    }

    /**
     * Get a {@link JsonArrayBuilder} in a thread-safe manner.
     * 
     * @return  an array builder
     */
    @Nonnull private synchronized JsonArrayBuilder getJsonArrayBuilder() {
        return objectBuilderFactory.createArrayBuilder();
    }
    
}