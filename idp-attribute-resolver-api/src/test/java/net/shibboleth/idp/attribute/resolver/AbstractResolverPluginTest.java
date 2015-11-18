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

import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/** Unit test for {@link ResolverPlugin}. */
public class AbstractResolverPluginTest {

    /** Test an instantiated object has the proper state. */
    @Test public void instantiation() {
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin(" foo ", "bar");

        Assert.assertEquals(plugin.getId(), "foo");
        Assert.assertTrue(plugin.isPropagateResolutionExceptions());
        Assert.assertNull(plugin.getActivationCondition());
        Assert.assertNotNull(plugin.getDependencies());
        Assert.assertTrue(plugin.getDependencies().isEmpty());
    }

    /** Test get/set activation criteria. */
    @Test public void activationCriteria() {
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin(" foo ", "bar");

        plugin.setActivationCondition(Predicates.<ProfileRequestContext> alwaysFalse());
        Assert.assertEquals(plugin.getActivationCondition(), Predicates.alwaysFalse());
        try {
            plugin.setActivationCondition(null);
            Assert.fail("Able to set a null activiation criteria");
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }
    
    @Test public void nativation() {
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin(" foo ", "bar");
        Assert.assertEquals(plugin.getProfileContextStrategy().getClass(), ParentContextLookup.class);
        plugin.setProfileContextStrategy(new TestFunc());
        Assert.assertEquals(plugin.getProfileContextStrategy().getClass(), TestFunc.class);
    }

    /** Test setters to {@link AbstractResolverPlugin#setPropagateResolutionExceptions(boolean)}. */
    @Test public void propogateSetters() {
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin("foo", "bar");

        plugin.setPropagateResolutionExceptions(true);
        Assert.assertTrue(plugin.isPropagateResolutionExceptions());

        plugin.setPropagateResolutionExceptions(true);
        Assert.assertTrue(plugin.isPropagateResolutionExceptions());

        plugin.setPropagateResolutionExceptions(false);
        Assert.assertFalse(plugin.isPropagateResolutionExceptions());
    }

    /** Test add, removing, setting dependencies. */
    @Test public void dependencies() {
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin("foo", "bar");

        plugin.setDependencies(null);
        Assert.assertNotNull(plugin.getDependencies());
        Assert.assertTrue(plugin.getDependencies().isEmpty());

        HashSet<ResolverPluginDependency> depdencies = new HashSet<>();
        plugin.setDependencies(depdencies);
        Assert.assertNotNull(plugin.getDependencies());
        Assert.assertTrue(plugin.getDependencies().isEmpty());

        depdencies.add(null);
        plugin.setDependencies(depdencies);
        Assert.assertNotNull(plugin.getDependencies());
        Assert.assertTrue(plugin.getDependencies().isEmpty());

        ResolverPluginDependency dep1 = new ResolverPluginDependency("foo");
        dep1.setDependencyAttributeId("bar");
        ResolverPluginDependency dep2 = new ResolverPluginDependency("foo");
        dep2.setDependencyAttributeId("baz");

        depdencies.add(dep1);
        depdencies.add(dep1);
        depdencies.add(dep2);

        plugin.setDependencies(depdencies);
        Assert.assertNotNull(plugin.getDependencies());
        Assert.assertTrue(plugin.getDependencies().size() == 2);

        try {
            plugin.getDependencies().add(dep1);
            Assert.fail("able to add entry to supossedly unmodifiable collection");
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Test {@link ResolverPlugin#resolve(AttributeResolutionContext)}. */
    @Test public void resolver() throws Exception {
        AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        MockBaseResolverPlugin plugin = new MockBaseResolverPlugin("foo", "bar");
        plugin.initialize();

        Assert.assertEquals(plugin.resolve(context), "bar");

        context = new AttributeResolutionContext();
        plugin = new MockBaseResolverPlugin(" foo ", "bar");
        plugin.setActivationCondition(Predicates.<ProfileRequestContext> alwaysFalse());

        plugin.initialize();
        Assert.assertNull(plugin.resolve(context));

    }

    /**
     * This class implements the minimal level of functionality and is meant only as a means of testing the abstract
     * {@link ResolverPlugin}.
     */
    private static final class MockBaseResolverPlugin extends AbstractResolverPlugin<String> {

        /** Static value return by resolution. */
        private String resolverValue;

        /**
         * Constructor.
         * 
         * @param id id of this plugin
         * @param value value returned by resolution
         */
        public MockBaseResolverPlugin(String id, String value) {
            setId(id);
            resolverValue = value;
        }

        /** {@inheritDoc} */
        @Override @Nullable protected String doResolve(@Nonnull final AttributeResolutionContext resolutionContext,
                @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
            return resolverValue;
        }
    }

    static class TestFunc implements Function<AttributeResolutionContext, ProfileRequestContext> {

        /** {@inheritDoc} */
        @Override @Nullable public ProfileRequestContext apply(@Nullable AttributeResolutionContext input) {
            return null;
        }

    }
}