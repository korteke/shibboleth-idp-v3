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

package net.shibboleth.idp.saml.nameid.impl;

import java.io.IOException;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.BasicKeystoreKeyStrategy;
import net.shibboleth.utilities.java.support.security.DataSealer;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link TransientSAML2NameIDGenerator} using crypto-based generator. */
public class CryptoTransientSAML2NameIDGeneratorTest extends OpenSAMLInitBaseTestCase {

    private static final long TIMEOUT = 500;
    
    private DataSealer sealer;
    
    private CryptoTransientIdGenerationStrategy transientGenerator;
    
    private TransientSAML2NameIDGenerator generator;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException, IOException {
        final Resource keyStore =
                new ClassPathResource("/net/shibboleth/idp/saml/impl/attribute/resolver/SealerKeyStore.jks");
        Assert.assertTrue(keyStore.exists());
        
        final Resource version =
                new ClassPathResource("/net/shibboleth/idp/saml/impl/attribute/resolver/SealerKeyStore.kver");
        Assert.assertTrue(version.exists());

        final BasicKeystoreKeyStrategy kstrategy = new BasicKeystoreKeyStrategy();
        kstrategy.setKeyAlias("secret");
        kstrategy.setKeyPassword("kpassword");
        kstrategy.setKeystorePassword("password");
        kstrategy.setKeystoreResource(ResourceHelper.of(keyStore));
        kstrategy.setKeyVersionResource(ResourceHelper.of(version));
        kstrategy.initialize();
        
        sealer = new DataSealer();
        sealer.setKeyStrategy(kstrategy);
        sealer.initialize();
        
        transientGenerator = new CryptoTransientIdGenerationStrategy();
        transientGenerator.setId("test");
        transientGenerator.setDataSealer(sealer);
        transientGenerator.setIdLifetime(TIMEOUT);
        transientGenerator.initialize();
        
        generator = new TransientSAML2NameIDGenerator();
        generator.setId("test");
        generator.setFormat(NameIdentifier.UNSPECIFIED);
        generator.setTransientIdGenerator(transientGenerator);
        generator.initialize();
    }
    
    @AfterMethod public void tearDown() {
        generator.destroy();
        transientGenerator.destroy();
        sealer.destroy();
    }

    @Test public void testNoPrincipal() throws Exception {        

        final ProfileRequestContext prc = new RequestContextBuilder().buildProfileRequestContext();
        
        final NameID name = generator.generate(prc, generator.getFormat());
        
        Assert.assertNull(name);
    }

    @Test public void testNoRelyingParty() throws Exception {        

        final ProfileRequestContext prc = new RequestContextBuilder().buildProfileRequestContext();
        prc.getSubcontext(RelyingPartyContext.class).setRelyingPartyId(null);
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("jdoe");
        
        final NameID name = generator.generate(prc, generator.getFormat());
        
        Assert.assertNull(name);
    }
    
    @Test public void testTransient() throws Exception {        

        final ProfileRequestContext prc = new RequestContextBuilder().buildProfileRequestContext();
        final RelyingPartyContext rpc = prc.getSubcontext(RelyingPartyContext.class);
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("jdoe");
        
        final NameID name = generator.generate(prc, generator.getFormat());
        
        Assert.assertNotNull(name);
        Assert.assertEquals(name.getFormat(), generator.getFormat());
        Assert.assertEquals(name.getNameQualifier(), rpc.getConfiguration().getResponderId());

        final String val = name.getValue();

        final String decode = sealer.unwrap(val);

        Assert.assertEquals(decode, rpc.getRelyingPartyId() + "!" + "jdoe");

        Thread.sleep(TIMEOUT*2);
        try {
            sealer.unwrap(val);
            Assert.fail("Timeout not set correctly");
        } catch (Exception e) {
            // OK
        }
    }
    
}