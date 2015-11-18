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

package net.shibboleth.idp.attribute.resolver.dc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractDataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Data Connector to extra attributes from a saml2 {@link Assertion}. It is hoped that this connector will eventually
 * end up being used in mainline operation, which is why the code looks more suitable for being plugged into a webflow
 * than into a test.
 */
public class SAMLAttributeDataConnector extends AbstractDataConnector {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SAMLAttributeDataConnector.class);

    /**
     * The way to get the list of (SAML) attributes from the resolution context.
     */
    private Function<AttributeResolutionContext, List<Attribute>> attributesStrategy;

    /**
     * Gets the strategy for finding the (SAML) Attributes from the resolution context.
     * 
     * @return the required strategy.
     */
    public Function<AttributeResolutionContext, List<Attribute>> getAttributesStrategy() {
        return attributesStrategy;
    }

    /**
     * Sets the strategy for finding the(SAML) Attributes from the resolution context.
     * 
     * @param strategy to set.
     */
    public void setAttributesStrategy(Function<AttributeResolutionContext, List<Attribute>> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        attributesStrategy = strategy;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == attributesStrategy) {
            throw new ComponentInitializationException("Attribute Connector '" + getId()
                    + "': no Attribute location strategy set");
        }

    }

    /**
     * Method to convert a singled {@link XMLObject} into an {@link IdPAttributeValue} if possible. For testing this
     * is hard-wired - strings become scoped or non scoped (with the scope delimiter being '@'), everything else an
     * {@link XMLObjectAttributeValue}
     * 
     * @param object the object to encode
     * @return an {@link IdPAttributeValue}, or null if no encoding exists.
     */
    protected IdPAttributeValue encodeValue(XMLObject object) {
        if (null == object) {
            return null;
        } else if (object instanceof XSString) {
            return encodeString((XSString) object);
        } else {
            return new XMLObjectAttributeValue(object);
        }
    }

    /**
     * Encode a string into an attribute value.
     * 
     * @param inputString string to encode
     * @return encoded attribute value
     */
    private IdPAttributeValue encodeString(XSString inputString) {
        String value = inputString.getValue();
        int separator = value.indexOf('@');

        if (separator < 0) {
            return new StringAttributeValue(value);
        } else {
            return new ScopedStringAttributeValue(value.substring(0, separator), value.substring(separator + 1));
        }
    }

    /**
     * Encode a list of SAML objects as set of {@link IdPAttributeValue}.
     * 
     * @param attributeValues the input values
     * @return a list of values, Possibly empty.
     */
    @Nullable protected @Nonnull List<IdPAttributeValue<?>> encodeValues(final List<XMLObject> attributeValues) {
        final ArrayList<IdPAttributeValue<?>> result = new ArrayList<>(attributeValues.size());

        for (XMLObject object : attributeValues) {
            IdPAttributeValue val = encodeValue(object);

            result.add(val);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        final List<Attribute> samlAttributes = attributesStrategy.apply(resolutionContext);

        if (null == samlAttributes) {
            log.info("Connector '{}' no attributes found", getId());
            return null;
        }

        final Map<String, IdPAttribute> retVal = new HashMap<>(samlAttributes.size());

        for (Attribute samlAttribute : samlAttributes) {
            final String attributeName = samlAttribute.getName();
            log.debug("Connector '{}': found attribute named '{}'", getId(), attributeName);

            final List<IdPAttributeValue<?>> values = encodeValues(samlAttribute.getAttributeValues());
            log.debug("Connector '{}': attribute '{}', values '{}'", new Object[] {getId(), attributeName, values,});

            final IdPAttribute IdPAttribute = new IdPAttribute(attributeName);
            IdPAttribute.setValues(values);

            retVal.put(attributeName, IdPAttribute);
        }
        return retVal;
    }

}