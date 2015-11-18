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

package net.shibboleth.idp.consent.flow.ar.impl;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.consent.context.impl.AttributeReleaseContext;
import net.shibboleth.idp.consent.flow.impl.AbstractConsentActionTest;
import net.shibboleth.idp.consent.impl.ConsentTestingSupport;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.testng.annotations.BeforeMethod;

/** {@link AbstractAttributeReleaseAction} unit test. */
public abstract class AbstractAttributeReleaseActionTest extends AbstractConsentActionTest {

    @BeforeMethod public void setUpAttributeReleaseAction() throws Exception {
        final AttributeContext attributeCtx = new AttributeContext();
        attributeCtx.setIdPAttributes(ConsentTestingSupport.newAttributeMap().values());
        prc.getSubcontext(RelyingPartyContext.class, true).addSubcontext(attributeCtx);

        prc.addSubcontext(new AttributeReleaseContext(), true);

        descriptor = new AttributeReleaseFlowDescriptor();
        descriptor.setId("test");
        prc.getSubcontext(ProfileInterceptorContext.class, false).setAttemptedFlow(descriptor);
    }
}
