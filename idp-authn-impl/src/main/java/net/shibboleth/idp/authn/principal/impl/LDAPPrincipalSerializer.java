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
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.jaas.LdapPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.shibboleth.idp.authn.principal.AbstractPrincipalSerializer;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Principal serializer for {@link LdapPrincipal}.
 */
@ThreadSafe
public class LDAPPrincipalSerializer extends AbstractPrincipalSerializer<String> {

    /** Field name of principal name. */
    @Nonnull @NotEmpty private static final String PRINCIPAL_NAME_FIELD = "LDAPN";

    /** Field name of principal entry. */
    @Nonnull @NotEmpty private static final String PRINCIPAL_ENTRY_FIELD = "LDAPE";

    /** Pattern used to determine if input is supported. */
    private static final Pattern JSON_PATTERN = Pattern.compile("^\\{\"LDAPN\":.*,\"LDAPE\":.*\\}$");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(LDAPPrincipalSerializer.class);
    
    /** JSON object bulder factory. */
    @Nonnull private final JsonBuilderFactory objectBuilderFactory;

    /** Constructor. */
    public LDAPPrincipalSerializer() {
        objectBuilderFactory = Json.createBuilderFactory(null);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean supports(@Nonnull final Principal principal) {
        return principal instanceof LdapPrincipal;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String serialize(@Nonnull final Principal principal) throws IOException {
        final StringWriter sink = new StringWriter(32);
        final JsonGenerator gen = getJsonGenerator(sink);
        gen.writeStartObject()
           .write(PRINCIPAL_NAME_FIELD, principal.getName());
        final LdapEntry entry = ((LdapPrincipal) principal).getLdapEntry();
        if (entry != null) {
            final JsonObjectBuilder objectBuilder = getJsonObjectBuilder();
            objectBuilder.add("dn", entry.getDn());
            for (LdapAttribute attr : entry.getAttributes()) {
                final JsonArrayBuilder arrayBuilder = getJsonArrayBuilder();
                for (String value : attr.getStringValues()) {
                    arrayBuilder.add(value);
                }
                objectBuilder.add(attr.getName(), arrayBuilder.build());
            }
            gen.write(PRINCIPAL_ENTRY_FIELD, objectBuilder.build());
        }
        gen.writeEnd();
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
    @Nullable public LdapPrincipal deserialize(@Nonnull @NotEmpty final String value) throws IOException {
        final JsonReader reader = getJsonReader(new StringReader(value));
        JsonStructure st = null;
        try {
            st = reader.read();
        } finally {
            reader.close();
        }
        if (!(st instanceof JsonObject)) {
            throw new IOException("Found invalid data structure while parsing LdapPrincipal");
        }
        final JsonObject obj = (JsonObject) st;
        final JsonString str = obj.getJsonString(PRINCIPAL_NAME_FIELD);
        if (str != null) {
            if (!Strings.isNullOrEmpty(str.getString())) {
                LdapEntry entry = null;
                final JsonObject jsonEntry = obj.getJsonObject(PRINCIPAL_ENTRY_FIELD);
                if (jsonEntry != null) {
                    entry = new LdapEntry();
                    for (Map.Entry<String, JsonValue> e : jsonEntry.entrySet()) {
                        if ("dn".equalsIgnoreCase(e.getKey())) {
                            entry.setDn(((JsonString) e.getValue()).getString());
                        } else {
                            final LdapAttribute attr = new LdapAttribute(e.getKey());
                            for (JsonValue v : (JsonArray) e.getValue()) {
                                attr.addStringValue(((JsonString) v).getString());
                            }
                            entry.addAttribute(attr);
                        }
                    }
                }
                return new LdapPrincipal(str.getString(), entry);
            } else {
                log.warn("Skipping null/empty LdapPrincipal");
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