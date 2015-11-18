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

package net.shibboleth.idp.attribute.filter.matcher.impl;

import java.util.Collections;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.profile.context.ProfileRequestContext;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link ScriptedMatcher} unit test. */
@ThreadSafe
public class ScriptedMatcherTest extends AbstractMatcherPolicyRuleTest {

    /** A script that returns a set that contains the one of values the attribute. */
    private EvaluableScript returnOneValueScript;

    /** A script that returns null. */
    private EvaluableScript nullReturnScript;

    /** A script that returns an object other than a set. */
    private EvaluableScript invalidReturnObjectScript;

    /** A script that returns a set contain values that were not attribute values. */
    private EvaluableScript addedValuesScript;

    /** A script that returns a set containing the prc name. */
    private EvaluableScript prcScript;

    private boolean isV8() {
        final String ver = System.getProperty("java.version");
        return ver.startsWith("1.8");
    }

    @BeforeTest public void setup() throws Exception {
        super.setUp();

        filterContext = new AttributeFilterContext();

        nullReturnScript = new EvaluableScript("JavaScript", "null;");

        if (!isV8()) {
            returnOneValueScript =
                    new EvaluableScript("JavaScript", new StringBuilder().append("importPackage(Packages.java.util);")
                            .append("filterContext.getPrefilteredIdPAttributes();").append("x = new HashSet();")
                            .append("x.add(attribute.getValues().iterator().next());").append("x;").toString());

            invalidReturnObjectScript = new EvaluableScript("JavaScript", "new java.lang.String();");

            addedValuesScript =
                    new EvaluableScript("JavaScript", new StringBuilder().append("importPackage(Packages.java.util);")
                            .append("x = new HashSet();").append("x.add(attribute.getValues().iterator().next());")
                            .append("x.add(new net.shibboleth.idp.attribute.StringAttributeValue(\"a\"));")
                            .append("x;").toString());
            prcScript =
                    new EvaluableScript(
                            "JavaScript",
                            new StringBuilder()
                                    .append("x = new java.util.HashSet(1);\n")
                                    .append("x.add(new net.shibboleth.idp.attribute.StringAttributeValue(profileContext.getClass().getName()));\n")
                                    .append("x;").toString());
        } else {

            returnOneValueScript =
                    new EvaluableScript("JavaScript", new StringBuilder()
                            .append("load('nashorn:mozilla_compat.js');importPackage(Packages.java.util);")
                            .append("filterContext.getPrefilteredIdPAttributes();").append("x = new HashSet();")
                            .append("x.add(attribute.getValues().iterator().next());").append("x;").toString());

            invalidReturnObjectScript =
                    new EvaluableScript("JavaScript", "load('nashorn:mozilla_compat.js');new java.lang.String();");

            addedValuesScript =
                    new EvaluableScript("JavaScript", new StringBuilder()
                            .append("load('nashorn:mozilla_compat.js');importPackage(Packages.java.util);")
                            .append("importPackage(Packages.net.shibboleth.idp.attribute);")
                            .append("x = new HashSet();").append("x.add(attribute.getValues().iterator().next());")
                            .append("x.add(new StringAttributeValue(\"a\"));").append("x;").toString());
            prcScript =
                    new EvaluableScript(
                            "JavaScript",
                            new StringBuilder("HashSet = Java.type(\"java.util.HashSet\");\n")
                                    .append("StringAttributeValue = Java.type(\"net.shibboleth.idp.attribute.StringAttributeValue\");\n")
                                    .append("x = new HashSet(1);\n")
                                    .append("x.add(new StringAttributeValue(profileContext.getClass().getName()));\n")
                                    .append("x;").toString());

        }
    }

    @Test public void testGetMatcher() throws Exception {

        final ScriptedMatcher matcher = new ScriptedMatcher(returnOneValueScript);
        matcher.setId("Test");
        matcher.initialize();

        Assert.assertNotNull(matcher.getScript());
    }

