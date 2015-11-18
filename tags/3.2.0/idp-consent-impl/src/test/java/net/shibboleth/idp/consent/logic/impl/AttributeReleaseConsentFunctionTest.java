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

package net.shibboleth.idp.consent.logic.impl;

import java.util.HashMap;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.consent.context.impl.AttributeReleaseContext;
import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.flow.impl.ConsentFlowDescriptor;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AttributeReleaseConsentFunction} unit test. */
public class AttributeReleaseConsentFunctionTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private AttributeValuesHashFunction attributeValuesHashFunction;

    private AttributeReleaseConsentFunction function;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        attributeValuesHashFunction = new AttributeValuesHashFunction();

        function = new AttributeReleaseConsentFunction();
    }

    /**
     * Add a {@link ConsentFlowDescriptor} to the {@link ProfileRequestContext}.
     * 
     * @param compareValues whether consent equality includes comparing consent values
     */
    private void setUpDescriptor(final boolean compareValues) {
        final ConsentFlowDescriptor descriptor = new ConsentFlowDescriptor();
        descriptor.setId("test");
        descriptor.setCompareValues(compareValues);

        final ProfileInterceptorContext pic = new ProfileInterceptorContext();
        pic.setAttemptedFlow(descriptor);
        prc.addSubcontext(pic);

        Assert.assertNotNull(prc.getSubcontext(ProfileInterceptorContext.class));
        Assert.assertNotNull(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow());
        Assert.assertTrue(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow() instanceof ConsentFlowDescriptor);

        Assert.assertEquals(((ConsentFlowDescriptor) prc.getSubcontext(ProfileInterceptorContext.class)
                .getAttemptedFlow()).compareValues(), compareValues);
    }

    @Test public void testNullInput() {
        Assert.assertNull(function.apply(null));
    }

    @Test public void testNullConsentContext() {
        Assert.assertNull(prc.getSubcontext(ConsentContext.class));
        setUpDescriptor(false);

        Assert.assertNull(function.apply(prc));
    }

    @Test public void testNullConsentFlowDescriptor() {
        prc.addSubcontext(new ConsentContext());
        prc.addSubcontext(new ProfileInterceptorContext());
        Assert.assertNull(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow());

        Assert.assertNull(function.apply(prc));
    }

    @Test public void testNullAttributeReleaseContext() {
        prc.addSubcontext(new ConsentContext());
        setUpDescriptor(false);
        Assert.assertNull(prc.getSubcontext(AttributeReleaseContext.class));

        Assert.assertNull(function.apply(prc));
    }

    @Test public void testNoConsentableAttributes() {
        prc.addSubcontext(new ConsentContext());
        prc.addSubcontext(new AttributeReleaseContext(), true);
        setUpDescriptor(false);
        Assert.assertTrue(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());

        Assert.assertTrue(function.apply(prc).isEmpty());
    }

    @Test public void testNoPreviousConsents() {
        prc.addSubcontext(new ConsentContext());
        final AttributeReleaseContext arc = new AttributeReleaseContext();
        arc.getConsentableAttributes().putAll(ConsentTestingSupport.newAttributeMap());
        prc.addSubcontext(arc);
        setUpDescriptor(false);
        Assert.assertFalse(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());
        Assert.assertTrue(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        final Map<String, Consent> expected = new HashMap<>();
        for (final IdPAttribute attr : ConsentTestingSupport.newAttributeMap().values()) {
            final Consent consent = new Consent();
            consent.setId(attr.getId());
            expected.put(consent.getId(), consent);
        }

        Assert.assertEquals(function.apply(prc), expected);
    }

    @Test public void testNoPreviousConsentsCompareValues() {
        prc.addSubcontext(new ConsentContext());
        final AttributeReleaseContext arc = new AttributeReleaseContext();
        arc.getConsentableAttributes().putAll(ConsentTestingSupport.newAttributeMap());
        prc.addSubcontext(arc);
        setUpDescriptor(true);
        Assert.assertFalse(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());
        Assert.assertTrue(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        final Map<String, Consent> expected = new HashMap<>();
        for (final IdPAttribute attr : ConsentTestingSupport.newAttributeMap().values()) {
            final Consent consent = new Consent();
            consent.setId(attr.getId());
            consent.setValue(attributeValuesHashFunction.apply(attr.getValues()));
            expected.put(consent.getId(), consent);
        }

        Assert.assertEquals(function.apply(prc), expected);
    }

    @Test public void testRememberPreviousConsents() {
        final Consent previousConsent = new Consent();
        previousConsent.setId("attribute1");
        previousConsent.setApproved(true);
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().put(previousConsent.getId(), previousConsent);
        prc.addSubcontext(consentCtx);

        final AttributeReleaseContext arc = new AttributeReleaseContext();
        arc.getConsentableAttributes().putAll(ConsentTestingSupport.newAttributeMap());
        prc.addSubcontext(arc);
        setUpDescriptor(false);
        Assert.assertFalse(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        final Map<String, Consent> expected = new HashMap<>();
        for (final IdPAttribute attr : ConsentTestingSupport.newAttributeMap().values()) {
            final Consent consent = new Consent();
            consent.setId(attr.getId());
            if (attr.getId().equals("attribute1")) {
                consent.setApproved(true);
            }
            expected.put(consent.getId(), consent);
        }

        Assert.assertEquals(function.apply(prc), expected);
    }

    @Test public void testRememberPreviousConsentsCompareValues() {
        final Consent previousConsent = new Consent();
        previousConsent.setId("attribute1");
        previousConsent.setValue(attributeValuesHashFunction.apply(ConsentTestingSupport.newAttributeMap()
                .get("attribute1").getValues()));
        previousConsent.setApproved(true);
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().put(previousConsent.getId(), previousConsent);
        prc.addSubcontext(consentCtx);

        final AttributeReleaseContext arc = new AttributeReleaseContext();
        arc.getConsentableAttributes().putAll(ConsentTestingSupport.newAttributeMap());
        prc.addSubcontext(arc);
        setUpDescriptor(true);
        Assert.assertFalse(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        final Map<String, Consent> expected = new HashMap<>();
        for (final IdPAttribute attr : ConsentTestingSupport.newAttributeMap().values()) {
            final Consent consent = new Consent();
            consent.setId(attr.getId());
            consent.setValue(attributeValuesHashFunction.apply(attr.getValues()));
            if (attr.getId().equals("attribute1")) {
                consent.setApproved(true);
            }
            expected.put(consent.getId(), consent);
        }

        Assert.assertEquals(function.apply(prc), expected);
    }

    @Test public void testRememberPreviousConsentsDifferentValueCompareValues() {
        final Consent previousConsent = new Consent();
        previousConsent.setId("attribute1");
        previousConsent.setValue("differentValue");
        previousConsent.setApproved(true);
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().put(previousConsent.getId(), previousConsent);
        prc.addSubcontext(consentCtx);

        final AttributeReleaseContext arc = new AttributeReleaseContext();
        arc.getConsentableAttributes().putAll(ConsentTestingSupport.newAttributeMap());
        prc.addSubcontext(arc);
        setUpDescriptor(true);
        Assert.assertFalse(prc.getSubcontext(AttributeReleaseContext.class).getConsentableAttributes().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        final Map<String, Consent> expected = new HashMap<>();
        for (final IdPAttribute attr : ConsentTestingSupport.newAttributeMap().values()) {
            final Consent consent = new Consent();
            consent.setId(attr.getId());
            consent.setValue(attributeValuesHashFunction.apply(attr.getValues()));
            expected.put(consent.getId(), consent);
        }

        Assert.assertEquals(function.apply(prc), expected);
    }

}
