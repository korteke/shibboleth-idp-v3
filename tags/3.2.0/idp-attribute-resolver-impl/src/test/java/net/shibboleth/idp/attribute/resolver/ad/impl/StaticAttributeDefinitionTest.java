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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.Arrays;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ad.impl.StaticAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for {@link StaticAttributeDefinition}
 *
 */
public class StaticAttributeDefinitionTest {

    @Test public void resolve() throws ComponentInitializationException, ResolutionException {
        StaticAttributeDefinition attrDef = new StaticAttributeDefinition();
        attrDef.setId("Static");
        Assert.assertNull(attrDef.getValue());
        
        attrDef.setValue(null);
        Assert.assertNull(attrDef.getValue());

        try {
            attrDef.initialize();
            Assert.fail("Cannot initialize without an attribute");
        } catch (ComponentInitializationException e) {
            //OK
        }

        IdPAttribute attribute = new IdPAttribute("attribute");
        attribute.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        attrDef.setValue(attribute);
        Assert.assertNotNull(attrDef.getValue());

        try {
            attrDef.resolve(new AttributeResolutionContext());
            Assert.fail("Need to be initialized to resolve");
        } catch (UninitializedComponentException e) {
            // OK
        }
        
        attrDef.initialize();

        Assert.assertNotNull(attrDef.getValue());

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        IdPAttribute result = attrDef.resolve(context);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), "attribute");
        
        try {
            attrDef.setValue(new IdPAttribute("other"));
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            // OK
        }

        attrDef.destroy();
        
        try {
            attrDef.initialize();
            Assert.fail();
        } catch (DestroyedComponentException e) {
            //OK
        }

        try {
            attrDef.setValue(new IdPAttribute("other"));
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            // OK
        } catch (DestroyedComponentException e) {
            //OK
        }

        try {
            attrDef.resolve(new AttributeResolutionContext());
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // OK
        }

    }
    
}