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

package net.shibboleth.idp.profile.spring.logic;

import net.shibboleth.ext.spring.context.FilesystemGenericApplicationContext;
import net.shibboleth.ext.spring.util.SchemaTypeAwareXMLBeanDefinitionReader;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.logic.RelyingPartyIdPredicate;

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class RelyingPartyIdPredicateTest {
    
    private boolean testCandidate(final RelyingPartyIdPredicate rpIdPredicate, final String rpId) {
        ProfileRequestContext prc = new ProfileRequestContext();
        RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class, true);
        rpc.setRelyingPartyId(rpId);
        return rpIdPredicate.apply(prc);
    }
    
    
    @Test
    public void constructors() {
        GenericApplicationContext context = new FilesystemGenericApplicationContext();
        try {
            context.setDisplayName("ApplicationContext: Matcher");
            SchemaTypeAwareXMLBeanDefinitionReader beanDefinitionReader =
                    new SchemaTypeAwareXMLBeanDefinitionReader(context);
    
            beanDefinitionReader.loadBeanDefinitions("net/shibboleth/idp/profile/spring/logic/relyingPartyIdPredicates.xml");
    
            context.refresh();
            
            RelyingPartyIdPredicate bean = context.getBean("candidate", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertFalse(testCandidate(bean, "Double"));
            Assert.assertFalse(testCandidate(bean, "Elephant"));
            
            bean = context.getBean("candidates", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertTrue(testCandidate(bean, "Double"));
            Assert.assertFalse(testCandidate(bean, "Elephant"));
            
            bean = context.getBean("pred", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertTrue(testCandidate(bean, "Double"));
            Assert.assertTrue(testCandidate(bean, "Elephant"));
    
            bean = context.getBean("candidate_0", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertFalse(testCandidate(bean, "Double"));
            Assert.assertFalse(testCandidate(bean, "Elephant"));
            
            bean = context.getBean("candidates_0", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertTrue(testCandidate(bean, "Double"));
            Assert.assertFalse(testCandidate(bean, "Elephant"));
            
            bean = context.getBean("pred_0", RelyingPartyIdPredicate.class);
            Assert.assertTrue(testCandidate(bean, "Single"));
            Assert.assertTrue(testCandidate(bean, "Double"));
            Assert.assertTrue(testCandidate(bean, "Elephant"));
        } finally {
            context.close();
        }
    }

}
