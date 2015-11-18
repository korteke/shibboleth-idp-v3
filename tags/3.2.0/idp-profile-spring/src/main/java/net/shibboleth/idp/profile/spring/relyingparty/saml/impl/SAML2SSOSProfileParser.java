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

import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser to generate {@link SSOSProfileConfiguration} from a <code>saml:SAML2SSOSProfile</code>.
 */
public class SAML2SSOSProfileParser extends SAML2BrowserSSOProfileParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(RelyingPartySAMLNamespaceHandler.NAMESPACE, "SAML2SSOSProfile");

    /** Constructor. */
    public SAML2SSOSProfileParser() {
        setArtifactAware(false);
    }

    /** {@inheritDoc} */
    @Override protected Class<SSOSProfileConfiguration> getBeanClass(Element element) {
        return SSOSProfileConfiguration.class;
    }

    /** {@inheritDoc} */
    @Override protected String getProfileBeanNamePrefix() {
        return "shibboleth.SAML2.SSOS.";
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        if (element.hasAttributeNS(null, "delegationPredicateRef")) {
            builder.addPropertyReference("delegationPredicate",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "delegationPredicateRef")));
        }
    }
}
