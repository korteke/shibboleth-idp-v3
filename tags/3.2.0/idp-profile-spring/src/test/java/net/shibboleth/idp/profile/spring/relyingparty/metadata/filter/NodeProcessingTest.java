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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter;

import java.io.IOException;
import java.util.List;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataParserTest;
import net.shibboleth.idp.saml.security.impl.KeyAuthorityNodeProcessor;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.EntityGroupName;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.EntitiesDescriptorNameProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for NodeProcessingMetadataFilter parser.
 */
public class NodeProcessingTest extends AbstractMetadataParserTest {

    @Test public void empty() throws IOException {
        final MetadataResolver resolver = getBean(MetadataResolver.class, "nodeproc/empty.xml");

        final NodeProcessingMetadataFilter filter = (NodeProcessingMetadataFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getNodeProcessors().size(), 0);
    }

    @Test public void both() throws IOException, ResolverException {
        final MetadataResolver resolver = getBean(MetadataResolver.class, "nodeproc/both.xml");

        final NodeProcessingMetadataFilter filter = (NodeProcessingMetadataFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getNodeProcessors().size(), 2);
        Assert.assertEquals(filter.getNodeProcessors().get(0).getClass(), EntitiesDescriptorNameProcessor.class);
        Assert.assertEquals(filter.getNodeProcessors().get(1).getClass(), KeyAuthorityNodeProcessor.class);

        final EntityDescriptor entity =
                resolver.resolveSingle(new CriteriaSet(new EntityIdCriterion("https://sp.example.org/sp/shibboleth")));
        Assert.assertNotNull(entity);
        final List<EntityGroupName> groups = entity.getObjectMetadata().get(EntityGroupName.class);
        Assert.assertEquals(groups.size(), 1);
        Assert.assertEquals(groups.get(0).getName(), "Example");
    }

}