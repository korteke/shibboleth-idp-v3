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

package net.shibboleth.idp.attribute;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Each attribute represents one piece of information about a user and has associated encoders used to turn that
 * information in to protocol-specific formats.
 * 
 * Instances of {@link IdPAttribute} are compared using their IDs. That is, two attributes are considered the same if
 * they have the same ID, regardless of whether their display names, descriptions, values, or encoders are the same.
 */
@NotThreadSafe
public class IdPAttribute implements Comparable<IdPAttribute>, Cloneable {

    /** ID of this attribute. */
    private final String id;

    /** Localized human intelligible attribute names. */
    private Map<Locale, String> displayNames;

    /** Localized human readable descriptions of attribute. */
    private Map<Locale, String> displayDescriptions;

    /** Values for this attribute. */
    private List<IdPAttributeValue<?>> values;

    /** Encoders that may be used to encode this attribute. */
    private Set<AttributeEncoder<?>> encoders;

    /**
     * Constructor.
     * 
     * @param attributeId unique identifier of the attribute
     */
    public IdPAttribute(@Nonnull @NotEmpty final String attributeId) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(attributeId), "Attribute ID may not be null");

        displayNames = Collections.emptyMap();
        displayDescriptions = Collections.emptyMap();

        values = Collections.emptyList();
        encoders = Collections.emptySet();
    }

    /**
     * Gets the unique ID of the attribute. This ID need not be related to any protocol-specific attribute identifiers.
     * 
     * @return unique ID of the attribute
     */
    @Nonnull @NotEmpty public String getId() {
        return id;
    }

    /**
     * Gets the localized human readable name of the attribute.
     * 
     * @return human readable name of the attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }

    /**
     * Process input to {@link #setDisplayNames(Map)} and {{@link #setDisplayDescriptions(Map)} to strip out null input,
     * null keys, and null values.
     * 
     * @param inputMap the input map.
     * @return the unmodifiable, non null-containing output.
     */
    @Nonnull @NonnullElements @Unmodifiable private Map<Locale, String> checkedNamesFrom(
            @Nullable @NullableElements final Map<Locale, String> inputMap) {
        
        final ImmutableMap.Builder<Locale,String> builder = ImmutableMap.builder();

        if (inputMap != null) {
            for (final Entry<Locale,String> entry : inputMap.entrySet()) {
                if (entry.getKey() != null) {
                    final String trimmedName = StringSupport.trimOrNull(entry.getValue());
                    if (trimmedName != null) {
                        builder.put(entry.getKey(), trimmedName);
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * Replaces the existing display names for this attribute with the given ones.
     * 
     * @param newNames the new names for this attribute
     */
    public void setDisplayNames(@Nullable @NullableElements final Map<Locale, String> newNames) {
        displayNames = checkedNamesFrom(newNames);
    }

    /**
     * Gets the localized human readable description of attribute.
     * 
     * @return human readable description of attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayDescriptions() {
        return displayDescriptions;
    }

    /**
     * Replaces the existing display descriptions for this attribute with the given ones.
     * 
     * @param newDescriptions the new descriptions for this attribute
     */
    public void setDisplayDescriptions(@Nullable @NullableElements final Map<Locale, String> newDescriptions) {
        displayDescriptions = checkedNamesFrom(newDescriptions);
    }

    /**
     * Get the unmodifiable ordered collection of values of the attribute.
     * 
     * @return values of the attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public List<IdPAttributeValue<?>> getValues() {
        return values;
    }

    /**
     * Replaces the existing values for this attribute with the given values.
     * 
     * @param newValues the new values for this attribute
     */
    public void setValues(@Nullable @NullableElements final Collection<? extends IdPAttributeValue<?>> newValues) {
        final ImmutableList.Builder<IdPAttributeValue<?>> builder = ImmutableList.builder();
        if (newValues != null) {
            builder.addAll(Collections2.filter(newValues, Predicates.notNull()));
        }
        values = builder.build();
    }

    /**
     * Gets the list of attribute encoders usable with this attribute.
     * 
     * @return attribute encoders usable with this attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public Set<AttributeEncoder<?>> getEncoders() {
        return encoders;
    }

    /**
     * Replaces the existing encoders for this attribute with the given encoders.
     * 
     * @param newEncoders the new encoders for this attribute
     */
    public void setEncoders(@Nullable @NullableElements final Collection<AttributeEncoder<?>> newEncoders) {
        final ImmutableSet.Builder<AttributeEncoder<?>> builder = ImmutableSet.builder();
        if (newEncoders != null) {
            builder.addAll(Collections2.filter(newEncoders, Predicates.notNull()));
        }
        encoders = builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final IdPAttribute other) {
        return getId().compareTo(other.getId());
    }

    /**
     * Clones an attribute. The clone will contains defensive copies of this objects display descriptions and names,
     * encoders, and values. The elements of each collection, however, are not themselves cloned.
     * 
     * {@inheritDoc}
     */
    @Override
    @Nonnull public IdPAttribute clone() throws CloneNotSupportedException {
        final IdPAttribute clone = (IdPAttribute) super.clone();
        clone.setDisplayDescriptions(getDisplayDescriptions());
        clone.setDisplayNames(getDisplayNames());
        clone.setEncoders(getEncoders());
        clone.setValues(getValues());
        return clone;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(id, values);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof IdPAttribute)) {
            return false;
        }

        IdPAttribute other = (IdPAttribute) obj;
        return java.util.Objects.equals(id, other.getId());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("displayNames", displayNames)
                .add("displayDescriptions", displayDescriptions).add("encoders", encoders).add("values", values)
                .toString();
    }
}