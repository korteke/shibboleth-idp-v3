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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.MoreObjects;

/**
 * {@link Matcher} that implements the negation of a matcher. <br/>
 * <br/>
 * A given attribute value is considered to have matched if it is not returned by the composed {@link Matcher}. The
 * predicate is the logical NOT of the composed {@link Matcher}. If the matcher fails then failure is returned.
 */
@ThreadSafe
public final class NotMatcher extends AbstractIdentifiableInitializableComponent implements Matcher {

    /** The matcher we are negating. */
    private final Matcher negatedMatcher;

    /**
     * Constructor.
     * 
     * @param valueMatcher attribute value matcher to be negated
     */
    public NotMatcher(@Nonnull final Matcher valueMatcher) {
        negatedMatcher = Constraint.isNotNull(valueMatcher, "Attribute value matcher can not be null");
    }

    /**
     * Get the matcher that is being negated.
     * 
     * @return matcher that is being negated
     */
    @Nonnull public Matcher getNegatedMatcher() {
        return negatedMatcher;
    }

    /**
     * A given attribute value is considered to have matched if it is not returned by the composed {@link Matcher}.
     * {@inheritDoc}
     */
    @Override @Nullable @NonnullElements public Set<IdPAttributeValue<?>> getMatchingValues(
            @Nonnull final IdPAttribute attribute, @Nonnull final AttributeFilterContext filterContext) {
        Constraint.isNotNull(attribute, "Attribute to be filtered can not be null");
        Constraint.isNotNull(filterContext, "Attribute filter context can not be null");

        // Capture the matchers to avoid race with setComposedMatchers
        // Do this before the test on destruction to avoid race with destroy code
        final Matcher currentMatcher = getNegatedMatcher();
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Set<IdPAttributeValue<?>> attributeValues = new HashSet<>(attribute.getValues());

        Set<IdPAttributeValue<?>> matches = currentMatcher.getMatchingValues(attribute, filterContext);
        if (null == matches) {
            return matches;
        }

        attributeValues.removeAll(matches);

        if (attributeValues.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(attributeValues);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("negatedMatcher", negatedMatcher).toString();
    }
}