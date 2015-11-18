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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.scripted.ScriptedIdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An encapsulated Attribute suitable for handing to scripts. This handles some of the cumbersome issues associated with
 * {@link IdPAttribute} and also a lot of the V2 backwards compatibility stuff. <br/>
 * NOTE, the java signature for this class may and will change on minor version changes. However the Scripting interface
 * will remain the same (methods will never be removed).
 */
public class ScriptedIdPAttributeImpl implements ScriptedIdPAttribute {

    /** The {@link IdPAttribute} we are encapsulating. */
    private final IdPAttribute encapsulatedAttribute;

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptedIdPAttributeImpl.class);

    /** has method {@link #getNativeAttribute()} be called. */
    private boolean calledGetNativeAttribute;

    /**
     * All the {@link StringAttributeValue}, but as strings.<br/>
     * All other attributes as their native representation. If null then the {@link #getValues()} method has not been
     * called.
     */
    private Collection<Object> attributeValues;

    /** The prefix for logging. Derived from the definition's logPrefix and the attribute ID. */
    private final String logPrefix;

    /**
     * Constructor.
     * 
     * @param attribute the attribute we are encapsulating.
     * @param prefix the log path from the definition.
     */
    public ScriptedIdPAttributeImpl(@Nonnull final IdPAttribute attribute, final String prefix) {
        encapsulatedAttribute = attribute;

        logPrefix =
                new StringBuilder(prefix).append(" scripted attribute '").append(attribute.getId()).append("':")
                        .toString();
    }

    /**
     * We use an internal list of attribute values to allow the legacy use of getValues().add().
     */
    private void setupAttributeValues() {
        if (null != attributeValues) {
            return;
        }
        log.debug("{} values being prepared", getLogPrefix());

        // NOTE. This has to be a List - the examples use get(0)
        final ArrayList<Object> newValues = new ArrayList<>(encapsulatedAttribute.getValues().size());
        for (final IdPAttributeValue value : encapsulatedAttribute.getValues()) {
            if ((value instanceof StringAttributeValue) && !(value instanceof ScopedStringAttributeValue)) {
                newValues.add(((StringAttributeValue) value).getValue());
            } else if (value instanceof EmptyAttributeValue) {
                // Shib2 made both empty strings and nulls null
                newValues.add(null);
            } else {
                newValues.add(value);
            }
        }
        attributeValues = newValues;
        log.debug("{} values are : {}", getLogPrefix(), newValues);
    }

    /**
     * Return all the values, but with {@link StringAttributeValue} values returned as strings.<br/>
     * This method is a helper method for V2 compatibility.
     * 
     * @return a modifiable collection of the string attributes (not the String
     * @throws ResolutionException if the script has called {@link #getNativeAttribute()}
     */
    @Override @Nullable @NonnullElements public Collection<Object> getValues() throws ResolutionException {
        if (calledGetNativeAttribute) {
            throw new ResolutionException(getLogPrefix()
                    + " cannot call getNativeAttribute() and getValues() or addValues() on the same attribute()");
        }
        if (null == attributeValues) {
            setupAttributeValues();
        }

        return attributeValues;
    }

    /**
     * return the underlying attribute.
     * 
     * @return the attribute
     * @throws ResolutionException if the script has called getValues.
     */
    @Override @Nonnull public IdPAttribute getNativeAttribute() throws ResolutionException {
        if (null != attributeValues) {
            throw new ResolutionException(getLogPrefix()
                    + "': cannot call getNativeAttribute() and getValues()/setValues() on the same attribute()");
        }
        calledGetNativeAttribute = true;
        return encapsulatedAttribute;
    }

    /**
     * Get the encapsulated attributeId.
     * 
     * @return the id
     */
    @Override @Nonnull @NotEmpty public String getId() {
        return encapsulatedAttribute.getId();
    }

    /**
     * Add the provided value to the provided list, converting {@link String} to {@link StringAttributeValue}.
     * 
     * @param values the list to add to.
     * @param value the value to add. Known to be a {@link String} or an {@link IdPAttributeValue}
     */
    private void addAsIdPAttributeValue(final List<IdPAttributeValue<?>> values, final Object value) {
        if (null == value) {
            values.add(new EmptyAttributeValue(EmptyType.NULL_VALUE));
        } else if (value instanceof String) {
            values.add(StringAttributeValue.valueOf((String) value));
        } else {
            values.add((IdPAttributeValue) value);
        }
    }

    /**
     * Check that provided object is of type {@link String} or {@link IdPAttributeValue}.
     * 
     * @param what value to check
     * @throws ResolutionException if there is a type conflict.
     */
    private void policeValueType(@Nullable final Object what) throws ResolutionException {
        if (null == what) {
            // This is OK, weird but OK
        } else if (!(what instanceof String) && !(what instanceof IdPAttributeValue)) {
            throw new ResolutionException(getLogPrefix()
                    + " added element must be a String or AttributeValue, provided = " + what.getClass().toString());
        }
    }

    /**
     * Add the provided object to the attribute values, policing for type.
     * 
     * @param what a {@link String} or a {@link IdPAttributeValue} to add.
     * @throws ResolutionException if the provided value is of the wrong type
     */
    @Override public void addValue(@Nullable final Object what) throws ResolutionException {
        policeValueType(what);

        if (null == attributeValues) {
            setupAttributeValues();
        }

        attributeValues.add(what);
    }

    /**
     * Function to reconstruct the attribute after the scripting. If {@link #getValues()} has been called then this is
     * taken as the content and the attribute updated, otherwise the Attribute is returned.
     * 
     * @return a suitable modified attribute.
     * @throws ResolutionException if we find the wrong type.
     */
    @Nonnull protected IdPAttribute getResultingAttribute() throws ResolutionException {

        if (null == attributeValues) {
            log.debug("{} return initial attribute unchanged", getLogPrefix());
            return encapsulatedAttribute;
        }

        // Otherwise re-marshall the {@link #attributeValues}
        final List<IdPAttributeValue<?>> valueList = new ArrayList<>(attributeValues.size());

        log.debug("{} recreating attribute contents from {}", getLogPrefix(), attributeValues);
        for (final Object object : attributeValues) {
            policeValueType(object);
            addAsIdPAttributeValue(valueList, object);
        }
        encapsulatedAttribute.setValues(valueList);
        log.debug("{} recreated attribute contents are {}", getLogPrefix(), valueList);
        return encapsulatedAttribute;
    }

    /**
     * The prefix for the logs.
     * 
     * @return the prefix
     */
    @Nonnull protected String getLogPrefix() {
        return logPrefix;
    }
}
