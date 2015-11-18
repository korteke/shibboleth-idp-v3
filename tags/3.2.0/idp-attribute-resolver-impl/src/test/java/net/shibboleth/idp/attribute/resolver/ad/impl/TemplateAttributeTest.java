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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.ad.impl.StaticAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.ad.impl.TemplateAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.collection.LazySet;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.apache.velocity.app.VelocityEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

/** test for {@link net.shibboleth.idp.attribute.resolver.impl.TemplateAttribute}. */
@ThreadSafe
public class TemplateAttributeTest {

    /** The name. */
    private static final String TEST_ATTRIBUTE_BASE_NAME = "TEMPLATE";

    /** Simple result. */
    private static final String SIMPLE_VALUE_STRING = "simple";

    private static final StringAttributeValue SIMPLE_VALUE_RESULT = new StringAttributeValue(SIMPLE_VALUE_STRING);

    /** A simple script to set a constant value. */
    private static final String TEST_SIMPLE_TEMPLATE = SIMPLE_VALUE_STRING;

    /** A simple script to set a value based on input values. */
    private static final String TEST_ATTRIBUTES_TEMPLATE_ATTR = "Att " + "${"
            + TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR + "}-" + "${" + TestSources.DEPENDS_ON_SECOND_ATTRIBUTE_NAME
            + "}";

    private static final String TEST_ATTRIBUTES_TEMPLATE_CONNECTOR = "Att " + "${"
            + TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR + "}-" + "${"
            + TestSources.DEPENDS_ON_SECOND_ATTRIBUTE_NAME + "}";

    /** Our singleton engine. */
    private static VelocityEngine engineSingleton;

