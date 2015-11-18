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

package net.shibboleth.idp.attribute.resolver.spring.ad;

import net.shibboleth.idp.attribute.resolver.spring.BaseAttributeDefinitionParserTest;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.CryptoTransientIdAttributeDefinitionParser;
import net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition;
import net.shibboleth.idp.saml.nameid.impl.CryptoTransientIdGenerationStrategy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link CryptoTransientIdAttributeDefinitionParser}
 */
public class CryptoTransientIdAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    private TransientIdAttributeDefinition getDefinition(String fileName) {

        return getAttributeDefn(fileName, "sealer.xml", TransientIdAttributeDefinition.class);
    }

    @Test public void withTime() throws ComponentInitializationException {

        TransientIdAttributeDefinition defn = getDefinition("cryptoWithTime.xml");

        Assert.assertTrue(defn.isInitialized());

        CryptoTransientIdGenerationStrategy strategy =
                (CryptoTransientIdGenerationStrategy) defn.getTransientIdGenerationStrategy();

        Assert.assertEquals(strategy.getIdLifetime(), 3 * 60 * 1000);
    }

    @Test public void noTime() throws ComponentInitializationException {

        TransientIdAttributeDefinition defn = getDefinition("cryptoNoTime.xml");
        Assert.assertTrue(defn.isInitialized());

        CryptoTransientIdGenerationStrategy strategy =
                (CryptoTransientIdGenerationStrategy) defn.getTransientIdGenerationStrategy();

        Assert.assertEquals(strategy.getIdLifetime(), 4 * 3600 * 1000);
    }
}
