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

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link ResolverPluginDependency}. */
public class ResolverPluginDependencyTest {

    /** Tests the state of a newly instantiated object. */
    @Test public void instantiation() {
        ResolverPluginDependency dep = new ResolverPluginDependency(" foo ");
        dep.setDependencyAttributeId(" bar ");
        Assert.assertEquals(dep.getDependencyPluginId(), "foo");
        Assert.assertEquals(dep.getDependencyAttributeId(), "bar");

        dep = new ResolverPluginDependency("foo ");
        dep.setDependencyAttributeId( "");
        Assert.assertEquals(dep.getDependencyPluginId(), "foo");
        Assert.assertNull(dep.getDependencyAttributeId());

        dep = new ResolverPluginDependency("foo ");
        dep.setDependencyAttributeId(null);
        Assert.assertEquals(dep.getDependencyPluginId(), "foo");
        Assert.assertNull(dep.getDependencyAttributeId());

        try {
            dep = new ResolverPluginDependency(null);
            Assert.fail("able to set null dependency ID");
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            dep = new ResolverPluginDependency(" ");
            Assert.fail("able to set empty dependency ID");
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void equalsToString() {
        ResolverPluginDependency dep = new ResolverPluginDependency(" foo ");
        dep.setDependencyAttributeId(" bar ");

        dep.toString();

        Assert.assertFalse(dep.equals(null));
        Assert.assertTrue(dep.equals(dep));
        Assert.assertFalse(dep.equals(this));

        ResolverPluginDependency other = new ResolverPluginDependency("foo");
        other.setDependencyAttributeId("bar  ");

        Assert.assertTrue(dep.equals(other));
        Assert.assertEquals(dep.hashCode(), other.hashCode());

        other = new ResolverPluginDependency(" bar ");
        other.setDependencyAttributeId(" foo");

        Assert.assertFalse(dep.equals(other));
        Assert.assertNotSame(dep.hashCode(), other.hashCode());

    }

}