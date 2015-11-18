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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link PreferExplicitOrderComparator} unit test. */
public class PreferExplicitOrderComparatorTest {

    private Comparator c;

    private List<String> valuesInOrder;

    private List<String> unknownValues;

    private List<String> toBeSorted;

    private List<String> expected;

    @BeforeMethod public void setUp() {
        valuesInOrder = Arrays.asList("first", "middle", "last");
        unknownValues = Arrays.asList("some", "unknown", "values");

        expected = new ArrayList<>();
        expected.addAll(valuesInOrder);
        expected.addAll(unknownValues);

        toBeSorted = new ArrayList<>();
        toBeSorted.addAll(valuesInOrder);
        toBeSorted.addAll(unknownValues);

        Collections.shuffle(toBeSorted);
    }

    @Test(invocationCount = 10) public void testExplicitOrdering() {

        Collections.shuffle(toBeSorted);

        c = new PreferExplicitOrderComparator(valuesInOrder);

        Collections.sort(toBeSorted, c);

        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testExplicitOrderingWithDuplicateValuesInConstructor() {
    
        c = new PreferExplicitOrderComparator(Arrays.asList("first", "middle", "last", "middle"));
    
        Collections.sort(toBeSorted, c);
    
        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testExplicitOrderingWithEmptyValuesInConstructor() {

        c = new PreferExplicitOrderComparator(Arrays.asList("first", "", "middle", " ", "last"));

        Collections.sort(toBeSorted, c);

        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testExplicitOrderingWithKnownValuesOnly() {

        toBeSorted = new ArrayList(valuesInOrder);

        Collections.shuffle(toBeSorted);

        c = new PreferExplicitOrderComparator(valuesInOrder);

        Collections.sort(toBeSorted, c);

        Assert.assertEquals(toBeSorted, valuesInOrder);
    }

    @Test public void testExplicitOrderingWithNullValuesInConstructor() {

        c = new PreferExplicitOrderComparator(Arrays.asList("first", null, "middle", "last"));

        Collections.sort(toBeSorted, c);

        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testExplicitOrderingWithWhitespaceInConstructor() {

        c = new PreferExplicitOrderComparator(Arrays.asList(" first ", "\tmiddle", "last\n"));

        Collections.sort(toBeSorted, c);

        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testNaturalOrderingWithEmptyConstructor() {
    
        // natural ordering
        Collections.sort(expected);
    
        c = new PreferExplicitOrderComparator(Collections.<String> emptyList());
    
        Collections.sort(toBeSorted, c);
    
        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testNaturalOrderingWithNoKnownValues() {
    
        // natural ordering
        Collections.sort(expected);
    
        c = new PreferExplicitOrderComparator(Arrays.asList("not", "known"));
    
        Collections.sort(toBeSorted, c);
    
        Assert.assertEquals(toBeSorted, expected);
    }

    @Test public void testNaturalOrderingWithNullConstructor() {
    
        // natural ordering
        Collections.sort(expected);
    
        c = new PreferExplicitOrderComparator(null);
    
        Collections.sort(toBeSorted, c);
    
        Assert.assertEquals(toBeSorted, expected);
    }

    @Test(expectedExceptions = NullPointerException.class) public void testNullValuesToBeSorted() {

        toBeSorted.add(null);

        Collections.shuffle(toBeSorted);

        c = new PreferExplicitOrderComparator(valuesInOrder);

        Collections.sort(toBeSorted, c);
    }

}
