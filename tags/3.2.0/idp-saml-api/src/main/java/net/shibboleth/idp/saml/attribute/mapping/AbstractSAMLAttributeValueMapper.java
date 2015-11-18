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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * The base of the classes that map SAML2 attribute values into IdP attribute values.
 */
public abstract class AbstractSAMLAttributeValueMapper extends AbstractInitializableComponent {

    /** logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractSAMLAttributeValueMapper.class);

    /** The String used to prefix log message. */
    private String logPrefix;

    /**
     * Convert from a list of the SAML Objects (usually a SAML2 AttributeValue) Value into the IDP Attributes.
     * 
     * @param inputs the list of SAML Attributes
     * @return a list of IdP Attributes
     */
    @Nonnull @Unmodifiable public List<IdPAttributeValue<?>>
            decodeValues(@Nonnull @NonnullElements List<XMLObject> inputs) {
        List<IdPAttributeValue<?>> outputs = new ArrayList<>(inputs.size());

        for (XMLObject input : inputs) {
            IdPAttributeValue output = decodeValue(input);
            if (null != output) {
                outputs.add(output);
            }
        }
        return Collections.unmodifiableList(outputs);

    }

    /**
     * Function to return the string contents if this is the correct type.
     * 
     * @param object The object to inspect.
     * @return Its contents suitably decoded. Returns null if we could not decode.
     */
    // Checkstyle: CyclomaticComplexity OFF
    @Nullable protected String getStringValue(@Nonnull final XMLObject object) {
        String retVal = null;

        if (object instanceof XSString) {

            retVal = ((XSString) object).getValue();

        } else if (object instanceof XSURI) {

            retVal = ((XSURI) object).getValue();

        } else if (object instanceof XSBoolean) {

            retVal = ((XSBoolean) object).getValue().getValue() ? "1" : "0";

        } else if (object instanceof XSInteger) {

            retVal = ((XSInteger) object).getValue().toString();

        } else if (object instanceof XSDateTime) {

            final DateTime dt = ((XSDateTime) object).getValue();
            if (dt != null) {
                retVal = ((XSDateTime) object).getDateTimeFormatter().print(dt);
            } else {
                retVal = null;
            }

        } else if (object instanceof XSBase64Binary) {

            retVal = ((XSBase64Binary) object).getValue();

        } else if (object instanceof XSAny) {

            final XSAny wc = (XSAny) object;
            if (wc.getUnknownAttributes().isEmpty() && wc.getUnknownXMLObjects().isEmpty()) {
                retVal = wc.getTextContent();
            } else {
                retVal = null;
            }
        }

        if (null == retVal) {
            log.info("{} value of type {} could not be converted", getLogPrefix(), object.getClass().toString());
        }
        return retVal;
    }

    // Checkstyle: CyclomaticComplexity ON

    /**
     * Return a string which is to be prepended to all log messages. This is set by the enclosing AttributeMapper.
     * 
     * @return Returns the logPrefix.
     */
    @NonnullAfterInit public String getLogPrefix() {
        return logPrefix;
    }

    /**
     * Set the log prefix to use.
     * 
     * @param prefix The logPrefix to set.
     */
    public void setLogPrefix(@Nonnull String prefix) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        logPrefix = Constraint.isNotNull(prefix, "prefix can not be null or empty");
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == logPrefix) {
            throw new ComponentInitializationException("No log prefix set");
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof AbstractSAMLAttributeValueMapper) {
            AbstractSAMLAttributeValueMapper other = (AbstractSAMLAttributeValueMapper) obj;

            return getClass().equals(other.getClass());
        }
        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return Objects.hashCode(getClass());
    }

    /**
     * Function to decode a single {@link XMLObject} into an {@link IdPAttributeValue}.
     * 
     * @param object the object to decode
     * @return the returned final {@link IdPAttributeValue} or null if decoding failed
     */
    @Nullable protected abstract IdPAttributeValue decodeValue(@Nonnull final XMLObject object);

    /**
     * Return the output type (for logging).
     * 
     * @return the output type.
     */
    @Nonnull protected abstract String getAttributeTypeName();

}
