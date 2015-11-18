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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link CounterStorageKeyComparator}. */
public class CounterStorageKeyComparatorTest {

    private CounterStorageKeyComparator c;

    private List<String> keys;

    private Map<String, Long> map;

    @BeforeMethod public void setUp() {

        keys = Arrays.asList("key1", "key2", "key3", "key4");

        map = new LinkedHashMap<>();

        c = new CounterStorageKeyComparator(keys, map);
    }

    @Test public void testNoCounters() {
        Collections.sort(keys, c);

        // insertion order
        Assert.assertEquals(keys, Arrays.asList("key1", "key2", "key3", "key4"));
    }

    @Test public void testSameCounters() {
        map.put("key1", Long.valueOf(1));
        map.put("key2", Long.valueOf(1));
        map.put("key3", Long.valueOf(1));
        map.put("key4", Long.valueOf(1));

        Collections.sort(keys, c);

        // insertion order
        Assert.assertEquals(keys, Arrays.asList("key1", "key2", "key3", "key4"));
    }

    @Test public void testDifferentCounters() {
        map.put("key1", Long.valueOf(4));
        map.put("key2", Long.valueOf(2));
        map.put("key3", Long.valueOf(1));
        map.put("key4", Long.valueOf(3));

        Collections.sort(keys, c);

        // lowest counters first
        Assert.assertEquals(keys, Arrays.asList("key3", "key2", "key4", "key1"));
    }

    @Test public void testSomeSameCounters1() {
        map.put("key1", Long.valueOf(2));
        map.put("key2", Long.valueOf(2));
        map.put("key3", Long.valueOf(1));
        map.put("key4", Long.valueOf(1));

        Collections.sort(keys, c);

        // lowest counters first, fall back to insertion order
        Assert.assertEquals(keys, Arrays.asList("key3", "key4", "key1", "key2"));
    }

    @Test public void testSomeSameCounters2() {
        map.put("key1", Long.valueOf(2));
        map.put("key2", Long.valueOf(1));
        map.put("key3", Long.valueOf(2));
        map.put("key4", Long.valueOf(1));

        Collections.sort(keys, c);

        // lowest counters first, fall back to insertion order
        Assert.assertEquals(keys, Arrays.asList("key2", "key4", "key1", "key3"));
    }

    @Test public void testMissingCounters1() {
        map.put("key1", Long.valueOf(1));

        map.put("key3", Long.valueOf(1));
        map.put("key4", Long.valueOf(1));

        Collections.sort(keys, c);
        
        // missing counters first, then lowest counters, fall back to insertion order
        Assert.assertEquals(keys, Arrays.asList("key2", "key1", "key3", "key4"));
    }

    @Test public void testMissingCounters2() {
        map.put("key1", Long.valueOf(2));

        map.put("key3", Long.valueOf(1));
        map.put("key4", Long.valueOf(1));

        Collections.sort(keys, c);

        // missing counters first, then lowest counters, fall back to insertion order
        Assert.assertEquals(keys, Arrays.asList("key2", "key3", "key4", "key1"));
    }
}
