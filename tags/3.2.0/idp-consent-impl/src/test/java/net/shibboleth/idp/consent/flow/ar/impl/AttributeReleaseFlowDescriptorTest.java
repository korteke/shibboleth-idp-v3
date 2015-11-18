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

package net.shibboleth.idp.consent.flow.ar.impl;

import java.util.Collection;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.consent.logic.impl.AttributeValuesHashFunction;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/** {@link AttributeReleaseFlowDescriptor} unit test. */
public class AttributeReleaseFlowDescriptorTest {

    private AttributeReleaseFlowDescriptor descriptor;

    @BeforeMethod public void setUp() {
        descriptor = new AttributeReleaseFlowDescriptor();
        descriptor.setId("test");
    }

    @Test public void testInstantation() {
        Assert.assertEquals(descriptor.getId(), "test");
        Assert.assertFalse(descriptor.isDoNotRememberConsentAllowed());
        Assert.assertFalse(descriptor.isGlobalConsentAllowed());
        Assert.assertFalse(descriptor.isPerAttributeConsentEnabled());
        Assert.assertNotNull(descriptor.getAttributeValuesHashFunction());
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullAttributeValuesHashFunction() {
        descriptor.setAttributeValuesHashFunction(null);
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void
            testSettingAttributeValuesHashFunctionAfterInitialization() throws Exception {
        descriptor.initialize();
        descriptor.setAttributeValuesHashFunction(new AttributeValuesHashFunction());
    }

    @Test public void testSettingAttributeValuesHashFunction() throws Exception {
        Function<Collection<IdPAttributeValue<?>>, String> function = new AttributeValuesHashFunction();
        descriptor.setAttributeValuesHashFunction(function);
        Assert.assertEquals(descriptor.getAttributeValuesHashFunction(), function);
    }

    @Test public void testMutatingDoNotRememberConsent() throws Exception {
        descriptor.setDoNotRememberConsentAllowed(true);
        Assert.assertTrue(descriptor.isDoNotRememberConsentAllowed());

        descriptor.setDoNotRememberConsentAllowed(false);
        Assert.assertFalse(descriptor.isDoNotRememberConsentAllowed());
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void
            testSettingDoNotRememberConsentAfterInitialization() throws Exception {
        descriptor.initialize();
        descriptor.setDoNotRememberConsentAllowed(true);
    }

    @Test public void testMutatingGlobalConsent() {
        descriptor.setGlobalConsentAllowed(true);
        Assert.assertTrue(descriptor.isGlobalConsentAllowed());

        descriptor.setGlobalConsentAllowed(false);
        Assert.assertFalse(descriptor.isGlobalConsentAllowed());
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void
            testSettingGlobalConsentAfterInitialization() throws Exception {
        descriptor.initialize();
        descriptor.setGlobalConsentAllowed(true);
    }

    @Test public void testMutatingPerAttributeConsent() {
        descriptor.setPerAttributeConsentEnabled(true);
        Assert.assertTrue(descriptor.isPerAttributeConsentEnabled());

        descriptor.setPerAttributeConsentEnabled(false);
        Assert.assertFalse(descriptor.isPerAttributeConsentEnabled());
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void
            testSettingPerAttributeConsentAfterInitialization() throws Exception {
        descriptor.initialize();
        descriptor.setPerAttributeConsentEnabled(true);
    }

}
