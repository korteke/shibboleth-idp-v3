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

package net.shibboleth.idp.attribute.filter.spring.saml.impl;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;

/** Namespace handler for the attribute filter engine SAML match functions. */
public class AttributeFilterSAMLNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Basic match function namespace. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:afp:mf:saml";

    /** {@inheritDoc} */
    @Override public void init() {

        registerBeanDefinitionParser(AttributeIssuerEntityAttributeExactRuleParser.SCHEMA_TYPE,
                new AttributeIssuerEntityAttributeExactRuleParser());

        registerBeanDefinitionParser(AttributeIssuerEntityAttributeRegexRuleParser.SCHEMA_TYPE,
                new AttributeIssuerEntityAttributeRegexRuleParser());

        registerBeanDefinitionParser(AttributeRequesterEntityAttributeExactRuleParser.SCHEMA_TYPE,
                new AttributeRequesterEntityAttributeExactRuleParser());
        registerBeanDefinitionParser(AttributeRequesterEntityAttributeExactRuleParser.SCHEMA_TYPE_V2,
                new AttributeRequesterEntityAttributeExactRuleParser());

        registerBeanDefinitionParser(AttributeRequesterEntityAttributeRegexRuleParser.SCHEMA_TYPE,
                new AttributeRequesterEntityAttributeRegexRuleParser());
        registerBeanDefinitionParser(AttributeRequesterEntityAttributeRegexRuleParser.SCHEMA_TYPE_V2,
                new AttributeRequesterEntityAttributeRegexRuleParser());

        registerBeanDefinitionParser(AttributeRequesterNameIdFormatRuleParser.SCHEMA_TYPE,
                new AttributeRequesterNameIdFormatRuleParser());
        registerBeanDefinitionParser(AttributeRequesterNameIdFormatRuleParser.SCHEMA_TYPE_V2,
                new AttributeRequesterNameIdFormatRuleParser());

        registerBeanDefinitionParser(AttributeIssuerNameIdFormatRuleParser.SCHEMA_TYPE,
                new AttributeIssuerNameIdFormatRuleParser());

        registerBeanDefinitionParser(AttributeIssuerInEntityGroupRuleParser.SCHEMA_TYPE,
                new AttributeIssuerInEntityGroupRuleParser());

        registerBeanDefinitionParser(AttributeRequesterInEntityGroupRuleParser.SCHEMA_TYPE,
                new AttributeRequesterInEntityGroupRuleParser());
        registerBeanDefinitionParser(AttributeRequesterInEntityGroupRuleParser.SCHEMA_TYPE_V2,
                new AttributeRequesterInEntityGroupRuleParser());

        registerBeanDefinitionParser(AttributeInMetadataRuleParser.ATTRIBUTE_IN_METADATA,
                new AttributeInMetadataRuleParser());

        registerBeanDefinitionParser(MappedAttributeInMetadataRuleParser.MAPPED_ATTRIBUTE_IN_METADATA,
                new MappedAttributeInMetadataRuleParser());

        registerBeanDefinitionParser(RegistrationAuthorityRuleParser.SCHEMA_TYPE,
                new RegistrationAuthorityRuleParser());
    }
    
}