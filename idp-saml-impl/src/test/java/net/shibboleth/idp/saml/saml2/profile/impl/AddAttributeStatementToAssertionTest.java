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

package net.shibboleth.idp.saml.saml2.profile.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringAttributeEncoder;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link AddAttributeStatementToAssertion} unit test. */
public class AddAttributeStatementToAssertionTest extends OpenSAMLInitBaseTestCase {

    /** The test namespace. */
    private final static String MY_NAMESPACE = "myNamespace";

    /** The name of the first attribute. */
    private final static String MY_NAME_1 = "myName1";

    /** The name of the second attribute. */
    private final static String MY_NAME_2 = "myName2";

    /** The second name of the first attribute. */
    private final static String MY_ALTNAME_1 = "myAltName1";

    /** The value of the first attribute. */
    private final static String MY_VALUE_1 = "myValue1";

    /** The value of the second attribute. */
    private final static String MY_VALUE_2 = "myValue2";
    
    private RequestContext rc;
    
    private ProfileRequestContext prc;
    
    private AddAttributeStatementToAssertion action;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        rc = new RequestContextBuilder().setOutboundMessage(
                SAML2ActionTestingSupport.buildResponse()).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        action = new AddAttributeStatementToAssertion();
    }

    /** Test that the action errors out properly if there is no relying party context. */
    @Test public void testNoRelyingPartyContext() throws Exception {
        prc.removeSubcontext(RelyingPartyContext.class);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }

    /** Test that the action errors out properly if there is no outbound context. */
    @Test public void testNoOutboundContext() throws Exception {
        prc.setOutboundMessageContext(null);

        final AttributeContext attribCtx = buildAttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attribCtx);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, EventIds.INVALID_MSG_CTX);
    }

    /** Test that the action continues properly if there is no attribute context. */
    @Test public void testNoAttributeContext() throws Exception {
        action.initialize();
        Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }

    /** Test that the action continues properly if the attribute context does not contain attributes. */
    @Test public void testNoAttributes() throws Exception {
        final AttributeContext attribCtx = new AttributeContext();
        prc.getSubcontext(RelyingPartyContext.class).addSubcontext(attribCtx);

        action.initialize();
        Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }

    /** Test that the action ignores attribute encoding errors. */
    @Test public void testIgnoreAttributeEncodingErrors() throws Exception {
        final MockSAML2StringAttributeEncoder attributeEncoder = new MockSAML2StringAttributeEncoder();

        final IdPAttribute attribute = new IdPAttribute(MY_NAME_1);
        attribute.setValues(Arrays.asList(new StringAttributeValue(MY_VALUE_1)));

        final Collection collection = Arrays.asList(attributeEncoder);
        attribute.setEncoders(collection);

        final AttributeContext attribCtx = new AttributeContext();
        attribCtx.setIdPAttributes(Arrays.asList(attribute));
        ((RelyingPartyContext) prc.getSubcontext(RelyingPartyContext.class)).addSubcontext(attribCtx);

        action.setIgnoringUnencodableAttributes(true);
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);
    }

    /** Test that the action returns the correct transition when an attribute encoding error occurs. */
    @Test public void failOnAttributeEncodingErrors() throws Exception {
        final MockSAML2StringAttributeEncoder attributeEncoder = new MockSAML2StringAttributeEncoder();

        final IdPAttribute attribute = new IdPAttribute(MY_NAME_1);
        attribute.setValues(Arrays.asList(new StringAttributeValue(MY_VALUE_1)));

        final Collection collection = Arrays.asList(attributeEncoder);
        attribute.setEncoders(collection);

        final AttributeContext attribCtx = new AttributeContext();
        attribCtx.setIdPAttributes(Arrays.asList(attribute));
        ((RelyingPartyContext) prc.getSubcontext(RelyingPartyContext.class)).addSubcontext(attribCtx);

        action.setIgnoringUnencodableAttributes(false);
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertEvent(result, IdPEventIds.UNABLE_ENCODE_ATTRIBUTE);
    }

    /**
     * Test that the attribute statement is correctly added as a new assertion of a response already containing an
     * assertion.
     */
    @Test public void testAddedAttributeStatement() throws Exception {

        ((Response) prc.getOutboundMessageContext().getMessage()).getAssertions().add(
                SAML2ActionTestingSupport.buildAssertion());

        final AttributeContext attribCtx = buildAttributeContext();
        ((RelyingPartyContext) prc.getSubcontext(RelyingPartyContext.class)).addSubcontext(attribCtx);

        action.setStatementInOwnAssertion(true);
        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);

        final Response response = (Response) prc.getOutboundMessageContext().getMessage();
        Assert.assertEquals(response.getAssertions().size(), 2);

        for (final Assertion assertion : response.getAssertions()) {
            if (!assertion.getAttributeStatements().isEmpty()) {
                Assert.assertNotNull(assertion.getAttributeStatements());
                Assert.assertEquals(assertion.getAttributeStatements().size(), 1);

                AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
                testAttributeStatement(attributeStatement);
            }
        }
    }

    /** Test that the attribute statement is correctly added to an assertion which already exists in the response. */
    @Test public void testAssertionInResponse() throws Exception {
        ((Response) prc.getOutboundMessageContext().getMessage()).getAssertions().add(
                SAML2ActionTestingSupport.buildAssertion());

        final AttributeContext attribCtx = buildAttributeContext();
        ((RelyingPartyContext) prc.getSubcontext(RelyingPartyContext.class)).addSubcontext(attribCtx);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);

        final Response response = (Response) prc.getOutboundMessageContext().getMessage();
        Assert.assertEquals(response.getAssertions().size(), 1);

        Assertion assertion = response.getAssertions().get(0);
        Assert.assertNotNull(assertion.getAttributeStatements());
        Assert.assertEquals(assertion.getAttributeStatements().size(), 1);

        final AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);

        testAttributeStatement(attributeStatement);
    }

    /**
     * Test that the attribute statement is correctly added to a newly created assertion of the response which
     * originally contained no assertions.
     */
    @Test public void testNoAssertionInResponse() throws Exception {
        final AttributeContext attribCtx = buildAttributeContext();
        ((RelyingPartyContext) prc.getSubcontext(RelyingPartyContext.class)).addSubcontext(attribCtx);

        action.initialize();
        final Event result = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(result);

        final Response response = (Response) prc.getOutboundMessageContext().getMessage();
        Assert.assertEquals(response.getAssertions().size(), 1);

        Assertion assertion = response.getAssertions().get(0);
        Assert.assertNotNull(assertion.getAttributeStatements());
        Assert.assertEquals(assertion.getAttributeStatements().size(), 1);

        final AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
        testAttributeStatement(attributeStatement);
    }

    /**
     * Build the attribute context containing two test attributes to be used as an input to the action.
     * 
     * @return the attribute context to be used as an input to the action
     * @throws ComponentInitializationException thrown if the attribute encoders can not be initialized
     */
    private AttributeContext buildAttributeContext() throws ComponentInitializationException {

        final IdPAttribute attribute1 = new IdPAttribute(MY_NAME_1);
        attribute1.setValues(Arrays.asList(new StringAttributeValue(MY_VALUE_1)));

        final SAML2StringAttributeEncoder attributeEncoder1 = new SAML2StringAttributeEncoder();
        attributeEncoder1.setName(MY_NAME_1);
        attributeEncoder1.setNameFormat(MY_NAMESPACE);
        attributeEncoder1.initialize();

        final SAML2StringAttributeEncoder attributeEncoder1_2 = new SAML2StringAttributeEncoder();
        attributeEncoder1_2.setName(MY_ALTNAME_1);
        attributeEncoder1_2.setNameFormat(MY_NAMESPACE);
        attributeEncoder1_2.initialize();

        final Collection collection1 = Arrays.asList(attributeEncoder1, attributeEncoder1_2);
        attribute1.setEncoders(collection1);

        final IdPAttribute attribute2 = new IdPAttribute(MY_NAME_2);
        attribute2.setValues(Collections.singleton(new StringAttributeValue(MY_VALUE_2)));

        final SAML2StringAttributeEncoder attributeEncoder2 = new SAML2StringAttributeEncoder();
        attributeEncoder2.setName(MY_NAME_2);
        attributeEncoder2.setNameFormat(MY_NAMESPACE);
        attributeEncoder2.initialize();

        final Collection collection2 = Arrays.asList(attributeEncoder2);
        attribute2.setEncoders(collection2);

        final AttributeContext attribCtx = new AttributeContext();
        attribCtx.setIdPAttributes(Arrays.asList(attribute1, attribute2));

        return attribCtx;
    }

    /**
     * Test that the attribute statement returned from the action is correct.
     * 
     * @param attributeStatement the attribute statement returned from the action to test
     */
    private void testAttributeStatement(AttributeStatement attributeStatement) {

        Assert.assertNotNull(attributeStatement.getAttributes());
        Assert.assertEquals(attributeStatement.getAttributes().size(), 3);

        boolean one = false, altone = false, two = false;
        
        for (final Attribute samlAttr : attributeStatement.getAttributes()) {
            Assert.assertNotNull(samlAttr.getAttributeValues());
            Assert.assertEquals(samlAttr.getAttributeValues().size(), 1);
            final XMLObject xmlObject = samlAttr.getAttributeValues().get(0);
            Assert.assertTrue(xmlObject instanceof XSStringImpl);
            if (samlAttr.getName().equals(MY_NAME_1)) {
                Assert.assertEquals(((XSStringImpl) xmlObject).getValue(), MY_VALUE_1);
                one = true;
            } else if (samlAttr.getName().equals(MY_NAME_2)) {
                Assert.assertEquals(((XSStringImpl) xmlObject).getValue(), MY_VALUE_2);
                altone = true;
            } else if (samlAttr.getName().equals(MY_ALTNAME_1)) {
                Assert.assertEquals(((XSStringImpl) xmlObject).getValue(), MY_VALUE_1);
                two = true;
            } else {
                Assert.fail("Incorrect attribute name.");
            }
        }

        
        if (!one || !altone || !two) {
            Assert.fail("Missing attribute");
        }
    }

    /** A mock SAML2 string attribute encoder which always throws an {@link AttributeEncodingException}. */
    private class MockSAML2StringAttributeEncoder extends SAML2StringAttributeEncoder {

        /** {@inheritDoc} */
        @Nullable public Attribute encode(@Nonnull final IdPAttribute attribute) throws AttributeEncodingException {
            throw new AttributeEncodingException("Always thrown.");
        }
    }

}