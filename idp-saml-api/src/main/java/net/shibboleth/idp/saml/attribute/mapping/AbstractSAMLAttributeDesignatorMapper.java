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
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.saml1.core.AttributeDesignator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Basis of all classes which map SAML1 {@link AttributeDesignator} into an IdP {@link IdPAttribute}.
 * 
 * @param <OutType> the output (IdP Attribute) type
 */
public abstract class AbstractSAMLAttributeDesignatorMapper<OutType extends IdPAttribute> extends
        AbstractIdentifiableInitializableComponent implements AttributeMapper<AttributeDesignator, OutType> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributeDesignatorMapper.class);

    /** The internal names to generate. */
    @Nonnull @NonnullElements private List<String> attributeIds = Collections.emptyList();

    /** The attribute namespace. */
    @Nullable private String attributeNamespace;

    /** the (SAML) attribute name. */
    @NonnullAfterInit private String theSAMLName;

    /** The String used to prefix log message. */
    @Nullable private String logPrefix;

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
     * Set the (optional) attribute namespace.
     * 
     * @param ns the namespace to set
     */
    public void setAttributeNamespace(@Nullable final String ns) {
        attributeNamespace = StringSupport.trimOrNull(ns);
    }

    /**
     * Get the (optional) attribute namespace.
     * 
     * @return the namespace
     */
    @Nullable public String getAttributeNamespace() {
        return attributeNamespace;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == theSAMLName) {
            throw new ComponentInitializationException(getLogPrefix() + " SAML name not present");
        }
        if (attributeIds.isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix()
                    + " At least one attribute Id should be provided");
        }
        logPrefix = null;
    }

    /**
     * Compare whether the name and namespace given "match" ours. Used in checking both against input attributes and as
     * part of equality checking.
     * 
     * @param otherSAMLName the name to compare against
     * @param otherSAMLNamespace the bamespace to compare against
     * @return whether there is a match.
     */
    protected boolean matches(@Nonnull @NotEmpty final String otherSAMLName,
            @Nullable final String otherSAMLNamespace) {
        if (!otherSAMLName.equals(theSAMLName)) {
            log.debug("{} SAML attribute name {} does not match {}", getLogPrefix(), otherSAMLName, getId());
            return false;
        } else if (!java.util.Objects.equals(otherSAMLNamespace, getAttributeNamespace())) {
            log.debug("{} SAML attribute namespace {} does not match {}", getLogPrefix(), otherSAMLNamespace,
                    getAttributeNamespace());
            return false;
        }
        
        return true;
    }

    /**
     * Determines if the input matches the provided parameterisation.
     * 
     * @param designator the designator to consider
     * @return whether it matches.
     */
    protected boolean attributeMatches(@Nonnull final AttributeDesignator designator) {
        return matches(designator.getAttributeName(), designator.getAttributeNamespace());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements public Map<String,OutType> mapAttribute(@Nonnull final AttributeDesignator prototype) {

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (!attributeMatches(prototype)) {
            return Collections.emptyMap();
        }

        final Map<String,OutType> output = new HashMap<>();

        log.debug("{} attribute id {} and aliases {} will be created", getLogPrefix(), getId(), getAttributeIds());

        OutType out = newAttribute(prototype, getId());

        for (String id : attributeIds) {
            out = newAttribute(prototype, getId());
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

        if (obj instanceof AbstractSAMLAttributeDesignatorMapper) {
            final AbstractSAMLAttributeDesignatorMapper other = (AbstractSAMLAttributeDesignatorMapper) obj;
            return matches(other.getSAMLName(), other.getAttributeNamespace())
                    && java.util.Objects.equals(getAttributeIds(), other.getAttributeIds());
        }
        return false;
    }

    /**
     * {@inheritDoc}. The identity is not part of equality of hash
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(attributeNamespace, theSAMLName, attributeIds);
    }

    /**
     * Function to summon up the output type based on an ID and an object of the input type. Typically the input type
     * will be inspected for extra parameterisation.
     * 
     * @param input the input value to inspect.
     * @param id the identifier of the new attribute.
     * @return an output, suitable set up with per object information.
     */
    @Nonnull protected abstract OutType newAttribute(@Nonnull final AttributeDesignator input,
            @Nonnull @NotEmpty final String id);

}