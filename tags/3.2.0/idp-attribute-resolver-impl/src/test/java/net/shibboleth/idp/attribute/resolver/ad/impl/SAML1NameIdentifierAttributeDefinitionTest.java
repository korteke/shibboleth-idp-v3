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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.EmptyAttributeValue.EmptyType;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.ResolverTestSupport;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.saml.attribute.resolver.impl.SAML1NameIdentifierAttributeDefinition;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.collection.LazySet;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SAML1NameIdentifierAttributeDefinition}.
 */
public class SAML1NameIdentifierAttributeDefinitionTest extends OpenSAMLInitBaseTestCase {

    /** The name. */
    private static final String TEST_ATTRIBUTE_NAME = "simple";

    private static final String SECOND_ATTRIBUTE_NAME = "second";

    private static final String IDP_ENTITY_ID = "https://idp.example.org/idp";

    private static final String ALTERNATIVE_FORMAT = "ALTERNATE_FORMAT";

    private static final String ALTERNATE_QUALIFIER = "ALTERNATE_QUALIFIER";

    @Test public void empty() throws ResolutionException, ComponentInitializationException {
        final SAML1NameIdentifierAttributeDefinition defn = new SAML1NameIdentifierAttributeDefinition();
        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        defn.initialize();

        final IdPAttribute result =
                defn.resolve(TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, null));

