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

package net.shibboleth.idp.profile.context.navigate;

import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class ScriptedFunctionTest {
    
    static final String STRING_RETURN_7 = "new java.lang.String(\"String\");";
    static final String STRING_RETURN_8 = "JavaString=Java.type(\"java.lang.String\"); new JavaString(\"String\");";
    static final String INTEGER_RETURN_7 = "new java.lang.Integer(37);";
    static final String INTEGER_RETURN_8 = "JavaInteger=Java.type(\"java.lang.Integer\"); new JavaInteger(37);";
    
    private boolean isV8() {
        final String ver = System.getProperty("java.version");
        return ver.startsWith("1.8");
    }

    private String stringReturn() {
        if (isV8()) {
            return STRING_RETURN_8;
        }
        return STRING_RETURN_7;
    }
    
    private String integerReturn() {
        if (isV8()) {
            return INTEGER_RETURN_8;
        }
        return INTEGER_RETURN_7;
    }

    
    @Test public void simpleScript() throws ScriptException {
        ProfileRequestContext prc = new ProfileRequestContext<>();
        
        final Object string = ScriptedContextLookupFunction.inlineScript(stringReturn()).apply(prc);

        String s = (String) string;
        Assert.assertEquals(s, "String");
        
        final Integer integer = (Integer) ScriptedContextLookupFunction.inlineScript(integerReturn()).apply(prc);
        Assert.assertEquals(integer.intValue(), 37);
    }
    
    @Test public void custom() throws ScriptException {
        ProfileRequestContext prc = new ProfileRequestContext<>();
        
        final ScriptedContextLookupFunction script = ScriptedContextLookupFunction.inlineScript("custom;");
        script.setCustomObject("String");
        Assert.assertEquals(script.apply(prc), "String");
 
        script.setCustomObject(new Integer(37));
        Assert.assertEquals(script.apply(prc), new Integer(37));
    }    
    
    
    @Test public void withType() throws ScriptException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ProfileRequestContext prc = new ProfileRequestContext<>();

        final ScriptedContextLookupFunction script1 = ScriptedContextLookupFunction.inlineScript(stringReturn(), Object.class);
        
        final String string = (String) script1.apply(prc);
        Assert.assertEquals(string, "String");
        
        Assert.assertEquals(ScriptedContextLookupFunction.inlineScript(stringReturn(), String.class).apply(prc), "String");
        
        Assert.assertNull(ScriptedContextLookupFunction.inlineScript(stringReturn(), Integer.class).apply(prc));
        
        final Integer integer = (Integer) ScriptedContextLookupFunction.inlineScript(integerReturn()).apply(prc);
        Assert.assertEquals(integer.intValue(), 37);
        
    }
    
    @Test(expectedExceptions={ClassCastException.class,}) public void wrongType() throws ScriptException {
        final ScriptedContextLookupFunction script1 = ScriptedContextLookupFunction.inlineScript(stringReturn(), Object.class);
        
        script1.apply(new MessageContext<>());
        
    }

    @Test public void messageContext() throws ScriptException {
        final ScriptedContextLookupFunction<MessageContext> script1 = ScriptedContextLookupFunction.inlineMessageContextScript(stringReturn(), Object.class);
        
        Assert.assertEquals(script1.apply(new MessageContext<>()), "String");
        Assert.assertEquals(script1.apply(null), "String");
    }
}
