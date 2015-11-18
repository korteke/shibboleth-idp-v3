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

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ad.impl.PrincipalNameAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link PrincipalNameAttributeDefinition}
 */
public class PrincipalNameAttributeDefinitionTest {

    @Test public void nullContext() throws ComponentInitializationException {
        PrincipalNameAttributeDefinition defn;
        defn = new PrincipalNameAttributeDefinition();
        defn.setId("id");
        defn.initialize();

        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        
        try {
            defn.resolve(context);
        } catch (ResolutionException e) {
            // OK
        }
    }

    @Test
    public void nullName() throws ComponentInitializationException{
        PrincipalNameAttributeDefinition defn;
        defn = new PrincipalNameAttributeDefinition();
        defn.setId("id");
        defn.initialize();
        
        try {
            defn.resolve(TestSources.createResolutionContext("", "issuer", "recipient"));
        } catch (ResolutionException e) {
            // OK
        }
        
    }

    @Test
    public void normal() throws ComponentInitializationException, ResolutionException{
        PrincipalNameAttributeDefinition defn;
        defn = new PrincipalNameAttributeDefinition();
        defn.setId("id");
        defn.initialize();
        
        IdPAttribute result = defn.resolve(TestSources.createResolutionContext("principal", "issuer", "recipient"));
        
        Assert.assertEquals(result.getValues().size(), 1);
        
        StringAttributeValue value = (StringAttributeValue) result.getValues().iterator().next();
        Assert.assertEquals(value.getValue(), "principal");

        
    }
}