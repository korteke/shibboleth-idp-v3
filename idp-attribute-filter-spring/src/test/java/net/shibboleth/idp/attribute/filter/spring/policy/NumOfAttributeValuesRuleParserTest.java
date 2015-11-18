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

package net.shibboleth.idp.attribute.filter.spring.policy;

import net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl.NumOfAttributeValuesPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumOfAttributeValuesRuleParserTest extends BaseAttributeFilterParserTest {

    @Test public void policy() throws ComponentInitializationException {

        NumOfAttributeValuesPolicyRule rule = (NumOfAttributeValuesPolicyRule) getPolicyRule("numberAttrValues.xml", true);
        Assert.assertEquals(rule.getAttributeId(), "uid");
        Assert.assertEquals(rule.getMinimum(), 1);
        Assert.assertEquals(rule.getMaximum(), 3);

        rule = (NumOfAttributeValuesPolicyRule) getPolicyRule("numberAttrValues.xml", false);
        Assert.assertEquals(rule.getAttributeId(), "uid");
        Assert.assertEquals(rule.getMinimum(), 1);
        Assert.assertEquals(rule.getMaximum(), 3);
}
 
}
