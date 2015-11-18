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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeDefinition}. This test does not test any methods inherited from
 * {@link ResolverPlugin}, those are covered in {@link AbstractResolverPluginTest}.
 */
public class AbstractAttributeDefinitionTest {

    /** Tests the state of a newly instantiated object. */
    @Test
    public void instantiation() {
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);

        Assert.assertEquals(definition.getId(), "foo");
        Assert.assertFalse(definition.isDependencyOnly());
        Assert.assertNotNull(definition.getAttributeEncoders());
        Assert.assertTrue(definition.getAttributeEncoders().isEmpty());
        Assert.assertNotNull(definition.getDisplayDescriptions());
        Assert.assertTrue(definition.getDisplayDescriptions().isEmpty());
        Assert.assertNotNull(definition.getDisplayNames());
        Assert.assertTrue(definition.getDisplayNames().isEmpty());
    }

    /** Tests setting and retrieving the dependency only option. */
    @Test
    public void dependencyOnly() {
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);
        Assert.assertFalse(definition.isDependencyOnly());

        definition.setDependencyOnly(true);
        Assert.assertTrue(definition.isDependencyOnly());

        definition.setDependencyOnly(true);
        Assert.assertTrue(definition.isDependencyOnly());

        definition.setDependencyOnly(false);
        Assert.assertFalse(definition.isDependencyOnly());

        definition.setDependencyOnly(false);
        Assert.assertFalse(definition.isDependencyOnly());
    }

    /** Tests setting and retrieving encoders. */
    @Test
    public void encoders() {
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);

        MockAttributeEncoder enc1 = new MockAttributeEncoder(null, null);
        MockAttributeEncoder enc2 = new MockAttributeEncoder(null, null);

        Set<AttributeEncoder<?>> encoders = new HashSet<>(2);

        definition.setAttributeEncoders(null);
        Assert.assertNotNull(definition.getAttributeEncoders());
        Assert.assertTrue(definition.getAttributeEncoders().isEmpty());

        definition.setAttributeEncoders(encoders);
        Assert.assertNotNull(definition.getAttributeEncoders());
        Assert.assertTrue(definition.getAttributeEncoders().isEmpty());

        encoders.add(enc1);
        encoders.add(null);
        encoders.add(enc2);
        Assert.assertNotNull(definition.getAttributeEncoders());
        Assert.assertTrue(definition.getAttributeEncoders().isEmpty());

        definition.setAttributeEncoders(encoders);
        Assert.assertEquals(definition.getAttributeEncoders().size(), 2);
        Assert.assertTrue(definition.getAttributeEncoders().contains(enc1));
        Assert.assertTrue(definition.getAttributeEncoders().contains(enc2));

        encoders.clear();
        encoders.add(enc2);
        definition.setAttributeEncoders(encoders);
        Assert.assertEquals(definition.getAttributeEncoders().size(), 1);
        Assert.assertFalse(definition.getAttributeEncoders().contains(enc1));
        Assert.assertTrue(definition.getAttributeEncoders().contains(enc2));

        try {
            definition.getAttributeEncoders().add(enc2);
            Assert.fail("able to add entry to supposedly unmodifiable collection");
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Tests that display descriptions are properly added and modified. */
    @Test
    public void displayDescriptions() {
        Locale en = new Locale("en");
        Locale enbr = new Locale("en", "br");

        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);
        
        Map<Locale, String> descriptions = new HashMap<>();
        descriptions.put(en, "english");
        descriptions.put(enbr, null);
        definition.setDisplayDescriptions(descriptions);
        
        Assert.assertFalse(definition.getDisplayDescriptions().isEmpty());
        Assert.assertEquals(definition.getDisplayDescriptions().size(), 1);
        Assert.assertNotNull(definition.getDisplayDescriptions().get(en));

        descriptions = definition.getDisplayDescriptions();
        try {
            descriptions.put(enbr, "british");
            Assert.fail("able to add description to unmodifable map");
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Tests that display names are properly added and modified. */
    @Test
    public void displayNames() {
        Locale en = new Locale("en");
        Locale enbr = new Locale("en", "br");

        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);

        Map<Locale, String> names = new HashMap<>();
        names.put(en, "english");
        names.put(enbr, null);
        definition.setDisplayNames(names);
        
        Assert.assertFalse(definition.getDisplayNames().isEmpty());
        Assert.assertEquals(definition.getDisplayNames().size(), 1);
        Assert.assertNotNull(definition.getDisplayNames().get(en));

        names = definition.getDisplayNames();
        try {
            names.put(enbr, "british");
            Assert.fail("able to add name to unmodifable map");
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Test resolve an attribute. */
    @Test
    public void resolve() throws Exception {
        AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);

        MockAttributeDefinition definition = new MockAttributeDefinition("foo", (IdPAttribute) null);
        definition.initialize();
        Assert.assertNull(definition.resolve(context));

        IdPAttribute attribute = new IdPAttribute("foo");
        definition = new MockAttributeDefinition("foo", attribute);
        definition.initialize();
        Assert.assertEquals(definition.resolve(context), attribute);

    }
    
    @Test
    public void dependencies() throws ComponentInitializationException {
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", null);
        
        definition.setDependencies(Collections.singleton(new ResolverPluginDependency("plugin")));
        definition.initialize();
        
        Set<ResolverPluginDependency> depends = definition.getDependencies();
        
        Assert.assertEquals(depends.size(), 1);
        Assert.assertNull(definition.getSourceAttributeId());
        Assert.assertNull(depends.iterator().next().getDependencyAttributeId());
        
        definition = new MockAttributeDefinition("foo", null);
        definition.setSourceAttributeId("source");
        definition.setDependencies(Collections.singleton(new ResolverPluginDependency("plugin")));
        definition.initialize();
        
        Assert.assertEquals(definition.getSourceAttributeId(), "source");
        
        depends = definition.getDependencies();
        
        Assert.assertEquals(depends.size(), 1);
        Assert.assertEquals(depends.iterator().next().getDependencyAttributeId(), "source");
    }
    
    @Test public void initDestroyValidate() throws ComponentInitializationException {
        MockAttributeEncoder encoder = new MockAttributeEncoder("foo", "baz");
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", (IdPAttribute) null);
        
        Set<AttributeEncoder<?>> encoders = new HashSet<>(1);
        encoders.add(encoder);
        definition.setAttributeEncoders(encoders);
        
        Assert.assertFalse(encoder.isInitialized());
        Assert.assertFalse(encoder.isDestroyed());

        encoder.initialize();
        definition.initialize();
        Assert.assertTrue(encoder.isInitialized());
        Assert.assertFalse(encoder.isDestroyed());

        definition.destroy();
        encoder.destroy();
        Assert.assertTrue(encoder.isInitialized());
        Assert.assertTrue(encoder.isDestroyed());
        
        
    }

    /**
     * This class implements the minimal level of functionality and is meant only as a means of testing the abstract
     * {@link AttributeDefinition}.
     */
    private static final class MockAttributeDefinition extends AbstractAttributeDefinition {

        /** Static attribute value returned from resolution. */
        private IdPAttribute staticAttribute;

        /**
         * Constructor.
         * 
         * @param id id of the attribute definition, never null or empty
         * @param attribute value returned from the resolution of this attribute, may be null
         */
        public MockAttributeDefinition(String id, IdPAttribute attribute) {
            setId(id);
            staticAttribute = attribute;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable protected IdPAttribute doAttributeDefinitionResolve(
                @Nonnull final AttributeResolutionContext resolutionContext,
                @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
            return staticAttribute;
        }
    }
}