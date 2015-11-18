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

import java.util.Map;
import java.util.Objects;

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

/** {@link IsConsentRequiredPredicate} unit test. */
public class IsConsentRequiredPredicateTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private IsConsentRequiredPredicate p;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        p = new IsConsentRequiredPredicate();
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

    /**
     * Set up consent context where the previous and current consents are the same. This would be the situation when the
     * attributes being released have not changed.
     */
    private void setUpMatchingPreviousAndCurrentConsents() {
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().putAll(ConsentTestingSupport.newConsentMap());
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());
        prc.addSubcontext(consentCtx);

        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getCurrentConsents().isEmpty());
        Assert.assertTrue(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents()));
    }

    /**
     * Set up consent context where a consent object exists as a previous consent but not a current consent. This would
     * be the situation when an attribute was previously released but not currently.
     */
    private void setUpInPreviousButNotCurrentConsents() {
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().putAll(ConsentTestingSupport.newConsentMap());

        final Map<String, Consent> consentSubset = ConsentTestingSupport.newConsentMap();
        consentSubset.remove("consent1");
        consentCtx.getCurrentConsents().putAll(consentSubset);
        prc.addSubcontext(consentCtx);

        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getCurrentConsents().isEmpty());
        Assert.assertFalse(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents()));
        Assert.assertFalse(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents().keySet(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents().keySet()));
    }

    /**
     * Set up consent context where a consent object exists as a current consent but not a previous consent. This would
     * be the situation when a new attribute is released.
     */
    private void setUpInCurrentButNotPreviousConsents() {
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getCurrentConsents().putAll(ConsentTestingSupport.newConsentMap());

        final Map<String, Consent> consentSubset = ConsentTestingSupport.newConsentMap();
        consentSubset.remove("consent1");
        consentCtx.getPreviousConsents().putAll(consentSubset);
        prc.addSubcontext(consentCtx);

        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getCurrentConsents().isEmpty());
        Assert.assertFalse(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents()));
    }

    /**
     * Set up consent context where a consent object exists as a current consent and a previous consent with different
     * values. This would be the situation when a released attribute's value has changed.
     */
    private void setUpInPreviousAndCurrentConsentsWithDifferentValue() {
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().putAll(ConsentTestingSupport.newConsentMap());

        final Map<String, Consent> consentSubset = ConsentTestingSupport.newConsentMap();
        consentSubset.get("consent1").setValue("differentValue");
        consentCtx.getCurrentConsents().putAll(consentSubset);
        prc.addSubcontext(consentCtx);

        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getCurrentConsents().isEmpty());
        Assert.assertFalse(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents()));
        Assert.assertTrue(Objects.equals(prc.getSubcontext(ConsentContext.class).getPreviousConsents().keySet(), prc
                .getSubcontext(ConsentContext.class).getCurrentConsents().keySet()));
    }

    @Test public void testNullInput() {
        Assert.assertFalse(p.apply(null));
    }

    @Test public void testNullConsentContext() {
        Assert.assertNull(prc.getSubcontext(ConsentContext.class));

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testNullConsentFlowDescriptor() {
        prc.addSubcontext(new ConsentContext());
        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));

        prc.addSubcontext(new ProfileInterceptorContext());
        Assert.assertNotNull(prc.getSubcontext(ProfileInterceptorContext.class));
        Assert.assertNull(prc.getSubcontext(ProfileInterceptorContext.class).getAttemptedFlow());

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testNoPreviousConsents() {
        prc.addSubcontext(new ConsentContext());
        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertTrue(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());

        prc.getSubcontext(ConsentContext.class).getCurrentConsents().put("test", new Consent());
        setUpDescriptor(false);

        Assert.assertTrue(p.apply(prc));
    }

    @Test public void testNoCurrentConsents() {
        final ConsentContext consentCtx = new ConsentContext();
        consentCtx.getPreviousConsents().putAll(ConsentTestingSupport.newConsentMap());
        prc.addSubcontext(consentCtx);

        Assert.assertNotNull(prc.getSubcontext(ConsentContext.class));
        Assert.assertFalse(prc.getSubcontext(ConsentContext.class).getPreviousConsents().isEmpty());
        Assert.assertTrue(prc.getSubcontext(ConsentContext.class).getCurrentConsents().isEmpty());

        setUpDescriptor(false);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testMatchingPreviousAndCurrentConsents() {
        setUpMatchingPreviousAndCurrentConsents();
        setUpDescriptor(false);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testInPreviousButNotCurrentConsents() {
        setUpInPreviousButNotCurrentConsents();
        setUpDescriptor(false);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testInCurrentButNotPreviousConsents() {
        setUpInCurrentButNotPreviousConsents();
        setUpDescriptor(false);

        Assert.assertTrue(p.apply(prc));
    }

    @Test public void testMatchingPreviousAndCurrentConsentsCompareValues() {
        setUpMatchingPreviousAndCurrentConsents();
        setUpDescriptor(true);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testInPreviousButNotCurrentConsentsCompareValues() {
        setUpInPreviousButNotCurrentConsents();
        setUpDescriptor(true);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testInCurrentButNotPreviousConsentsCompareValues() {
        setUpInCurrentButNotPreviousConsents();
        setUpDescriptor(true);

        Assert.assertTrue(p.apply(prc));
    }

    @Test public void testInPreviousAndCurrentConsentsWithDifferentValue() {
        setUpInPreviousAndCurrentConsentsWithDifferentValue();
        setUpDescriptor(false);

        Assert.assertFalse(p.apply(prc));
    }

    @Test public void testInPreviousAndCurrentConsentsWithDifferentValueCompareValues() {
        setUpInPreviousAndCurrentConsentsWithDifferentValue();
        setUpDescriptor(true);

        Assert.assertTrue(p.apply(prc));
    }

}