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
import java.util.Locale;
import java.util.Map;

import net.shibboleth.idp.consent.flow.impl.ConsentFlowDescriptor;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link MessageSourceConsentFunction} unit test. */
public class MessageSourceConsentFunctionTest {

    private RequestContext src;

    private ProfileRequestContext prc;

    private MessageSource messageSource;

    private HashFunction hashFunction;

    private MessageSourceConsentFunction function;

    @BeforeMethod public void setUp() throws Exception {
        src = new RequestContextBuilder().buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(src);

        messageSource = new MockMessageSource();

        hashFunction = new HashFunction();

        function = new MessageSourceConsentFunction();
        function.setMessageSource(messageSource);
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

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullIdMessageCode() throws Exception {
        function.setConsentKeyLookupStrategy(null);
        function.initialize();
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testNullValueMessageCode()
            throws Exception {
        function.setConsentValueMessageCodeSuffix(null);
        function.initialize();
    }

    @Test(expectedExceptions = ConstraintViolationException.class) public void testEmptyValueMessageCode()
            throws Exception {
        function.setConsentValueMessageCodeSuffix("");
        function.initialize();
    }

    @Test(expectedExceptions = UnmodifiableComponentException.class) public void testInstantiationIdMessageCode()
            throws Exception {
        function.setConsentKeyLookupStrategy(FunctionSupport.<ProfileRequestContext,String>constant("consentIdMessageCode"));
        function.initialize();

        function.setConsentKeyLookupStrategy(FunctionSupport.<ProfileRequestContext,String>constant("consentIdMessageCode"));
    }

    @Test public void testMessageSourceConsent() throws Exception {

        setUpDescriptor(false);

        final Consent consent = new Consent();
        consent.setId("id");

        final Map<String, Consent> expected = new HashMap<>();
        expected.put(consent.getId(), consent);

        function.setConsentKeyLookupStrategy(FunctionSupport.<ProfileRequestContext,String>constant("key"));
        function.initialize();

        Assert.assertEquals(function.apply(prc), expected);
    }

    @Test public void testMessageSourceConsentCompareValues() throws Exception {

        setUpDescriptor(true);

        final Consent consent = new Consent();
        consent.setId("id");
        consent.setValue(hashFunction.apply("value"));

        final Map<String, Consent> expected = new HashMap<>();
        expected.put(consent.getId(), consent);

        function.setConsentKeyLookupStrategy(FunctionSupport.<ProfileRequestContext,String>constant("key"));
        function.initialize();

        Assert.assertEquals(function.apply(prc), expected);
    }

    private class MockMessageSource implements MessageSource {

        /** {@inheritDoc} */
        public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
            if (code.equals("key")) {
                return "id";
            } else if (code.equals("id.text")) {
                return "value";
            } else {
                return defaultMessage;
            }
        }

        /** {@inheritDoc} */
        public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
            if (code.equals("key")) {
                return "id";
            } else if (code.equals("id.text")) {
                return "value";
            }
            throw new NoSuchMessageException("No such message");
        }

        /** {@inheritDoc} */
        public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
            if (resolvable.getCodes()[0].equals("key")) {
                return "id";
            } else if (resolvable.getCodes()[0].equals("id.text")) {
                return "value";
            }
            throw new NoSuchMessageException("No such message");
        }
    }

}