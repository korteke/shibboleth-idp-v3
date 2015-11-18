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

package net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.idp.attribute.filter.matcher.impl.DataSources;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link PrincipalNamePolicyRule}.
 */
public class PrincipalNamePolicyRuleTest {
    
    private PrincipalNamePolicyRule getMatcher(boolean b) throws ComponentInitializationException {
        PrincipalNamePolicyRule matcher = new PrincipalNamePolicyRule();
        matcher.setMatchString("principal");
        matcher.setIgnoreCase(!b);
        matcher.setId("Test");
        matcher.initialize();
        return matcher;
    }
    
    @Test public void testNull() throws ComponentInitializationException {

        try {
            new PrincipalNamePolicyRule().matches(null);
            Assert.fail();
        } catch (UninitializedComponentException ex) {
            // OK
        }       
    }
    
    @Test public void testUnpopulated() throws ComponentInitializationException {
        final PrincipalNamePolicyRule matcher = getMatcher(true);
        Assert.assertEquals(matcher.matches(DataSources.unPopulatedFilterContext()), Tristate.FAIL);
    }

    @Test public void testNoPrincipal() throws ComponentInitializationException {
        final PrincipalNamePolicyRule matcher = getMatcher(true);
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext(null, null, null)), Tristate.FAIL);
    }

    @Test public void testCaseSensitive() throws ComponentInitializationException {

        PrincipalNamePolicyRule matcher = getMatcher(true);
        
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("wibble", null, null)), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("PRINCIPAL", null, null)), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("principal", null, null)), Tristate.TRUE);        
    }

    
    @Test public void testCaseInsensitive() throws ComponentInitializationException {

        PrincipalNamePolicyRule matcher = getMatcher(false);
        
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("wibble", null, null)), Tristate.FALSE);
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("PRINCIPAL", null, null)), Tristate.TRUE);
        Assert.assertEquals(matcher.matches(DataSources.populatedFilterContext("principal", null, null)), Tristate.TRUE);        
    }
}