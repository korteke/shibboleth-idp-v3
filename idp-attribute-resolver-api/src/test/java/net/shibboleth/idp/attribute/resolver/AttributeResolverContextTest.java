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

package net.shibboleth.idp.attribute.resolver;

import java.util.Collections;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/** Unit test for {@link AttributeResolutionContext}. */
public class AttributeResolverContextTest {

    /** Test instantiation and post-instantiation state. */
    @Test public void gettersSetters() {

        AttributeResolutionContext context = new AttributeResolutionContext();
        Assert.assertNull(context.getAttributeIssuerID());
        Assert.assertNull(context.getAttributeRecipientID());
        Assert.assertNull(context.getPrincipalAuthenticationMethod());
        Assert.assertTrue(context.getResolvedIdPAttributes().isEmpty());
        Assert.assertTrue(context.getRequestedIdPAttributeNames().isEmpty());
        
        context.setAttributeIssuerID("AIID");
        Assert.assertEquals(context.getAttributeIssuerID(), "AIID");
        
        context.setAttributeRecipientID("ARID");
        Assert.assertEquals(context.getAttributeRecipientID(), "ARID");
        
        context.setPrincipalAuthenticationMethod("PAM");
        Assert.assertEquals(context.getPrincipalAuthenticationMethod(), "PAM");
        
        final IdPAttribute attr = new IdPAttribute("AttrId");
        context.setResolvedIdPAttributes(Collections.singleton(attr));
        Assert.assertEquals(context.getResolvedIdPAttributes().size(), 1);
        Assert.assertSame(context.getResolvedIdPAttributes().values().iterator().next(), attr);
                
        context.setRequestedIdPAttributeNames(Lists.newArrayList("Foo", null, "bar"));
        Assert.assertEquals(context.getRequestedIdPAttributeNames().size(), 2);
        Assert.assertTrue(context.getRequestedIdPAttributeNames().contains("Foo"));
        Assert.assertTrue(context.getRequestedIdPAttributeNames().contains("bar"));
    }

}