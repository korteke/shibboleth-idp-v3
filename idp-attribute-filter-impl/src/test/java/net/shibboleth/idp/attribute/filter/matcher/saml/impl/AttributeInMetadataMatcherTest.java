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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.idp.attribute.filter.matcher.saml.impl.AttributeInMetadataMatcher;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringAttributeEncoder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.messaging.context.AttributeConsumingServiceContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link AttributeInMetadataMatcher}
 */
public class AttributeInMetadataMatcherTest extends OpenSAMLInitBaseTestCase {

    private SAMLObjectBuilder<AttributeConsumingService> acsBuilder;
    private SAMLObjectBuilder<RequestedAttribute> reqAttributeBuilder;
    private XMLObjectBuilder<XSString> valueBuilder;

    @BeforeMethod public void setUp() {
        acsBuilder = (SAMLObjectBuilder<AttributeConsumingService>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<AttributeConsumingService>getBuilderOrThrow(
                        AttributeConsumingService.DEFAULT_ELEMENT_NAME);
        reqAttributeBuilder = (SAMLObjectBuilder<RequestedAttribute>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<RequestedAttribute>getBuilderOrThrow(
                        RequestedAttribute.DEFAULT_ELEMENT_NAME);
        valueBuilder = XMLObjectProviderRegistrySupport.getBuilderFactory().<XSString>getBuilderOrThrow(XSString.TYPE_NAME);
    }

    private IdPAttribute makeAttribute(String id, List<? extends IdPAttributeValue<?>> values) {
        final IdPAttribute attr = new IdPAttribute(id);
        attr.setValues(values);
        
        final SAML2StringAttributeEncoder encoder = new SAML2StringAttributeEncoder();
        encoder.setName(id);
        encoder.setNameFormat(Attribute.BASIC);
        
        attr.setEncoders(Collections.<AttributeEncoder<?>>singleton(encoder));
        return attr;
    }

    private AttributeInMetadataMatcher makeMatcher(String id, boolean matchIfMetadataSilent, boolean onlyIfRequired,
            String name, String nameFormat)
            throws ComponentInitializationException {
        AttributeInMetadataMatcher matcher = new AttributeInMetadataMatcher();
        matcher.setMatchIfMetadataSilent(matchIfMetadataSilent);
        matcher.setOnlyIfRequired(onlyIfRequired);
        matcher.setAttributeName(name);
        matcher.setAttributeNameFormat(nameFormat);
        matcher.setId(id);
        matcher.initialize();
        return matcher;
    }

    private void setRequestedAttributesInContext(final AttributeFilterContext context,
            final Collection<RequestedAttribute> attributes) {
        final SAMLMetadataContext samlMetadataContext = context.getSubcontext(SAMLMetadataContext.class, true);
        final AttributeConsumingServiceContext acsCtx =
                samlMetadataContext.getSubcontext(AttributeConsumingServiceContext.class, true);
        acsCtx.setAttributeConsumingService(acsBuilder.buildObject());
        acsCtx.getAttributeConsumingService().getRequestAttributes().addAll(attributes);
        context.setRequesterMetadataContextLookupStrategy(
                new ChildContextLookup<AttributeFilterContext,SAMLMetadataContext>(SAMLMetadataContext.class));
    }

    private AttributeFilterContext makeContext(RequestedAttribute attribute) {
        final AttributeFilterContext context = new AttributeFilterContext();
        if (attribute != null) {
            setRequestedAttributesInContext(context, Collections.singletonList(attribute));
        }
        return context;
    }

    @Test public void getters() throws ComponentInitializationException {
        AttributeInMetadataMatcher matcher = makeMatcher("test", true, true, null, null);
        Assert.assertTrue(matcher.getMatchIfMetadataSilent());
        Assert.assertTrue(matcher.getOnlyIfRequired());
        Assert.assertNull(matcher.getAttributeName());
        Assert.assertNull(matcher.getAttributeNameFormat());
        
        matcher = makeMatcher("test", false, false, null, null);
        Assert.assertFalse(matcher.getMatchIfMetadataSilent());
        Assert.assertFalse(matcher.getOnlyIfRequired());
        Assert.assertNull(matcher.getAttributeName());
        Assert.assertNull(matcher.getAttributeNameFormat());

        matcher = makeMatcher("test", false, true, "foo", "bar");
        Assert.assertFalse(matcher.getMatchIfMetadataSilent());
        Assert.assertTrue(matcher.getOnlyIfRequired());
        Assert.assertEquals(matcher.getAttributeName(), "foo");
        Assert.assertEquals(matcher.getAttributeNameFormat(), "bar");
}

    @Test public void noRequested() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        Set<IdPAttributeValue<?>> result =
                makeMatcher("test", true, true, null, null).getMatchingValues(attr, new AttributeFilterContext());

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        result = makeMatcher("test", false, true, null, null).getMatchingValues(attr, new AttributeFilterContext());
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void wrongRequested() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final AttributeInMetadataMatcher matcher = makeMatcher("test", true, false, null, null);
        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attr, makeContext(null));

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        final RequestedAttribute wrongAttr = reqAttributeBuilder.buildObject();
        wrongAttr.setName("wrongAttr");
        wrongAttr.setNameFormat(Attribute.BASIC);
        result = matcher.getMatchingValues(attr, makeContext(wrongAttr));
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void otherRequested() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final AttributeInMetadataMatcher matcher = makeMatcher("test", false, false, "attr2", Attribute.BASIC);

        final RequestedAttribute wrongAttr = reqAttributeBuilder.buildObject();
        wrongAttr.setName("attr2");
        wrongAttr.setNameFormat(Attribute.BASIC);
        
        final Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attr, makeContext(wrongAttr));
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

    }
    
    @Test public void isRequiredOnly() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final RequestedAttribute req = reqAttributeBuilder.buildObject();
        req.setName("attr");
        req.setNameFormat(Attribute.BASIC);
        final AttributeFilterContext context = makeContext(req);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, false, null, null).getMatchingValues(attr, context);

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));

        result = makeMatcher("test", false, true, null, null).getMatchingValues(attr, context);
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void values() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final RequestedAttribute req = reqAttributeBuilder.buildObject();
        req.setName("attr");
        req.setNameFormat(Attribute.BASIC);
        req.setIsRequired(true);
        final XSString val = valueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        val.setValue(DataSources.STRING_VALUE.getValue());
        req.getAttributeValues().add(val);

        final AttributeFilterContext context = makeContext(req);

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, true, null, null).getMatchingValues(attr, context);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
    }
    
    @Test public void multiValues() throws ComponentInitializationException {

        final IdPAttribute attr =
                makeAttribute("attr", Arrays.asList(DataSources.STRING_VALUE, DataSources.NON_MATCH_STRING_VALUE));

        final RequestedAttribute req = reqAttributeBuilder.buildObject();
        req.setName("attr");
        req.setNameFormat(Attribute.BASIC);
        req.setIsRequired(true);

        final XSString val1 = valueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        val1.setValue(DataSources.STRING_VALUE.getValue());
        req.getAttributeValues().add(val1);

        final XSString val2 = valueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        val2.setValue(DataSources.NON_MATCH_STRING_VALUE.getValue());
        req.getAttributeValues().add(val2);

        final AttributeFilterContext context = new AttributeFilterContext();

        setRequestedAttributesInContext(context, Collections.singletonList(req));

        Set<IdPAttributeValue<?>> result = makeMatcher("test", false, true, null, null).getMatchingValues(attr, context);
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(DataSources.STRING_VALUE));
        Assert.assertTrue(result.contains(DataSources.NON_MATCH_STRING_VALUE));
    }

}