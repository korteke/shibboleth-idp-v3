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

package net.shibboleth.idp.profile.spring.relyingparty;

import java.util.Arrays;
import java.util.HashSet;

import net.shibboleth.ext.spring.config.DurationToLongConverter;
import net.shibboleth.ext.spring.config.StringToIPRangeConverter;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test a &lt;RelyingParty&gt;
 */
public class RelyingPartyTest extends OpenSAMLInitBaseTestCase {

    private static final String PATH = "/net/shibboleth/idp/profile/spring/relyingparty/";

    @Test public void relyingParties() {
        final Resource[] resources = new Resource[2];

        resources[0] = new ClassPathResource(PATH + "beans.xml");
        resources[1] = new ClassPathResource(PATH + "relying-party.xml");

        final GenericApplicationContext context = new GenericApplicationContext();
        try{
            ConversionServiceFactoryBean service = new ConversionServiceFactoryBean();
            context.setDisplayName("ApplicationContext: ");
            service.setConverters(new HashSet<>(Arrays.asList(new DurationToLongConverter(), new StringToIPRangeConverter())));
            service.afterPropertiesSet();
    
            context.getBeanFactory().setConversionService(service.getObject());
    
            final XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(context);
    
            configReader.setValidating(true);
    
            configReader.loadBeanDefinitions(resources);
            context.refresh();
    
            RelyingPartyConfiguration rpConf = context.getBean(RelyingPartyConfiguration.class);
    
            Assert.assertEquals(rpConf.getId(), "the_RP");
            Assert.assertTrue(rpConf.isDetailedErrors());
            Assert.assertEquals(rpConf.getProfileConfigurations().size(), 1);
    
            ProfileRequestContext ctx = new ProfileRequestContext<>();
            RelyingPartyContext rpCtx = ctx.getSubcontext(RelyingPartyContext.class, true);
            rpCtx.setRelyingPartyId("the_RP");
            
            Assert.assertTrue(rpConf.apply(ctx));
            //
            // TODO the EntitiesGroup thing
            //
        } finally {
            context.close();
        }
    }

}