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

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.ResolverTestSupport;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.collection.LazySet;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for prescoped attribute definitions.
 */
public class PrescopedAtributeTest {
    /** The name. resolve to */
    private static final String TEST_ATTRIBUTE_NAME = "prescoped";

    private static final String DELIMITER = "@";

    /**
     * Test regexp. The test Data Connector provides an input attribute "at1" with values at1-Data and at1-Connector. We
     * can feed these into the prescoped, looking for '-'
     * 
     * @throws ResolutionException on resolution issues.
     * @throws ComponentInitializationException if any of our initializtions failed (which it shouldn't)
     */
    @Test public void preScoped() throws ResolutionException, ComponentInitializationException {

        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        ResolverPluginDependency depend = new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME);
        depend.setDependencyAttributeId(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR);
        dependencySet.add(depend);
        final PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        attrDef.setScopeDelimiter("-");
        attrDef.setDependencies(dependencySet);
        attrDef.initialize();

        // And resolve
        final Set<DataConnector> connectorSet = new LazySet<>();
        connectorSet.add(TestSources.populatedStaticConnector());

        final Set<AttributeDefinition> attributeSet = new LazySet<>();
        attributeSet.add(attrDef);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attributeSet, connectorSet, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        resolver.resolveAttributes(context);
        final Collection f = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(f.size(), 2);
        Assert.assertTrue(f.contains(new ScopedStringAttributeValue("at1", "Data")));
        Assert.assertTrue(f.contains(new ScopedStringAttributeValue("at1", "Connector")));
    }

    /**
     * Test the prescoped attribute resolve when there are no matches.
     * 
     * @throws ResolutionException if resolution fails.
     * @throws ComponentInitializationException if any of our initializations failed (which it shouldn't)
     */
    @Test public void preScopedNoValues() throws ResolutionException, ComponentInitializationException {

        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        ResolverPluginDependency depend = new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME);
        depend.setDependencyAttributeId(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR);
        dependencySet.add(depend);
        final PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        attrDef.setScopeDelimiter(DELIMITER);
        attrDef.setDependencies(dependencySet);
        attrDef.initialize();

        // And resolve
        final Set<DataConnector> connectorSet = new LazySet<>();
        connectorSet.add(TestSources.populatedStaticConnector());

        final Set<AttributeDefinition> attributeSet = new LazySet<>();
        attributeSet.add(attrDef);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attributeSet, connectorSet, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
            Assert.fail();
        } catch (ResolutionException e) {
            // OK
        }
    }

    @Test public void invalidValueType() throws ComponentInitializationException {
        IdPAttribute attr = new IdPAttribute(ResolverTestSupport.EPA_ATTRIB_ID);
        attr.setValues(Collections.singletonList(new ByteAttributeValue(new byte[] {1, 2, 3})));

        AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1", attr));

        final PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        attrDef.setScopeDelimiter("@");
        ResolverPluginDependency depend = new ResolverPluginDependency("connector1");
        depend.setDependencyAttributeId(ResolverTestSupport.EPA_ATTRIB_ID);
        attrDef.setDependencies(Collections.singleton(depend));
        attrDef.initialize();

        try {
            attrDef.resolve(resolutionContext);
            Assert.fail("Invalid type");
        } catch (ResolutionException e) {
            //
        }
    }
    
    @Test public void nullValueType() throws ComponentInitializationException, ResolutionException {
        final List<IdPAttributeValue<?>> values = new ArrayList<>(4);
        values.add(new StringAttributeValue("one@two"));
        values.add(new EmptyAttributeValue(EmptyType.NULL_VALUE));
        values.add(new StringAttributeValue("three@four"));
        values.add(new EmptyAttributeValue(EmptyType.ZERO_LENGTH_VALUE));
        final IdPAttribute attr = new IdPAttribute(ResolverTestSupport.EPA_ATTRIB_ID);

        attr.setValues(values);

        AttributeResolutionContext resolutionContext =
                ResolverTestSupport.buildResolutionContext(ResolverTestSupport.buildDataConnector("connector1", attr));
        ResolverPluginDependency depend = new ResolverPluginDependency("connector1");
        depend.setDependencyAttributeId(ResolverTestSupport.EPA_ATTRIB_ID);

        final PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        attrDef.setScopeDelimiter("@");
        attrDef.setDependencies(Collections.singleton(depend));
        attrDef.initialize();
        IdPAttribute result = attrDef.resolve(resolutionContext);
        
        final Collection f = result.getValues();

        Assert.assertEquals(f.size(), 2);
        Assert.assertTrue(f.contains(new ScopedStringAttributeValue("one", "two")));
        Assert.assertTrue(f.contains(new ScopedStringAttributeValue("three", "four")));

    }


    @Test public void emptyValueType() throws ResolutionException, ComponentInitializationException {
        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        ResolverPluginDependency depend = new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME);
        depend.setDependencyAttributeId(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR);
        dependencySet.add(depend);
        final PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        // delimiter that will produce an empty value
        attrDef.setScopeDelimiter("at1-");
        attrDef.setDependencies(dependencySet);
        attrDef.initialize();

        // And resolve
        final Set<DataConnector> connectorSet = new LazySet<>();
        connectorSet.add(TestSources.populatedStaticConnector());

        final Set<AttributeDefinition> attributeSet = new LazySet<>();
        attributeSet.add(attrDef);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", attributeSet, connectorSet, null);
        resolver.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        resolver.resolveAttributes(context);

        final Collection f = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        // 2 empty attribute values are produced, but they get de-duped into a single value
        Assert.assertEquals(f.size(), 1);
        Assert.assertEquals(f.iterator().next(), EmptyAttributeValue.ZERO_LENGTH);
    }

    @Test public void initDestroyParms() throws ResolutionException, ComponentInitializationException {

        PrescopedAttributeDefinition attrDef = new PrescopedAttributeDefinition();
        ResolverPluginDependency depend = new ResolverPluginDependency("connector1");
        depend.setDependencyAttributeId(ResolverTestSupport.EPA_ATTRIB_ID);
        Set<ResolverPluginDependency> pluginDependencies = Collections.singleton(depend);
        attrDef.setDependencies(pluginDependencies);
        attrDef.setId(TEST_ATTRIBUTE_NAME);

        try {
            attrDef.setScopeDelimiter(null);
            Assert.fail("set null delimiter");
        } catch (ConstraintViolationException e) {
            // OK
        }

        attrDef = new PrescopedAttributeDefinition();
        attrDef.setId(TEST_ATTRIBUTE_NAME);
        Assert.assertNotNull(attrDef.getScopeDelimiter());
        attrDef.setScopeDelimiter(DELIMITER);
        try {
            attrDef.initialize();
            Assert.fail("no Dependency - should fail");
        } catch (ComponentInitializationException e) {
            // OK
        }
        attrDef.setDependencies(pluginDependencies);

        try {
            attrDef.resolve(new AttributeResolutionContext());
            Assert.fail("resolve not initialized");
        } catch (UninitializedComponentException e) {
            // OK
        }
        attrDef.initialize();

        Assert.assertEquals(attrDef.getScopeDelimiter(), DELIMITER);

        try {
            attrDef.resolve(null);
            Assert.fail("Null context not allowed");
        } catch (ConstraintViolationException e) {
            // OK
        }

        attrDef.destroy();
        try {
            attrDef.initialize();
            Assert.fail("Init after destroy");
        } catch (DestroyedComponentException e) {
            // OK
        }
        try {
            attrDef.resolve(new AttributeResolutionContext());
            Assert.fail("Resolve after destroy");
        } catch (DestroyedComponentException e) {
            // OK
        }
        try {
            attrDef.setScopeDelimiter(DELIMITER);
            Assert.fail("Set Delimiter after destroy");
        } catch (DestroyedComponentException e) {
            // OK
        }
    }
}