        Assert.assertTrue(result.getValues().isEmpty());
    }

    @Test public void simple() throws ResolutionException, ComponentInitializationException {
        final SAML1NameIdentifierAttributeDefinition defn = new SAML1NameIdentifierAttributeDefinition();
        defn.setId(TEST_ATTRIBUTE_NAME);
        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        defn.setDependencies(dependencySet);
        defn.initialize();

        // And resolve
        final Set<AttributeDefinition> am = new LazySet<>();
        am.add(defn);
        am.add(TestSources.populatedStaticAttribute());

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, null, null);
        resolver.initialize();

        AttributeResolutionContext context = TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, null);
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }
        final Collection<IdPAttributeValue<?>> values = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(values.size(), 2);
        Collection<String> nameValues = new HashSet<>(2);
        for (IdPAttributeValue val : values) {
            NameIdentifier id = (NameIdentifier) val.getValue();
            Assert.assertEquals(id.getFormat(),  "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
            Assert.assertEquals(id.getNameQualifier(), IDP_ENTITY_ID);
            nameValues.add(id.getValue());
        }
        Assert.assertTrue(nameValues.contains(TestSources.COMMON_ATTRIBUTE_VALUE_STRING));
        Assert.assertTrue(nameValues.contains(TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING));
    }
    
    @Test public void nullValueType() throws ComponentInitializationException, ResolutionException {
        final List<IdPAttributeValue<?>> values = new ArrayList<>(3);
        values.add(new StringAttributeValue(TestSources.COMMON_ATTRIBUTE_VALUE_STRING));
        values.add(new EmptyAttributeValue(EmptyType.NULL_VALUE));
        values.add(new StringAttributeValue(TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING));
        final IdPAttribute attr = new IdPAttribute(ResolverTestSupport.EPA_ATTRIB_ID);
        attr.setValues(values);
        
        final ResolverPluginDependency depend = new ResolverPluginDependency("connector1");
        depend.setDependencyAttributeId(ResolverTestSupport.EPA_ATTRIB_ID);

        
        final SAML1NameIdentifierAttributeDefinition defn = new SAML1NameIdentifierAttributeDefinition();
        defn.setId(TEST_ATTRIBUTE_NAME);
        // Set the dependency on the data connector
        defn.setDependencies(Collections.singleton(depend));
        defn.initialize();

        // And resolve
        final Set<AttributeDefinition> am = new LazySet<>();
        am.add(defn);

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, Collections.singleton((DataConnector)ResolverTestSupport.buildDataConnector("connector1", attr)), null);
        resolver.initialize();

        AttributeResolutionContext context = TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, null);
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }
        final Collection<IdPAttributeValue<?>> outValues = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(outValues.size(), 2);
        Collection<String> nameValues = new HashSet<>(2);
        for (IdPAttributeValue val : outValues) {
            NameIdentifier id = (NameIdentifier) val.getValue();
            Assert.assertEquals(id.getFormat(),  "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
            Assert.assertEquals(id.getNameQualifier(), IDP_ENTITY_ID);
            nameValues.add(id.getValue());
        }
        Assert.assertTrue(nameValues.contains(TestSources.COMMON_ATTRIBUTE_VALUE_STRING));
        Assert.assertTrue(nameValues.contains(TestSources.ATTRIBUTE_ATTRIBUTE_VALUE_STRING));
    }

    @Test public void nullIssuerID() throws ComponentInitializationException {
        final SAML1NameIdentifierAttributeDefinition defn = new SAML1NameIdentifierAttributeDefinition();
        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));

        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        defn.setDependencies(dependencySet);
        defn.initialize();
        final Set<AttributeDefinition> am = new LazySet<>();
        am.add(defn);
        am.add(TestSources.populatedStaticAttribute());
        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, null, null);
        resolver.initialize();

        AttributeResolutionContext context = new AttributeResolutionContext();
        try {
            resolver.resolveAttributes(context);
            Assert.fail("resolution Should have failed");
        } catch (ResolutionException e) {
            // OK
        }
    }

    @Test public void badValue() throws ResolutionException, ComponentInitializationException {
        final AttributeDefinition defn = TestSources.nonStringAttributeDefiniton(TEST_ATTRIBUTE_NAME);

        final SAML1NameIdentifierAttributeDefinition defn2 = new SAML1NameIdentifierAttributeDefinition();
        defn2.setId(SECOND_ATTRIBUTE_NAME);
        defn2.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));

        // Set the dependency on the data connector
        Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(new ResolverPluginDependency(TEST_ATTRIBUTE_NAME));
        defn2.setDependencies(dependencySet);
        defn2.initialize();

        // And resolve
        Set<AttributeDefinition> am = new LazySet<>();
        am.add(defn);
        am.add(TestSources.populatedStaticAttribute());
        am.add(defn2);

        AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, null, null);
        resolver.initialize();

        //
        // Attribute recipient needed because the nonStringAttributeDefinition needs it
        //
        AttributeResolutionContext context =
                TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID);
        resolver.resolveAttributes(context);
        Assert.assertNull(context.getResolvedIdPAttributes().get(SECOND_ATTRIBUTE_NAME));
    }

    @Test public void singleValueWithOptions() throws ResolutionException, ComponentInitializationException {
        final SAML1NameIdentifierAttributeDefinition defn = new SAML1NameIdentifierAttributeDefinition();
        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));

        // Set the dependency on the data connector
        final Set<ResolverPluginDependency> dependencySet = new LazySet<>();
        dependencySet.add(TestSources.makeResolverPluginDependency(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR));
        defn.setDependencies(dependencySet);
        defn.setNameIdFormat(ALTERNATIVE_FORMAT);
        defn.setNameIdQualifier(ALTERNATE_QUALIFIER);
        defn.initialize();

        // And resolve
        final Set<AttributeDefinition> am = new LazySet<>();
        am.add(defn);
        am.add(TestSources.populatedStaticAttribute(TestSources.STATIC_ATTRIBUTE_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR, 1));

        final AttributeResolverImpl resolver = new AttributeResolverImpl("foo", am, null, null);
        resolver.initialize();

        AttributeResolutionContext context = TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, null);
        try {
            resolver.resolveAttributes(context);
        } catch (ResolutionException e) {
            Assert.fail("resolution failed", e);
        }
        final Collection<IdPAttributeValue<?>> values = context.getResolvedIdPAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(values.size(), 1);
        NameIdentifier id = (NameIdentifier) values.iterator().next().getValue();
        Assert.assertEquals(id.getFormat(), ALTERNATIVE_FORMAT);
        Assert.assertEquals(defn.getNameIdFormat(), id.getFormat());
        Assert.assertEquals(id.getNameQualifier(), ALTERNATE_QUALIFIER);
        Assert.assertEquals(defn.getNameIdQualifier(), id.getNameQualifier());
        Assert.assertEquals(id.getValue(), TestSources.COMMON_ATTRIBUTE_VALUE_STRING);

    }
}