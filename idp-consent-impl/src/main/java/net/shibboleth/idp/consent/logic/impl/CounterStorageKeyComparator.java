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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Comparator} used to order storage keys so that the least used and oldest storage keys are returned first.
 * 
 * The constructor for this class accepts two arguments : (1) the storage keys in order of oldest first and (2) the
 * number of times the storage keys were used.
 */
public class CounterStorageKeyComparator implements Comparator<String> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CounterStorageKeyComparator.class);

    /** Storage keys in FIFO order. */
    @Nonnull private final List<String> storageKeys;

    /** Map of storage keys to counters. */
    @Nonnull private final Map<String, Long> keyToCounterMap;

    /**
     * 
     * Constructor.
     *
     * @param keys storage keys in FIFO order
     * @param map number of times the storage keys were used
     */
    public CounterStorageKeyComparator(@Nonnull final List<String> keys, @Nonnull final Map<String, Long> map) {
        storageKeys = Constraint.isNotNull(keys, "Storage keys cannot be null");
        keyToCounterMap = Constraint.isNotNull(map, "Storage key to counter map cannot be null");
    }

    /** {@inheritDoc} */
    public int compare(String o1, String o2) {

        final Long counter1 = keyToCounterMap.get(o1);
        final Long counter2 = keyToCounterMap.get(o2);

        if (counter1 == null && counter2 == null) {
            // Compare based on storage key list ordering.
            return Integer.compare(storageKeys.indexOf(o1), storageKeys.indexOf(o2));
        } else if (counter1 == null) {
            return -1;
        } else if (counter2 == null) {
            return 1;
        } else if (counter1.equals(counter2)) {
            // Compare based on storage key list ordering.
            return Integer.compare(storageKeys.indexOf(o1), storageKeys.indexOf(o2));
        } else {
            // Compare counters.
            return Long.compare(counter1, counter2);
        }

    }

}
