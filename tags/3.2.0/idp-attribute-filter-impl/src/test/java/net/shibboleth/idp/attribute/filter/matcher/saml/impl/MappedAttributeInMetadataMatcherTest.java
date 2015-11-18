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

package net.shibboleth.idp.attribute.filter.matcher.saml.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.IdPRequestedAttribute;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.idp.saml.attribute.mapping.AttributesMapContainer;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Tests for {@link MappedAttributeInMetadataMatcher}.
 */
public class MappedAttributeInMetadataMatcherTest extends OpenSAMLInitBaseTestCase {
    
    private SAMLObjectBuilder<AttributeConsumingService> acsBuilder;

    @BeforeMethod public void setUp() {
        acsBuilder = (SAMLObjectBuilder<AttributeConsumingService>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<AttributeConsumingService>getBuilderOrThrow(
                        AttributeConsumingService.DEFAULT_ELEMENT_NAME);
    }
    
    private IdPAttribute makeAttribute(String id, List<? extends IdPAttributeValue<?>> values) {
        IdPAttribute attr = new IdPAttribute(id);
        attr.setValues(values);
        return attr;
    }

    private MappedAttributeInMetadataMatcher makeMatcher(String id, boolean matchIfMetadataSilent, boolean onlyIfRequired)
            throws ComponentInitializationException {
        MappedAttributeInMetadataMatcher matcher = new MappedAttributeInMetadataMatcher();
        matcher.setMatchIfMetadataSilent(matchIfMetadataSilent);
        matcher.setOnlyIfRequired(onlyIfRequired);
        matcher.setId(id);
        matcher.initialize();
        return matcher;
    }

    private void setRequestedAttributesInContext(final AttributeFilterContext context,
            final Multimap<String, IdPRequestedAttribute> multimap) {
        final AttributesMapContainer<IdPRequestedAttribute> container = new AttributesMapContainer<>(multimap);
        final SAMLMetadataContext samlMetadataContext = context.getSubcontext(SAMLMetadataContext.class, true);
        final AttributeConsumingServiceContext acsCtx =
                samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true);
        acsCtx.setAttributeConsumingService(acsBuilder.buildObject());
        acsCtx.getAttributeConsumingService().getObjectMetadata().put(container);
        context.setRequesterMetadataContextLookupStrategy(new ChildContextLookup<AttributeFilterContext, SAMLMetadataContext>(
                SAMLMetadataContext.class));
    }

    private AttributeFilterContext makeContext(String attributeId, IdPRequestedAttribute attribute) {

        final AttributeFilterContext context = new AttributeFilterContext();

        if (null != attributeId) {
            final Multimap<String, IdPRequestedAttribute> multimap = ArrayListMultimap.create();
            multimap.put(attributeId, attribute);
            setRequestedAttributesInContext(context, multimap);
        }
        return context;
    }

    private AttributeFilterContext makeContext(IdPRequestedAttribute attribute) {

        if (null == attribute) {
            return makeContext(null, null);
        }
        return makeContext(attribute.getId(), attribute);
    }

    @Test public void getters() throws ComponentInitializationException {
        MappedAttributeInMetadataMatcher matcher = makeMatcher("test", true, true);
        Assert.assertTrue(matcher.getMatchIfMetadataSilent());
        Assert.assertTrue(matcher.getOnlyIfRequired());

        matcher = makeMatcher("test", false, false);
        Assert.assertFalse(matcher.getMatchIfMetadataSilent());
        Assert.assertFalse(matcher.getOnlyIfRequired());
    }

    @Test public void noRequested() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        Set<IdPAttributeValue<?>> result =
                makeMatcher("test", true, true).getMatchingValues(attr, new AttributeFilterContext());

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        result = makeMatcher("test", false, true).getMatchingValues(attr, new AttributeFilterContext());
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void wrongRequested() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final MappedAttributeInMetadataMatcher matcher = makeMatcher("test", true, true);
        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attr, makeContext(null));

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        result = matcher.getMatchingValues(attr, makeContext(new IdPRequestedAttribute("wrongAttr")));
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void isRequiredOnly() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        IdPRequestedAttribute required = new IdPRequestedAttribute("attr");
        required.setRequired(false);

        AttributeFilterContext context = makeContext(required);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, false).getMatchingValues(attr, context);

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        result = makeMatcher("test", false, true).getMatchingValues(attr, context);
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void values() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        IdPRequestedAttribute required = new IdPRequestedAttribute("attr");
        required.setRequired(true);
        required.setValues(Collections.singleton(DataSources.STRING_VALUE));

        AttributeFilterContext context = makeContext(required);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, true).getMatchingValues(attr, context);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
    }

    @Test public void valuesButNoConvert() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        AttributeFilterContext context = makeContext("attr", null);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, true).getMatchingValues(attr, context);
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void multiValues() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        IdPRequestedAttribute req1 = new IdPRequestedAttribute("attr");
        req1.setRequired(true);
        req1.setValues(Collections.singleton(DataSources.STRING_VALUE));

        IdPRequestedAttribute req2 = new IdPRequestedAttribute("attr");
        req2.setRequired(true);
        req2.setValues(Collections.singleton(DataSources.NON_MATCH_STRING_VALUE));

        final AttributeFilterContext context = new AttributeFilterContext();

        final Multimap<String, IdPRequestedAttribute> multimap = ArrayListMultimap.create();
        multimap.put(req1.getId(), req1);
        multimap.put(req2.getId(), req2);
        setRequestedAttributesInContext(context, multimap);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, true).getMatchingValues(attr, context);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));
    }

}
