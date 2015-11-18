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

package net.shibboleth.idp.attribute.resolver.ad.mapped.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.shibboleth.idp.attribute.StringAttributeValue;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link ValueMap}.
 */
public class ValueMapTest {
    
    
    @Test public void setterGetter() {
        final SourceValue value = new SourceValue("value", true, true);
        
        final ValueMap map = new ValueMap();
        
        map.setSourceValues(Collections.singleton(value));
        map.setReturnValue("return");
        
        
        Assert.assertEquals(map.getReturnValue(), "return");
        Assert.assertEquals(map.getSourceValues().size(), 1);
        Assert.assertTrue(map.getSourceValues().contains(value));
    }
    
    @Test public void subString() {
        final SourceValue value = new SourceValue("value", true, true);
        
        final ValueMap map = new ValueMap();
        
        map.setSourceValues(Collections.singleton(value));
        map.setReturnValue("return");
        
        Set<StringAttributeValue> result = map.apply("elephant");
        
        Assert.assertTrue(result.isEmpty());

        result = map.apply("elephantvaluegiraffe");
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(new StringAttributeValue("return")));
    }

    @Test public void regexp() {
        final HashSet<SourceValue> sources = new HashSet<>(3);
        
        sources.add(new SourceValue("R(.+)", false, false));
        sources.add(new SourceValue("RE(.+)", true, false));
        final ValueMap map = new ValueMap();
        map.setSourceValues(sources);
        map.setReturnValue("foo$1");
        
        Set<StringAttributeValue> result = map.apply("elephant");
        Assert.assertTrue(result.isEmpty());

        result = map.apply("Recursion");
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue("fooecursion")));
        Assert.assertTrue(result.contains(new StringAttributeValue("foocursion")));
        
    }

}
