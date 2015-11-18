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

package net.shibboleth.idp.attribute.resolver.spring.dc;

import java.util.List;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.dc.impl.StaticDataConnector;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.dc.impl.StaticDataConnectorParser;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link StaticDataConnectorParser}
 */
public class StaticDataConnectorParserTest extends BaseAttributeDefinitionParserTest {
    
    @Test public void simple() {
        StaticDataConnector connector = getDataConnector("staticAttributes.xml", StaticDataConnector.class);
        
        Assert.assertEquals(connector.getAttributes().keySet().size(), 2);
        IdPAttribute epe = connector.getAttributes().get("eduPersonEntitlement");
        List<IdPAttributeValue<?>> values = epe.getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue("urn:example.org:entitlement:entitlement1")));
        Assert.assertTrue(values.contains(new StringAttributeValue("urn:mace:dir:entitlement:common-lib-terms")));
        
        values = connector.getAttributes().get("staticEpA").getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("member")));
    }

    @Test public void nativesimple() {
        StaticDataConnector connector = getDataConnector("staticAttributesNative.xml", StaticDataConnector.class);
        
        Assert.assertEquals(connector.getAttributes().keySet().size(), 2);
        IdPAttribute epe = connector.getAttributes().get("eduPersonEntitlement");
        List<IdPAttributeValue<?>> values = epe.getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue("urn:example.org:entitlement:entitlement1")));
        Assert.assertTrue(values.contains(new StringAttributeValue("urn:mace:dir:entitlement:common-lib-terms")));
        
        values = connector.getAttributes().get("staticEpA").getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue("member")));
    }
}
