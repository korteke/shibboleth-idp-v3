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

package net.shibboleth.idp.attribute.resolver.ad.mapped.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Performs many to one mapping of source values to a return value. SourceValue strings may include regular expressions
 * and the ReturnValue may include back references to capturing groups as supported by {@link java.util.regex.Pattern}.
 */
public class ValueMap implements Function<String, Set<StringAttributeValue>> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ValueMap.class);

    /** Return value. */
    private String returnValue;

    /** Source values. */
    @Nonnull @NonnullElements private Collection<SourceValue> sourceValues;

    /** Constructor. */
    public ValueMap() {
        sourceValues = Collections.emptySet();
    }

    /**
     * Gets the return value.
     *
     * @return the return value
     */
    @Nullable public String getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the return value.
     *
     * @param newReturnValue the return value
     */
    public void setReturnValue(@Nonnull @NotEmpty String newReturnValue) {
        returnValue =
                Constraint.isNotNull(StringSupport.trimOrNull(newReturnValue), "ReturnValue must be non null or empty");
    }

    /**
     * Sets the Source values for the mapping.
     *
     * @param newValues functions used to map an input value to an output value
     */
    public void setSourceValues(@Nullable @NullableElements final Collection<SourceValue> newValues) {

        sourceValues = ImmutableSet.copyOf(Iterables.filter(newValues, Predicates.notNull()));
    }

    /**
     * Gets the collection of source values.
     *
     * @return the collection of source values
     */
    @Nonnull @NonnullElements @Unmodifiable public Collection<SourceValue> getSourceValues() {
        return sourceValues;
    }

    /**
     * Evaluate an incoming attribute value against this value map.
     *
     * @param attributeValue incoming attribute value
     * @return set of new values the incoming value mapped to
     */
    /** {@inheritDoc} */
    @Override
    @Nullable public Set<StringAttributeValue> apply(@Nullable String attributeValue) {
        
        if (attributeValue == null) {
            log.debug("Input value was null, returning empty set");
            return Collections.emptySet();
        }
        
        log.debug("Attempting to map attribute value '{}'", attributeValue);
        final Set<StringAttributeValue> mappedValues = new HashSet<>();

        for (final SourceValue sourceValue : sourceValues) {
            String newValue = null;

            if (sourceValue.isPartialMatch()) {
                log.debug("Performing partial match comparison.");
                if (sourceValue.getValue() == null) {
                    log.debug("Source value was null, no partial match");
                } else if (attributeValue.contains(sourceValue.getValue())) {
                    newValue = returnValue;
                    log.debug("Attribute value '{}' matches source value '{}' it will be mapped to '{}'", new Object[] {
                            attributeValue, sourceValue.getValue(), newValue,});
                }
            } else {
                log.debug("Performing regular expression based comparison");
                try {
                    final Matcher m = sourceValue.getPattern().matcher(attributeValue);
                    if (m.matches()) {
                        newValue = returnValue != null ? m.replaceAll(returnValue) : null;
                        log.debug("Attribute value '{}' matches regular expression it will be mapped to '{}'",
                                attributeValue, newValue);
                    }
                } catch (final PatternSyntaxException e) {
                    log.debug("Error matching value {}.  Skipping this value.", attributeValue);
                }
            }

            if (newValue != null) {
                mappedValues.add(new StringAttributeValue(newValue));
            }
        }

        return mappedValues;
    }

}