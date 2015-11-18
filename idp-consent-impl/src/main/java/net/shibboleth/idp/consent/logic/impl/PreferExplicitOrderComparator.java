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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.TrimOrNullStringFunction;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * Comparator which prefers to order strings according to the order in which they appear in a list, and which falls back
 * to natural ordering for strings not present in the list.
 * 
 * Relies on Guava's {@link Ordering#explicit(List)} to compare strings present in the list. Strings not present in the
 * list are treated as greater than strings present in the list and are compared according to their natural ordering.
 * Does not support comparing null values. Is not serializable, and such should not be used as part of a TreeMap, for
 * example, which is serialized.
 */
public class PreferExplicitOrderComparator implements Comparator<String> {

    /** Explicit ordering. */
    @Nullable private Ordering explicitOrdering;

    /** Strings in order. */
    @Nonnull @NonnullElements @Unmodifiable private List<String> explicitOrder;

    /**
     * Constructor.
     * 
     * @param order the desired order, null and empty strings are ignored, duplicates are removed
     */
    public PreferExplicitOrderComparator(@Nullable @NullableElements final List<String> order) {
        if (order == null) {
            explicitOrder = Collections.emptyList();
        } else {
            // trimmed
            explicitOrder = Lists.transform(order, TrimOrNullStringFunction.INSTANCE);

            // non-null
            explicitOrder = ImmutableList.copyOf(Iterables.filter(explicitOrder, Predicates.notNull()));

            // no duplicates
            explicitOrder = ImmutableSet.copyOf(explicitOrder).asList();

            explicitOrdering = Ordering.explicit(explicitOrder);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException if either argument is null
     */
    public int compare(String o1, String o2) {

        final boolean containsLeft = explicitOrder.contains(o1);
        final boolean containsRight = explicitOrder.contains(o2);

        if (containsLeft && containsRight) {
            return explicitOrdering.compare(o1, o2);
        }

        if (containsLeft) {
            return -1;
        }

        if (containsRight) {
            return 1;
        }

        return Ordering.natural().compare(o1, o2);
    }

}
