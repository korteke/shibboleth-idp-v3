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
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.TransientIdAttributeDefinitionParser;
import net.shibboleth.idp.saml.attribute.resolver.impl.TransientIdAttributeDefinition;
import net.shibboleth.idp.saml.nameid.impl.StoredTransientIdGenerationStrategy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link TransientIdAttributeDefinitionParser}
 */
public class TransientIdAttributeDefinitionParserTest extends BaseAttributeDefinitionParserTest {

    private TransientIdAttributeDefinition getDefinition(String fileName) {

        return getAttributeDefn(fileName, "idStore.xml", TransientIdAttributeDefinition.class);
    }

    @Test public void withTime() throws ComponentInitializationException {

        try {
            getDefinition("transientWithTime.xml");
            Assert.fail();
        } catch (BeanCreationException e) {
            // OK
        }
        TransientIdAttributeDefinition defn =
                getAttributeDefn("transientWithTime.xml", "idStore2.xml", TransientIdAttributeDefinition.class);

        Assert.assertEquals(defn.getId(), "transientIdWithTime");
        
        Assert.assertTrue(defn.isInitialized());
        
        StoredTransientIdGenerationStrategy generator = (StoredTransientIdGenerationStrategy) defn.getTransientIdGenerationStrategy();
        
        Assert.assertEquals(generator.getIdLifetime(), 1000 * 60 * 3);
        Assert.assertEquals(generator.getIdSize(), 16);
    }

    @Test public void noTime() throws ComponentInitializationException {

        TransientIdAttributeDefinition defn = getDefinition("transientNoTime.xml");
        Assert.assertTrue(defn.isInitialized());
        
        StoredTransientIdGenerationStrategy generator = (StoredTransientIdGenerationStrategy) defn.getTransientIdGenerationStrategy();

        Assert.assertEquals(defn.getId(), "transientId");
        Assert.assertEquals(generator.getIdLifetime(), 1000 * 60 * 60 * 4);
        Assert.assertEquals(generator.getIdSize(), 16);
    }

}