    /**
     * Create new or return the velocity engine with enough hardwired properties to get us going.
     * 
     * @return a new engine suitable groomed
     */
    private VelocityEngine getEngine() {
        if (null == engineSingleton) {
            engineSingleton = new VelocityEngine();
            try {
                engineSingleton.addProperty("string.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
                engineSingleton.addProperty("classpath.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                engineSingleton.addProperty("resource.loader", "classpath, string");
                engineSingleton.init();
            } catch (Exception e) {
                Assert.fail("couldn't create engine", e);
            }
        }
        return engineSingleton;
    }

    /**
     * Test resolution of an template script (statically generated data).
     * 
     * @throws ResolutionException id resolution fails
     * @throws ComponentInitializationException only if bad things thingas
     */
    @Test public void simple() throws ResolutionException, ComponentInitializationException {

        final String name = TEST_ATTRIBUTE_BASE_NAME + "1";
        TemplateAttributeDefinition attr = new TemplateAttributeDefinition();
        attr.setId(name);
        Assert.assertNull(attr.getTemplate());
        attr.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        try {
            attr.initialize();
            Assert.fail("No template");
        } catch (ComponentInitializationException ex) {
            // OK
        }
        attr = new TemplateAttributeDefinition();
        attr.setId(name);
        Assert.assertNull(attr.getTemplateText());
        attr.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_ATTR);
        Assert.assertNull(attr.getVelocityEngine());
        attr.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        try {
            attr.initialize();
            Assert.fail("engine");
        } catch (ComponentInitializationException ex) {
            // OK
        }

        attr = new TemplateAttributeDefinition();
        attr.setId(name);
        attr.setVelocityEngine(getEngine());
        attr.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_ATTR);
        try {
            attr.initialize();
            Assert.fail("No dependencies");
        } catch (ComponentInitializationException ex) {
            // OK
        }
        Assert.assertNotNull(attr.getTemplateText());

        attr.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        
        attr.initialize();
        Assert.assertNotNull(attr.getTemplate());
        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        final IdPAttribute val = attr.resolve(context);
        final Collection<?> results = val.getValues();

        Assert.assertEquals(results.size(), 0, "Templated value count");

        attr = new TemplateAttributeDefinition();
        attr.setId(name);
        attr.setVelocityEngine(getEngine());
        attr.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_ATTR);
        attr.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        
        attr.setSourceAttributes(Collections.singletonList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        attr.initialize();
        Assert.assertNotNull(attr.getTemplate());
        try {
            attr.resolve(context);
        } catch (ResolutionException e) {
            // OK
        }

        attr = new TemplateAttributeDefinition();
        attr.setId(name);
        attr.setVelocityEngine(getEngine());
        attr.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        try {
            attr.initialize();
            Assert.fail("No Text or attributes");
        } catch (ComponentInitializationException ex) {
            // OK
        }
        attr.setSourceAttributes(Collections.singletonList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        attr.initialize();
        Assert.assertEquals(attr.getTemplateText(), "${" + TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR + "} ");
        Assert.assertEquals(attr.getSourceAttributes().get(0), TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);
        Assert.assertEquals(attr.getSourceAttributes().size(), 1);

    }

    /**
     * Test resolution of an template script (statically generated data). By giving it attributes we create some values.
     * 
     * @throws ResolutionException if resolution fails
     * @throws ComponentInitializationException only if things go wrong
     */
    @Test public void simpleWithValues() throws ResolutionException, ComponentInitializationException {

        final String name = TEST_ATTRIBUTE_BASE_NAME + "2";

        final TemplateAttributeDefinition templateDef = new TemplateAttributeDefinition();
        templateDef.setId(name);
        templateDef.setVelocityEngine(getEngine());
        templateDef.setTemplateText(TEST_SIMPLE_TEMPLATE);

        final Set<ResolverPluginDependency> ds = new LazySet<>();
        ds.add(new ResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME));
        // ds.add(TestSources.makeResolverPluginDependency(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR,
        // TestSources.STATIC_ATTRIBUTE_NAME));
        templateDef.setDependencies(ds);
        templateDef.setSourceAttributes(Collections.singletonList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        templateDef.initialize();

        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(templateDef);
        attrDefinitions.add(TestSources.populatedStaticAttribute());
        final Set<DataConnector> dataDefinitions = new LazySet<>();
        dataDefinitions.add(TestSources.populatedStaticConnector());

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, dataDefinitions, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        resolver.resolveAttributes(context);

        IdPAttribute a = context.getResolvedIdPAttributes().get(name);
        final Collection results = a.getValues();
        Assert.assertEquals(results.size(), 1, "Templated value count");
        Assert.assertTrue(results.contains(SIMPLE_VALUE_RESULT), "Single value context is correct");

    }

    /**
     * Test resolution of an template script with data generated from the attributes.
     * 
     * @throws ResolutionException if it goes wrong.
     * @throws ComponentInitializationException if it goes wrong.
     */
    @Test public void templateWithValues() throws ResolutionException, ComponentInitializationException {

        final String name = TEST_ATTRIBUTE_BASE_NAME + "3";

        final TemplateAttributeDefinition templateDef = new TemplateAttributeDefinition();
        templateDef.setId(name);
        templateDef.setVelocityEngine(getEngine());
        templateDef.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_CONNECTOR);

        Set<ResolverPluginDependency> ds = new LazySet<>();
        ds.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        ds.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_SECOND_ATTRIBUTE_NAME));
        templateDef.setDependencies(ds);
        templateDef.setSourceAttributes(Arrays.asList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR,
                TestSources.DEPENDS_ON_SECOND_ATTRIBUTE_NAME));
        templateDef.initialize();

        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(templateDef);
        attrDefinitions.add(TestSources.populatedStaticAttribute());
        final Set<DataConnector> dataDefinitions = new LazySet<>();
        dataDefinitions.add(TestSources.populatedStaticConnector());

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, dataDefinitions, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        resolver.resolveAttributes(context);

        final IdPAttribute a = context.getResolvedIdPAttributes().get(name);
        final Collection results = a.getValues();
        Assert.assertEquals(results.size(), 2, "Templated value count");
        String s =
                "Att " + TestSources.COMMON_ATTRIBUTE_VALUE_STRING + "-"
                        + TestSources.SECOND_ATTRIBUTE_VALUE_STRINGS[0];
        Assert.assertTrue(results.contains(new StringAttributeValue(s)), "First Match");
        s = "Att " + TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING + "-" + TestSources.SECOND_ATTRIBUTE_VALUE_STRINGS[1];
        Assert.assertTrue(results.contains(new StringAttributeValue(s)), "Second Match");
    }

    @Test public void emptyValues() throws ResolutionException, ComponentInitializationException {
        final String name = TEST_ATTRIBUTE_BASE_NAME + "3";

        final TemplateAttributeDefinition templateDef = new TemplateAttributeDefinition();
        templateDef.setId(name);
        templateDef.setVelocityEngine(getEngine());
        templateDef.setTemplateText("Att ${at1}");

        Set<ResolverPluginDependency> ds = new LazySet<>();
        ds.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        templateDef.setDependencies(ds);
        templateDef.setSourceAttributes(Arrays.asList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        templateDef.initialize();

        final List<IdPAttributeValue<?>> values = new ArrayList<>();
        values.add(EmptyAttributeValue.ZERO_LENGTH);
        values.add(EmptyAttributeValue.NULL);
        IdPAttribute attr = new IdPAttribute(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);
        attr.setValues(values);
        StaticAttributeDefinition simple = new StaticAttributeDefinition();
        simple.setId(TestSources.STATIC_ATTRIBUTE_NAME);
        simple.setValue(attr);
        simple.initialize();

        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(templateDef);
        attrDefinitions.add(simple);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, Collections.EMPTY_SET, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        resolver.resolveAttributes(context);

        final IdPAttribute a = context.getResolvedIdPAttributes().get(name);
        final Collection results = a.getValues();
        Assert.assertEquals(results.size(), 2, "Templated value count");
        Assert.assertTrue(results.contains(new StringAttributeValue("Att ")), "First Match");
        Assert.assertTrue(results.contains(new StringAttributeValue("Att ${at1}")), "Second Match");
    }

    @Test public void failMisMatchCount() throws ResolutionException, ComponentInitializationException {
        final String name = TEST_ATTRIBUTE_BASE_NAME + "3";

        final TemplateAttributeDefinition templateDef = new TemplateAttributeDefinition();
        templateDef.setId(name);
        templateDef.setVelocityEngine(getEngine());
        templateDef.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_CONNECTOR);
        final String otherDefName = TestSources.STATIC_ATTRIBUTE_NAME + "2";
        final String otherAttrName = TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR + "2";

        Set<ResolverPluginDependency> ds = new LazySet<>();
        ds.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        ds.add(TestSources.makeResolverPluginDependency(otherDefName, otherAttrName));
        templateDef.setDependencies(ds);
        templateDef.setSourceAttributes(Arrays.asList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR, otherAttrName));
        templateDef.initialize();

        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(templateDef);
        attrDefinitions.add(TestSources.populatedStaticAttribute());
        attrDefinitions.add(TestSources.populatedStaticAttribute(otherDefName, otherAttrName, 1));

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, Collections.EMPTY_SET, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
            Assert.fail();
        } catch (ResolutionException ex) {
            // OK
        }
    }

    @Test public void wrongType() throws ResolutionException, ComponentInitializationException {
        final String name = TEST_ATTRIBUTE_BASE_NAME + "3";

        final TemplateAttributeDefinition templateDef = new TemplateAttributeDefinition();
        templateDef.setId(name);
        templateDef.setVelocityEngine(getEngine());
        templateDef.setTemplateText(TEST_ATTRIBUTES_TEMPLATE_ATTR);

        Set<ResolverPluginDependency> ds = new LazySet<>();
        ds.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        templateDef.setDependencies(ds);
        templateDef.setSourceAttributes(Collections.singletonList(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        
        templateDef.initialize();

        IdPAttribute attr = new IdPAttribute(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);
        attr.setValues(Collections.singletonList(new ByteAttributeValue(new byte[] {1, 2, 3})));
        StaticAttributeDefinition simple = new StaticAttributeDefinition();
        simple.setId(TestSources.STATIC_ATTRIBUTE_NAME);
        simple.setValue(attr);
        simple.initialize();
        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(templateDef);
        attrDefinitions.add(simple);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, Collections.EMPTY_SET, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
            Assert.fail();
        } catch (ResolutionException ex) {
            // OK
        }
    }

}
