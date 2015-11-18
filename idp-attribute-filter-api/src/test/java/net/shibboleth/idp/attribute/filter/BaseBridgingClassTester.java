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

package net.shibboleth.idp.attribute.filter;

import java.util.Arrays;
import java.util.HashSet;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Base for the various XX from YY test clases
 */
public class BaseBridgingClassTester {
    
    protected final IdPAttributeValue<?> VALUE1 = new StringAttributeValue("value1");
    protected final IdPAttributeValue<?> VALUE2 = new StringAttributeValue("value2");
    protected final IdPAttributeValue<?> VALUE3 = new StringAttributeValue("value3");
    
    protected final String NAME1 = "foo";
    protected final String NAME2 = "bar";
    
    protected AttributeFilterContext setUpCtx() {
        HashSet<IdPAttribute> attributes = new HashSet<>(2);

        IdPAttribute attribute = new IdPAttribute(NAME1);
        attribute.setValues(Arrays.asList(VALUE1, VALUE2));
        attributes.add(attribute);
        

        attribute = new IdPAttribute(NAME2);
        attribute.setValues(Arrays.asList(VALUE1, VALUE3));
        attributes.add(attribute);
        AttributeFilterContext filterContext = new AttributeFilterContext();

        filterContext.setPrefilteredIdPAttributes(attributes);
        
        return filterContext;
    }

    @Test public void baseClass() throws ComponentInitializationException {
        
        BaseBridgingClass base = new BaseBridgingClass(new Object()) {};
        
        String s = base.getLogPrefix();
        base.setId(NAME2);
        Assert.assertEquals(base.getLogPrefix(), s);
        base.initialize();
        Assert.assertNotEquals(base.getLogPrefix(), s);
        base.destroy();
        
    }

}