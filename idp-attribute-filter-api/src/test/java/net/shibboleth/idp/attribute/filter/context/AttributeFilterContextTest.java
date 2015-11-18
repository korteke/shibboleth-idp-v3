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

package net.shibboleth.idp.attribute.filter.context;

import java.util.Arrays;
import java.util.List;

import net.shibboleth.idp.attribute.IdPAttribute;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeFilterContext}. */
public class AttributeFilterContextTest {

    /** Test that post-construction state is what is expected. */
    @Test public void testPostConstructionState() {
        AttributeFilterContext context = new AttributeFilterContext();
        AttributeFilterWorkContext child = context.getSubcontext(AttributeFilterWorkContext.class, true);
        Assert.assertNotNull(context.getFilteredIdPAttributes());
        Assert.assertTrue(context.getFilteredIdPAttributes().isEmpty());
        Assert.assertNull(context.getParent());
        Assert.assertNotNull(context.getPrefilteredIdPAttributes());
        Assert.assertTrue(context.getPrefilteredIdPAttributes().isEmpty());
        Assert.assertNotNull(child.getPermittedIdPAttributeValues());
        Assert.assertTrue(child.getPermittedIdPAttributeValues().isEmpty());
        Assert.assertNotNull(child.getDeniedAttributeValues());
        Assert.assertTrue(child.getDeniedAttributeValues().isEmpty());
    }

    /** Test methods related to prefiltered attributes. */
    @Test public void testPrefilteredAttributes() {
        AttributeFilterContext context = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        context.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);
        Assert.assertEquals(context.getPrefilteredIdPAttributes().size(), 1);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute1"), attribute1);

        IdPAttribute attribute2 = new IdPAttribute("attribute2");
        IdPAttribute attribute3 = new IdPAttribute("attribute3");
        List<IdPAttribute> attributes = Arrays.asList(attribute2, attribute3);
        context.setPrefilteredIdPAttributes(attributes);
        Assert.assertEquals(context.getPrefilteredIdPAttributes().size(), 2);
        Assert.assertFalse(context.getPrefilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute2"), attribute2);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute3"), attribute3);

        context.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);
        Assert.assertEquals(context.getPrefilteredIdPAttributes().size(), 3);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute2"), attribute2);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute3"), attribute3);

        context.getPrefilteredIdPAttributes().remove("attribute2");
        Assert.assertEquals(context.getPrefilteredIdPAttributes().size(), 2);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertFalse(context.getPrefilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute3"), attribute3);

        try {
            context.getPrefilteredIdPAttributes().put(null, new IdPAttribute("foo"));
            Assert.fail("null attribute id not allowed");
        } catch (NullPointerException e) {
        }

        try {
            context.getPrefilteredIdPAttributes().put("foo", null);
            Assert.fail("null attribute not allowed");
        } catch (NullPointerException e) {
        }

        context.getPrefilteredIdPAttributes().remove(null);
        Assert.assertEquals(context.getPrefilteredIdPAttributes().size(), 2);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertTrue(context.getPrefilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getPrefilteredIdPAttributes().get("attribute3"), attribute3);

        context.setPrefilteredIdPAttributes(null);
        Assert.assertNotNull(context.getPrefilteredIdPAttributes());
        Assert.assertTrue(context.getPrefilteredIdPAttributes().isEmpty());
    }

    /** Test methods related to filtered attributes. */
    @Test public void testFilteredAttributes() {
        AttributeFilterContext context = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        context.getFilteredIdPAttributes().put(attribute1.getId(), attribute1);
        Assert.assertEquals(context.getFilteredIdPAttributes().size(), 1);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute1"), attribute1);

        IdPAttribute attribute2 = new IdPAttribute("attribute2");
        IdPAttribute attribute3 = new IdPAttribute("attribute3");
        List<IdPAttribute> attributes = Arrays.asList(attribute2, attribute3);
        context.setFilteredIdPAttributes(attributes);
        Assert.assertEquals(context.getFilteredIdPAttributes().size(), 2);
        Assert.assertFalse(context.getFilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute2"), attribute2);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute3"), attribute3);

        context.getFilteredIdPAttributes().put(attribute1.getId(), attribute1);
        Assert.assertEquals(context.getFilteredIdPAttributes().size(), 3);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute2"), attribute2);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute3"), attribute3);

        context.getFilteredIdPAttributes().remove("attribute2");
        Assert.assertEquals(context.getFilteredIdPAttributes().size(), 2);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertFalse(context.getFilteredIdPAttributes().containsKey("attribute2"));
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute3"), attribute3);

        try {
            context.getFilteredIdPAttributes().put(null, new IdPAttribute("foo"));
            Assert.fail("null attribute id not allowed");
        } catch (NullPointerException e) {
        }

        try {
            context.getFilteredIdPAttributes().put("foo", null);
            Assert.fail("null attribute not allowed");
        } catch (NullPointerException e) {
        }

        context.getFilteredIdPAttributes().remove(null);
        Assert.assertEquals(context.getFilteredIdPAttributes().size(), 2);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute1"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute1"), attribute1);
        Assert.assertTrue(context.getFilteredIdPAttributes().containsKey("attribute3"));
        Assert.assertEquals(context.getFilteredIdPAttributes().get("attribute3"), attribute3);

        context.setFilteredIdPAttributes(null);
        Assert.assertNotNull(context.getFilteredIdPAttributes());
        Assert.assertTrue(context.getFilteredIdPAttributes().isEmpty());
    }

    @Test public void fields() {
        AttributeFilterContext context = new AttributeFilterContext();
        Assert.assertNull(context.getAttributeIssuerID());
        Assert.assertNull(context.getAttributeRecipientID());
        Assert.assertNull(context.getPrincipal());
        Assert.assertNull(context.getPrincipalAuthenticationMethod());

        context.setAttributeIssuerID("aiid");
        Assert.assertEquals(context.getAttributeIssuerID(), "aiid");

        context.setAttributeRecipientID("arid");
        Assert.assertEquals(context.getAttributeRecipientID(), "arid");

        context.setPrincipal("princ");
        Assert.assertEquals(context.getPrincipal(), "princ");

        context.setPrincipalAuthenticationMethod("princam");
        Assert.assertEquals(context.getPrincipalAuthenticationMethod(), "princam");

    }

    @Test public void strategies() {
        AttributeFilterContext context = new AttributeFilterContext();
        Assert.assertNull(context.getRequesterMetadataContext());

        final SAMLMetadataContext mas = context.getSubcontext(SAMLMetadataContext.class, true);
        context.setRequesterMetadataContextLookupStrategy(new ChildContextLookup<AttributeFilterContext, SAMLMetadataContext>(
                SAMLMetadataContext.class));

        Assert.assertSame(context.getRequesterMetadataContext(), mas);
    }
}