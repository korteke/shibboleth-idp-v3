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

package net.shibboleth.idp.attribute.filter;

import java.util.Arrays;
import java.util.Collection;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AttributeRule}
 */
public class AttributeValueFilterPolicyTest {

    @Test public void testInitDestroy() throws ComponentInitializationException {
        AttributeRule policy = new AttributeRule();
        MockMatcher matcher = new MockMatcher();        
        policy.setMatcher(matcher);
        policy.setIsDenyRule(false);


        Assert.assertFalse(policy.isInitialized(), "Created - not initialized");
        Assert.assertFalse(matcher.isInitialized(), "Create - not initialized");
        Assert.assertFalse(policy.isDestroyed(), "Created - not destroyed");
        Assert.assertFalse(matcher.isDestroyed(), "Created - not destroyed");

        policy.setId("id");
        policy.setAttributeId("foo");
        policy.initialize();
        matcher.initialize();

        Assert.assertTrue(policy.isInitialized(), "Initialized");
        Assert.assertTrue(matcher.isInitialized(), "Initialized");
        Assert.assertFalse(policy.isDestroyed(), "Initialized - not destroyed");
        Assert.assertFalse(matcher.isDestroyed(), "Initialized - not destroyed");

        policy.destroy();
        matcher.destroy();
        Assert.assertTrue(policy.isDestroyed(), "Destroyed");
        Assert.assertTrue(matcher.isDestroyed(), "Destroyed");

        boolean thrown = false;
        try {
            policy.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "initialize after destroy");

    }

    @Test public void testAttributeId() throws ComponentInitializationException {
        AttributeRule policy = new AttributeRule();
        policy.setId("id");
        boolean thrown = false;
        try {
            policy.initialize();
        } catch (ComponentInitializationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "AttributeId can never initialized be null");

        thrown = false;
        try {
            policy.setAttributeId(null);
            policy.initialize();
        } catch (ComponentInitializationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "null Attribute Id");

        thrown = false;
        try {
            policy.setAttributeId("");
            policy.initialize();
        } catch (ComponentInitializationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "empty Attribute Id");

        policy = new AttributeRule();
        policy.setId("id");                
        policy.setAttributeId(" ID ");
        Assert.assertEquals(policy.getAttributeId(), "ID", "Get Attribute ID");
        policy.setMatcher(Matcher.MATCHES_ALL);
        policy.setIsDenyRule(false);
        policy.initialize();
        Assert.assertEquals(policy.getAttributeId(), "ID", "Get Attribute ID");

        thrown = false;
        try {
            policy.setAttributeId("foo");
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "SetAttributeId after init");
        Assert.assertEquals(policy.getAttributeId(), "ID", "Get Attribute ID");

        policy.destroy();
        thrown = false;
        try {
            policy.getAttributeId();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "GetAttributeId after destroy");

        policy = new AttributeRule();
        policy.destroy();
        thrown = false;
        try {
            policy.setAttributeId("foo");
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "SetAttributeId after destroy");
    }


    @Test public void testValueMatcher() throws ComponentInitializationException {
        AttributeRule policy = new AttributeRule();
        policy.setId("id");
        policy.setAttributeId("foo");

        policy.setMatcher(Matcher.MATCHES_ALL);
        policy.setIsDenyRule(false);
        Assert.assertEquals(policy.getMatcher(), Matcher.MATCHES_ALL,
                "AttributeValueMatcher - changed");
        Assert.assertFalse(policy.getIsDenyRule());
        policy.initialize();
        Assert.assertEquals(policy.getMatcher(), Matcher.MATCHES_ALL,
                "AttributeValueMatcher - initialized");

        boolean thrown = false;
        try {
            policy.setMatcher(Matcher.MATCHES_NONE);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "AttributeValueMatcher - set after initialized");
        Assert.assertEquals(policy.getMatcher(), Matcher.MATCHES_ALL,
                "AttributeValueMatcher - set after initialized");

        policy = new AttributeRule();
        policy.setId("id");
        policy.setAttributeId(" foo");
        policy.setMatcher(Matcher.MATCHES_ALL);
        policy.setIsDenyRule(false);
        policy.initialize();
        policy.destroy();
        thrown = false;
        try {
            policy.setMatcher(Matcher.MATCHES_NONE);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "setMatchingPermittedValues after destroy");

        thrown = false;
        try {
            policy.getMatcher();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "isMatchingPermittedValues after destroy");

    }

    @Test public void testApply() throws ComponentInitializationException { 
        MockMatcher matcher = new MockMatcher();

        final StringAttributeValue aStringAttributeValue = new StringAttributeValue("a");
        final StringAttributeValue bStringAttributeValue = new StringAttributeValue("b");
        final StringAttributeValue cStringAttributeValue = new StringAttributeValue("c");
        final StringAttributeValue dStringAttributeValue = new StringAttributeValue("d");
        final String ATTR_NAME = "one";
        final IdPAttribute attribute1 = new IdPAttribute(ATTR_NAME);
        attribute1.setValues(Arrays.asList(aStringAttributeValue, bStringAttributeValue, cStringAttributeValue, dStringAttributeValue));

        matcher.setMatchingAttribute(ATTR_NAME);
        matcher.setMatchingValues(Arrays.asList(aStringAttributeValue, cStringAttributeValue));

        AttributeRule policy = new AttributeRule();
        policy.setId("id");        
        policy.setMatcher(matcher);
        policy.setIsDenyRule(false);
        policy.setAttributeId(ATTR_NAME);
        policy.initialize();

        boolean thrown = false;
        try {
            policy.apply(null, new AttributeFilterContext());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Null attribute");

        thrown = false;
        try {
            policy.apply(new IdPAttribute(ATTR_NAME), null);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Null context");

        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrefilteredIdPAttributes(Arrays.asList(attribute1));
        AttributeFilterWorkContext workCtx = context.getSubcontext(AttributeFilterWorkContext.class, true);

        policy.apply(attribute1, context);

        Collection<IdPAttributeValue> result = workCtx.getPermittedIdPAttributeValues().get(ATTR_NAME);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(aStringAttributeValue));
        Assert.assertTrue(result.contains(cStringAttributeValue));
        Assert.assertNull(workCtx.getDeniedAttributeValues().get(ATTR_NAME));

        policy = new AttributeRule();
        policy.setId("id");
        policy.setMatcher(matcher);
        policy.setIsDenyRule(true);
        policy.setAttributeId(ATTR_NAME);
        policy.initialize();

        context = new AttributeFilterContext();
        workCtx = context.getSubcontext(AttributeFilterWorkContext.class, true);
        context.setPrefilteredIdPAttributes(Arrays.asList(attribute1));

        policy.apply(attribute1, context);

        result = workCtx.getDeniedAttributeValues().get(ATTR_NAME);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(aStringAttributeValue));
        Assert.assertTrue(result.contains(cStringAttributeValue));
        Assert.assertNull(workCtx.getPermittedIdPAttributeValues().get(ATTR_NAME));

        policy.destroy();

        thrown = false;
        try {
            policy.apply(attribute1, context);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "validate after destroy");
    }
}
