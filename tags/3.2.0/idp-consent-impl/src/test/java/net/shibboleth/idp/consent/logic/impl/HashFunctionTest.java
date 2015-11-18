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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link HashFunction} unit test. */
public class HashFunctionTest {

    private HashFunction function;

    @BeforeMethod public void setUp() {
        function = new HashFunction();
    }

    @Test public void testNullInput() {
        Assert.assertNull(function.apply(null));
    }

    @Test public void testEmptyInput() {
        Assert.assertEquals(function.apply(""), "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    }

    @Test public void testHash() {
        Assert.assertEquals(function.apply("foo"), "LCa0a2j/xo/5m0U8HTBBNBNCLXBkg7+g+YpeiGJm564=");
    }

}
