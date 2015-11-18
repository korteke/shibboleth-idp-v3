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

package net.shibboleth.idp.attribute.resolver.dc.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;

/**
 * test for the {@link SAMLAttributeDataConnector}
 */
public class SAMLAttributeDataConnectorTest extends XMLObjectBaseTestCase {

    static final private String PATH =  "/data/net/shibboleth/idp/attribute/resolver/impl/dc/entityAttributes.xml";

    @Test public void connector() throws ResolutionException, ComponentInitializationException {
        
        Locator entityLocator = new Locator(PATH);
        
        SAMLAttributeDataConnector connector = new SAMLAttributeDataConnector();
        connector.setAttributesStrategy(entityLocator);
        connector.setId(PATH);
        connector.initialize();
    
        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        final Map<String, IdPAttribute> attributes = connector.resolve(context);
        Assert.assertEquals(attributes.size(), 2);
        
        List<IdPAttributeValue<?>> attributeValues = attributes.get("SamlName").getValues();
        Assert.assertEquals(attributeValues.size(), 1);
        Assert.assertTrue(attributeValues.iterator().next() instanceof XMLObjectAttributeValue);
        
        attributeValues = attributes.get("MultiName").getValues();
        Assert.assertEquals(attributeValues.size(), 3);
        for (IdPAttributeValue val: attributeValues) {
            Assert.assertTrue(val instanceof StringAttributeValue);    
        }
    }

    final class Locator implements Function<AttributeResolutionContext, List<Attribute>> {

        final EntityAttributes obj;

        public Locator(String path) {
            obj = (EntityAttributes) unmarshallElement(path);
        }

        /** {@inheritDoc} */
        @Nullable public List<Attribute> apply(@Nullable AttributeResolutionContext input) {
            return obj.getAttributes();
        }

    }
}