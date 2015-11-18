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

import java.util.Arrays;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;

/** Base class for {@link Matcher} and {@link PolicyRequirementRule} unit tests. */
public abstract class AbstractMatcherPolicyRuleTest {

    /** An attribute value. */
    protected StringAttributeValue value1;

    /** An attribute value. */
    protected StringAttributeValue value2;

    /** An attribute value. */
    protected StringAttributeValue value3;

    /** Attribute whose values are being matched. */
    protected IdPAttribute attribute;

    /** Current filter context. */
    protected AttributeFilterContext filterContext;

    /**
     * Initializes classes protected fields.
     */
    public void setUp() {
        value1 = new StringAttributeValue("value1");
        value2 = new StringAttributeValue("value2");
        value3 = new StringAttributeValue("value3");

        attribute = new IdPAttribute("foo");
        
        attribute.setValues(Arrays.asList(value1, value2, value3));

        filterContext = new AttributeFilterContext();
    }
}