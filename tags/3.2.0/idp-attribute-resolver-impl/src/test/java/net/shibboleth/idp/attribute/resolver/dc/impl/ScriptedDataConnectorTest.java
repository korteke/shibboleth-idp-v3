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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link ScriptedDataConnector}
 * 
 */
public class ScriptedDataConnectorTest {

    private boolean isV8() {
        final String ver = System.getProperty("java.version");
        return ver.startsWith("1.8");
    }

    private String getScript(String fileName) throws IOException {
        final String name;
        if (isV8()) {
            name = "/data/net/shibboleth/idp/attribute/resolver/impl/dc/v8/" + fileName;
        } else {
            name = "/data/net/shibboleth/idp/attribute/resolver/impl/dc/" + fileName;
        }
        return StringSupport.inputStreamToString(getClass().getResourceAsStream(name), null);
    }

    @Test public void simple() throws ComponentInitializationException, ResolutionException, ScriptException, IOException {

        final ScriptedDataConnector connector = new ScriptedDataConnector();
        connector.setId("Scripted");
        final EvaluableScript definitionScript = new EvaluableScript("javascript", getScript("scriptedConnector.js"));
        connector.setScript(definitionScript);

        connector.initialize();

        final AttributeResolutionContext context = new ProfileRequestContext<>().getSubcontext(AttributeResolutionContext.class,  true);
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        final Map<String, IdPAttribute> result = connector.resolve(context);

        Assert.assertEquals(result.size(), 3);
        
        List<IdPAttributeValue<?>> values = result.get("ScriptedOne").getValues();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains(new StringAttributeValue("Value 1")));
        Assert.assertTrue(values.contains(new StringAttributeValue("Value 2")));

        values = result.get("TwoScripted").getValues();
        Assert.assertEquals(values.size(), 3);
        Assert.assertTrue(values.contains(new StringAttributeValue("1Value")));
        Assert.assertTrue(values.contains(new StringAttributeValue("2Value")));
        Assert.assertTrue(values.contains(new StringAttributeValue("3Value")));

        values = result.get("ThreeScripted").getValues();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains(new StringAttributeValue(AttributeResolutionContext.class.getSimpleName())));

    }
    
    @Test public void custom() throws ComponentInitializationException, ResolutionException, ScriptException, IOException {

        final ScriptedDataConnector connector = new ScriptedDataConnector();
        connector.setId("Scripted");
        
        final IdPAttribute attribute = new IdPAttribute("attr");
        attribute.setValues((Collection)Collections.singleton((IdPAttributeValue)new StringAttributeValue("bar")));
        connector.setCustomObject(attribute);
        
        final EvaluableScript definitionScript = new EvaluableScript("javascript", getScript("custom.js"));
        connector.setScript(definitionScript);

        connector.initialize();

        final AttributeResolutionContext context = new ProfileRequestContext<>().getSubcontext(AttributeResolutionContext.class,  true);
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        final Map<String, IdPAttribute> result = connector.resolve(context);

        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(attribute.getId()),attribute);
    }


}
