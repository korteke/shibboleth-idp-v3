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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponent;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Base class for {@link Matcher} implementations that are compositions of two or more other
 * {@link Matcher}s.
 */
public abstract class AbstractComposedMatcher extends AbstractIdentifiableInitializableComponent implements
        Matcher, UnmodifiableComponent {

    /** The composed matchers. */
    private final List<Matcher> matchers;

    /**
     * Constructor.
     * 
     * @param composedMatchers matchers being composed
     */
    public AbstractComposedMatcher(@Nullable @NullableElements final 
            Collection<Matcher> composedMatchers) {
        ArrayList<Matcher> checkedMatchers = new ArrayList<>();

        if (composedMatchers != null) {
            CollectionSupport.addIf(checkedMatchers, composedMatchers, Predicates.notNull());
        }

        matchers = ImmutableList.copyOf(Iterables.filter(checkedMatchers, Predicates.notNull()));
    }

    /**
     * Get the composed matchers.
     * 
     * @return the composed matchers
     */
    @Nonnull @NonnullElements @Unmodifiable public List<Matcher> getComposedMatchers() {
        return matchers;
    }

}