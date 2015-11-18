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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import java.util.Collections;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.AttributeRequesterEntityAttributeExactPolicyRule;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataNodeProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.EntitiesDescriptorNameProcessor;
import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.testng.annotations.BeforeClass;

/**
 * tests for {@link AttributeRequesterEntityAttributeExactPolicyRule}.
 */
public class BaseMetadataTests extends XMLObjectBaseTestCase {

    private EntitiesDescriptor metadata;

    static private final String IDP_ENTITY_ID = "https://idp.shibboleth.net/idp/shibboleth";

    static private final String JIRA_ENTITY_ID = "https://issues.shibboleth.net/shibboleth";

    static private final String WIKI_ENTITY_ID = "https://wiki.shibboleth.net/shibboleth";

    static private final String NONE_ENTITY_ID = "https://none.shibboleth.net/shibboleth";

    protected EntityDescriptor idpEntity;

    protected EntityDescriptor jiraEntity;

    protected EntityDescriptor wikiEntity;

    protected EntityDescriptor noneEntity;

    @BeforeClass(dependsOnMethods = "initXMLObjectSupport") public void setUp()
            throws FilterException, ComponentInitializationException {
        metadata = unmarshallElement("/data/net/shibboleth/idp/filter/impl/saml/shibboleth.net-metadata.xml");
        
        final NodeProcessingMetadataFilter filter = new NodeProcessingMetadataFilter();
        filter.setNodeProcessors(Collections.<MetadataNodeProcessor>singletonList(new EntitiesDescriptorNameProcessor()));
        filter.initialize();
        filter.filter(metadata);

        for (EntityDescriptor entity : metadata.getEntityDescriptors()) {
            if (IDP_ENTITY_ID.equals(entity.getEntityID())) {
                idpEntity = entity;
            } else if (JIRA_ENTITY_ID.equals(entity.getEntityID())) {
                jiraEntity = entity;
            } else if (WIKI_ENTITY_ID.equals(entity.getEntityID())) {
                wikiEntity = entity;
            } else if (NONE_ENTITY_ID.equals(entity.getEntityID())) {
                noneEntity = entity;
            }
        }
    }

    static protected AttributeFilterContext metadataContext(EntityDescriptor sp, String principal) {

        AttributeFilterContext filterContext = new AttributeFilterContext();
        SAMLMetadataContext metadataContext = filterContext.getSubcontext(SAMLMetadataContext.class, true);

        metadataContext.setEntityDescriptor(sp);
        if (sp != null) {
            metadataContext.setRoleDescriptor(sp.getSPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol"));
            filterContext.setAttributeRecipientID(sp.getEntityID());
        }

        filterContext.setPrincipal(principal);
        filterContext
                .setRequesterMetadataContextLookupStrategy(new ChildContextLookup<AttributeFilterContext, SAMLMetadataContext>(
                        SAMLMetadataContext.class, false));
        return filterContext;
    }
}
