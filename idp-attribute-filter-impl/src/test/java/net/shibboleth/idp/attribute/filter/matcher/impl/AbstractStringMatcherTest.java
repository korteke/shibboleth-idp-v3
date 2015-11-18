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

package net.shibboleth.idp.attribute.filter.matcher.impl;

import net.shibboleth.idp.attribute.IdPAttributeValue;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AbstractStringMatcher}
 */
public class AbstractStringMatcherTest {

    @Test public void testSettersGetters() {
        AbstractStringMatcher matcher = new AbstractStringMatcher(){

            @Override
            protected boolean compareAttributeValue(IdPAttributeValue value) {
                return false;
            }};

        Assert.assertNull(matcher.getMatchString());
        Assert.assertFalse(!matcher.isIgnoreCase());

        matcher.setIgnoreCase(false);
        Assert.assertFalse(matcher.isIgnoreCase());
        matcher.setIgnoreCase(true);
        Assert.assertTrue(matcher.isIgnoreCase());

        matcher.setMatchString(DataSources.TEST_STRING);
        Assert.assertEquals(matcher.getMatchString(), DataSources.TEST_STRING);
    }

    @Test public void testApply() {
        AbstractStringMatcher matcher = new AbstractStringMatcher() {

            @Override
            protected boolean compareAttributeValue(IdPAttributeValue value) {
                return false;
            }};
        matcher.setIgnoreCase(false);
        matcher.setMatchString(DataSources.TEST_STRING);

        Assert.assertTrue(matcher.stringCompare(DataSources.TEST_STRING));
        Assert.assertFalse(matcher.stringCompare(DataSources.TEST_STRING_UPPER));
        matcher.setIgnoreCase(true);
        Assert.assertTrue(matcher.stringCompare(DataSources.TEST_STRING));
        Assert.assertTrue(matcher.stringCompare(DataSources.TEST_STRING_UPPER));
        
        Assert.assertFalse(matcher.stringCompare(null));
    }
    

}
