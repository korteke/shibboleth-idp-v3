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

package net.shibboleth.idp.installer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.shibboleth.idp.installer.ant.MetadataGeneratorTask;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 * Test the metadata Generator.
 */
public class TestMetadataGenerator extends XMLObjectBaseTestCase {

    @Test public void withSAMLLogout() throws IOException {
        final MetadataGeneratorTask task = new MetadataGeneratorTask();
        final Resource backChannelCrt = new ClassPathResource("/credentials/idp-backchannel.crt");
        task.setBackchannelCert(backChannelCrt.getFile());
        task.setSAML2LogoutCommented(false);
        final File out = File.createTempFile("TestMetadataGenerator", ".xml");
        System.setProperty("idp.home", "classpath:");

        try {
            task.setBackchannelCert(backChannelCrt.getFile());
            task.setDnsName("idp.example.org");

            task.setOutput(out);

            task.execute();

            final EntityDescriptor entity = (EntityDescriptor) unmarshallElement(out.getAbsolutePath());
            final IDPSSODescriptor idpsso = entity.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
            Assert.assertNotNull(idpsso);
            Assert.assertSame(entity.getIDPSSODescriptor(SAMLConstants.SAML11P_NS), idpsso);
            Assert.assertSame(entity.getIDPSSODescriptor("urn:mace:shibboleth:1.0"), idpsso);
            Assert.assertNull(entity.getIDPSSODescriptor("urn:mace:shibboleth:1.0:nameid"));
            Assert.assertEquals(idpsso.getArtifactResolutionServices().size(), 2);
            Assert.assertEquals(idpsso.getSingleLogoutServices().size(), 4);
            Assert.assertEquals(idpsso.getSingleSignOnServices().size(), 4);
            Assert.assertEquals(idpsso.getNameIDFormats().size(), 2);
            List<XMLObject> exts = idpsso.getExtensions().getUnknownXMLObjects();
            Assert.assertEquals(exts.size(), 1);
            Assert.assertEquals(idpsso.getKeyDescriptors().size(), 3);

            final AttributeAuthorityDescriptor aa = entity.getAttributeAuthorityDescriptor(SAMLConstants.SAML11P_NS);
            Assert.assertNotNull(aa);
            Assert.assertSame(entity.getAttributeAuthorityDescriptor(SAMLConstants.SAML11P_NS), aa);
            Assert.assertNull(entity.getAttributeAuthorityDescriptor("urn:mace:shibboleth:1.0"));
            Assert.assertEquals(aa.getAttributeServices().size(), 1);
            exts = aa.getExtensions().getUnknownXMLObjects();
            Assert.assertEquals(exts.size(), 1);
            Assert.assertEquals(aa.getKeyDescriptors().size(), 3);
        } finally {
            out.delete();
        }
    }

    @Test public void withSaml2AQ() throws IOException {
        final MetadataGeneratorTask task = new MetadataGeneratorTask();
        final Resource backChannelCrt = new ClassPathResource("/credentials/idp-backchannel.crt");
        task.setBackchannelCert(backChannelCrt.getFile());
        final File out = File.createTempFile("TestMetadataGenerator", ".xml");
        System.setProperty("idp.home", "classpath:");

        try {
            task.setBackchannelCert(backChannelCrt.getFile());
            task.setDnsName("idp.example.org");
            task.setSAML2AttributeQueryCommented(false);
            task.setOutput(out);

            task.execute();

            final EntityDescriptor entity = (EntityDescriptor) unmarshallElement(out.getAbsolutePath());
            final IDPSSODescriptor idpsso = entity.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
            Assert.assertNotNull(idpsso);
            Assert.assertSame(entity.getIDPSSODescriptor(SAMLConstants.SAML11P_NS), idpsso);
            Assert.assertSame(entity.getIDPSSODescriptor("urn:mace:shibboleth:1.0"), idpsso);
            Assert.assertNull(entity.getIDPSSODescriptor("urn:mace:shibboleth:1.0:nameid"));
            Assert.assertEquals(idpsso.getArtifactResolutionServices().size(), 2);
            Assert.assertEquals(idpsso.getSingleLogoutServices().size(), 0);
            Assert.assertEquals(idpsso.getSingleSignOnServices().size(), 4);
            Assert.assertEquals(idpsso.getNameIDFormats().size(), 2);
            List<XMLObject> exts = idpsso.getExtensions().getUnknownXMLObjects();
            Assert.assertEquals(exts.size(), 1);
            Assert.assertEquals(idpsso.getKeyDescriptors().size(), 3);

            final AttributeAuthorityDescriptor aa = entity.getAttributeAuthorityDescriptor(SAMLConstants.SAML11P_NS);
            Assert.assertNotNull(aa);
            Assert.assertSame(entity.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS), aa);
            Assert.assertNull(entity.getAttributeAuthorityDescriptor("urn:mace:shibboleth:1.0"));
            Assert.assertEquals(aa.getAttributeServices().size(), 2);
            exts = aa.getExtensions().getUnknownXMLObjects();
            Assert.assertEquals(exts.size(), 1);
            Assert.assertEquals(aa.getKeyDescriptors().size(), 3);
        } finally {
            out.delete();
        }
    }

    /** Version to look at the filesystem of {@inheritDoc} */
    @Override
    protected Document parseXMLDocument(String xmlFilename) throws XMLParserException {
        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(new File(xmlFilename)));
        } catch (FileNotFoundException e) {
           throw new XMLParserException(e);
        }
        Document doc = parserPool.parse(is);
        return doc;
        }
}
