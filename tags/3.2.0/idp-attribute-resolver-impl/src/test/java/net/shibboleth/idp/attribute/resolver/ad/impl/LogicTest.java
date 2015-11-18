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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.Collections;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.logic.AttributeIssuerIdPredicate;
import net.shibboleth.idp.attribute.resolver.logic.AttributePrincipalPredicate;
import net.shibboleth.idp.attribute.resolver.logic.AttributeRecipientIdPredicate;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * Test for {@link AttributeIssuerIdPredicate}, {@link AttributePrincipalPredicate} and {@link AttributeRecipientIdPredicate}.
 */
public class LogicTest {

    final private AttributeIssuerIdPredicate aiip = new AttributeIssuerIdPredicate(Collections.singleton("AII"));
    final private AttributePrincipalPredicate app = new AttributePrincipalPredicate(Lists.newArrayList("AP", "Principal"));
    final private AttributeRecipientIdPredicate arip = new AttributeRecipientIdPredicate(Collections.singleton("ARI"));
    

    @Test public void empty() {
        final ProfileRequestContext pfc = new ProfileRequestContext<>();
        
        Assert.assertFalse(aiip.apply(pfc));
        Assert.assertFalse(app.apply(pfc));
        Assert.assertFalse(arip.apply(pfc));

        pfc.getSubcontext(AttributeResolutionContext.class, true);
        Assert.assertFalse(aiip.apply(pfc));
        Assert.assertFalse(app.apply(pfc));
        Assert.assertFalse(arip.apply(pfc));
    }
    
    @Test public void wrong() {
        final ProfileRequestContext pfc = new ProfileRequestContext<>();
        final AttributeResolutionContext arc = pfc.getSubcontext(AttributeResolutionContext.class, true);
        
        arc.setAttributeIssuerID("badValue");
        arc.setPrincipal("badValue");
        arc.setAttributeRecipientID("badValue");
        
        Assert.assertFalse(aiip.apply(pfc));
        Assert.assertFalse(app.apply(pfc));
        Assert.assertFalse(arip.apply(pfc));
    }

    @Test public void correct() {
        final ProfileRequestContext pfc = new ProfileRequestContext<>();
        final AttributeResolutionContext arc = pfc.getSubcontext(AttributeResolutionContext.class, true);
        
        arc.setAttributeIssuerID("AII");
        arc.setPrincipal("Principal");
        arc.setAttributeRecipientID("ARI");
        
        Assert.assertTrue(aiip.apply(pfc));
        Assert.assertTrue(app.apply(pfc));
        Assert.assertTrue(arip.apply(pfc));
    }
}
