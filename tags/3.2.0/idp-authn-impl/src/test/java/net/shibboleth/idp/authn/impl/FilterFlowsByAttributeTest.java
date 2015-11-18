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

package net.shibboleth.idp.authn.impl;

import java.util.Arrays;
import java.util.Collections;

import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.TestPrincipal;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link FilterFlowsByAttribute} unit test. */
public class FilterFlowsByAttributeTest extends PopulateAuthenticationContextTest {
    
    private FilterFlowsByAttribute action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new FilterFlowsByAttribute();
        action.setAttributeId("foo");
        
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        final AuthenticationResult active = new AuthenticationResult("test3", new Subject());
        active.getSubject().getPrincipals().add(new TestPrincipal("test3"));
        authCtx.setActiveResults(Arrays.asList(active));
    }

    @Test public void testNoAttributeID() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        
        action = new FilterFlowsByAttribute();
        action.initialize();
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 3);
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
    }

    @Test public void testNoAttribute() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        
        action.initialize();
        Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 3);
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
        
        authCtx.getSubcontext(AttributeContext.class, true).setIdPAttributes(
                Collections.singletonList(new IdPAttribute("foo")));
        event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 3);
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
    }
    
    @Test public void testNoMatch() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        final IdPAttribute attr = new IdPAttribute("foo");
        authCtx.getSubcontext(AttributeContext.class, true).setIdPAttributes(Collections.singletonList(attr));
        attr.setValues(Collections.singleton(new StringAttributeValue("bar")));
        
        action.setFilterActiveResults(false);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 0);
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
    }

    @Test public void testMatch() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        final IdPAttribute attr = new IdPAttribute("foo");
        authCtx.getSubcontext(AttributeContext.class, true).setIdPAttributes(Collections.singletonList(attr));
        attr.setValues(Collections.singleton(new StringAttributeValue("bar")));
        
        authCtx.getPotentialFlows().get("test1").getSupportedPrincipals().add(new TestPrincipal("baz"));
        authCtx.getPotentialFlows().get("test2").getSupportedPrincipals().add(new TestPrincipal("bar"));
        authCtx.getPotentialFlows().get("test3").getSupportedPrincipals().add(new TestPrincipal("bay"));
        
        action.setFilterActiveResults(false);
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 1);
        Assert.assertEquals(authCtx.getPotentialFlows().entrySet().iterator().next().getValue().getId(), "test2");
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
    }
    
    @Test public void testMatchActive() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        final IdPAttribute attr = new IdPAttribute("foo");
        authCtx.getSubcontext(AttributeContext.class, true).setIdPAttributes(Collections.singletonList(attr));
        attr.setValues(Collections.singleton(new StringAttributeValue("test3")));
        
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 0);
        Assert.assertEquals(authCtx.getActiveResults().size(), 1);
    }

    @Test public void testNoMatchActive() throws ComponentInitializationException {
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class);
        final IdPAttribute attr = new IdPAttribute("foo");
        authCtx.getSubcontext(AttributeContext.class, true).setIdPAttributes(Collections.singletonList(attr));
        attr.setValues(Collections.singleton(new StringAttributeValue("test2")));
        
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(authCtx.getPotentialFlows().size(), 0);
        Assert.assertEquals(authCtx.getActiveResults().size(), 0);
    }
    
}