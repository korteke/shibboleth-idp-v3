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

import java.util.Collections;

import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDCanonicalizationFlowDescriptor;
import net.shibboleth.idp.saml.nameid.TransientIdParameters;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.action.ActionTestingSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link BaseTransientDecoder} unit test. */
public class TransientDecoderTest extends OpenSAMLInitBaseTestCase {

    private static final String RECIPIENT="TheRecipient";
    private static final String PRINCIPAL="ThePrincipalName";
    
    private MemoryStorageService store;

    @BeforeMethod void setUp() throws ComponentInitializationException {
        store = new MemoryStorageService();
        store.setId("test");
        store.initialize();
    }
    
    @Test public void testSucess() throws Exception {

        final String principalTokenId;
        principalTokenId = new TransientIdParameters(RECIPIENT, PRINCIPAL).encode();

        final String id = "THE_ID";

        final long expiration = System.currentTimeMillis() + 50000;

        Assert.assertTrue(store.create(TransientIdParameters.CONTEXT, id, principalTokenId, expiration),
                "initial store");

        final BaseTransientDecoder decoder = new BaseTransientDecoder(){};
        decoder.setId("decoder");
        decoder.setIdStore(store);
        decoder.initialize();

        Assert.assertEquals(decoder.decode(id, RECIPIENT), PRINCIPAL);
    }

    @Test public void testExpired() throws Exception {

        final String principalTokenId;
        principalTokenId = new TransientIdParameters(RECIPIENT, PRINCIPAL).encode();

        final String id = "THE_ID";

        final long expiration = System.currentTimeMillis() - 50000;

        Assert.assertTrue(store.create(TransientIdParameters.CONTEXT, id, principalTokenId, expiration),
                "initial store");

        final BaseTransientDecoder decoder = new BaseTransientDecoder(){};
        decoder.setId("decoder");
        decoder.setIdStore(store);
        decoder.initialize();

        Assert.assertNull(decoder.decode(id, RECIPIENT));
    }


    @Test public void testNotFound() throws Exception {

        final BaseTransientDecoder decoder = new BaseTransientDecoder(){};
        decoder.setId("decoder");
        decoder.setIdStore(store);
        decoder.initialize();

        Assert.assertNull(decoder.decode("THE_ID", RECIPIENT));
    }


    @Test(expectedExceptions={NameDecoderException.class,}) public void testBadRecipient() throws Exception {

        final String principalTokenId;
        principalTokenId = new TransientIdParameters(RECIPIENT, PRINCIPAL).encode();

        final String id = "THE_ID";

        final long expiration = System.currentTimeMillis() + 50000;

        Assert.assertTrue(store.create(TransientIdParameters.CONTEXT, id, principalTokenId, expiration),
                "initial store");

        final BaseTransientDecoder decoder = new BaseTransientDecoder(){};
        decoder.setId("decoder");
        decoder.setIdStore(store);
        decoder.initialize();

        decoder.decode(id, PRINCIPAL);
    }

    
    @Test public void decode() throws Exception {
        
        final StoredTransientIdGenerationStrategy strategy = new StoredTransientIdGenerationStrategy();
        strategy.setId("strategy");
        strategy.setIdStore(store);        
        strategy.initialize();
        
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId("id");
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        defn.initialize();
    
        final IdPAttribute result =
                defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID,
                        TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));
    
    
        final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
        encoder.setNameFormat("https://example.org/");
        final NameID nameid = encoder.encode(result);

        final NameIDCanonicalizationFlowDescriptor descriptor = new NameIDCanonicalizationFlowDescriptor();
        descriptor.setFormats(Collections.singleton("https://example.org/"));
        descriptor.setId("NameIdFlowDescriptor");
        descriptor.initialize();
        final NameIDCanonicalization canon = new NameIDCanonicalization();
       
        final TransientNameIDDecoder decoder = new TransientNameIDDecoder();
        decoder.setId("decoder");
        decoder.setIdStore(store);
        decoder.initialize();
        canon.setDecoder(decoder);
        canon.initialize();
        
        final ProfileRequestContext prc = new ProfileRequestContext<>();
        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        final Subject subject = new Subject();
        subject.getPrincipals().add(new NameIDPrincipal(nameid));
        scc.setSubject(subject);
        scc.setAttemptedFlow(descriptor);
        
        scc.setRequesterId(TestSources.SP_ENTITY_ID);
        scc.setResponderId(TestSources.IDP_ENTITY_ID);
        
        canon.execute(prc);
        
        ActionTestingSupport.assertProceedEvent(prc);
        
        Assert.assertEquals(scc.getPrincipalName(), TestSources.PRINCIPAL_ID);
    }

}