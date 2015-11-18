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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.UnsupportedAttributeTypeException;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Implementation of Mapped Attributes. <br/>
 * An attribute definition that takes the values from previous resolution stages and converts them as it creates the
 * output attribute. Each value is compared with a lookup table (a {@link java.util.Collection} of @link{ValueMap}s) and
 * if it matches then the appropriate value(s) is/are substituted. Non matches are either passed through or are removed
 * depending on the setting 'passThru'.
 * */
@ThreadSafe
public class MappedAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(MappedAttributeDefinition.class);

    /** Value maps. */
    @Nonnull @NonnullElements private Set<ValueMap> valueMaps = Collections.emptySet();

    /** Whether the definition passes thru unmatched values. */
    private boolean passThru;

    /** Default return value. */
    @Nullable private StringAttributeValue defaultValue;

    /**
     * Gets the functions used to map an input value to an output value.
     * 
     * @return functions used to map an input value to an output value
     */
    @Nonnull @NonnullElements @Unmodifiable public Collection<ValueMap> getValueMaps() {
        return valueMaps;
    }

    /**
     * Sets the functions used to map an input value to an output value.
     * 
     * @param mappings functions used to map an input value to an output value
     */
    public void setValueMaps(@Nullable @NullableElements final Collection<ValueMap> mappings) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        valueMaps = ImmutableSet.copyOf(Iterables.filter(mappings, Predicates.notNull()));
    }

    /**
     * Gets the default return value.
     * 
     * @return the default return value.
     */
    @Nullable public StringAttributeValue getDefaultAttributeValue() {
        return defaultValue;
    }

    /**
     * Gets the default return value.
     * 
     * @return the default return value.
     */
    @Nullable public String getDefaultValue() {
        if (null == defaultValue) {
            return null;
        }
        return defaultValue.getValue();
    }

    /**
     * Sets the default return value.
     * 
     * @param newDefaultValue the default return value
     */
    public void setDefaultValue(@Nullable String newDefaultValue) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        String trimmedDefault = StringSupport.trimOrNull(newDefaultValue);
        if (null == trimmedDefault) {
            defaultValue = null;
        } else {
            defaultValue = new StringAttributeValue(trimmedDefault);
        }
    }

    /**
     * Gets whether the definition passes unmatched values through.
     * 
     * @return whether the definition passes unmatched values unchanged or suppresses them.
     */
    public boolean isPassThru() {
        return passThru;
    }

    /**
     * Sets whether the definition passes unmatched values through.
     * 
     * @param newPassThru whether the definition passes unmatched values unchanged or suppresses them.
     */
    public void setPassThru(boolean newPassThru) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        passThru = newPassThru;
    }

    /**
     * Maps the value from a dependency in to the value(s) for this attribute.
     * 
     * @param value the value from the dependency
     * 
     * @return the set of attribute values that the given dependency value maps in to
     */
    protected List<StringAttributeValue> mapValue(@Nullable String value) {
        log.debug("Attribute Definition {}: mapping dependency attribute value {}", getId(), value);

        final List<StringAttributeValue> mappedValues = new ArrayList<>();

        if (!Strings.isNullOrEmpty(value)) {
            boolean valueMapMatch = false;
            for (final ValueMap valueMap : valueMaps) {
                mappedValues.addAll(valueMap.apply(value));
                if (!mappedValues.isEmpty()) {
                    valueMapMatch = true;
                }
            }

            if (!valueMapMatch) {
                if (passThru) {
                    mappedValues.add(new StringAttributeValue(value));
                } else if (defaultValue != null) {
                    mappedValues.add(defaultValue);
                }
            }
        }

        log.debug("Attribute Definition {}: mapped dependency attribute value {} to the values {}", new Object[] {
                getId(), value, mappedValues,});

        return mappedValues;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        Constraint.isNotNull(resolutionContext, "Attribute resolution context can not be null");

        final List<IdPAttributeValue<?>> unmappedResults =
                PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies(), getId());
        log.debug("Attribute Definition '{}': Attempting to map the following values: {}", getId(), unmappedResults);

        // Bucket for results
        final IdPAttribute resultAttribute = new IdPAttribute(getId());

        if (unmappedResults == null || unmappedResults.isEmpty()) {
            log.debug("Attribute Definition {}: No values from dependencies", getId());
            if (null != defaultValue) {
                log.debug("Attribute Definition {}: Default value of {} added as the value for this attribute",
                        getId(), defaultValue);
                resultAttribute.setValues(Collections.singletonList(defaultValue));
            }
        } else {

            final List<StringAttributeValue> valueList = new ArrayList<>();
            for (final IdPAttributeValue<?> unmappedValue : unmappedResults) {
                if (unmappedValue instanceof EmptyAttributeValue) {
                    valueList.addAll(mapValue(null));                    
                } else if (unmappedValue instanceof StringAttributeValue) {
                    valueList.addAll(mapValue(((StringAttributeValue) unmappedValue).getValue()));
                } else {
                    throw new ResolutionException(new UnsupportedAttributeTypeException("Attribute definition '"
                            + getId() + "' does not support dependency values of type "
                            + unmappedValue.getClass().getName()));
                }
            }
            resultAttribute.setValues(valueList);
        }
        return resultAttribute;
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        valueMaps = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no dependencies were configured");
        }

        if (valueMaps.isEmpty()) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no value mappings were configured");
        }
    }

}