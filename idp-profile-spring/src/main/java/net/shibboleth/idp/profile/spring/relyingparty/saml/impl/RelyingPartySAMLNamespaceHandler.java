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

package net.shibboleth.idp.profile.spring.relyingparty.saml.impl;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;

/**
 * Namespace handler for the Relying Party SAML configuration. Perfoms no function since we do not parse these children
 * in this way, but serves as an anchor and site for future extension.
 */
public class RelyingPartySAMLNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:relying-party:saml";

    /** {@inheritDoc} */
    @Override public void init() {
        registerBeanDefinitionParser(SAML2ArtifactResolutionProfileParser.ELEMENT_NAME,
                new SAML2ArtifactResolutionProfileParser());
        registerBeanDefinitionParser(SAML2LogoutRequestProfileParser.ELEMENT_NAME,
                new SAML2LogoutRequestProfileParser());
        registerBeanDefinitionParser(SAML2AttributeQueryProfileParser.ELEMENT_NAME,
                new SAML2AttributeQueryProfileParser());
        registerBeanDefinitionParser(SAML2BrowserSSOProfileParser.ELEMENT_NAME, new SAML2BrowserSSOProfileParser());
        registerBeanDefinitionParser(SAML2ECPProfileParser.ELEMENT_NAME, new SAML2ECPProfileParser());

        registerBeanDefinitionParser(SAML1ArtifactResolutionProfileParser.ELEMENT_NAME,
                new SAML1ArtifactResolutionProfileParser());
        registerBeanDefinitionParser(SAML1AttributeQueryProfileParser.ELEMENT_NAME,
                new SAML1AttributeQueryProfileParser());
        registerBeanDefinitionParser(SAML1AttributeQueryProfileParser.ELEMENT_NAME,
                new SAML1AttributeQueryProfileParser());
        registerBeanDefinitionParser(ShibbolethSSOProfileParser.ELEMENT_NAME, new ShibbolethSSOProfileParser());

    }
}