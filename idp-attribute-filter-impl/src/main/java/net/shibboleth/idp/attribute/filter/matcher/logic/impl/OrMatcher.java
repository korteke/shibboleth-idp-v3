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

package net.shibboleth.idp.attribute.filter.matcher.logic.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.collection.LazySet;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * {@link Matcher} that implements the disjunction of matchers. That is, a given attribute value is considered to have
 * matched if it is returned by any of the composed {@link Matcher}. If any of the matchers fail then failure is
 * returned.
 */
@ThreadSafe
public class OrMatcher extends AbstractComposedMatcher {

    /**
     * Constructor.
     * 
     * @param composedMatchers matchers being composed
     */
    public OrMatcher(@Nullable @NullableElements final Collection<Matcher> composedMatchers) {
        super(composedMatchers);
    }

    /** {@inheritDoc} */
    @Nullable @NonnullElements public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull IdPAttribute attribute,
            @Nonnull AttributeFilterContext filterContext) {
        Constraint.isNotNull(attribute, "Attribute to be filtered can not be null");
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");

        // Capture the matchers to avoid race with setComposedMatchers
        // Do this before the test on destruction to avoid race with destroy code
        final List<Matcher> currentMatchers = getComposedMatchers();
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final Set<IdPAttributeValue<?>> matchingValues = new LazySet<>();
        for (Matcher matchFunctor : currentMatchers) {
            Set<IdPAttributeValue<?>> matches = matchFunctor.getMatchingValues(attribute, filterContext);
            if (null == matches) {
                return null;
            }
            matchingValues.addAll(matches);
        }

        return Collections.unmodifiableSet(matchingValues);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (getComposedMatchers().isEmpty()) {
            throw new ComponentInitializationException("No matchers supplied to OR");
        }
    }

}