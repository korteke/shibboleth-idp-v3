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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.saml.profile.impl.ReloadMetadata;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.service.ReloadableService;

import org.opensaml.profile.action.EventIds;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReloadMetadataTest extends AbstractMetadataParserTest {

    private ReloadableService<MetadataResolver> service;

    private ReloadableService<MetadataResolver> chainingservice;

    private RequestContext src;
    
    @BeforeClass public void setup() throws IOException {
        service = getBean(ReloadableService.class, "../reload/beans.xml");
        chainingservice = getBean(ReloadableService.class, "../reload/chainingbeans.xml");
    }
    
    @BeforeMethod public void setUpAction() throws ComponentInitializationException {
        src = new RequestContextBuilder().buildRequestContext();
    }
    
    @Test public void noResponse() throws ComponentInitializationException {
        
        final ReloadMetadata action = new ReloadMetadata();
        action.setMetadataResolver(service);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    @Test public void serviceNotSpecified() throws ComponentInitializationException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final ReloadMetadata action = new ReloadMetadata();
        action.setMetadataResolver(service);
        action.setHttpServletRequest(request);
        action.setHttpServletResponse(response);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test public void serviceNotFound() throws ComponentInitializationException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final ReloadMetadata action = new ReloadMetadata();
        action.setMetadataResolver(service);
        action.setHttpServletRequest(request);
        action.setHttpServletResponse(response);
        action.initialize();

        request.setParameter(ReloadMetadata.RESOLVER_ID, "foo");
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND);
    }
    
    @Test public void serviceAction() throws ComponentInitializationException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setParameter(ReloadMetadata.RESOLVER_ID, "fileEntity");
        
        final ReloadMetadata action = new ReloadMetadata();
        action.setHttpServletRequest(request);
        action.setHttpServletResponse(response);
        action.setMetadataResolver(service);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test public void chainingAction() throws ComponentInitializationException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setParameter(ReloadMetadata.RESOLVER_ID, "fileEntity2");
        
        final ReloadMetadata action = new ReloadMetadata();
        action.setHttpServletRequest(request);
        action.setHttpServletResponse(response);
        action.setMetadataResolver(chainingservice);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);

        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
    }

    @AfterClass public void teardown() {
        ComponentSupport.destroy(service);
        ComponentSupport.destroy(chainingservice);
    }

}