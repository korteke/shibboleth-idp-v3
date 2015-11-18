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

package net.shibboleth.idp.profile.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.MockMatcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.impl.AttributeFilterImpl;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.service.AbstractReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link FilterAttributes} unit test. */
public class FilterAttributesTest {

    private RequestContext src;
    
    private ProfileRequestContext prc;
    
    @BeforeMethod public void setUpAction() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);
    }
    
    /** Test that the action proceeds properly if there is no attribute context. */
    @Test public void testNoAttributeContext() throws Exception {
        prc.getSubcontext(SubjectContext.class, true);

        final AttributeFilterImpl engine = new AttributeFilterImpl("test", null);
        engine.initialize();

        final FilterAttributes action = new FilterAttributes(new FilterService(engine));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    /** Test that the action proceeds properly if there are no attributes to filter . */
    @Test public void testNoAttributes() throws Exception {
        prc.getSubcontext(SubjectContext.class, true);

        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true);

        final AttributeFilterImpl engine = new AttributeFilterImpl("test", null);
        engine.initialize();
        
        final FilterAttributes action = new FilterAttributes(new FilterService(engine));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
    }

    /** Test that the action filters attributes and proceeds properly while auto-creating a filter context. */
    @Test public void testFilterAttributesAutoCreateFilterContext() throws Exception {
        final IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        final IdPAttribute attribute2 = new IdPAttribute("attribute2");
        attribute2.setValues(Arrays.asList(new StringAttributeValue("a"), new StringAttributeValue("b")));

        final List<IdPAttribute> attributes = Arrays.asList(attribute1, attribute2);

        final MockMatcher attribute1Matcher = new MockMatcher();
        attribute1Matcher.setMatchingAttribute("attribute1");
        attribute1Matcher.setMatchingValues(null);

        final AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(attribute1Matcher);
        attribute1Policy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        policy.initialize();
        attribute1Policy.initialize();
        attribute1Matcher.initialize();
        engine.initialize();

        prc.getSubcontext(SubjectContext.class, true);

        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);

        final FilterAttributes action = new FilterAttributes(new FilterService(engine));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        // The attribute filter context should be removed by the filter attributes action.
        Assert.assertNull(prc.getSubcontext(RelyingPartyContext.class).getSubcontext(
                AttributeFilterContext.class));

        final AttributeContext resultAttributeCtx =
                prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class);
        Assert.assertNotNull(resultAttributeCtx);

        final Map<String, IdPAttribute> resultAttributes = resultAttributeCtx.getIdPAttributes();
        Assert.assertEquals(resultAttributes.size(), 1);

        final List<IdPAttributeValue<?>> resultAttributeValue = resultAttributes.get("attribute1").getValues();
        Assert.assertEquals(resultAttributeValue.size(), 2);
        Assert.assertTrue(resultAttributeValue.contains(new StringAttributeValue("one")));
        Assert.assertTrue(resultAttributeValue.contains(new StringAttributeValue("two")));
    }

    /** Test that the action filters attributes and proceeds properly with an existing filter context. */
    @Test public void testFilterAttributesExistingFilterContext() throws Exception {
        final IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        final IdPAttribute attribute2 = new IdPAttribute("attribute2");
        attribute2.setValues(Arrays.asList(new StringAttributeValue("a"), new StringAttributeValue("b")));

        final List<IdPAttribute> attributes = Arrays.asList(attribute1, attribute2);

        final MockMatcher attribute1Matcher = new MockMatcher();
        attribute1Matcher.setMatchingAttribute("attribute1");
        attribute1Matcher.setMatchingValues(null);

        final AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(attribute1Matcher);
        attribute1Policy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        policy.initialize();
        attribute1Policy.initialize();
        attribute1Matcher.initialize();
        engine.initialize();

        prc.getSubcontext(SubjectContext.class, true);

        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);

        final AttributeFilterContext attributeFilterCtx = new AttributeFilterContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeFilterCtx);

        final FilterAttributes action = new FilterAttributes(new FilterService(engine));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        // The attribute filter context should be removed by the filter attributes action.
        Assert.assertNull(prc.getSubcontext(RelyingPartyContext.class).getSubcontext(
                AttributeFilterContext.class));

        final AttributeContext resultAttributeCtx =
                prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class);
        Assert.assertNotNull(resultAttributeCtx);

        final Map<String, IdPAttribute> resultAttributes = resultAttributeCtx.getIdPAttributes();
        Assert.assertEquals(resultAttributes.size(), 1);

        final List<IdPAttributeValue<?>> resultAttributeValue = resultAttributes.get("attribute1").getValues();
        Assert.assertEquals(resultAttributeValue.size(), 2);
        Assert.assertTrue(resultAttributeValue.contains(new StringAttributeValue("one")));
        Assert.assertTrue(resultAttributeValue.contains(new StringAttributeValue("two")));
    }

    /** Test that action returns the proper event if the attributes are not able to be filtered. */
    @Test public void testUnableToFilterAttributes() throws Exception {
        final IdPAttribute attribute1 = new MockUncloneableAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        final List<IdPAttribute> attributes = Arrays.asList(attribute1);

        final MockMatcher attribute1Matcher = new MockMatcher();
        attribute1Matcher.setMatchingAttribute("attribute1");
        attribute1Matcher.setMatchingValues(null);

        final AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(attribute1Matcher);
        attribute1Policy.setIsDenyRule(false);

        final AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Collections.singletonList(attribute1Policy));

        final AttributeFilterImpl engine = new AttributeFilterImpl("engine", Collections.singletonList(policy));
        policy.initialize();
        attribute1Policy.initialize();
        attribute1Matcher.initialize();
        engine.initialize();

        prc.getSubcontext(SubjectContext.class, true);

        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);

        final AttributeFilterContext attributeFilterCtx = new AttributeFilterContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeFilterCtx);

        final FilterAttributes action = new FilterAttributes(new FilterService(engine));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertTrue(attributeCtx.getIdPAttributes().isEmpty());
    }
    
    /** Test that action returns the proper event if the attribute configuration is broken */
    @Test public void testUnableToFindFilter() throws Exception {
        final IdPAttribute attribute1 = new MockUncloneableAttribute("attribute1");
        attribute1.setValues(Arrays.asList(new StringAttributeValue("one"), new StringAttributeValue("two")));

        prc.getSubcontext(SubjectContext.class, true);

        final AttributeContext attributeCtx = new AttributeContext();
        final List<IdPAttribute> attributes = Collections.singletonList(attribute1);
        attributeCtx.setIdPAttributes(attributes);
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeCtx);

        final AttributeFilterContext attributeFilterCtx = new AttributeFilterContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attributeFilterCtx);

        final FilterAttributes action = new FilterAttributes(new FilterService(null));
        action.setMaskFailures(false);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, IdPEventIds.UNABLE_FILTER_ATTRIBS);
    }


    /** {@link IdPAttribute} which always throws a {@link CloneNotSupportedException}. */
    private class MockUncloneableAttribute extends IdPAttribute {

        /**
         * Constructor.
         * 
         * @param attributeId
         */
        public MockUncloneableAttribute(String attributeId) {
            super(attributeId);
        }

        /** Always throws exception. */
        @Override
        public IdPAttribute clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }
    

    private static class FilterService extends AbstractReloadableService<AttributeFilter> {

        private ServiceableComponent<AttributeFilter> component;

        protected FilterService(ServiceableComponent<AttributeFilter> what) {
            component = what;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable public ServiceableComponent<AttributeFilter> getServiceableComponent() {
            if (null == component) {
                return null;
            }
            component.pinComponent();
            return component;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean shouldReload() {
            return false;
        }
    }

}