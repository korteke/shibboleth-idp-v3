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

package net.shibboleth.idp.profile.impl;

import net.shibboleth.idp.profile.RequestContextBuilder;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.test.MockRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link InitializeProfileRequestContext}. */
public class InitializeProfileRequestContextTest {

    @Test public void testExecute() throws Exception {

        MockRequestContext springRequestContext = new MockRequestContext();
        springRequestContext.setExternalContext(new RequestContextBuilder().buildServletExternalContext());

        InitializeProfileRequestContext action = new InitializeProfileRequestContext();
        action.setProfileId("test");
        action.initialize();

        action.execute(springRequestContext);

        ProfileRequestContext prc =
                (ProfileRequestContext) springRequestContext.getConversationScope().get(ProfileRequestContext.BINDING_KEY);
        Assert.assertNotNull(prc);
        Assert.assertEquals(prc.getProfileId(), "test");
    }
    
}