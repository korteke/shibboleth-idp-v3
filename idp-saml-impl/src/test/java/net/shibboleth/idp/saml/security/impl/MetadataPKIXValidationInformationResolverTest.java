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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.opensaml.security.x509.TrustedNamesCriterion;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 * Testing the Shibboleth metadata PKIX validation information resolver.
 */
public class MetadataPKIXValidationInformationResolverTest extends XMLObjectBaseTestCase {
    
    private String protocolBlue = "PROTOCOL_BLUE";
    
    private String protocolGreen = "PROTOCOL_GREEN";
    
    private String fooEntityID = "http://foo.example.org/shibboleth";
    
    private String barEntityID = "http://bar.example.org/shibboleth";
    
    private CriteriaSet criteriaSet;
    
    @BeforeMethod
    protected void setUp() throws Exception {
        criteriaSet = new CriteriaSet();
    }
    
    @Test
    public void testEmpty() throws XMLParserException, ComponentInitializationException, ResolverException {
       MetadataPKIXValidationInformationResolver resolver = getResolver("empty-metadata-pkix.xml");
       criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
       criteriaSet.add( new EntityIdCriterion(fooEntityID) );
       criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
       criteriaSet.add( new ProtocolCriterion(protocolBlue) );
       
       Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
       
       Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
    }
    
