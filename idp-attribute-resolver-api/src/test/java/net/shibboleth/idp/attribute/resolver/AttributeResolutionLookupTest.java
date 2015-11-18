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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.navigate.AbstractAttributeResolutionLookupFunction;

import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AbstractAttributeResolutionLookupFunction}.
 */
public class AttributeResolutionLookupTest {
    
    @Test public void apply() {
        final ProfileRequestContext pfc = new ProfileRequestContext<>();
        pfc.getSubcontext(AttributeResolutionContext.class, true).setAttributeIssuerID("child");

        final TestClass what = new TestClass();
        Assert.assertEquals(what.apply(pfc), "child");
    }
    
    @Test public void applyParent() {
        final AttributeResolutionContext parent = new AttributeResolutionContext();
        final ProfileRequestContext pfc = parent.getSubcontext(ProfileRequestContext.class, true);
        parent.setAttributeIssuerID("parent");

        final TestClass what = new TestClass();
        what.setAttributeResolutionContextLookupStrategy(new ParentContextLookup<ProfileRequestContext, AttributeResolutionContext>());        
        Assert.assertEquals(what.apply(pfc), "parent");
    }

    
    private class TestClass extends AbstractAttributeResolutionLookupFunction<String> {
        /** {@inheritDoc} */
        @Override
        @Nullable protected String doApply(@Nonnull final AttributeResolutionContext input) {
            return input.getAttributeIssuerID();
        }
    }
}
