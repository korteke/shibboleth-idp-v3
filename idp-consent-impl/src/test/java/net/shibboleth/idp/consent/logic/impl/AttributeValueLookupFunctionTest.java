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

import java.util.Collections;
import java.util.Map;

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AttributeValueLookupFunction} unit test. */
public class AttributeValueLookupFunctionTest {

    private AttributeValueLookupFunction function;

    private RequestContext src;

    private ProfileRequestContext prc;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        final AttributeContext attributeCtx = new AttributeContext();
        
        final Map<String, IdPAttribute> attributes = ConsentTestingSupport.newAttributeMap();
        attributeCtx.setIdPAttributes(attributes.values());
        
        final Map<String, IdPAttribute> unfilteredAttributes = ConsentTestingSupport.newAttributeMap();
        final IdPAttribute attribute4 = new IdPAttribute("attribute4");
        attribute4.setValues(Collections.singleton(new StringAttributeValue("value4")));
        unfilteredAttributes.put(attribute4.getId(), attribute4);
        attributeCtx.setUnfilteredIdPAttributes(unfilteredAttributes.values());
        
        prc.getSubcontext(RelyingPartyContext.class, true).addSubcontext(attributeCtx);
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testEmptyConstructor() {
        function = new AttributeValueLookupFunction("");
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullConstructor() {
        function = new AttributeValueLookupFunction(null);
    }

    @Test public void testNullProfileRequestContext() {
        function = new AttributeValueLookupFunction("foo");

        Assert.assertNull(function.apply(null));
    }

    @Test public void testAttributeValue() {
        function = new AttributeValueLookupFunction("attribute1");
        Assert.assertEquals(function.apply(prc), "value1");
    }

    @Test public void testAttributeNotFound() {
        function = new AttributeValueLookupFunction("notFound");

        Assert.assertNull(function.apply(prc));
    }

    @Test public void testAttributeWithNoValues() {
        final AttributeContext attributeCtx =
                prc.getSubcontext(RelyingPartyContext.class, true).getSubcontext(AttributeContext.class);
        attributeCtx.setIdPAttributes(Collections.singleton(new IdPAttribute("EmptyAttribute")));

        function = new AttributeValueLookupFunction("EmptyAttribute");
        Assert.assertNull(function.apply(prc));
    }

    @Test public void testNonStringAttributeValue() {
        byte[] data = {1, 2, 3, 0xF};

        final IdPAttribute byteAttribute = new IdPAttribute("ByteAttribute");
        byteAttribute.setValues(Collections.singleton(new ByteAttributeValue(data)));

        final AttributeContext attributeCtx =
                prc.getSubcontext(RelyingPartyContext.class, true).getSubcontext(AttributeContext.class);
        attributeCtx.setIdPAttributes(Collections.singleton(byteAttribute));

        function = new AttributeValueLookupFunction("ByteAttribute");
        Assert.assertNull(function.apply(prc));
    }

    @Test public void testUseFilteredAttributes() {
        function = new AttributeValueLookupFunction("attribute4");
        function.setUseUnfilteredAttributes(false);
        Assert.assertNull(function.apply(prc));
    }

    @Test public void testUseUnfilteredAttributes() {
        function = new AttributeValueLookupFunction("attribute4");
        Assert.assertEquals(function.apply(prc), "value4");
    }
}
