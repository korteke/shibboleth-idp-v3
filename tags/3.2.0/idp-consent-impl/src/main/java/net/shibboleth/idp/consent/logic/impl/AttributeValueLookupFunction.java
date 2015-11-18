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

package net.shibboleth.idp.consent.logic.impl;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.ContextDataLookupFunction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * {@link ContextDataLookupFunction} to return the value of an attribute from an {@link AttributeContext}.
 */
public class AttributeValueLookupFunction implements ContextDataLookupFunction<ProfileRequestContext, String> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeValueLookupFunction.class);

    /** The attribute ID to look for. */
    @Nonnull @NotEmpty private String attributeId;

    /** Strategy used to find the {@link AttributeContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;
    
    /** Whether to use filtered or unfiltered attributes. */
    private boolean useUnfilteredAttributes;

    /**
     * Constructor.
     *
     * @param userAttributeId the attribute id
     */
    public AttributeValueLookupFunction(@Nonnull @NotEmpty final String userAttributeId) {
        attributeId =
                Constraint.isNotNull(StringSupport.trimOrNull(userAttributeId),
                        "User attribute ID cannot be null nor empty");

        attributeContextLookupStrategy =
                Functions.compose(new ChildContextLookup<>(AttributeContext.class),
                        new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
        
        useUnfilteredAttributes = true;
    }

    /**
     * Set the attribute context lookup strategy.
     * 
     * @param strategy the attribute context lookup strategy
     */
    public void setAttributeContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeContext> strategy) {
        attributeContextLookupStrategy =
                Constraint.isNotNull(strategy, "Attribute context lookup strategy cannot be null");
    }
    
    /**
     * Set whether to use filtered or unfiltered attributes.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param flag flag to set
     */
    public void setUseUnfilteredAttributes(final boolean flag) {
        useUnfilteredAttributes = flag;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nullable final ProfileRequestContext input) {

        final AttributeContext attributeContext = attributeContextLookupStrategy.apply(input);
        if (attributeContext == null) {
            log.debug("No attribute context within profile request context");
            return null;
        }

        final Map<String,IdPAttribute> attributes = useUnfilteredAttributes
                ? attributeContext.getUnfilteredIdPAttributes()
                : attributeContext.getIdPAttributes();

        final IdPAttribute attribute = attributes.get(attributeId);
        if (attribute == null || attribute.getValues().isEmpty()) {
            log.debug("Attribute '{}' does not exist or has no values", attributeId);
            return null;
        }

        if (attribute.getValues().size() != 1) {
            log.debug("Returning first string value of attribute '{}'", attributeId);
        }

        for (final IdPAttributeValue value : attribute.getValues()) {
            if (value instanceof StringAttributeValue) {
                log.debug("Returning value '{}' of attribute '{}'", ((StringAttributeValue) value).getValue(),
                        attributeId);
                return ((StringAttributeValue) value).getValue();
            }
        }

        return null;
    }

}
