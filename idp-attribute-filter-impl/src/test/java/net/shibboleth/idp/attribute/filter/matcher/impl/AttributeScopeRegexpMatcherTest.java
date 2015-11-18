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

import net.shibboleth.idp.attribute.filter.matcher.impl.AttributeScopeRegexpMatcher;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test For {@link AttributeScopeRegexpMatcher}.
 */
public class AttributeScopeRegexpMatcherTest {
    
    @Test public void testApply() throws ComponentInitializationException {
        AttributeScopeRegexpMatcher matcher = new AttributeScopeRegexpMatcher();
        matcher.setRegularExpression(DataSources.TEST_REGEX);
        matcher.setId("TestId");
        matcher.initialize();
        
        Assert.assertFalse(matcher.compareAttributeValue(DataSources.STRING_VALUE));
        Assert.assertFalse(matcher.compareAttributeValue(DataSources.SCOPED_VALUE_VALUE_MATCH));
        Assert.assertTrue(matcher.compareAttributeValue(DataSources.SCOPED_VALUE_SCOPE_MATCH));
        Assert.assertFalse(matcher.compareAttributeValue(null));
        
    }

}
