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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matcher that checks, via an exact match, if the attribute requester contains an entity attribute with a given value.
 */
public class AttributeRequesterEntityAttributeExactPolicyRule extends AbstractEntityAttributePolicyRule {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeRequesterEntityAttributeExactPolicyRule.class);

    /** The value of the entity attribute the entity must have. */
    @NonnullAfterInit private String value;

    /**
     * Gets the value of the entity attribute the entity must have.
     * 
     * @return value of the entity attribute the entity must have
     */
    @NonnullAfterInit public String getValue() {
        return value;
    }

    /**
     * Sets the value of the entity attribute the entity must have.
     * 
     * @param attributeValue value of the entity attribute the entity must have
     */
    public void setValue(@Nonnull final String attributeValue) {
        value = Constraint.isNotNull(attributeValue, "Attribute value cannot be null.");
    }

    /** {@inheritDoc} */
    @Override
    protected boolean entityAttributeValueMatches(@Nullable final String stringValue) {
        return Objects.equals(value, stringValue);
    }

    /** {@inheritDoc} */
    @Override @Nullable protected EntityDescriptor getEntityMetadata(final AttributeFilterContext filterContext) {
        final SAMLMetadataContext metadataContext = filterContext.getRequesterMetadataContext();

        if (null == metadataContext) {
            log.warn("{} Could not locate SP metadata context", getLogPrefix());
            return null;
        }
        return metadataContext.getEntityDescriptor();
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (null == value) {
            throw new ComponentInitializationException(getLogPrefix() + " No value supplied to compare against");
        }
    }

}
