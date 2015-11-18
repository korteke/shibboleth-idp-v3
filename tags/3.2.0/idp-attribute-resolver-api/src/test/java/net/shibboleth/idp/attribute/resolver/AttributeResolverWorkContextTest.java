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
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeResolverWorkContext}. */
public class AttributeResolverWorkContextTest {

    /** Test instantiation and post-instantiation state. */
    @Test public void instantiation() {

        AttributeResolverWorkContext context = new AttributeResolverWorkContext();
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions());
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().isEmpty());
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertTrue(context.getResolvedDataConnectors().isEmpty());
    }

    /** Test adding and retrieving attribute definitions. */
    @Test public void resolvedAttributeDefinitions() throws Exception {
        AttributeResolutionContext parent = new AttributeResolutionContext();
        AttributeResolverWorkContext context = new AttributeResolverWorkContext();
        parent.addSubcontext(context);

        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions());
        Assert.assertNull(context.getResolvedIdPAttributeDefinitions().get("foo"));

        IdPAttribute attribute = new IdPAttribute("foo");
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", attribute);
        definition.initialize();

        context.recordAttributeDefinitionResolution(definition, attribute);
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions());
        Assert.assertEquals(context.getResolvedIdPAttributeDefinitions().size(), 1);
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions().get("foo"));
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("foo") instanceof ResolvedAttributeDefinition);
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("foo").getResolvedDefinition() == definition);
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("foo").resolve(parent) == attribute);

        try {
            context.recordAttributeDefinitionResolution(definition, attribute);
            Assert.fail("able to record a second resolution for a single attribute definition");
        } catch (ResolutionException e) {
            // expected this
        }

        definition = new MockAttributeDefinition("bar", (IdPAttribute) null);
        definition.initialize();

        context.recordAttributeDefinitionResolution(definition, null);
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions());
        Assert.assertEquals(context.getResolvedIdPAttributeDefinitions().size(), 2);
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions().get("foo"));
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("foo") instanceof ResolvedAttributeDefinition);
        Assert.assertNotNull(context.getResolvedIdPAttributeDefinitions().get("bar"));
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("bar") instanceof ResolvedAttributeDefinition);
        Assert.assertTrue(context.getResolvedIdPAttributeDefinitions().get("bar").getResolvedDefinition() == definition);
        Assert.assertNull(context.getResolvedIdPAttributeDefinitions().get("bar").resolve(parent));
    }

    /** Test adding and retrieving data connectors. */
    @Test public void resolvedDataConnectors() throws Exception {
        AttributeResolutionContext parent = new AttributeResolutionContext();
        AttributeResolverWorkContext context = new AttributeResolverWorkContext();
        parent.addSubcontext(context);

        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertNull(context.getResolvedDataConnectors().get("foo"));

        IdPAttribute attribute = new IdPAttribute("foo");

        Map<String, IdPAttribute> attributes = new HashMap<>();
        attributes.put(attribute.getId(), attribute);

        MockDataConnector connector = new MockDataConnector("foo", attributes);
        connector.initialize();

        context.recordDataConnectorResolution(connector, attributes);
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertEquals(context.getResolvedDataConnectors().size(), 1);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("foo"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo") instanceof ResolvedDataConnector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo").getResolvedConnector() == connector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo").resolve(parent) == attributes);

        try {
            context.recordDataConnectorResolution(connector, attributes);
            Assert.fail("able to record a second resolution for a single data connector");
        } catch (ResolutionException e) {
            // expected this
        }

        connector = new MockDataConnector("bar", (Map) null);
        connector.initialize();

        context.recordDataConnectorResolution(connector, null);
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertEquals(context.getResolvedDataConnectors().size(), 2);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("foo"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo") instanceof ResolvedDataConnector);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("bar"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("bar") instanceof ResolvedDataConnector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("bar").getResolvedConnector() == connector);
        Assert.assertNull(context.getResolvedDataConnectors().get("bar").resolve(parent));
        
        try {
            MockStaticDataConnector other = new MockStaticDataConnector();
            other.setId("bar");
            other.setValues(Collections.EMPTY_LIST);
            other.initialize();
            context.recordDataConnectorResolution(other, null);
            Assert.fail("Cannot cross the same bridge twice or add the same resolvedId twice");
        } catch (ResolutionException ex) {
            //OK
        }
    }
}