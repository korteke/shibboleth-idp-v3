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
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.profile.RequestContextBuilder;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.testing.DatabaseTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.profile.SAML2ActionTestingSupport;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Test for {@link StoredPersistentIdDecoder}. */
public class StoredPersistentIdDecoderTest extends OpenSAMLInitBaseTestCase {

    private DataSource testSource;
    
    private ProfileRequestContext prc;
    
    private PersistentSAML2NameIDGenerator generator;
    
    private StoredPersistentIdDecoder decoder;
    
    @BeforeClass public void setUpSource() {
        testSource = DatabaseTestingSupport.GetMockDataSource(PersistentSAML2NameIDGeneratorTest.INIT_FILE, "StoredIDDataConnectorStore");
    }
    
    @AfterClass public void teardown() {
        DatabaseTestingSupport.InitializeDataSource(PersistentSAML2NameIDGeneratorTest.DELETE_FILE, testSource);
    }

    @SuppressWarnings("deprecation")
    @BeforeMethod public void setUp() throws SQLException, IOException, ComponentInitializationException {
        
        final JDBCPersistentIdStore store = new JDBCPersistentIdStore();
        store.setDataSource(testSource);
        store.initialize();
        
        final StoredPersistentIdGenerationStrategy strategy = new StoredPersistentIdGenerationStrategy();
        strategy.setIDStore(store);
        strategy.initialize();
        
        generator = new PersistentSAML2NameIDGenerator();
        generator.setId("test");
        generator.setPersistentIdGenerator(strategy);
        generator.setAttributeSourceIds(Collections.singletonList("SOURCE"));
        
        decoder = new StoredPersistentIdDecoder();
        decoder.setId("test");
        decoder.setPersistentIdStore(store);
        decoder.initialize();

        prc = new RequestContextBuilder().setInboundMessageIssuer(TestSources.SP_ENTITY_ID)
                .setOutboundMessageIssuer(TestSources.IDP_ENTITY_ID).buildProfileRequestContext();
    }
    
    @Test
    public void testMissingID() throws Exception {

        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        scc.setRequesterId(TestSources.SP_ENTITY_ID);
        scc.setResponderId(TestSources.IDP_ENTITY_ID);
        
        final Subject subject = SAML2ActionTestingSupport.buildSubject("foo");
        Assert.assertNull(decoder.decode(scc, subject.getNameID()));
    }

    @Test(expectedExceptions={NameDecoderException.class})
    public void testNoQualifiers() throws Exception {

        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        
        final Subject subject = SAML2ActionTestingSupport.buildSubject("foo");
        decoder.decode(scc, subject.getNameID());
    }
    
    @Test
    public void testBadQualifier() throws Exception {
        generator.initialize();
        
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("foo");
        Assert.assertNull(generator.generate(prc, NameID.PERSISTENT));
        
        final IdPAttribute source = new IdPAttribute("SOURCE");
        source.setValues(Collections.singleton(new StringAttributeValue(TestSources.COMMON_ATTRIBUTE_VALUE_STRING)));
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setUnfilteredIdPAttributes(
                Collections.singleton(source));
        final NameID id = generator.generate(prc, NameID.PERSISTENT);
        Assert.assertNotNull(id);
        Assert.assertNotNull(id.getValue());
        Assert.assertEquals(id.getFormat(), NameID.PERSISTENT);

        id.setNameQualifier(null);
        id.setSPNameQualifier(null);
        
        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        scc.setRequesterId("Bad");
        scc.setResponderId(TestSources.IDP_ENTITY_ID);
        
        Assert.assertNull(decoder.decode(scc, id));
    }
    
    @Test
    public void testStoredIdDecode() throws Exception {
        generator.initialize();
        
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("foo");
        Assert.assertNull(generator.generate(prc, NameID.PERSISTENT));
        
        final IdPAttribute source = new IdPAttribute("SOURCE");
        source.setValues(Collections.singleton(new StringAttributeValue(TestSources.COMMON_ATTRIBUTE_VALUE_STRING)));
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setUnfilteredIdPAttributes(
                Collections.singleton(source));
        final NameID id = generator.generate(prc, NameID.PERSISTENT);
        Assert.assertNotNull(id);
        Assert.assertNotNull(id.getValue());
        Assert.assertEquals(id.getFormat(), NameID.PERSISTENT);
        Assert.assertEquals(id.getNameQualifier(), TestSources.IDP_ENTITY_ID);
        Assert.assertEquals(id.getSPNameQualifier(), TestSources.SP_ENTITY_ID);

        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        scc.setRequesterId(TestSources.SP_ENTITY_ID);
        scc.setResponderId(TestSources.IDP_ENTITY_ID);
        
        final String decoded = decoder.decode(scc, id);
        Assert.assertEquals(decoded, "foo");
    }
    
    @Test
    public void testAffiliation() throws Exception {
        generator.setSPNameQualifier("http://affiliation.org");
        generator.initialize();
        
        prc.getSubcontext(SubjectContext.class, true).setPrincipalName("foo");
        Assert.assertNull(generator.generate(prc, NameID.PERSISTENT));
        
        final IdPAttribute source = new IdPAttribute("SOURCE");
        source.setValues(Collections.singleton(new StringAttributeValue(TestSources.COMMON_ATTRIBUTE_VALUE_STRING)));
        prc.getSubcontext(RelyingPartyContext.class).getSubcontext(AttributeContext.class, true).setUnfilteredIdPAttributes(
                Collections.singleton(source));
        final NameID id = generator.generate(prc, NameID.PERSISTENT);
        Assert.assertNotNull(id);
        Assert.assertNotNull(id.getValue());
        Assert.assertEquals(id.getFormat(), NameID.PERSISTENT);
        Assert.assertEquals(id.getNameQualifier(), TestSources.IDP_ENTITY_ID);
        Assert.assertEquals(id.getSPNameQualifier(), "http://affiliation.org");

        final SubjectCanonicalizationContext scc = prc.getSubcontext(SubjectCanonicalizationContext.class, true);
        scc.setRequesterId(TestSources.SP_ENTITY_ID);
        scc.setResponderId(TestSources.IDP_ENTITY_ID);
        
        final String decoded = decoder.decode(scc, id);
        Assert.assertEquals(decoded, "foo");
    }

}