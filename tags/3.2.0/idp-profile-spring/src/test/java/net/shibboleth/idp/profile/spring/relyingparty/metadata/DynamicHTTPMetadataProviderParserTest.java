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

import java.util.Arrays;
import java.util.Collections;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.HTTPEntityIDRequestURLBuilder;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamicHTTPMetadataProviderParserTest extends AbstractMetadataParserTest {
    
    @Test
    public void testDefaults() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicDefaults.xml", "beans.xml");
        
        Assert.assertTrue(resolver.isInitialized());
        Assert.assertTrue(resolver.isFailFastInitialization());
        Assert.assertTrue(resolver.isRequireValidMetadata());
        Assert.assertNull(resolver.getMetadataFilter());
        Assert.assertNotNull(resolver.getParserPool());
        
        Assert.assertEquals(resolver.getRefreshDelayFactor(), 0.75f);
        Assert.assertEquals(resolver.getMinCacheDuration(), new Long(10*60*1000L));
        Assert.assertEquals(resolver.getMaxCacheDuration(), new Long(8*60*60*1000L));
        Assert.assertEquals(resolver.getMaxIdleEntityData(), new Long(8*60*60*1000L));
        Assert.assertTrue(resolver.isRemoveIdleEntityData());
        Assert.assertEquals(resolver.getCleanupTaskInterval(), new Long(30*60*1000L));
        
        Assert.assertEquals(resolver.getSupportedContentTypes(), 
                Arrays.asList("application/samlmetadata+xml", "application/xml", "text/xml"));
        
        Assert.assertEquals(resolver.getRequestURLBuilder().getClass(), HTTPEntityIDRequestURLBuilder.class);
    }
    
    @Test
    public void testBasicParams() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicBasicParams.xml", "beans.xml", "httpClient.xml");
        
        Assert.assertTrue(resolver.isInitialized());
        Assert.assertFalse(resolver.isFailFastInitialization());
        Assert.assertFalse(resolver.isRequireValidMetadata());
        Assert.assertNull(resolver.getMetadataFilter());
        Assert.assertNotNull(resolver.getParserPool());
        
        Assert.assertEquals(resolver.getRefreshDelayFactor(), 0.50f);
        Assert.assertEquals(resolver.getMinCacheDuration(), new Long(5*60*1000L));
        Assert.assertEquals(resolver.getMaxCacheDuration(), new Long(4*60*60*1000L));
        Assert.assertEquals(resolver.getMaxIdleEntityData(), new Long(2*60*60*1000L));
        Assert.assertFalse(resolver.isRemoveIdleEntityData());
        Assert.assertEquals(resolver.getCleanupTaskInterval(), new Long(20*60*1000L));
        
        Assert.assertEquals(resolver.getSupportedContentTypes(), Collections.singletonList("text/xml"));
        
        Assert.assertEquals(resolver.getRequestURLBuilder().getClass(), HTTPEntityIDRequestURLBuilder.class);
    }
    
    @Test
    public void testWellKnown() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicWellKnown.xml", "beans.xml");
        
        //TODO update with permanent test target, if there is a better one.
        String entityID = "https://issues.shibboleth.net/shibboleth";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testTemplate() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicTemplate.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testMDQ() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicMetadataQueryProtocol.xml", "beans.xml");
        
        String entityID = "https://foo1.example.org/idp/shibboleth";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testRegex() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamicRegex.xml", "beans.xml");
        
        String entityID = "https://idp.example.org/idp/shibboleth";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHttpCachingNone() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-httpCaching-none.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testHttpCachingMemory() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-httpCaching-memory.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }

    @Test
    public void testHttpCachingFile() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-httpCaching-file.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSNoTrustEngine() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-noTrustEngine.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineExplicitKey() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-trustEngine-explicitKey.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineInvalidKey() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-trustEngine-invalidKey.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNull(ed);
    }
    
    @Test
    public void testHTTPSTrustEngineValidPKIX() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-trustEngine-validPKIX.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineValidPKIXExplicitTrustedName() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-trustEngine-validPKIX-explicitTrustedName.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNotNull(ed);
        Assert.assertEquals(ed.getEntityID(), entityID);
    }
    
    @Test
    public void testHTTPSTrustEngineInvalidPKIX() throws Exception {
        FunctionDrivenDynamicHTTPMetadataResolver resolver = getBean(FunctionDrivenDynamicHTTPMetadataResolver.class, 
                "dynamic-https-trustEngine-invalidPKIX.xml", "beans.xml");
        
        String entityID = "https://www.example.org/sp";
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIdCriterion(entityID));
        
        EntityDescriptor ed = resolver.resolveSingle(criteriaSet);
        Assert.assertNull(ed);
    }

}
