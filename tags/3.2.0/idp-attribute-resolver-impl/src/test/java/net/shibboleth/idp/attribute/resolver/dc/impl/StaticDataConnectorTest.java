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

package net.shibboleth.idp.attribute.resolver.dc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.StaticDataConnector;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for {@link StaticDataConnector}
 *
 */
public class StaticDataConnectorTest {

    @Test public void resolve() throws ComponentInitializationException, ResolutionException {
        IdPAttribute attribute = new IdPAttribute("attribute");
        attribute.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        StaticDataConnector connector = new StaticDataConnector();
        connector.setId("Static");

        Assert.assertNull(connector.getAttributes());
        connector.setValues(null);
        Assert.assertNull(connector.getAttributes());

        try {
            connector.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            //OK
        }
        
        List<IdPAttribute> input = new ArrayList<>();
        connector.setValues(input);
        Assert.assertNotNull(connector.getAttributes());

        input.add(null);
        connector.setValues(input);
        Assert.assertNotNull(connector.getAttributes());        

        input.add(attribute);
        input.add(null);
        input.add(new IdPAttribute("thingy"));
        
        connector.setValues(input);
        connector.initialize();

        Assert.assertEquals(connector.getAttributes().size(), 2);

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        Map<String, IdPAttribute> result = connector.resolve(context);

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.containsKey("attribute"));
        Assert.assertTrue(result.containsKey("thingy"));

    }
    
    @Test public void initDestroy() throws ComponentInitializationException, ResolutionException {

        StaticDataConnector connector = new StaticDataConnector();
        connector.setId("Static");

        List<IdPAttribute> input = new ArrayList<>();
        input.add(null);
        connector.setValues(input);
        
        try {
            connector.resolve(new AttributeResolutionContext());
            Assert.fail();
        } catch (UninitializedComponentException e) {
            //OK
        }

        input.add(new IdPAttribute("thingy"));
        
        connector.setValues(input);
        connector.initialize();

        try {
            connector.setValues(Collections.singletonList(new IdPAttribute("whatever")));
            Assert.fail();
        } catch (UnmodifiableComponentException ex) {
            // OK
        }

        connector.destroy();

        try {
            connector.resolve(new AttributeResolutionContext());
            Assert.fail();
        } catch (DestroyedComponentException e) {
            //OK
        }
    }

}
