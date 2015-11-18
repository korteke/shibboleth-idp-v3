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

package net.shibboleth.idp.attribute.filter;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableSet;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;

/** Java definition of MatchFunctorType as applied to value filtering. */
@ThreadSafe
public interface Matcher extends IdentifiedComponent {

    /** A {@link Matcher} that returns true/all attribute values as matched. */
    public static final Matcher MATCHES_ALL = new Matcher() {

        /** {@inheritDoc} */
        @Override public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull IdPAttribute attribute,
                @Nonnull AttributeFilterContext filterContext) {
            return ImmutableSet.copyOf(attribute.getValues());
        }

        @Override @Nullable public String getId() {
            return "MATCHES_ALL";
        }

    };

    /** A {@link Matcher} that returns false/no attribute values as matched. */
    public static final Matcher MATCHES_NONE = new Matcher() {

        /** {@inheritDoc} */
        @Override public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull IdPAttribute attribute,
                @Nonnull AttributeFilterContext filterContext) {
            return Collections.emptySet();
        }

        @Override @Nullable public String getId() {
            return "MATCHES_NONE";
        }

    };

    /** A {@link Matcher} that fails. targetted primarily at testing, but also at odd corners of parsing. */
    public static final Matcher MATCHER_FAILS = new Matcher() {

        /** {@inheritDoc} */
        @Override public Set<IdPAttributeValue<?>> getMatchingValues(@Nonnull IdPAttribute attribute,
                @Nonnull AttributeFilterContext filterContext) {
            return null;
        }

        @Override @Nullable public String getId() {
            return "MATCHER_FAILS";
        }

    };

    /**
     * Return those {@link IdPAttributeValue}s which match this rule, or null if the matcher failed.
     * 
     * @param attribute the attribute under question.
     * @param filterContext the filter context
     * @return The result of this rule. Null if we failed.
     */
    @Nullable @NonnullElements @Unmodifiable public Set<IdPAttributeValue<?>> getMatchingValues(
            @Nonnull final IdPAttribute attribute, @Nonnull final AttributeFilterContext filterContext);

}