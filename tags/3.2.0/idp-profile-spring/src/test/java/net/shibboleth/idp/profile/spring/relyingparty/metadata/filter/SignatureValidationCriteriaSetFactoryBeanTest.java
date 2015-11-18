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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.SignatureValidationCriteriaSetFactoryBean;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureValidationConfiguration;
import org.opensaml.xmlsec.signature.support.SignatureValidationParametersCriterion;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SignatureValidationCriteriaSetFactoryBeanTest extends OpenSAMLInitBaseTestCase {
    
    private SignatureValidationCriteriaSetFactoryBean factoryBean;
    
    @BeforeMethod
    public void setUp() {
        factoryBean = new SignatureValidationCriteriaSetFactoryBean();
    }
    
    @Test
    public void testNoInputs() throws Exception {
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertFalse(criteriaSet.isEmpty());
        Assert.assertEquals(criteriaSet.size(), 1);
        Assert.assertTrue(criteriaSet.contains(SignatureValidationParametersCriterion.class));
    }
    
    @Test
    public void testNoOpenSAMLGlobal() throws Exception {
        factoryBean.setIncludeOpenSAMLGlobalConfig(false);
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertTrue(criteriaSet.isEmpty());
    }
    
    @Test
    public void testOpenSAMLGlobalOnly() throws Exception {
        factoryBean.setIncludeOpenSAMLGlobalConfig(true);
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertFalse(criteriaSet.isEmpty());
        Assert.assertEquals(criteriaSet.size(), 1);
        Assert.assertTrue(criteriaSet.contains(SignatureValidationParametersCriterion.class));
    }
    
    @Test
    public void testExplicitConfigs() throws Exception {
        List<SignatureValidationConfiguration> configs = new ArrayList<>();
        configs.add(new BasicSignatureValidationConfiguration());
        configs.add(new BasicSignatureValidationConfiguration());
        factoryBean.setSignatureValidationConfigurations(configs);
        
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertFalse(criteriaSet.isEmpty());
        Assert.assertEquals(criteriaSet.size(), 1);
        Assert.assertTrue(criteriaSet.contains(SignatureValidationParametersCriterion.class));
    }
    
    @Test
    public void testOtherCriteriaOnly() throws Exception {
        factoryBean.setOtherCriteria(Arrays.asList(new UsageCriterion(UsageType.SIGNING), new EntityIdCriterion("foo")));
        factoryBean.setIncludeOpenSAMLGlobalConfig(false);
        
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertFalse(criteriaSet.isEmpty());
        Assert.assertEquals(criteriaSet.size(), 2);
        Assert.assertFalse(criteriaSet.contains(SignatureValidationParametersCriterion.class));
        Assert.assertTrue(criteriaSet.contains(UsageCriterion.class));
        Assert.assertTrue(criteriaSet.contains(EntityIdCriterion.class));
    }

    @Test
    public void testEverything() throws Exception {
        List<SignatureValidationConfiguration> configs = new ArrayList<>();
        configs.add(new BasicSignatureValidationConfiguration());
        configs.add(new BasicSignatureValidationConfiguration());
        factoryBean.setSignatureValidationConfigurations(configs);
        
        factoryBean.setIncludeOpenSAMLGlobalConfig(true);
        
        factoryBean.setOtherCriteria(Arrays.asList(new UsageCriterion(UsageType.SIGNING), new EntityIdCriterion("foo")));
        
        CriteriaSet criteriaSet = factoryBean.getObject();
        Assert.assertNotNull(criteriaSet);
        Assert.assertFalse(criteriaSet.isEmpty());
        Assert.assertEquals(criteriaSet.size(), 3);
        Assert.assertTrue(criteriaSet.contains(SignatureValidationParametersCriterion.class));
        Assert.assertTrue(criteriaSet.contains(UsageCriterion.class));
        Assert.assertTrue(criteriaSet.contains(EntityIdCriterion.class));
    }
    

}