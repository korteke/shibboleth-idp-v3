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

package net.shibboleth.idp.attribute.resolver;

import java.util.Collections;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;
/**
 * Largely boilerplate test for {@link ResolvedAttributeDefinition}
 * 
 */
public class ResolvedAttributeDefinitionTest {

    @Test public void init() {
        IdPAttribute attribute = new IdPAttribute("foo");
        MockStaticAttributeDefinition attrDef = new MockStaticAttributeDefinition();

        try {
            new ResolvedAttributeDefinition(null, attribute);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // OK
        }

        try {
            new ResolvedAttributeDefinition(attrDef, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // OK
        }

        try {
            new ResolvedAttributeDefinition(new MockStaticAttributeDefinition(), attribute);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // OK
        }

    }

    @Test public void equalsHashToString() throws ComponentInitializationException {
        IdPAttribute attribute = new IdPAttribute("foo");
        MockStaticAttributeDefinition attrDef = new MockStaticAttributeDefinition();
        attrDef.setValue(attribute);
        attrDef.setId("Defn");
        attrDef.initialize();
        ResolvedAttributeDefinition resolvedAttributeDefinition =
                new ResolvedAttributeDefinition(attrDef, new IdPAttribute("foo"));

        resolvedAttributeDefinition.toString();

        ResolvedAttributeDefinition otherResolvedAttributeDefinition;
        MockStaticAttributeDefinition otherDef = new MockStaticAttributeDefinition();
        otherDef.setValue(new IdPAttribute("bar"));
        otherDef.setId("OtherDefn");
        otherDef.initialize();
        otherResolvedAttributeDefinition = new ResolvedAttributeDefinition(otherDef, new IdPAttribute("bar"));

        Assert.assertFalse(resolvedAttributeDefinition.equals(null));
        Assert.assertFalse(resolvedAttributeDefinition.equals(this));
        Assert.assertFalse(resolvedAttributeDefinition.equals(otherResolvedAttributeDefinition));
        Assert.assertTrue(resolvedAttributeDefinition.equals(resolvedAttributeDefinition));
        Assert.assertTrue(resolvedAttributeDefinition.equals(attrDef));

        Assert.assertNotSame(resolvedAttributeDefinition.hashCode(), otherResolvedAttributeDefinition.hashCode());
        Assert.assertEquals(resolvedAttributeDefinition.hashCode(), attrDef.hashCode());

    }

    @Test public void noops() throws ComponentInitializationException {

        IdPAttribute attribute = new IdPAttribute("foo");
        MockStaticAttributeDefinition attrDef = new MockStaticAttributeDefinition();
        attrDef.setValue(attribute);
        attrDef.setId("Defn");
        ResolverPluginDependency dep = new ResolverPluginDependency("doo");
        dep.setDependencyAttributeId("foo");
        attrDef.setDependencies(Collections.singleton(dep));
        attrDef.setPropagateResolutionExceptions(false);
        attrDef.initialize();

        ResolvedAttributeDefinition resolvedAttributeDefinition =
                new ResolvedAttributeDefinition(attrDef, new IdPAttribute("foo"));
        resolvedAttributeDefinition.getActivationCondition();

        Assert.assertEquals(resolvedAttributeDefinition.getDependencies(), attrDef.getDependencies());
        Assert.assertNull(resolvedAttributeDefinition.getActivationCondition());
        Assert.assertFalse(resolvedAttributeDefinition.isPropagateResolutionExceptions());

        resolvedAttributeDefinition.setDependencyOnly(true);
        resolvedAttributeDefinition.setDisplayDescriptions(null);
        resolvedAttributeDefinition.setDisplayNames(null);

        resolvedAttributeDefinition.setPropagateResolutionExceptions(true);
        Assert.assertFalse(resolvedAttributeDefinition.isPropagateResolutionExceptions());

    }
}
