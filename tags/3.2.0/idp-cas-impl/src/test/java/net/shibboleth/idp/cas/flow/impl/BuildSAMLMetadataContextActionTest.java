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

package net.shibboleth.idp.cas.flow.impl;

import java.util.List;

import com.google.common.base.Function;
import net.shibboleth.idp.cas.config.impl.LoginConfiguration;
import net.shibboleth.idp.cas.service.Service;
import net.shibboleth.idp.saml.profile.context.navigate.SAMLMetadataContextLookupFunction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.metadata.EntityGroupName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link BuildSAMLMetadataContextAction}.
 *
 */
public class BuildSAMLMetadataContextActionTest extends AbstractFlowActionTest {

    @Autowired
    private BuildSAMLMetadataContextAction action;

    private Function<ProfileRequestContext, SAMLMetadataContext> mdLookupFunction =
            new SAMLMetadataContextLookupFunction();

    @Test
    public void testServiceWithGroup() throws Exception {
        final Service service = new Service("https://service-1.example.org/", "group-1", true);
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addRelyingPartyContext(service.getName(), true, null)
                .addServiceContext(service)
                .build();
        assertNull(action.execute(context));
        final SAMLMetadataContext mdc = mdLookupFunction.apply(getProfileContext(context));
        assertNotNull(mdc);
        final List<EntityGroupName> groups = mdc.getEntityDescriptor().getObjectMetadata().get(EntityGroupName.class);
        assertEquals(groups.size(), 1);
        assertEquals(groups.get(0).getName(), service.getGroup());
    }

    @Test
    public void testServiceWithoutGroup() throws Exception {
        final Service service = new Service("https://service-2.example.org/", null, true);
        final RequestContext context = new TestContextBuilder(LoginConfiguration.PROFILE_ID)
                .addRelyingPartyContext(service.getName(), true, null)
                .addServiceContext(service)
                .build();
        assertNull(action.execute(context));
        final SAMLMetadataContext mdc = mdLookupFunction.apply(getProfileContext(context));
        assertNotNull(mdc);
        final List<EntityGroupName> groups = mdc.getEntityDescriptor().getObjectMetadata().get(EntityGroupName.class);
        assertTrue(groups.isEmpty());
    }
}