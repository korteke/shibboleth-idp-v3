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

import java.util.Collections;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.IdPEventIds;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.context.navigate.WebflowRequestContextProfileRequestContextLookup;
import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.profile.context.EncryptionContext;
import org.opensaml.xmlsec.EncryptionParameters;
import org.opensaml.xmlsec.EncryptionParametersResolver;
import org.opensaml.xmlsec.criterion.EncryptionConfigurationCriterion;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link PopulateEncryptionParameters}. */
public class PopulateEncryptionParametersTest extends OpenSAMLInitBaseTestCase {

    private RequestContext rc;
    
    private ProfileRequestContext prc;
    
    private PopulateEncryptionParameters action;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        rc = new RequestContextBuilder().setRelyingPartyProfileConfigurations(
                Collections.<ProfileConfiguration>singletonList(new BrowserSSOProfileConfiguration())).buildRequestContext();
        prc = new WebflowRequestContextProfileRequestContextLookup().apply(rc);
        
        action = new PopulateEncryptionParameters();
    }
    
    @Test(expectedExceptions=ComponentInitializationException.class)
    public void testConfig() throws ComponentInitializationException {
        action.initialize();
    }
    
    @Test public void testNoContext() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(false));
        action.initialize();
        
        prc.removeSubcontext(RelyingPartyContext.class);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_RELYING_PARTY_CTX);
    }

    @Test public void testNoConfig() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(false));
        action.initialize();
        
        prc.getSubcontext(RelyingPartyContext.class).setProfileConfig(null);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, IdPEventIds.INVALID_PROFILE_CONFIG);
    }

    @Test public void testWrongConfig() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(false));
        action.initialize();
        
        prc.getSubcontext(RelyingPartyContext.class).setProfileConfig(
                new net.shibboleth.idp.saml.saml1.profile.config.BrowserSSOProfileConfiguration());
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
    }
    
    @Test public void testResolverError() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(true));
        action.initialize();
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_SEC_CFG);
    }    

    @Test public void testSuccess() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(false));
        action.initialize();
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        
        final EncryptionContext ctx = prc.getSubcontext(RelyingPartyContext.class).getSubcontext(EncryptionContext.class); 
        Assert.assertNotNull(ctx);
        Assert.assertNotNull(ctx.getAssertionEncryptionParameters());
        Assert.assertNull(ctx.getIdentifierEncryptionParameters());
        Assert.assertNull(ctx.getAttributeEncryptionParameters());
    }

    @Test public void testOptional() throws Exception {
        action.setEncryptionParametersResolver(new MockResolver(true));
        action.initialize();
        
        ((BrowserSSOProfileConfiguration) prc.getSubcontext(
                RelyingPartyContext.class).getProfileConfig()).setEncryptionOptional(true);
        
        final Event event = action.execute(rc);
        ActionTestingSupport.assertProceedEvent(event);
        
        final EncryptionContext ctx = prc.getSubcontext(RelyingPartyContext.class).getSubcontext(EncryptionContext.class); 
        Assert.assertNotNull(ctx);
        Assert.assertNull(ctx.getAssertionEncryptionParameters());
        Assert.assertNull(ctx.getIdentifierEncryptionParameters());
        Assert.assertNull(ctx.getAttributeEncryptionParameters());
    }
    
    private class MockResolver implements EncryptionParametersResolver {

        private boolean throwException;
        
        public MockResolver(final boolean shouldThrow) {
            throwException = shouldThrow;
        }
        
        /** {@inheritDoc} */
        @Override
        public Iterable<EncryptionParameters> resolve(CriteriaSet criteria) throws ResolverException {
            return Collections.singletonList(resolveSingle(criteria));
        }

        /** {@inheritDoc} */
        @Override
        public EncryptionParameters resolveSingle(CriteriaSet criteria) throws ResolverException {
            if (throwException) {
                throw new ResolverException();
            }
            
            Constraint.isNotNull(criteria.get(EncryptionConfigurationCriterion.class), "Criterion was null");
            return new EncryptionParameters();
        }
        
    }
    
}