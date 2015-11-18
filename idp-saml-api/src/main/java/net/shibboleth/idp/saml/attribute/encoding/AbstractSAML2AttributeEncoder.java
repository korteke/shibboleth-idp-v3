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
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;

import com.google.common.base.Objects;

/**
 * Base class for encoders that produce a SAML 2 {@link Attribute}.
 * 
 * @param <EncodedType> the type of data that can be encoded by the encoder
 */
public abstract class AbstractSAML2AttributeEncoder<EncodedType extends IdPAttributeValue> extends
        AbstractSAMLAttributeEncoder<Attribute, EncodedType> implements SAML2AttributeEncoder<EncodedType> {

    /** Builder used to construct {@link Attribute} objects. */
    @Nonnull private final SAMLObjectBuilder<Attribute> attributeBuilder;

    /** A friendly, human readable, name for the attribute. */
    @Nullable private String friendlyName;

    /** The format of the attribute name. */
    @Nullable private String format;

    /** Constructor. */
    public AbstractSAML2AttributeEncoder() {
        attributeBuilder =
                (SAMLObjectBuilder<Attribute>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(
                        Attribute.TYPE_NAME);
        if (attributeBuilder == null) {
            throw new ConstraintViolationException("SAML 2 Attribute builder is unavailable");
        }
        setNameFormat(Attribute.URI_REFERENCE);
    }

    /** {@inheritDoc} */
    @Override @Nonnull public final String getProtocol() {
        return SAMLConstants.SAML20P_NS;
    }

    /**
     * Get the friendly, human readable, name for the attribute.
     * 
     * @return friendly, human readable, name for the attribute
     */
    @Nullable public final String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Set the friendly, human readable, name for the attribute.
     * 
     * @param attributeFriendlyName friendly, human readable, name for the attribute
     */
    public void setFriendlyName(@Nullable final String attributeFriendlyName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        friendlyName = StringSupport.trimOrNull(attributeFriendlyName);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public String getNameFormat() {
        return format;
    }

    /**
     * Get the name format, or the SAML constants for "unspecified", if not set.
     * 
     * @return the effective name format regardless of nulls
     */
    @Nonnull @NotEmpty public String getEffectiveNameFormat() {
        return format != null ? format : Attribute.UNSPECIFIED;
    }

    /**
     * Set the format of the attribute name.
     * 
     * @param nameFormat format in which the attribute name is interpreted
     */
    public void setNameFormat(@Nullable final String nameFormat) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        format = StringSupport.trimOrNull(nameFormat);
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected Attribute buildAttribute(@Nonnull final IdPAttribute idpAttribute,
            @Nonnull @NonnullElements final List<XMLObject> attributeValues) throws AttributeEncodingException {

        final Attribute samlAttribute = attributeBuilder.buildObject();
        samlAttribute.setName(getName());
        samlAttribute.setFriendlyName(getFriendlyName());
        samlAttribute.setNameFormat(getNameFormat());
        samlAttribute.getAttributeValues().addAll(attributeValues);

        return samlAttribute;
    }

    /**
     * Generate an Id suitable for the mapper.
     * 
     * @return a suitable Id for the mapper
     */
    @Nonnull protected String getMapperId() {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        if (null != getFriendlyName()) {
            return "MapperFor" + getFriendlyName();
        }
        return "MapperForAttribute" + getName();
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof AbstractSAML2AttributeEncoder)) {
            return false;
        }

        final AbstractSAML2AttributeEncoder other = (AbstractSAML2AttributeEncoder) obj;
        return java.util.Objects.equals(getEffectiveNameFormat(), other.getEffectiveNameFormat());
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), getEffectiveNameFormat());
    }
}