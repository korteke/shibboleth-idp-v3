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
import javax.annotation.Nullable;
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
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An attribute definition that creates {@link ScopedStringAttributeValue}s by taking a source attribute value splitting
 * it at a delimiter. The first atom becomes the attribute value and the second value becomes the scope.
 */
@ThreadSafe
public class PrescopedAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrescopedAttributeDefinition.class);

    /** Delimiter between value and scope. Default value: @ */
    private String scopeDelimiter = "@";

    /**
     * Get delimiter between value and scope.
     * 
     * @return delimiter between value and scope
     */
    @Nonnull public String getScopeDelimiter() {
        return scopeDelimiter;
    }

    /**
     * Set the delimiter between value and scope.
     * 
     * @param newScopeDelimiter delimiter between value and scope
     */
    public void setScopeDelimiter(@Nonnull @NotEmpty final String newScopeDelimiter) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scopeDelimiter =
                Constraint.isNotNull(StringSupport.trimOrNull(newScopeDelimiter), getLogPrefix()
                        + " Scope delimiter can not be null or empty");
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        Constraint.isNotNull(workContext, getLogPrefix() + " AttributeResolverWorkContext cannot be null");
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final IdPAttribute resultantAttribute = new IdPAttribute(getId());

        final List<IdPAttributeValue<?>> dependencyValues =
                PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies(), getId());
        log.debug("{} Dependencies {} provided unmapped values of {}", getLogPrefix(), getDependencies(),
                dependencyValues);

        final List<IdPAttributeValue<?>> valueList = new ArrayList<>(dependencyValues.size());
        for (final IdPAttributeValue<?> dependencyValue : dependencyValues) {
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

            valueList.add(buildScopedStringAttributeValue((StringAttributeValue) dependencyValue));
        }
        resultantAttribute.setValues(valueList);
        return resultantAttribute;
    }

    /**
     * Builds a {@link ScopedStringAttributeValue} from a {@link StringAttributeValue} whose value contains a delimited
     * value.
     * 
     * @param value the original attribute value
     * 
     * @return the scoped attribute value
     * 
     * @throws ResolutionException thrown if the given attribute value does not contain a delimited value
     */
    @Nonnull private IdPAttributeValue<?> buildScopedStringAttributeValue(
            @Nonnull final StringAttributeValue value) throws ResolutionException {
        Constraint.isNotNull(value, getLogPrefix() + " Attribute value can not be null");

        final String[] stringValues = value.getValue().split(scopeDelimiter);
        if (stringValues.length < 2) {
            log.error("{} Input attribute value {} does not contain delimiter {} and can not be split", new Object[] {
                    getLogPrefix(), value.getValue(), scopeDelimiter,});
            throw new ResolutionException("Input attribute value can not be split.");
        }

        log.debug("{} Value '{}' was split into {} at scope delimiter '{}'",
                new Object[] {getLogPrefix(), value.getValue(), stringValues, scopeDelimiter,});
        return ScopedStringAttributeValue.valueOf(stringValues[0], stringValues[1]);
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix() + " no dependencies were configured");
        }
    }
}