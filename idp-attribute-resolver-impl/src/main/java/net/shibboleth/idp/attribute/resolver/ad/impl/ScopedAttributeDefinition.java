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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.UnsupportedAttributeTypeException;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An attribute definition that creates {@link ScopedStringAttributeValue}s by taking a source attribute value and
 * applying a static scope to each.
 */
@ThreadSafe
public class ScopedAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScopedAttributeDefinition.class);

    /** Scope value. */
    @NonnullAfterInit private String scope;

    /**
     * Get scope value.
     * 
     * @return Returns the scope.
     */
    @NonnullAfterInit public String getScope() {
        return scope;
    }

    /**
     * Set the scope for this definition.
     * 
     * @param newScope what to set.
     */
    public void setScope(@Nonnull @NotEmpty final String newScope) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scope = Constraint.isNotNull(StringSupport.trimOrNull(newScope), "Scope can not be null or empty");
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final IdPAttribute resultantAttribute = new IdPAttribute(getId());

        final List<IdPAttributeValue<?>> dependencyValues =
                PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies(), getId());

        final List<StringAttributeValue> valueList = new ArrayList<>(dependencyValues.size());

        for (final IdPAttributeValue dependencyValue : dependencyValues) {
            if (dependencyValue instanceof EmptyAttributeValue) {
                final EmptyAttributeValue emptyVal = (EmptyAttributeValue) dependencyValue;
                log.debug("{} ignored empty value of type {}", getLogPrefix(), emptyVal.getDisplayValue());
                continue;
            }
            if (!(dependencyValue instanceof StringAttributeValue)) {
                throw new ResolutionException(new UnsupportedAttributeTypeException(getLogPrefix()
                        + "This attribute definition only supports attribute value types of "
                        + StringAttributeValue.class.getName() + " not values of type "
                        + dependencyValue.getClass().getName()));
            }

            valueList.add(new ScopedStringAttributeValue((String) dependencyValue.getValue(), scope));
        }
        resultantAttribute.setValues(valueList);
        return resultantAttribute;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == scope) {
            throw new ComponentInitializationException(getLogPrefix() + "': no scope was configured");
        }

        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix() + "': no dependencies were configured");
        }
    }
    
}