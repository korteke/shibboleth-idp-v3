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

package net.shibboleth.idp.saml.security.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeyAuthorityNodeProcessorTest extends XMLObjectBaseTestCase {
    
    private FilesystemMetadataResolver metadataProvider;
    
    private File mdFile;
    
    private NodeProcessingMetadataFilter metadataFilter;
    
    private ArrayList<MetadataNodeProcessor> processors;
    
    @BeforeMethod
    protected void setUp() throws Exception {
        URL mdURL = KeyAuthorityNodeProcessorTest.class
                .getResource("/net/shibboleth/idp/saml/impl/security/KeyAuthority-NodeProcessor-metadata.xml");
        mdFile = new File(mdURL.toURI());

        processors = new ArrayList<>();
        processors.add(new KeyAuthorityNodeProcessor());
        
        metadataFilter = new NodeProcessingMetadataFilter();
        metadataFilter.setNodeProcessors(processors);
        metadataFilter.initialize();
        
        metadataProvider = new FilesystemMetadataResolver(mdFile);
        metadataProvider.setParserPool(parserPool);
        metadataProvider.setMetadataFilter(metadataFilter);
        metadataProvider.setId("test");
        metadataProvider.initialize();
    }
    
    @Test
    public void testPKIXHierarchy() throws ResolverException {
        EntityDescriptor entityDescriptor = null;
        List<PKIXValidationInformation> pkixInfos = null;
        
        entityDescriptor = metadataProvider.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://idp-top.example.org")));
        pkixInfos = entityDescriptor.getObjectMetadata().get(PKIXValidationInformation.class);
        Assert.assertEquals(pkixInfos.size(), 2);
        
        entityDescriptor = metadataProvider.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://idp-sub1.example.org")));
        pkixInfos = entityDescriptor.getObjectMetadata().get(PKIXValidationInformation.class);
        Assert.assertEquals(pkixInfos.size(), 3);
        
        entityDescriptor = metadataProvider.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://idp-sub2.example.org")));
        pkixInfos = entityDescriptor.getObjectMetadata().get(PKIXValidationInformation.class);
        Assert.assertEquals(pkixInfos.size(), 4);
        
        entityDescriptor = metadataProvider.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://idp-sub2a.example.org")));
        pkixInfos = entityDescriptor.getObjectMetadata().get(PKIXValidationInformation.class);
        Assert.assertEquals(pkixInfos.size(), 5);
    }

}
