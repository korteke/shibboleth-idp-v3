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

package net.shibboleth.idp.attribute.resolver.spring.pc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl;
import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.pc.impl.StoredIdConnectorParser.NotImplementedNameIdentifierDecoder;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.attribute.principalconnector.impl.PrincipalConnector;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.impl.StoredPersistentIdDecoder;
import net.shibboleth.idp.testing.DatabaseTestingSupport;

import org.opensaml.saml.saml2.core.NameID;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * test for direct principal connector parsers.
 */
public class StoredIdTest extends BaseAttributeDefinitionParserTest {
    
    private static final String INIT_FILE = "/net/shibboleth/idp/attribute/resolver/spring/pc/StoredIdStore.sql";

    private static final String DELETE_FILE = "/net/shibboleth/idp/attribute/resolver/spring/pc/DeleteStore.sql";

    private DataSource testSource;
    
    private PrincipalConnector principalConnector;
    
    private DataConnector dataConnector; 
    
    private  AttributeResolverImpl ar;

    @BeforeTest public void setupSource() throws SQLException, IOException {

        testSource = DatabaseTestingSupport.GetMockDataSource(INIT_FILE, "storedId");
    }

    @AfterClass public void teardown() {
        DatabaseTestingSupport.InitializeDataSource(DELETE_FILE, testSource);
    }

    protected void setupConnectors(String fileName) {

        GenericApplicationContext context = new GenericApplicationContext();
        setTestContext(context);
        context.setDisplayName("ApplicationContext: " + PrincipalConnector.class);

        principalConnector = getBean(PRINCIPALCONNECTOR_FILE_PATH + fileName, PrincipalConnector.class, context);
        dataConnector = context.getBean("dc", DataConnector.class);
        ar = BaseAttributeDefinitionParserTest.getResolver(context);
    }



    @Test public void simple() {
        setupConnectors("stored.xml");
        
        Assert.assertEquals(principalConnector.getId(), "pc");
        Assert.assertEquals(dataConnector.getId(), "dc");
        
        Assert.assertTrue(principalConnector.getNameIDDecoder() instanceof StoredPersistentIdDecoder);
        Assert.assertTrue(principalConnector.getNameIdentifierDecoder() instanceof NotImplementedNameIdentifierDecoder);
        Assert.assertEquals(principalConnector.getFormat(), "https://example.org/stored");
        Assert.assertTrue(principalConnector.getRelyingParties().isEmpty());
    }
    
    @Test public void case426() throws ResolutionException {
        setupConnectors("stored.xml");
        
        final AttributeResolutionContext context = new AttributeResolutionContext();
        context.setPrincipal("PRINCIPAL");
        context.setAttributeIssuerID("ISSUER");
        context.setAttributeRecipientID("Recipient");
        context.getSubcontext(AttributeResolverWorkContext.class, true);
       
        ar.resolveAttributes(context);
        
        final Map<String, IdPAttribute> result = context.getResolvedIdPAttributes();

        Assert.assertNotNull(result.get("result"));
    }

    
    @Test public void directStore() throws ResolutionException, AttributeEncodingException, NameDecoderException {
        setupConnectors("stored-direct.xml");
        
        final AttributeResolutionContext arc = new AttributeResolutionContext();
        arc.setPrincipal("PRINCIPAL");
        arc.setAttributeIssuerID("ISSUER");
        arc.setAttributeRecipientID("Recipient");
        arc.getSubcontext(AttributeResolverWorkContext.class, true);
       
        ar.resolveAttributes(arc);
        
        final IdPAttribute result = arc.getResolvedIdPAttributes().get("result");
        
        final AttributeEncoder<NameID> encoder = (AttributeEncoder<NameID>) ar.getAttributeDefinitions().get("result").getAttributeEncoders().iterator().next();
        
        NameID nameID = encoder.encode(result);
        
        Assert.assertEquals(result.getValues().size(), 1);
        
        final SubjectCanonicalizationContext scc = new SubjectCanonicalizationContext();
        scc.setPrincipalName(arc.getPrincipal());
        scc.setRequesterId(arc.getAttributeRecipientID());
        scc.setResponderId(arc.getAttributeIssuerID());
        
        Assert.assertEquals(principalConnector.getNameIDDecoder().decode(scc, nameID), arc.getPrincipal());
    }
    
    @Test public void connectorStore() throws ResolutionException, AttributeEncodingException, NameDecoderException {
        
        setupConnectors("stored-beanConnector.xml");
        
        final AttributeResolutionContext arc = new AttributeResolutionContext();
        arc.setPrincipal("PRINCIPAL");
        arc.setAttributeIssuerID("ISSUER");
        arc.setAttributeRecipientID("Recipient");
        arc.getSubcontext(AttributeResolverWorkContext.class, true);
       
        ar.resolveAttributes(arc);
        
        final IdPAttribute result = arc.getResolvedIdPAttributes().get("result");
        
        final AttributeEncoder<NameID> encoder = (AttributeEncoder<NameID>) ar.getAttributeDefinitions().get("result").getAttributeEncoders().iterator().next();
        
        NameID nameID = encoder.encode(result);
        
        Assert.assertEquals(result.getValues().size(), 1);
        
        final SubjectCanonicalizationContext scc = new SubjectCanonicalizationContext();
        scc.setPrincipalName(arc.getPrincipal());
        scc.setRequesterId(arc.getAttributeRecipientID());
        scc.setResponderId(arc.getAttributeIssuerID());
        
        Assert.assertEquals(principalConnector.getNameIDDecoder().decode(scc, nameID), arc.getPrincipal());
    }

    
}
