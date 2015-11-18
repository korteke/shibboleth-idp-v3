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

import java.io.IOException;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataParserTest;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.saml.metadata.resolver.RefreshableMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MultiSourceFileTest extends AbstractMetadataParserTest {

    /** the service. */
    private ReloadableService<RefreshableMetadataResolver> service;

    
    @BeforeClass public void setup() throws IOException {
        service = getBean(ReloadableService.class, "../metadata/serviceBeans.xml");
    }
    
    @Test public void multipleEntities() throws ResolverException {
        final ServiceableComponent<RefreshableMetadataResolver> component = service.getServiceableComponent();
        try {
          final  RefreshableMetadataResolver resolver = component.getComponent();
          final EntityDescriptor e2 = resolver.resolveSingle(criteriaFor("https://idp.example.org/idp2/shibboleth"));
          Assert.assertNotNull(e2);
          Assert.assertEquals(e2.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol").getSingleSignOnServices().get(0).getLocation(), "https://idp.example.org/idpc0/profile/SAML2/Redirect/SSO");
          final EntityDescriptor e1 = resolver.resolveSingle(criteriaFor("https://idp.example.org/idp/shibboleth")); 
          Assert.assertNotNull(e1);
          Assert.assertEquals(e1.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol").getSingleSignOnServices().get(0).getLocation(), "https://idp.example.org/idpc1/profile/SAML2/Redirect/SSO");
        } finally {
            if (component != null) {
                component.unpinComponent();
            }
        }
        
    }

}