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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.io.IOException;

import net.shibboleth.idp.saml.metadata.RelyingPartyMetadataProvider;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.RefreshableMetadataResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class EmptyChainService extends AbstractMetadataParserTest {

    @Test public void setup() throws IOException {
        final ReloadableService<RefreshableMetadataResolver> service = getBean(ReloadableService.class, "empty-chain-svc.xml");
        final ServiceableComponent<RefreshableMetadataResolver> comp = service.getServiceableComponent();
        try {
            final RelyingPartyMetadataProvider rpmp = (RelyingPartyMetadataProvider) comp.getComponent();
            final ChainingMetadataResolver chain = (ChainingMetadataResolver) rpmp.getEmbeddedResolver();
            Assert.assertTrue(chain.getResolvers().isEmpty());
            
        } finally {
            comp.unpinComponent();
        }
    }

}
