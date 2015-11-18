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

import java.util.List;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for all classes which extend
 * {@link net.shibboleth.idp.saml.saml2.profile.config.AbstractSAML2ProfileConfiguration} and for elements which inherit
 * from <code>saml:SAML2ProfileConfigutationType</code>.
 */
public abstract class BaseSAML2ProfileConfigurationParser extends BaseSAMLProfileConfigurationParser {

    /**
     * Get the list of proxy audiences from the &lt;ProxyAudience&gt; sub-elements.
     * 
     * @param element the element under discussion
     * @return the list of elements (which are subject to property replacement)
     */
    protected List<String> getProxyAudiences(Element element) {
        List<Element> audienceElems =
                ElementSupport.getChildElementsByTagNameNS(element, RelyingPartySAMLNamespaceHandler.NAMESPACE,
                        "ProxyAudience");
        return SpringSupport.getElementTextContentAsManagedList(audienceElems);
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "encryptionOptional")) {
            builder.addPropertyValue("encryptionOptional",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "encryptionOptional")));
        }

        if (element.hasAttributeNS(null, "encryptAssertions")) {
            builder.addPropertyValue("encryptAssertions", predicateForEncryption(
                    StringSupport.trimOrNull(element.getAttributeNS(null, "encryptAssertions"))));
        }
        if (element.hasAttributeNS(null, "encryptNameIds")) {
            builder.addPropertyValue("encryptNameIDs", predicateForEncryption(
                    StringSupport.trimOrNull(element.getAttributeNS(null, "encryptNameIds"))));
        }
        if (element.hasAttributeNS(null, "encryptAttributes")) {
            builder.addPropertyValue("encryptAttributes", predicateForEncryption(
                    StringSupport.trimOrNull(element.getAttributeNS(null, "encryptAttributes"))));
        }
        if (element.hasAttributeNS(null, "assertionProxyCount")) {
            builder.addPropertyValue("proxyCount",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "assertionProxyCount")));
        }
        builder.addPropertyValue("proxyAudiences", getProxyAudiences(element));
    }
}
