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

package net.shibboleth.idp.cas.service.impl;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link DefaultServiceComparator}.
 */
public class DefaultServiceComparatorTest {

    @Test
    public void testCompareSame() throws Exception {
        assertEquals(new DefaultServiceComparator().compare(
                "https://a.example.com/foo?bar=baz",
                "https://A.example.com/Foo?bar=BAZ"), 0);
    }

    @Test
    public void testCompareDifferent() throws Exception {
        assertEquals(new DefaultServiceComparator().compare(
                "https://a.example.com/foo?bar=1",
                "https://a.example.com/Foo?bar=2"), -1);
    }

    @Test
    public void testCompareSameWithJSessionID() throws Exception {
        assertEquals(new DefaultServiceComparator().compare(
                "https://a.example.com/foo/bar?baz=1",
                "https://A.example.com/Foo;jsessionid=abacefghijklmnop/bar?BAZ=1"), 0);
    }

    @Test
    public void testCompareDifferentWithJSessionID() throws Exception {
        assertFalse(new DefaultServiceComparator().compare(
                "https://a.example.com/foo/bar?baz=1",
                "https://A.example.com/Foo;jsessionid=abacefghijklmnop;a=b/bar?BAZ=1") == 0);
    }

    @Test
    public void testCompareSameWithMultipleParams() throws Exception {
        assertEquals(new DefaultServiceComparator("a", "b", "c").compare(
                "https://a.sub.example.com/foo/bar?baz=1",
                "https://a.sub.example.com/Foo;a=1;b=2/bar;c=3?BAZ=1"), 0);
    }
}