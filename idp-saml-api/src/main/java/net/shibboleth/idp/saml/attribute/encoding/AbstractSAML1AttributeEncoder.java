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

import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeDesignatorMapper;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml1.core.Attribute;

import com.google.common.base.Objects;

/**
 * Base class for encoders that produce a SAML 1 {@link Attribute}.
 * 
 * @param <EncodedType> the type of data that can be encoded by the encoder
 */
public abstract class AbstractSAML1AttributeEncoder<EncodedType extends IdPAttributeValue>
        extends AbstractSAMLAttributeEncoder<Attribute, EncodedType>
        implements SAML1AttributeEncoder<EncodedType>, AttributeDesignatorMapperProcessor<IdPAttribute> {

    /** Builder used to construct {@link Attribute} objects. */
    @Nonnull private final SAMLObjectBuilder<Attribute> attributeBuilder;
    
    /** The namespace in which the attribute name is interpreted. */
    @NonnullAfterInit private String namespace;
    
    /** Constructor. */
    public AbstractSAML1AttributeEncoder() {
        attributeBuilder =
                (SAMLObjectBuilder<Attribute>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(
                        Attribute.TYPE_NAME);
        if (attributeBuilder == null) {
            throw new ConstraintViolationException("SAML 1 Attribute builder is unavailable");
        }
        namespace = net.shibboleth.idp.saml.xml.SAMLConstants.SAML1_ATTR_NAMESPACE_URI;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public final String getProtocol() {
        return SAMLConstants.SAML11P_NS;
    }

    /** {@inheritDoc} */
    @Override @NonnullAfterInit public String getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace in which the attribute name is interpreted.
     * 
     * @param attributeNamespace namespace in which the attribute name is interpreted
     */
    public void setNamespace(@Nonnull @NotEmpty final String attributeNamespace) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        namespace = Constraint.isNotNull(StringSupport.trimOrNull(attributeNamespace),
                "Attribute namespace cannot be null or empty");
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (namespace == null) {
            throw new ComponentInitializationException("Attribute namespace cannot be null or empty");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull protected Attribute buildAttribute(@Nonnull final IdPAttribute idpAttribute,
            @Nonnull @NonnullElements final List<XMLObject> attributeValues) throws AttributeEncodingException {

        final Attribute samlAttribute = attributeBuilder.buildObject();
        samlAttribute.setAttributeName(getName());
        samlAttribute.setAttributeNamespace(getNamespace());
        samlAttribute.getAttributeValues().addAll(attributeValues);

        return samlAttribute;
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public void populateAttributeMapper(
            @Nonnull final AbstractSAMLAttributeDesignatorMapper<IdPAttribute> mapper) {
        mapper.setAttributeNamespace(getNamespace());
        mapper.setId(getMapperId());
        mapper.setSAMLName(getName());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {

        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof AbstractSAML1AttributeEncoder)) {
            return false;
        }

        final AbstractSAML1AttributeEncoder other = (AbstractSAML1AttributeEncoder) obj;
        return java.util.Objects.equals(getNamespace(), other.getNamespace());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getNamespace());
    }

    /**
     * Generate an Id suitable for the mapper.
     * 
     * @return a suitable Id for the mapper
     */
    @Nonnull @NotEmpty private String getMapperId() {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        return "MapperForAttribute" + getName();
    }
    
}