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

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.saml.saml2.profile.SAML2ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import java.util.Collections;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ProcessRequestedAuthnContext} unit test. */
public class ProcessRequestedAuthnContextTest extends OpenSAMLInitBaseTestCase {

    private MockRequestContext src; 
    
    private ProfileRequestContext<AuthnRequest, Object> prc;
    
    private AuthenticationContext ac;
    
    private ProcessRequestedAuthnContext action;
    
    private SAMLObjectBuilder<RequestedAuthnContext> racBuilder;
    
    private SAMLObjectBuilder<AuthnContextClassRef> classBuilder;
    
    private SAMLObjectBuilder<AuthnContextDeclRef> declBuilder;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        racBuilder = (SAMLObjectBuilder<RequestedAuthnContext>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<RequestedAuthnContext>getBuilderOrThrow(
                        RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        classBuilder = (SAMLObjectBuilder<AuthnContextClassRef>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<AuthnContextClassRef>getBuilderOrThrow(
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        declBuilder = (SAMLObjectBuilder<AuthnContextDeclRef>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<AuthnContextDeclRef>getBuilderOrThrow(
                        AuthnContextDeclRef.DEFAULT_ELEMENT_NAME);
        
        src = (MockRequestContext) new RequestContextBuilder().buildRequestContext();
        prc = (ProfileRequestContext) src.getConversationScope().get(ProfileRequestContext.BINDING_KEY);
        ac = prc.getSubcontext(AuthenticationContext.class, true);
        
        action = new ProcessRequestedAuthnContext();
        action.initialize();
    }
    
    @Test public void testNoRequest() {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_MSG_CTX);
    }

    @Test public void testNoRAC() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(ac.getSubcontext(RequestedPrincipalContext.class, false));
    }
    
    @Test public void testEmptyRef() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        final AuthnContextClassRef ref = classBuilder.buildObject();
        rac.getAuthnContextClassRefs().add(ref);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(ac.getSubcontext(RequestedPrincipalContext.class, false));
    }

    @Test public void testNoOperator() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        final AuthnContextClassRef ref = classBuilder.buildObject();
        ref.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        rac.getAuthnContextClassRefs().add(ref);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, false); 
        Assert.assertNotNull(rpc);
        Assert.assertEquals(rpc.getOperator(), AuthnContextComparisonTypeEnumeration.EXACT.toString());
        Assert.assertEquals(rpc.getRequestedPrincipals().size(), 1);
        Assert.assertEquals(rpc.getRequestedPrincipals().get(0).getName(), AuthnContext.PPT_AUTHN_CTX);
    }

    @Test public void testOperator() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        rac.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextClassRef ref = classBuilder.buildObject();
        ref.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        rac.getAuthnContextClassRefs().add(ref);
        ref = classBuilder.buildObject();
        ref.setAuthnContextClassRef(AuthnContext.KERBEROS_AUTHN_CTX);
        rac.getAuthnContextClassRefs().add(ref);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, false); 
        Assert.assertNotNull(rpc);
        Assert.assertEquals(rpc.getOperator(), AuthnContextComparisonTypeEnumeration.MINIMUM.toString());
        Assert.assertEquals(rpc.getRequestedPrincipals().size(), 2);
    }

    @Test public void testDecls() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        rac.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextDeclRef ref = declBuilder.buildObject();
        ref.setAuthnContextDeclRef(AuthnContext.PPT_AUTHN_CTX);
        rac.getAuthnContextDeclRefs().add(ref);
        ref = declBuilder.buildObject();
        ref.setAuthnContextDeclRef(AuthnContext.KERBEROS_AUTHN_CTX);
        rac.getAuthnContextDeclRefs().add(ref);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, false); 
        Assert.assertNotNull(rpc);
        Assert.assertEquals(rpc.getOperator(), AuthnContextComparisonTypeEnumeration.MINIMUM.toString());
        Assert.assertEquals(rpc.getRequestedPrincipals().size(), 2);
    }

    @Test public void testIgnore() {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        final AuthnContextClassRef ref = classBuilder.buildObject();
        ref.setAuthnContextClassRef(AuthnContext.UNSPECIFIED_AUTHN_CTX);
        rac.getAuthnContextClassRefs().add(ref);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, false); 
        Assert.assertNull(rpc);
    }

    @Test public void testIgnore2() throws ComponentInitializationException {
        prc.getInboundMessageContext().setMessage(SAML2ActionTestingSupport.buildAuthnRequest());
        final RequestedAuthnContext rac = racBuilder.buildObject();
        prc.getInboundMessageContext().getMessage().setRequestedAuthnContext(rac);
        final AuthnContextClassRef ref = classBuilder.buildObject();
        ref.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        rac.getAuthnContextClassRefs().add(ref);

        action = new ProcessRequestedAuthnContext();
        action.setIgnoredContexts(Collections.singletonList(AuthnContext.PPT_AUTHN_CTX));
        action.initialize();
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, false); 
        Assert.assertNull(rpc);
    }
}