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
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.dc.impl.ScriptedDataConnector;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.dc.impl.ScriptDataConnectorParser;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link ScriptDataConnectorParser}
 */
public class ScriptDataConnectorParserTest extends BaseAttributeDefinitionParserTest {
    
    private boolean isV8() {
        final String ver = System.getProperty("java.version");
        return ver.startsWith("1.8");
    }

    @Test public void simple() throws ComponentInitializationException, ResolutionException {
        final String source;
        if (isV8()) {
            source = "scriptedAttributes-8.xml";
        } else {
            source = "scriptedAttributes.xml";
        } 
        ScriptedDataConnector dataConnector = getDataConnector(source, ScriptedDataConnector.class);
        dataConnector.initialize();
        
        final Map custom = (Map) dataConnector.getCustomObject();
        
        Assert.assertEquals(custom.size(), 1);
        Assert.assertEquals(custom.get("bar"), "foo");

        
        AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);
        Map<String, IdPAttribute> result = dataConnector.resolve(context);
        
        Assert.assertEquals(result.size(), 2);
        
        List<IdPAttributeValue<?>> values = result.get("ScriptedOne").getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue("Value 1")));
        Assert.assertTrue(values.contains(new StringAttributeValue("Value 2")));

        values = result.get("TwoScripted").getValues();
        Assert.assertEquals(values.size(), 3);
        Assert.assertTrue(values.contains(new StringAttributeValue("1Value")));
        Assert.assertTrue(values.contains(new StringAttributeValue("2Value")));
        Assert.assertTrue(values.contains(new StringAttributeValue("3Value")));
        
    }

}
