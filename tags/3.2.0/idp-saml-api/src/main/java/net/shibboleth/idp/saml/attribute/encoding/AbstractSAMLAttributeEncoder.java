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

package net.shibboleth.idp.saml.attribute.encoding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Base class for encoders that produce SAML attributes.
 * 
 * @param <AttributeType> type of attribute produced
 * @param <EncodedType> the type of data that can be encoded by the encoder
 */
public abstract class AbstractSAMLAttributeEncoder<AttributeType extends SAMLObject,
            EncodedType extends IdPAttributeValue> extends AbstractInitializableComponent
                implements AttributeEncoder<AttributeType>, UnmodifiableComponent {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributeEncoder.class);

    /** Condition for use of this encoder. */
    @Nonnull private Predicate<ProfileRequestContext> activationCondition;
    
    /** The name of the attribute. */
    @NonnullAfterInit private String name;

    /** Whether to encode with xsi:type or not. */
    private boolean encodeType;
    
    /** Constructor. */
    public AbstractSAMLAttributeEncoder() {
        activationCondition = Predicates.alwaysTrue();
        encodeType = true;
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public Predicate<ProfileRequestContext> getActivationCondition() {
        return activationCondition;
    }
    
    /**
     * Set the activation condition for this encoder.
     * 
     * @param condition condition to set
     */
    public void setActivationCondition(@Nonnull final Predicate<ProfileRequestContext> condition) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        activationCondition = Constraint.isNotNull(condition, "Activation condition cannot be null");
    }
    
    /**
     * Get the name of the attribute.
     * 
     * @return name of the attribute
     */
    @NonnullAfterInit public final String getName() {
        return name;
    }

    /**
     * Set the name of the attribute.
     * 
     * @param attributeName name of the attribute
     */
    public void setName(@Nonnull @NotEmpty final String attributeName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        name = Constraint.isNotNull(StringSupport.trimOrNull(attributeName),
                "Attribute name cannot be null or empty");
    }
    
    /**
     * Get whether to encode type information.
     * 
     * <p>Defaults to 'true'</p>
     * 
     * @return true iff type information should be encoded
     */
    public boolean encodeType() {
        return encodeType;
    }
    
    /**
     * Set whether to encode type information.
     * 
     * @param flag flag to set
     */
    public void setEncodeType(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        encodeType = flag;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (name == null) {
            throw new ComponentInitializationException("Attribute name cannot be null or empty");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public AttributeType encode(@Nonnull final IdPAttribute attribute) throws AttributeEncodingException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        Constraint.isNotNull(attribute, "Attribute to encode cannot be null");
        final String attributeId = attribute.getId();
        log.debug("Beginning to encode attribute {}", attributeId);

        if (attribute.getValues().isEmpty()) {
            throw new AttributeEncodingException("Unable to encode " + attributeId
                    + " attribute.  It does not contain any values");
        }

        final List<XMLObject> samlAttributeValues = new ArrayList<>();

        EncodedType attributeValue;
        XMLObject samlAttributeValue;
        for (IdPAttributeValue o : attribute.getValues()) {
            if (o == null) {
                // filtered out upstream leave in test for sanity
                log.debug("Skipping null value of attribute {}", attributeId);
                continue;
            }

            if (!canEncodeValue(attribute, o)) {
                log.debug("Skipping value of attribute {}; Type {} cannot be encoded by this encoder.", attributeId,
                        o.getClass().getName());
                continue;
            }

            attributeValue = (EncodedType) o;
            samlAttributeValue = encodeValue(attribute, attributeValue);
            if (samlAttributeValue == null) {
                log.debug("Skipping empty value for attribute {}", attributeId);
            } else {
                samlAttributeValues.add(samlAttributeValue);
            }
        }

        if (samlAttributeValues.isEmpty()) {
            throw new AttributeEncodingException("Attribute " + attributeId + " did not contain any encodeable values");
        }

        log.debug("Completed encoding {} values for attribute {}", samlAttributeValues.size(), attributeId);
        return buildAttribute(attribute, samlAttributeValues);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AbstractSAMLAttributeEncoder)) {
            return false;
        }

        final AbstractSAMLAttributeEncoder other = (AbstractSAMLAttributeEncoder) obj;
        return java.util.Objects.equals(getName(), other.getName())
                && java.util.Objects.equals(getProtocol(), other.getProtocol());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getProtocol());
    }

    /**
     * Checks if the given value can be handled by the encoder. In many cases this is simply a check to see if the given
     * object is of the right type.
     * 
     * @param idpAttribute the attribute being encoded, never null
     * @param value the value to check, never null
     * 
     * @return true if the encoder can encoder this value, false if not
     */
    protected abstract boolean canEncodeValue(@Nonnull final IdPAttribute idpAttribute,
            @Nonnull final IdPAttributeValue value);

    /**
     * Encodes an attribute value in to a SAML attribute value element.
     * 
     * @param idpAttribute the attribute being encoded, never null
     * @param value the value to encoder, never null
     * 
     * @return the attribute value or null if the resulting attribute value would be empty
     * 
     * @throws AttributeEncodingException thrown if there is a problem encoding the attribute value
     */
    @Nullable protected abstract XMLObject encodeValue(@Nonnull final IdPAttribute idpAttribute,
            @Nonnull final EncodedType value) throws AttributeEncodingException;

    /**
     * Builds a SAML attribute element from the given attribute values.
     * 
     * @param idpAttribute the attribute being encoded, never null
     * @param attributeValues the encoded values for the attribute, never null or containing null elements
     * 
     * @return the SAML attribute element
     * 
     * @throws AttributeEncodingException thrown if there is a problem constructing the SAML attribute
     */
    @Nonnull protected abstract AttributeType buildAttribute(@Nonnull final IdPAttribute idpAttribute,
            @Nonnull @NonnullElements final List<XMLObject> attributeValues) throws AttributeEncodingException;
}