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

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataParserTest;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.xml.QNameSupport;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.EntityRoleFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for parser for EntityRoleWhileList filter
 */
public class EntityRoleFilterParserTest extends AbstractMetadataParserTest {

    @Test public void validUntil() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/entityRole.xml");

        final EntityRoleFilter filter = (EntityRoleFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getRemoveEmptyEntitiesDescriptors(), true);
        Assert.assertEquals(filter.getRemoveRolelessEntityDescriptors(), true);
        Assert.assertEquals(filter.getRoleWhiteList().size(), 0);
    }

    @Test public void param() throws IOException {
        MetadataResolver resolver = getBean(MetadataResolver.class, "filter/entityRoleParams.xml");

        final EntityRoleFilter filter = (EntityRoleFilter) resolver.getMetadataFilter();
        Assert.assertEquals(filter.getRemoveEmptyEntitiesDescriptors(), false);
        Assert.assertEquals(filter.getRemoveRolelessEntityDescriptors(), false);
        final List<QName> roles = filter.getRoleWhiteList();
        Assert.assertEquals(roles.size(), 2);
        QName r1 = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "qname1");
        Assert.assertTrue(r1.equals(roles.get(0)));
        Assert.assertTrue(roles.contains(QNameSupport.constructQName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "qname1", null)));
        Assert.assertTrue(roles.contains(QNameSupport.constructQName(SAMLConstants.SAML20MD_NS, "qname2", null)));
    }

}