    @Test
    public void testNames() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("names-entities-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Set<String> names = resolver.resolveTrustedNames(criteriaSet);
        
        Assert.assertNotNull(names, "Set of resolved trusted names was null");
        Assert.assertFalse(names.isEmpty(), "Set of trusted names was empty"); 
        Assert.assertEquals(names.size(), 2, "Set of trusted names had incorrect size");
        Assert.assertTrue(names.contains("foo.example.org"), "Did't find expected name value");
        Assert.assertTrue(names.contains(fooEntityID), "Did't find expected name value");
        
        criteriaSet.clear();
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolGreen) );
        
        names = resolver.resolveTrustedNames(criteriaSet);
        
        Assert.assertNotNull(names, "Set of resolved trusted names was null");
        Assert.assertFalse(names.isEmpty(), "Set of trusted names was empty");
        Assert.assertEquals(names.size(), 3, "Set of trusted names had incorrect size");
        Assert.assertTrue(names.contains("CN=foo.example.org,O=Internet2"), "Did't find expected name value");
        Assert.assertTrue(names.contains("idp.example.org"), "Did't find expected name value");
        Assert.assertTrue(names.contains(fooEntityID), "Did't find expected name value");
        
        criteriaSet.clear();
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(barEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        names = resolver.resolveTrustedNames(criteriaSet);
        
        Assert.assertNotNull(names, "Set of resolved trusted names was null");
        Assert.assertFalse(names.isEmpty(), "Set of trusted names was empty");
        Assert.assertEquals(names.size(), 1, "Set of trusted names had incorrect size");
        Assert.assertTrue(names.contains(barEntityID), "Did't find expected name value");
        
        // Test dynamic trusted names
        final Set<String> dynamicNames = new HashSet<>(Arrays.asList("foo", "bar"));
        criteriaSet.clear();
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        criteriaSet.add( new TrustedNamesCriterion(dynamicNames) );
        
        names = resolver.resolveTrustedNames(criteriaSet);
        
        Assert.assertNotNull(names, "Set of resolved trusted names was null");
        Assert.assertFalse(names.isEmpty(), "Set of trusted names was empty"); 
        Assert.assertEquals(names.size(), 4, "Set of trusted names had incorrect size");
        Assert.assertTrue(names.contains("foo.example.org"), "Did't find expected name value");
        Assert.assertTrue(names.contains(fooEntityID), "Did't find expected name value");
        Assert.assertTrue(names.containsAll(dynamicNames), "Did't find expected name value");
     }
    
    @Test
    public void testNonExistentEntityID() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("oneset-entities-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion("http://doesnt.exist.example.org/shibboleth") );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
        
        Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
     }
    
    @Test
    public void testOneSetOnEntitiesDescriptor() throws ResolverException, XMLParserException, ComponentInitializationException {
       MetadataPKIXValidationInformationResolver resolver = getResolver("oneset-entities-metadata-pkix.xml");
       criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
       criteriaSet.add( new EntityIdCriterion(fooEntityID) );
       criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
       criteriaSet.add( new ProtocolCriterion(protocolBlue) );
       
       Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
       
       PKIXValidationInformation infoSet = null;
       Assert.assertTrue(iter.hasNext(), "Iterator was empty");
       infoSet = iter.next();
       Assert.assertEquals(infoSet.getCertificates().size(), 3, "Incorrect number of certificates");
       Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
       Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
       
       Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
    }
    
    @Test
    public void testNoVerifyDepth() throws ResolverException, XMLParserException, ComponentInitializationException {
       MetadataPKIXValidationInformationResolver resolver = getResolver("nodepth-entities-metadata-pkix.xml");
       criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
       criteriaSet.add( new EntityIdCriterion(fooEntityID) );
       criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
       criteriaSet.add( new ProtocolCriterion(protocolBlue) );
       
       Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
       
       PKIXValidationInformation infoSet = null;
       Assert.assertTrue(iter.hasNext(), "Iterator was empty");
       infoSet = iter.next();
       // 1 is the default VerifyDepth value
       Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(1), "Incorrect VerifyDepth");
       
       Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
    }
    
    @Test
    public void testOneSetOnEntitiesDescriptor3KeyInfo() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("oneset-3keyinfo-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
        
        PKIXValidationInformation infoSet = null;
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 7, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 2, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
        
        Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
     }
    
    @Test
    public void testOneSetOnEntityDescriptor() throws ResolverException, XMLParserException, ComponentInitializationException {
       MetadataPKIXValidationInformationResolver resolver = getResolver("oneset-entity-metadata-pkix.xml");
       criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
       criteriaSet.add( new EntityIdCriterion(fooEntityID) );
       criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
       criteriaSet.add( new ProtocolCriterion(protocolBlue) );
       
       Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
       
       PKIXValidationInformation infoSet = null;
       Assert.assertTrue(iter.hasNext(), "Iterator was empty");
       infoSet = iter.next();
       Assert.assertEquals(infoSet.getCertificates().size(), 3, "Incorrect number of certificates");
       Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
       Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
       
       Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
       
       // Now check other entity ID resolves as empty
       criteriaSet.clear();
       criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
       criteriaSet.add( new EntityIdCriterion(barEntityID) );
       criteriaSet.add( new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME) );
       criteriaSet.add( new ProtocolCriterion(protocolBlue) );
       
       iter = resolver.resolve(criteriaSet).iterator();
       
       Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
    }
    
    @Test
    public void testTwoSetOnEntitiesAndEntityDescriptor() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("twoset-entity-entities-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
        
        PKIXValidationInformation infoSet = null;
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 1, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(3), "Incorrect VerifyDepth");
        
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 6, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
        
        Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
     }
    
    @Test
    public void testTwoSetOn2Authorities() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("twoset-2authorities-entities-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
        
        PKIXValidationInformation infoSet = null;
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 3, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
        
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 1, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(3), "Incorrect VerifyDepth");
        
        
        Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
    }
    
    @Test
    public void testThreeSetOn3Authorities() throws ResolverException, XMLParserException, ComponentInitializationException {
        MetadataPKIXValidationInformationResolver resolver = getResolver("threeset-entity-entities-entities-metadata-pkix.xml");
        criteriaSet.add( new UsageCriterion(UsageType.SIGNING) );
        criteriaSet.add( new EntityIdCriterion(fooEntityID) );
        criteriaSet.add( new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME) );
        criteriaSet.add( new ProtocolCriterion(protocolBlue) );
        
        Iterator<PKIXValidationInformation> iter = resolver.resolve(criteriaSet).iterator();
        
        PKIXValidationInformation infoSet = null;
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 1, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(3), "Incorrect VerifyDepth");
        
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 3, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 0, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
        
        Assert.assertTrue(iter.hasNext(), "Iterator was empty");
        infoSet = iter.next();
        Assert.assertEquals(infoSet.getCertificates().size(), 4, "Incorrect number of certificates");
        Assert.assertEquals(infoSet.getCRLs().size(), 1, "Incorrect number of CRL's");
        Assert.assertEquals(infoSet.getVerificationDepth(), new Integer(5), "Incorrect VerifyDepth");
        
        Assert.assertFalse(iter.hasNext(), "Iterator was not empty");
     } 
    
    private MetadataPKIXValidationInformationResolver getResolver(String fileName) throws XMLParserException, 
            ComponentInitializationException {
        Document mdDoc = null;
        String mdFileName = "/net/shibboleth/idp/saml/impl/security/" + fileName;
        
        mdDoc = parserPool.parse(MetadataPKIXValidationInformationResolverTest.class.getResourceAsStream(mdFileName));
        
        DOMMetadataResolver mdProvider = new DOMMetadataResolver(mdDoc.getDocumentElement());
        
        List<MetadataNodeProcessor> processors = new ArrayList<>();
        processors.add(new KeyAuthorityNodeProcessor());
        
        NodeProcessingMetadataFilter nodeFilter =  new NodeProcessingMetadataFilter();
        nodeFilter.setNodeProcessors(processors);
        nodeFilter.initialize();
        
        mdProvider.setMetadataFilter(nodeFilter);
        mdProvider.setId("Test");
        mdProvider.initialize();
        
        BasicRoleDescriptorResolver roleResolver = new BasicRoleDescriptorResolver(mdProvider);
        roleResolver.initialize();
        
        MetadataPKIXValidationInformationResolver resolver = new MetadataPKIXValidationInformationResolver(roleResolver);
        resolver.initialize();
        return resolver;
    }

}
