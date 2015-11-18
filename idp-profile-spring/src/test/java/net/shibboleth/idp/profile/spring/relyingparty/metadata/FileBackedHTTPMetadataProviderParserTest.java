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

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileBackedHTTPMetadataProviderParserTest extends AbstractMetadataParserTest {

    
    @Test public void entity() throws Exception {

        FileBackedHTTPMetadataResolver resolver = getBean(FileBackedHTTPMetadataResolver.class, "fileBackedHTTPEntity.xml", "beans.xml");
        
        Assert.assertEquals(resolver.getId(), "fileBackedHTTPEntity");
        
   
        final Iterator<EntityDescriptor> entities = resolver.resolve(criteriaFor(IDP_ID)).iterator();
        Assert.assertTrue(resolver.isFailFastInitialization());
        Assert.assertTrue(resolver.isRequireValidMetadata());
        
        Assert.assertEquals(entities.next().getEntityID(), IDP_ID);
        Assert.assertFalse(entities.hasNext());

        Assert.assertEquals(resolver.getRefreshDelayFactor(), 0.75, 0.001);
        Assert.assertSame(resolver.getParserPool(), parserPool);
        
        Assert.assertNull(resolver.resolveSingle(criteriaFor(SP_ID)));
    }
    
    @Test public void entities() throws Exception {

        MetadataResolver resolver = getBean(FileBackedHTTPMetadataResolver.class, "fileBackedHTTPEntities.xml", "beans.xml");

        Assert.assertEquals(resolver.getId(), "fileBackedHTTPEntities");
        
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(IDP_ID)));
        Assert.assertNotNull(resolver.resolveSingle(criteriaFor(SP_ID)));
    }
    
}
