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

package net.shibboleth.idp.attribute.filter.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.MapConstraints;

/**
 * A context which carries and collects information through the attribute filtering process, and coordinates data
 * between the filter implementation and the various resolver MatchFunctor implementations.
 * 
 * <p>
 * This should be considered a private API limited to plugin implementations.
 * </p>
 */
@NotThreadSafe
public final class AttributeFilterWorkContext extends BaseContext {

    /** Values, for a given attribute, that are permitted to be released. */
    private final Map<String, Set<IdPAttributeValue>> permittedValues;

    /** Values, for a given attribute, that are not permitted to be released. */
    private final Map<String, Set<IdPAttributeValue>> deniedValues;

    /** How to get from hus to the SP metadata context. */
    /** Constructor. */
    public AttributeFilterWorkContext() {
        permittedValues =
                MapConstraints.constrainedMap(new HashMap<String, Set<IdPAttributeValue>>(), MapConstraints.notNull());
        deniedValues =
                MapConstraints.constrainedMap(new HashMap<String, Set<IdPAttributeValue>>(), MapConstraints.notNull());
    }

    /**
     * Gets the collection of attribute values, indexed by ID, that are permitted to be released.
     * 
     * @return collection of attribute values, indexed by ID, that are permitted to be released,
     */
    @Nonnull @NonnullElements @Unmodifiable public
            Map<String, Set<IdPAttributeValue>> getPermittedIdPAttributeValues() {
        return Collections.unmodifiableMap(permittedValues);
    }

    /**
     * Adds a collection of attribute values that are permitted to be released. Attempting to add values for an
     * attribute that is not a member of {@link AttributeFilterContext#getPrefilteredIdPAttributes()} will result in an
     * {@link IllegalArgumentException}. Attempting to add an attribute value that is not a member of
     * {@link IdPAttribute#getValues()} will result in an {@link IllegalArgumentException}.
     * 
     * @param attributeId ID of the attribute whose values are permitted to be released
     * @param attributeValues values for the attribute that are permitted to be released
     */
    public void addPermittedIdPAttributeValues(@Nonnull @NotEmpty String attributeId,
            @Nullable @NullableElements Collection<? extends IdPAttributeValue> attributeValues) {
        AttributeFilterContext parent = (AttributeFilterContext) getParent();
        final Map<String, IdPAttribute> prefilteredAttributes = parent.getPrefilteredIdPAttributes();
        String trimmedAttributeId =
                Constraint.isNotNull(StringSupport.trimOrNull(attributeId), "Attribute ID can not be null or empty");
        Constraint.isTrue(prefilteredAttributes.containsKey(trimmedAttributeId), "no attribute with ID "
                + trimmedAttributeId + " exists in the pre-filtered attribute set");

        if (attributeValues == null || attributeValues.isEmpty()) {
            return;
        }

        Set<IdPAttributeValue> permittedAttributeValues = permittedValues.get(trimmedAttributeId);
        if (permittedAttributeValues == null) {
            permittedAttributeValues = new HashSet<>();
            permittedValues.put(trimmedAttributeId, permittedAttributeValues);
        }

        for (IdPAttributeValue value : attributeValues) {
            if (value != null) {
                if (!prefilteredAttributes.get(trimmedAttributeId).getValues().contains(value)) {
                    throw new IllegalArgumentException("permitted value is not a current value of attribute "
                            + trimmedAttributeId);
                }

                if (!permittedAttributeValues.contains(value)) {
                    permittedAttributeValues.add(value);
                }
            }
        }
    }

    /**
     * Gets the unmodifiable collection of attribute values, indexed by ID, that are not permitted to be released.
     * 
     * @return collection of attribute values, indexed by ID, that are not permitted to be released
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, Set<IdPAttributeValue>> getDeniedAttributeValues() {
        return Collections.unmodifiableMap(deniedValues);
    }

    /**
     * Adds a collection of attribute values that are not permitted to be released. Attempting to add values for an
     * attribute that is not a member of {@link AttributeFilterContext#getPrefilteredIdPAttributes()} will result in an
     * {@link IllegalArgumentException}. Attempting to add an attribute value that is not a member of
     * {@link IdPAttribute#getValues()} will result in an {@link IllegalArgumentException}.
     * 
     * @param attributeId ID of the attribute whose values are not permitted to be released
     * @param attributeValues values for the attribute that are not permitted to be released
     */
    public void addDeniedIdPAttributeValues(@Nonnull @NotEmpty String attributeId,
            @Nullable @NullableElements Collection<? extends IdPAttributeValue> attributeValues) {
        AttributeFilterContext parent = (AttributeFilterContext) getParent();
        final Map<String, IdPAttribute> prefilteredAttributes = parent.getPrefilteredIdPAttributes();
        final String trimmedAttributeId =
                Constraint.isNotNull(StringSupport.trimOrNull(attributeId), "Attribute ID can not be null or empty");
        Constraint.isTrue(prefilteredAttributes.containsKey(trimmedAttributeId), "No attribute with ID "
                + trimmedAttributeId + " exists in the pre-filtered attribute set");

        if (attributeValues == null || attributeValues.isEmpty()) {
            return;
        }

        Set<IdPAttributeValue> deniedAttributeValues = deniedValues.get(trimmedAttributeId);
        if (deniedAttributeValues == null) {
            deniedAttributeValues = new HashSet<>();
            deniedValues.put(trimmedAttributeId, deniedAttributeValues);
        }

        for (IdPAttributeValue value : attributeValues) {
            if (value != null) {
                if (!prefilteredAttributes.get(trimmedAttributeId).getValues().contains(value)) {
                    throw new IllegalArgumentException("denied value is not a current value of attribute "
                            + trimmedAttributeId);
                }

                if (!deniedAttributeValues.contains(value)) {
                    deniedAttributeValues.add(value);
                }
            }
        }
    }

}