    @Test public void testNullArguments() throws Exception {

        ScriptedMatcher matcher = new ScriptedMatcher(returnOneValueScript);
        matcher.setId("Test");
        matcher.initialize();

        try {
            matcher.getMatchingValues(null, filterContext);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            matcher.getMatchingValues(attribute, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            matcher.getMatchingValues(null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }

        matcher = new ScriptedMatcher(returnOneValueScript);
        try {
            matcher.setScript(null);
        } catch (ConstraintViolationException e) {
            // expected this
        }

        try {
            new ScriptedMatcher(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testValidScript() throws Exception {
        final ScriptedMatcher matcher = new ScriptedMatcher(returnOneValueScript);
        matcher.setId("Test");
        matcher.initialize();

        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(value1) || result.contains(value2) || result.contains(value3));
    }
    
    @Test public void custom() throws Exception {
        
        final ScriptedMatcher matcher = new ScriptedMatcher(new EvaluableScript("custom;"));
        final Set<IdPAttributeValue> custom = Collections.singleton((IdPAttributeValue)attribute.getValues().get(0));
        matcher.setId("Test");
        matcher.initialize();
        matcher.setCustomObject(custom);

        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(value1) || result.contains(value2) || result.contains(value3));
    }


    @Test public void testNullReturnScript() throws Exception {

        final ScriptedMatcher matcher = new ScriptedMatcher(nullReturnScript);
        matcher.setId("Test");
        matcher.initialize();

        Assert.assertNull(matcher.getMatchingValues(attribute, filterContext));
    }
    
    @Test public void testInvalidReturnObjectValue() throws Exception {

        final ScriptedMatcher matcher = new ScriptedMatcher(invalidReturnObjectScript);
        matcher.setId("Test");
        matcher.initialize();

        Assert.assertNull(matcher.getMatchingValues(attribute, filterContext));
    }

    @Test public void testAddedValuesScript() throws Exception {

        final ScriptedMatcher matcher = new ScriptedMatcher(addedValuesScript);
        matcher.setId("Test");
        matcher.initialize();

        final Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(value1) || result.contains(value2) || result.contains(value3));
    }

    @Test public void testInitTeardown() throws ComponentInitializationException {

        final ScriptedMatcher matcher = new ScriptedMatcher(returnOneValueScript);

        boolean thrown = false;
        try {
            matcher.getMatchingValues(attribute, filterContext);
        } catch (UninitializedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "getMatchingValues before init");

        matcher.setId("Test");
        matcher.initialize();
        matcher.getMatchingValues(attribute, filterContext);

        thrown = false;
        try {
            matcher.setScript(returnOneValueScript);
        } catch (UnmodifiableComponentException e) {
            thrown = true;
        }

        matcher.destroy();

        thrown = false;
        try {
            matcher.initialize();
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "getMatchingValues after destroy");

        thrown = false;
        try {
            matcher.getMatchingValues(attribute, filterContext);
        } catch (DestroyedComponentException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "getMatchingValues after destroy");
    }

    @Test public void testEqualsHashToString() {

        final ScriptedMatcher matcher = new ScriptedMatcher(addedValuesScript);

        matcher.toString();

        Assert.assertFalse(matcher.equals(null));
        Assert.assertTrue(matcher.equals(matcher));
        Assert.assertFalse(matcher.equals(this));

        ScriptedMatcher other = new ScriptedMatcher(addedValuesScript);

        Assert.assertTrue(matcher.equals(other));
        Assert.assertEquals(matcher.hashCode(), other.hashCode());

        other = new ScriptedMatcher(nullReturnScript);

        Assert.assertFalse(matcher.equals(other));
        Assert.assertNotSame(matcher.hashCode(), other.hashCode());

    }

    @Test public void testPrc() throws ComponentInitializationException, CloneNotSupportedException {
        ScriptedMatcher matcher = new ScriptedMatcher(prcScript);

        matcher.setId("prc");
        matcher.initialize();
        new ProfileRequestContext<>().getSubcontext(RelyingPartyContext.class, true).addSubcontext(filterContext);
        Set<IdPAttributeValue<?>> result = matcher.getMatchingValues(attribute, filterContext);
        Assert.assertEquals(result.size(), 0);

        final IdPAttribute newAttr = attribute.clone();

        newAttr.setValues(Collections.singleton((IdPAttributeValue<?>) new StringAttributeValue(
                ProfileRequestContext.class.getName())));
        result = matcher.getMatchingValues(newAttr, filterContext);
        Assert.assertEquals(result.size(), 1);

    }

}