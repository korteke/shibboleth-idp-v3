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

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.nameid.TransientIdParameters;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit test for {@link TransientSAML2NameIDGenerator} using storage-based generator. */
public class StoredTransientSAML2NameIDGeneratorTest extends OpenSAMLInitBaseTestCase {

    private MemoryStorageService store;
    
    private StoredTransientIdGenerationStrategy transientGenerator;
    
    private TransientSAML2NameIDGenerator generator;
    
    @BeforeMethod public void setUp() throws ComponentInitializationException {
        store = new MemoryStorageService();
        store.setId("test");
        store.initialize();
        
        transientGenerator = new StoredTransientIdGenerationStrategy();
        transientGenerator.setId("test");
        transientGenerator.setIdStore(store);
        transientGenerator.initialize();
        
        generator = new TransientSAML2NameIDGenerator();
        generator.setId("test");
        generator.setFormat(NameIdentifier.UNSPECIFIED);
        generator.setTransientIdGenerator(transientGenerator);
        generator.initialize();
    }
    
    @AfterMethod public void tearDown() {
        store.destroy();
        transientGenerator.destroy();
        generator.destroy();
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
        Assert.assertEquals(name.getSPNameQualifier(), rpc.getRelyingPartyId());

        String val = name.getValue();

        final StorageRecord record = store.read(TransientIdParameters.CONTEXT, val);
        
        Assert.assertNotNull(record);
        Assert.assertTrue(val.length() >= transientGenerator.getIdSize());
 
        TransientIdParameters parms = new TransientIdParameters(record.getValue());
        
        Assert.assertNotNull(parms);
        Assert.assertEquals(parms.getAttributeRecipient(), rpc.getRelyingPartyId());
        Assert.assertEquals(parms.getPrincipal(), "jdoe");
    }
    
}