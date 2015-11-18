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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeFilterContext}. */
public class AttributeFilterWorkContextTest {

    private final StringAttributeValue aStringAttributeValue = new StringAttributeValue("a");

    private final StringAttributeValue bStringAttributeValue = new StringAttributeValue("b");

    private final StringAttributeValue cStringAttributeValue = new StringAttributeValue("c");



    /** Testing getting and adding permitted attribute values. */
    @Test public void testPermittedAttributeValues() {
        AttributeFilterContext parent = new AttributeFilterContext();
        AttributeFilterWorkContext context = parent.getSubcontext(AttributeFilterWorkContext.class, true);

        IdPAttribute attribute1 = new IdPAttribute("one");
        attribute1.setValues(Arrays.asList(aStringAttributeValue, bStringAttributeValue));
        parent.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        context.addPermittedIdPAttributeValues("one", Collections.singletonList(aStringAttributeValue));
        Assert.assertEquals(context.getPermittedIdPAttributeValues().get("one").size(), 1);

        context.addPermittedIdPAttributeValues("one", null);
        Assert.assertEquals(context.getPermittedIdPAttributeValues().get("one").size(), 1);

        context.addPermittedIdPAttributeValues("one", new ArrayList<IdPAttributeValue>());
        Assert.assertEquals(context.getPermittedIdPAttributeValues().get("one").size(), 1);

        context.addPermittedIdPAttributeValues("one", Collections.singletonList(bStringAttributeValue));
        Assert.assertEquals(context.getPermittedIdPAttributeValues().get("one").size(), 2);

        try {
            context.addPermittedIdPAttributeValues(null, Collections.singletonList(aStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addPermittedIdPAttributeValues("", Collections.singletonList(aStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addPermittedIdPAttributeValues("two", Collections.singletonList(aStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addPermittedIdPAttributeValues("one", Collections.singletonList(cStringAttributeValue));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }

    /** Testing getting and adding denied attribute values. */
    @Test public void testDeniedAttributeValues() {
        AttributeFilterContext parent = new AttributeFilterContext();
        AttributeFilterWorkContext context = parent.getSubcontext(AttributeFilterWorkContext.class, true);

        IdPAttribute attribute1 = new IdPAttribute("one");
        attribute1.setValues(Arrays.asList(aStringAttributeValue, bStringAttributeValue));
        parent.getPrefilteredIdPAttributes().put(attribute1.getId(), attribute1);

        context.addDeniedIdPAttributeValues("one", Collections.singletonList(aStringAttributeValue));
        Assert.assertEquals(context.getDeniedAttributeValues().get("one").size(), 1);

        context.addDeniedIdPAttributeValues("one", null);
        Assert.assertEquals(context.getDeniedAttributeValues().get("one").size(), 1);

        context.addDeniedIdPAttributeValues("one", new ArrayList<IdPAttributeValue>());
        Assert.assertEquals(context.getDeniedAttributeValues().get("one").size(), 1);

        context.addDeniedIdPAttributeValues("one", Collections.singletonList(bStringAttributeValue));
        Assert.assertEquals(context.getDeniedAttributeValues().get("one").size(), 2);

        try {
            context.addDeniedIdPAttributeValues(null, Collections.singletonList(bStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addDeniedIdPAttributeValues("", Collections.singletonList(bStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addDeniedIdPAttributeValues("two", Collections.singletonList(bStringAttributeValue));
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            context.addDeniedIdPAttributeValues("one", Collections.singletonList(cStringAttributeValue));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }
    
}