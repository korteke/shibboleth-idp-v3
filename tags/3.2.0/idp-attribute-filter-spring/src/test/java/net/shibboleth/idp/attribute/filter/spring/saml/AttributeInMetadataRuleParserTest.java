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

package net.shibboleth.idp.attribute.filter.spring.saml;

import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.PolicyFromMatcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.matcher.saml.impl.AttributeInMetadataMatcher;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.saml.impl.AttributeInMetadataRuleParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeInMetadataRuleParser}.
 */
public class AttributeInMetadataRuleParserTest extends  BaseAttributeFilterParserTest {

    @Test public void requested() throws ComponentInitializationException {
        requested(false);
//        requested(true);
    }
    
    public void requested(boolean isAfp) throws ComponentInitializationException {
        GenericApplicationContext context = new FilesystemGenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: Matcher");
        
        final AttributeRule rule = getBean(MATCHER_PATH + (isAfp?"afp/":"") + "requestedInMetadata.xml", AttributeRule.class, context);
        rule.initialize();
        AttributeInMetadataMatcher matcher = (AttributeInMetadataMatcher) rule.getMatcher();
     
        Assert.assertTrue(matcher.getMatchIfMetadataSilent());
        Assert.assertTrue(matcher.getOnlyIfRequired());
        Assert.assertTrue(matcher.getId().endsWith(":PermitRule"));
    
       final PolicyFromMatcher policyRule = (PolicyFromMatcher) getBean(PolicyRequirementRule.class, context);
       matcher = (AttributeInMetadataMatcher) policyRule.getMatcher();
       Assert.assertTrue(matcher.getMatchIfMetadataSilent());
       Assert.assertTrue(matcher.getOnlyIfRequired());
       Assert.assertTrue(matcher.getId().endsWith(":PRR"));
    }
    
}