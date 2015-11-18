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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.impl;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.ChainingParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.EntitiesDescriptorNameParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.EntityAttributesFilterParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.EntityRoleFilterParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.KeyAuthorityParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.NodeProcessingParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.PredicateFilterParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.RequiredValidUntilParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.SchemaValidationParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl.SignatureValidationParser;
import net.shibboleth.idp.profile.spring.resource.impl.ClasspathResourceParser;
import net.shibboleth.idp.profile.spring.resource.impl.SVNResourceParser;

/** Namespace handler for <code>urn:mace:shibboleth:2.0:metadata</code>. */
public class MetadataNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Metadata provider element name. */
    public static final QName METADATA_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "MetadataProvider");

    /** Metadata node processor Element name. */
    public static final QName METADATA_NODE_PROC_ELEMENT_NAME = new QName(
            AbstractMetadataProviderParser.METADATA_NAMESPACE, "MetadataNodeProcessor");

    /** {@inheritDoc} */
    @Override public void init() {
        // Profile Configuration
        registerBeanDefinitionParser(AbstractMetadataProviderParser.CHAINING_PROVIDER_ELEMENT_NAME,
                new ChainingMetadataProviderParser());
        registerBeanDefinitionParser(InlineMetadataProviderParser.ELEMENT_NAME, new InlineMetadataProviderParser());
        registerBeanDefinitionParser(FilesystemMetadataProviderParser.ELEMENT_NAME,
                new FilesystemMetadataProviderParser());
        registerBeanDefinitionParser(HTTPMetadataProviderParser.ELEMENT_NAME, new HTTPMetadataProviderParser());
        registerBeanDefinitionParser(FileBackedHTTPMetadataProviderParser.ELEMENT_NAME,
                new FileBackedHTTPMetadataProviderParser());
        registerBeanDefinitionParser(ResourceBackedMetadataProviderParser.ELEMENT_NAME,
                new ResourceBackedMetadataProviderParser());
        registerBeanDefinitionParser(DynamicHTTPMetadataProviderParser.ELEMENT_NAME,
                new DynamicHTTPMetadataProviderParser());

        // Resources
        registerBeanDefinitionParser(ClasspathResourceParser.ELEMENT_NAME, new ClasspathResourceParser());
        registerBeanDefinitionParser(SVNResourceParser.ELEMENT_NAME, new SVNResourceParser());

        // Filters
        registerBeanDefinitionParser(RequiredValidUntilParser.TYPE_NAME, new RequiredValidUntilParser());
        registerBeanDefinitionParser(ChainingParser.TYPE_NAME, new ChainingParser());
        registerBeanDefinitionParser(EntityAttributesFilterParser.TYPE_NAME, new EntityAttributesFilterParser());
        registerBeanDefinitionParser(EntityRoleFilterParser.TYPE_NAME, new EntityRoleFilterParser());
        registerBeanDefinitionParser(PredicateFilterParser.TYPE_NAME, new PredicateFilterParser());
        registerBeanDefinitionParser(SchemaValidationParser.TYPE_NAME, new SchemaValidationParser());
        registerBeanDefinitionParser(SignatureValidationParser.TYPE_NAME, new SignatureValidationParser());
        registerBeanDefinitionParser(NodeProcessingParser.TYPE_NAME, new NodeProcessingParser());

        // Node Processors
        registerBeanDefinitionParser(EntitiesDescriptorNameParser.TYPE_NAME, new EntitiesDescriptorNameParser());
        registerBeanDefinitionParser(KeyAuthorityParser.TYPE_NAME, new KeyAuthorityParser());
    }

}