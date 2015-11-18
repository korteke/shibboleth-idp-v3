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

package net.shibboleth.idp.saml.attribute.resolver.impl;

import java.io.IOException;
import java.util.List;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.idp.saml.nameid.impl.CryptoTransientIdGenerationStrategy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.BasicKeystoreKeyStrategy;
import net.shibboleth.utilities.java.support.security.DataSealer;
import net.shibboleth.utilities.java.support.security.DataSealerException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for CryptoTransientIdAttributeDefinition, which is a
 * 
 * @link {@link TransientIdAttributeDefinition} with an injected {@link CryptoTransientIdGenerationStrategy}
 */

public class CryptoTransientIdAttributeDefinitionTest extends OpenSAMLInitBaseTestCase {

    private static final String ID = "CryptoTransientIdAttributeDefn";

    private static final long TIMEOUT = 500;

    private CryptoTransientIdGenerationStrategy strategy;

    private DataSealer dataSealer;

    /**
     * Set up the data sealer. We take advantage of the fact that Spring a {@link ClassPathResource} wraps a files.
     * 
     * @throws IOException
     * @throws DataSealerException
     * @throws ComponentInitializationException
     */
    @BeforeClass public void setupStrategyAndSealer() throws IOException, DataSealerException,
            ComponentInitializationException {

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
        
        dataSealer = new DataSealer();
        dataSealer.setKeyStrategy(kstrategy);
        dataSealer.initialize();

        strategy = new CryptoTransientIdGenerationStrategy();
        strategy.setDataSealer(dataSealer);
        strategy.setId("strategy");
        strategy.setIdLifetime(TIMEOUT);
        strategy.initialize();

    }

    @Test public void badVals() throws ComponentInitializationException {
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId(ID);
        defn.initialize();

        AttributeResolutionContext context = new AttributeResolutionContext();
        context.getSubcontext(AttributeResolverWorkContext.class, true);
        try {
            defn.resolve(context);
            Assert.fail("No SP");
        } catch (ResolutionException e) {
            // OK
        }

        try {
            defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID, null));
            Assert.fail("No SP");
        } catch (ResolutionException e) {
            // OK
        }
        try {
            defn.resolve(TestSources.createResolutionContext(null, TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));
            Assert.fail("No Principal");
        } catch (ResolutionException e) {
            // OK
        }
    }

    @Test public void encode() throws ComponentInitializationException, ResolutionException, DataSealerException,
            InterruptedException {
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId(ID);
        defn.initialize();

        final AttributeResolutionContext context =
                TestSources.createResolutionContext(TestSources.PRINCIPAL_ID, TestSources.IDP_ENTITY_ID,
                        TestSources.SP_ENTITY_ID);

        final IdPAttribute result = defn.resolve(context);

        final List<IdPAttributeValue<?>> values = result.getValues();
        Assert.assertEquals(values.size(), 1);
        final String code = ((StringAttributeValue) values.get(0)).getValue();

        final String decode = dataSealer.unwrap(code);

        Assert.assertEquals(decode, TestSources.SP_ENTITY_ID + "!" + TestSources.PRINCIPAL_ID);

        Thread.sleep(2*TIMEOUT);
        try {
            dataSealer.unwrap(code);
            Assert.fail("Timeout not set correctly");
        } catch (Exception e) {
            // OK
        }
    }
}