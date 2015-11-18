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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.util.Iterator;

import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HTTPMetadataProviderParserTest extends AbstractMetadataParserTest {

    
    @Test public void entity() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntity.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntity");
        
   
        final Iterator<EntityDescriptor> entities = resolver.resolve(criteriaFor(IDP_ID)).iterator();
        Assert.assertTrue(resolver.isFailFastInitialization());
        Assert.assertTrue(resolver.isRequireValidMetadata());
        
        Assert.assertEquals(entities.next().getEntityID(), IDP_ID);
        Assert.assertFalse(entities.hasNext());

        Assert.assertEquals(resolver.getRefreshDelayFactor(), 0.75, 0.001);
        Assert.assertSame(resolver.getParserPool(), parserPool);
        
        Assert.assertNull(resolver.resolveSingle(criteriaFor(SP_ID)));
    }
    
    @Test public void httpsNoTrustEngine() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-noTrustEngine.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    }
    
    @Test public void httpsTrustEngineExplicitKey() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-trustEngine-explicitKey.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    }
    
    @Test(expectedExceptions=BeanCreationException.class) public void httpsTrustEngineInvalidKey() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-trustEngine-invalidKey.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    } 
    
    @Test public void httpsTrustEngineValidPKIX() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-trustEngine-validPKIX.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    }
    
    @Test public void httpsTrustEngineValidPKIXExplicitTrustedName() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-trustEngine-validPKIX-explicitTrustedName.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    }
    
    @Test(expectedExceptions=BeanCreationException.class) public void httpsTrustEngineInvalidPKIX() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "https-trustEngine-invalidPKIX.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPSEntity");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
    } 
    
    /**Test the proxy parameters.  This will throw an exception because we do not
     * have a proxy to test against.  It is here to allow hand walking of the code during
     * development and as a placeholder against when we get a proxy gost.
     * @throws Exception
     */
    @Test(expectedExceptions={BeanCreationException.class,}, enabled=false) public void proxy() throws Exception {

        getBean(HTTPMetadataResolver.class, "HTTPProxy.xml", "beans.xml");
    }

    @Test public void entities() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntities.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntities");
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(SP_ID)));
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        Assert.assertNotSame(resolver.getParserPool(), parserPool);
        
    }
    
    @Test public void httpClient() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntitiesClient.xml", "beans.xml", "httpClient.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntities");
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(SP_ID)));
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        Assert.assertNotSame(resolver.getParserPool(), parserPool);
        
    }
    
    @Test public void httpCachingNone() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntity-httpCaching-none.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntity");
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        
    }
    
    @Test public void httpCachingMemory() throws Exception {

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntity-httpCaching-memory.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntity");
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        
    }
    
    @Test public void httpCachingFile() throws Exception {
        System.out.println(System.getProperty("java.io.tmpdir"));

        HTTPMetadataResolver resolver = getBean(HTTPMetadataResolver.class, "HTTPEntity-httpCaching-file.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "HTTPEntity");
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        
    }
    
}
