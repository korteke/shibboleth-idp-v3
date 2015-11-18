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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.UnsupportedAttributeTypeException;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link net.shibboleth.idp.attribute.resolver.AttributeDefinition} that produces its attribute values by taking the
 * first group match of a regular expression evaluating against the values of this definition's dependencies.
 */
@ThreadSafe
public class RegexSplitAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RegexSplitAttributeDefinition.class);

    /** Regular expression used to split values. */
    @Nullable private Pattern regexp;

    /**
     * Gets the regular expression used to split input values.
     * 
     * @return regular expression used to split input values
     */
    @Nullable @NonnullAfterInit public Pattern getRegularExpression() {
        return regexp;
    }

    /**
     * Sets the regular expression used to split input values.
     * 
     * @param expression regular expression used to split input values
     */
    public void setRegularExpression(@Nonnull final Pattern expression) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        regexp = Constraint.isNotNull(expression, "Regular expression cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final List<IdPAttributeValue<?>> resultantValues = new ArrayList<>();
        final IdPAttribute resultantAttribute = new IdPAttribute(getId());

        final List<IdPAttributeValue<?>> dependencyValues =
                PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies(), getId());

        for (final IdPAttributeValue dependencyValue : dependencyValues) {
            final String inputValue;
            if (dependencyValue instanceof EmptyAttributeValue) {
                final EmptyAttributeValue emptyVal = (EmptyAttributeValue) dependencyValue;
                if (EmptyType.NULL_VALUE == emptyVal.getValue()) {
                    log.debug("{} ignored empty value of type {}", getLogPrefix(), emptyVal.getDisplayValue());
                    continue;
                }
                inputValue = "";
            } else if (!(dependencyValue instanceof StringAttributeValue)) {
                throw new ResolutionException(new UnsupportedAttributeTypeException(getLogPrefix()
                        + "This attribute definition only operates on attribute values of type "
                        + StringAttributeValue.class.getName() + "; was given " + 
                        dependencyValue.getClass().getName()));
            } else {
                inputValue = (String) dependencyValue.getValue();
            }

            log.debug("{} Applying regexp '{}' to input value '{}'", getLogPrefix(), regexp.pattern(), inputValue);
            final Matcher matcher = regexp.matcher(inputValue);
            if (matcher.matches()) {
                log.debug("{} Computed the value '{}' by apply regexp '{}' to input value '{}'", 
                        getLogPrefix(), matcher.group(1), regexp.pattern(), inputValue);
                resultantValues.add(StringAttributeValue.valueOf(matcher.group(1)));
            } else {
                log.debug("{} Regexp '{}' did not match anything in input value '{}'", getLogPrefix(),
                        regexp.pattern(), inputValue);
            }
        }

        if (!resultantValues.isEmpty()) {
            resultantAttribute.setValues(resultantValues);
        }
        return resultantAttribute;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == regexp) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no regular expression was configured");
        }

        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no dependencies were configured");
        }
    }
}