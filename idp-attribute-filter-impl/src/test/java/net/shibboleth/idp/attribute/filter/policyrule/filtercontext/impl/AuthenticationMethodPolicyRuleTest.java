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

package net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AuthenticationMethodPolicyRule}.
 */
public class AuthenticationMethodPolicyRuleTest {

    protected static String METHOD = "AuthnMethod";
    
    private AuthenticationMethodPolicyRule getMatcher() throws ComponentInitializationException {
        return getMatcher(true);
    }
    
    protected static AttributeFilterContext filterContextWithAuthn(String authn) {
        
        AttributeFilterContext context = new AttributeFilterContext();
        context.setPrincipalAuthenticationMethod(authn);
        return context;
    }
    
    private AuthenticationMethodPolicyRule getMatcher(boolean caseSensitive) throws ComponentInitializationException {
        final AuthenticationMethodPolicyRule matcher = new AuthenticationMethodPolicyRule();
        matcher.setMatchString(METHOD);
        matcher.setIgnoreCase(!caseSensitive);
        matcher.setId("Test");
        matcher.initialize();
        return matcher;
    }

    @Test public void testNull() throws ComponentInitializationException {

        try {
            new AuthenticationMethodPolicyRule().matches(null);
            Assert.fail();
        } catch (UninitializedComponentException ex) {
            // OK
        }
    }

    @Test public void testUnpopulated()
            throws ComponentInitializationException {
        Assert.assertEquals(getMatcher().matches(DataSources.unPopulatedFilterContext()), Tristate.FAIL);
    }

    @Test public void testNoIssuer()
            throws ComponentInitializationException {
        Assert.assertEquals(getMatcher().matches(filterContextWithAuthn(null)), Tristate.FAIL);
    }

    @Test public void testCaseSensitive() throws ComponentInitializationException {

        final AuthenticationMethodPolicyRule matcher = getMatcher();

        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD+METHOD)), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD.toLowerCase())), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD)), Tristate.TRUE);
    }

    @Test public void testCaseInsensitive() throws ComponentInitializationException {

        final AuthenticationMethodPolicyRule matcher = getMatcher(false);

        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD+METHOD)), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD.toLowerCase())), Tristate.TRUE);
        Assert.assertEquals(matcher.matches(filterContextWithAuthn(METHOD)), Tristate.TRUE);
    }
}