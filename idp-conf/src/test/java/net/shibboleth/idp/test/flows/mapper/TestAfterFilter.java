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

package net.shibboleth.idp.test.flows.mapper;

import javax.annotation.Nonnull;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.context.RelyingPartyContext;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;

/**
 *
 */
public class TestAfterFilter extends AbstractProfileAction {
    
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext) {

        RelyingPartyContext rpc = profileRequestContext.getSubcontext(RelyingPartyContext.class);
        
        AttributeContext ac = rpc.getSubcontext(AttributeContext.class);
        
        Assert.assertNotNull(ac.getIdPAttributes().get("IdPRA1"));
        Assert.assertFalse(ac.getIdPAttributes().get("IdPRA1").getValues().isEmpty());
        Assert.assertNull(ac.getIdPAttributes().get("IdPEA1"));
        
        Assert.assertNotNull(ac.getIdPAttributes().get("IdPOK"));
        Assert.assertFalse(ac.getIdPAttributes().get("IdPOK").getValues().isEmpty());
        Assert.assertNull(ac.getIdPAttributes().get("IdPNotOK"));
                
    }
}
