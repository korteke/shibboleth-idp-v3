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

package net.shibboleth.idp.saml.attribute.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Basis of all classes which map SAML2 {@link Attribute} into an IdP {@link IdPAttribute}.
 * 
 * @param <InType> the input (SAML2 attribute) type
 * @param <OutType> the output (IdP Attribute) type
 */
public abstract class AbstractSAMLAttributeMapper<InType extends Attribute, OutType extends IdPAttribute> extends
        AbstractIdentifiableInitializableComponent implements AttributeMapper<InType, OutType> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributeMapper.class);

    /** The internal names to generate. */
    @Nonnull @NonnullElements private List<String> attributeIds = Collections.emptyList();

    /** The attribute format. */
    @Nullable private String attributeFormat;

    /** the (SAML) attribute name. */
    @NonnullAfterInit private String theSAMLName;

    /** The String used to prefix log message. */
    @Nullable private String logPrefix;

    /** The value mapper. */
    @NonnullAfterInit private AbstractSAMLAttributeValueMapper valueMapper;

    /**
     * Set the list of internal identifiers.
     * 
     * @param theIds the list
     */
    public void setAttributeIds(@Nullable @NullableElements final List<String> theIds) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        if (null == theIds) {
            return;
        }

        attributeIds = ImmutableList.copyOf(StringSupport.normalizeStringCollection(theIds));
    }

    /**
     * Get the list of internal identifiers.
     * 
     * @return the identifiers
     */
    @Nonnull @NonnullElements @Unmodifiable public List<String> getAttributeIds() {
        return attributeIds;
    }

    /**
     * Set the SAML attribute name.
     * 
     * @param name the name
     */
    public void setSAMLName(@Nullable final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        theSAMLName = StringSupport.trimOrNull(name);
    }

    /**
     * Get the SAML attribute name.
     * 
     * @return the name
     */
    @NonnullAfterInit public String getSAMLName() {
        return theSAMLName;
    }

    /**
     * Get the class which converts types.
     * 
     * @param mapper The valueMapper to set.
     */
    public void setValueMapper(@Nullable final AbstractSAMLAttributeValueMapper mapper) {
        valueMapper = mapper;
    }

    /**
     * Get the class which converts types.
     * 
     * @return Returns the valueMapper.
     */
    @NonnullAfterInit public AbstractSAMLAttributeValueMapper getValueMapper() {
        return valueMapper;
    }

    /**
     * Set the (optional) attribute format.
     * 
     * @param format the format
     */
    public void setAttributeFormat(@Nullable final String format) {
        attributeFormat = StringSupport.trimOrNull(format);
    }

    /**
     * Get the (optional) attribute format.
     * 
     * @return the format
     */
    @Nullable public String getAttributeFormat() {
        return attributeFormat;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == theSAMLName) {
            throw new ComponentInitializationException(getLogPrefix() + " SAML name not present");
        }
        if (null == valueMapper) {
            throw new ComponentInitializationException(getLogPrefix() + " No value mapper present");
        }
        if (attributeIds.isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix()
                    + " At least one attribute Id should be provided");
        }
        logPrefix = null;
        valueMapper.setLogPrefix(getLogPrefix());
    }

    /**
     * Compare whether the name and format given "match" ours. Used in checking both against input attributes and as
     * part of equality checking. For the sake of comparison null is considered to be the same as
     * {@link Attribute#UNSPECIFIED}
     * 
     * @param otherSAMLName the name to compare against
     * @param otherSAMLFormat the format to compare against
     * @return whether there is a match.
     */
    protected boolean matches(@Nonnull @NotEmpty final String otherSAMLName, @Nullable final String otherSAMLFormat) {
        if (!otherSAMLName.equals(theSAMLName)) {
            log.debug("{} SAML attribute name {} does not match {}", getLogPrefix(), otherSAMLName, getId());
            return false;
        }

        String format = otherSAMLFormat;
        if (format != null && Attribute.UNSPECIFIED.equals(format)) {
            format = null;
        }

        if (getAttributeFormat() != null && format != null && !getAttributeFormat().equals(format)
                && !Attribute.UNSPECIFIED.equals(getAttributeFormat())) {
            log.debug("{} SAML name format {} does not match {}", getLogPrefix(), format, getAttributeFormat());
            return false;
        }
        return true;

    }

    /**
     * Determines if the attribute matches the provided parameterisation.
     * 
     * @param attribute the attribute to consider
     * @return whether it matches.
     */
    protected boolean attributeMatches(@Nonnull final InType attribute) {
        return matches(attribute.getName(), attribute.getNameFormat());
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Map<String,OutType> mapAttribute(@Nonnull final InType prototype) {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (!attributeMatches(prototype)) {
            return Collections.emptyMap();
        }

        final List<XMLObject> inputValues = prototype.getAttributeValues();
        final List<IdPAttributeValue<?>> outputValues = getValueMapper().decodeValues(inputValues);

        if (!inputValues.isEmpty() && outputValues.isEmpty()) {
            log.warn("{} Attribute '{}' value conversion yielded no suitable values", getLogPrefix(),
                    prototype.getName());
            return Collections.emptyMap();
        }

        final Map<String,OutType> output = new HashMap<>(inputValues.size());

        log.debug("{} attribute id {} and aliases {} will be created", getLogPrefix(), getId(), getAttributeIds());

        OutType out = newAttribute(prototype, getId());
        out.setValues(outputValues);

        for (String id : attributeIds) {
            out = newAttribute(prototype, getId());
            out.setValues(outputValues);
            output.put(id, out);
        }
        return output;
    }

    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "<type> Attribute Mapper '<mapper ID>' :"
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        // local cache of cached entry to allow unsynchronised clearing.
        String prefix = logPrefix;
        if (null == prefix) {
            StringBuilder builder = new StringBuilder();
            if (null != getValueMapper()) {
                builder.append(getValueMapper().getAttributeTypeName()).append(" ");
            }

            builder.append("Attribute Mapper '").append(getId()).append("':");
            prefix = builder.toString();
            if (null == logPrefix) {
                logPrefix = prefix;
            }
        }
        return prefix;
    }

    /**
     * {@inheritDoc}. The identity is not part of equality of hash
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof AbstractSAMLAttributeMapper) {
            final AbstractSAMLAttributeMapper other = (AbstractSAMLAttributeMapper) obj;
            return matches(other.getSAMLName(), other.getAttributeFormat())
                    && java.util.Objects.equals(getAttributeIds(), other.getAttributeIds());
        }
        return false;
    }

    /**
     * {@inheritDoc}. The identity is not part of equality of hash
     */
    @Override
    public int hashCode() {
        String myFormat = getAttributeFormat();
        if (null == myFormat) {
            myFormat = Attribute.UNSPECIFIED;
        }
        return Objects.hashCode(myFormat, theSAMLName, attributeIds);
    }

    /**
     * Function to summon up the output type based on an ID and an object of the input type. Typically the input type
     * will be inspected for extra parameterisation.
     * 
     * @param input the input value to inspect.
     * @param id the identifier of the new attribute.
     * @return an output, suitable set up with per object information.
     */
    @Nonnull protected abstract OutType newAttribute(@Nonnull final InType input, @Nonnull @NotEmpty final String id);

}