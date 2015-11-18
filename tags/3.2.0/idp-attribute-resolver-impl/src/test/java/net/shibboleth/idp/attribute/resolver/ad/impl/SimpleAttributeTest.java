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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.ResolverTestSupport;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.collection.LazySet;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** test for {@link net.shibboleth.idp.attribute.resolver.impl.SimpleAttribute}. */
public class SimpleAttributeTest {

    /** The name. */
    private static final String TEST_ATTRIBUTE_NAME = "simple";

    /**
     * Test resolution of an empty definition to nothing.
     * 
     * @throws ResolutionException if resolution failed.
     * @throws ComponentInitializationException if initialization fails (which it shouldn't).
     */
    @Test public void empty() throws ResolutionException, ComponentInitializationException {
        final SimpleAttributeDefinition simple = new SimpleAttributeDefinition();
        simple.setId(TEST_ATTRIBUTE_NAME);
        try {
            simple.initialize();
            Assert.fail("no dependencies");
        } catch (ComponentInitializationException e) {
            //OK
        }
        simple.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        simple.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        final IdPAttribute result = simple.resolve(context);

        Assert.assertTrue(result.getValues().isEmpty());
    }

    /**
     * Test when dependent on a data connector.
     * 
     * @throws ComponentInitializationException if initialization fails (which it shouldn't).
     */
    @Test public void dataConnector() throws ComponentInitializationException {

        // Set the dependency on the data connector
        final SimpleAttributeDefinition simple = new SimpleAttributeDefinition();
        simple.setId(TEST_ATTRIBUTE_NAME);

        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR));
        simple.setDependencies(dependencySet);
        simple.initialize();

        // And resolve
        final Set<DataConnector> connectorSet = new LazySet<>();
        connectorSet.add(TestSources.populatedStaticConnector());

        final Set<AttributeDefinition> attributeSet = new LazySet<>();
        attributeSet.add(simple);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attributeSet, connectorSet, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        final Collection values = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(TestSources.COMMON_ATTRIBUTE_VALUE_RESULT), "looking for " + TestSources.COMMON_ATTRIBUTE_VALUE_STRING);
        Assert.assertTrue(values.contains(TestSources.CONNECTOR_ATTRIBUTE_VALUE_RESULT),
                "looking for " + TestSources.CONNECTOR_ATTRIBUTE_VALUE_STRING);
    }

    /**
     * Test when dependent on another attribute.
     * 
     * @throws ComponentInitializationException if initialization fails (which it shouldn't).
     */
    @Test public void attribute() throws ComponentInitializationException {

        final SimpleAttributeDefinition simple = new SimpleAttributeDefinition();
        simple.setId(TEST_ATTRIBUTE_NAME);

        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        simple.setDependencies(dependencySet);
        simple.initialize();

        // And resolve
        final Set<AttributeDefinition> am = new LazySet<>();
        am.add(simple);
        am.add(TestSources.populatedStaticAttribute());

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, null, null);
        resolver.initialize();

        AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }
        final Collection<IdPAttributeValue<?>> values = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(TestSources.COMMON_ATTRIBUTE_VALUE_RESULT),
                "looking for value " + TestSources.COMMON_ATTRIBUTE_VALUE_STRING);
        Assert.assertTrue(values.contains(TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_RESULT),
                "looking for value " + TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING);
    }
    
    /**
     * Test resolution of an empty definition to nothing.
     * 
     * @throws ResolutionException if resolution failed.
     * @throws ComponentInitializationException if initialization fails (which it shouldn't).
     */
    @Test public void nullValue() throws ResolutionException, ComponentInitializationException {
        final List<IdPAttributeValue<?>> values = new ArrayList<>(3);
        values.add(TestSources.COMMON_ATTRIBUTE_VALUE_RESULT);
        values.add(new EmptyAttributeValue(EmptyType.NULL_VALUE));
        final IdPAttribute attr = new IdPAttribute(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);

        attr.setValues(values);

       final AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1", attr));
        final ResolverPluginDependency depend = new ResolverPluginDependency("connector1");
        depend.setDependencyAttributeId(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);

        final SimpleAttributeDefinition simple = new SimpleAttributeDefinition();
        simple.setId(TEST_ATTRIBUTE_NAME);
        simple.setDependencies(Collections.singleton(depend));
        simple.initialize();

        final IdPAttribute result = simple.resolve(resolutionContext);

       final List<IdPAttributeValue<?>> outValues = result.getValues();
        Assert.assertEquals(outValues.size(), 2);
        Assert.assertTrue(outValues.contains(TestSources.COMMON_ATTRIBUTE_VALUE_RESULT));
        Assert.assertTrue(outValues.contains(new EmptyAttributeValue(EmptyType.NULL_VALUE)));

    }



    /**
     * Test when dependent on a data connector and another attribute.
     * 
     * @throws ComponentInitializationException if initialization fails (which it shouldn't).
     */
    @Test public void both() throws ComponentInitializationException {

        final SimpleAttributeDefinition simple = new SimpleAttributeDefinition();
        simple.setId(TEST_ATTRIBUTE_NAME);

        Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR));
        simple.setDependencies(dependencySet);
        simple.initialize();

        // And resolve
        final Set<AttributeDefinition> attrDefinitions = new LazySet<>();
        attrDefinitions.add(simple);
        attrDefinitions.add(TestSources.populatedStaticAttribute());

        final Set<DataConnector> dataDefinitions = new LazySet<>();
        dataDefinitions.add(TestSources.populatedStaticConnector());

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attrDefinitions, dataDefinitions, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        final Collection values = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();
        Assert.assertTrue(values.contains(TestSources.COMMON_ATTRIBUTE_VALUE_RESULT),
                "looking for value " + TestSources.COMMON_ATTRIBUTE_VALUE_STRING);
        Assert.assertTrue(values.contains(TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_RESULT),
                "looking for value " + TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING);
        Assert.assertTrue(values.contains(TestSources.CONNECTOR_ATTRIBUTE_VALUE_RESULT),
                "looking for value " + TestSources.CONNECTOR_ATTRIBUTE_VALUE_STRING);
        Assert.assertEquals(values.size(), 3);
    }
    
}