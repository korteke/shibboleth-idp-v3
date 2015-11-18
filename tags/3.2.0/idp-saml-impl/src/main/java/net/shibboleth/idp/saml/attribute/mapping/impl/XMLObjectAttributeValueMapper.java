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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.saml.attribute.mapping.AbstractSAMLAttributeValueMapper;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.core.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Decoder for {@link XMLObject} into {@link XMLObjectAttributeValue}.
 */
public class XMLObjectAttributeValueMapper extends AbstractSAMLAttributeValueMapper {

    /** logger . */
    private Logger log = LoggerFactory.getLogger(XMLObjectAttributeValueMapper.class);
    
    /** Do we treat the entire AttributeValue as the object to be embedded. */
    private boolean includeAttributeValue;
    
    /** Do we include the attribute value, or just its first child?
     * @return Returns the includeAttributeValue.
     */
    public boolean isIncludeAttributeValue() {
        return includeAttributeValue;
    }

    /** Sets whether we look at the AttributeValue, or just its child.
     * @param doInclude The includeAttributeValue to set.
     */
    public void setIncludeAttributeValue(boolean doInclude) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        includeAttributeValue = doInclude;
    }

    
    /** {@inheritDoc} */
    @Nullable protected IdPAttributeValue decodeValue(@Nonnull final XMLObject object)  {
        Constraint.isNotNull(object, "Object supplied to must not be null");
        
        if (includeAttributeValue) {
            return new XMLObjectAttributeValue(object);
        }
        
        final List<XMLObject> children = object.getOrderedChildren();
        
        if (null == children || children.isEmpty()) {
            log.debug("{} attribute had no XML children, ignored", getLogPrefix());
            return null;
        }
        if (children.size() > 1) {
            log.debug("{} attribute has more than one child, returning first value only", getLogPrefix());
        }
        final XMLObject child = children.get(0);
        log.debug("{} returning value of type {}", getLogPrefix(), child.getClass().toString());
        return new XMLObjectAttributeValue(child);
    }

    /** {@inheritDoc} */
    @Nonnull protected String getAttributeTypeName() {
        return "XMLObject";
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }
        
        if (obj instanceof XMLObjectAttributeValueMapper) {
            XMLObjectAttributeValueMapper other = (XMLObjectAttributeValueMapper) obj;
            return isIncludeAttributeValue() == other.isIncludeAttributeValue(); 
        }
        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return Objects.hashCode(getClass(), isIncludeAttributeValue());
    }
}
