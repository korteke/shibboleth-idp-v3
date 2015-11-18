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

package net.shibboleth.idp.attribute.resolver.spring.enc.impl;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/** Namespace handler for the attribute resolver. */
public class AttributeEncoderNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    @Nonnull @NotEmpty public static final String NAMESPACE = "urn:mace:shibboleth:2.0:attribute:encoder";

    /** {@inheritDoc} */
    @Override
    public void init() {
        registerBeanDefinitionParser(SAML1StringAttributeEncoderParser.TYPE_NAME,
                new SAML1StringAttributeEncoderParser());

        registerBeanDefinitionParser(SAML1Base64AttributeEncoderParser.TYPE_NAME,
                new SAML1Base64AttributeEncoderParser());

        registerBeanDefinitionParser(SAML1ScopedStringAttributeEncoderParser.TYPE_NAME,
                new SAML1ScopedStringAttributeEncoderParser());

        registerBeanDefinitionParser(SAML1XMLObjectAttributeEncoderParser.TYPE_NAME,
                new SAML1XMLObjectAttributeEncoderParser());

        registerBeanDefinitionParser(SAML1StringNameIdentifierEncoderParser.SCHEMA_TYPE,
                new SAML1StringNameIdentifierEncoderParser());

        registerBeanDefinitionParser(SAML2StringAttributeEncoderParser.TYPE_NAME,
                new SAML2StringAttributeEncoderParser());

        registerBeanDefinitionParser(SAML2ScopedStringAttributeEncoderParser.TYPE_NAME,
                new SAML2ScopedStringAttributeEncoderParser());

        registerBeanDefinitionParser(SAML2Base64AttributeEncoderParser.TYPE_NAME,
                new SAML2Base64AttributeEncoderParser());

        registerBeanDefinitionParser(SAML2XMLObjectAttributeEncoderParser.TYPE_NAME,
                new SAML2XMLObjectAttributeEncoderParser());

        registerBeanDefinitionParser(SAML2StringNameIDEncoderParser.SCHEMA_TYPE,
                new SAML2StringNameIDEncoderParser());
    }
    
}