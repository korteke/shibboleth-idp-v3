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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl;

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base parser for all &lt;ValidationInfo&gt; types. This does all the heavy lifting of creating lists of strings from
 * the sub-elements. The derived classes specify which factory bean to create and that in turn converts from string to
 * CRL or Certificate (either inline or from a file).
 */
public abstract class AbstractPKIXValidationInfoParser extends AbstractSingleBeanDefinitionParser {

    /** The element &lt;Certificate&gt;. */
    private static final QName CERTIFICATE =
            new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE, "Certificate");

    /** The element &lt;CRL&gt;. */
    private static final QName CRL = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE, "CRL");

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        builder.addPropertyValue("configDescription", parserContext.getReaderContext().getResource().getDescription());

        if (element.hasAttributeNS(null, "verifyDepth")) {
            builder.addPropertyValue("verifyDepth",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "verifyDepth")));
        }

        final List<Element> certificates = ElementSupport.getChildElements(element, CERTIFICATE);
        final List<String> certStrings = new ManagedList<>(certificates.size());

        for (final Element cert : certificates) {
            certStrings.add(cert.getTextContent());
        }

        builder.addPropertyValue("certificates", certStrings);

        final List<Element> crls = ElementSupport.getChildElements(element, CRL);
        final List<String> crlStrings = new ManagedList<>(certificates.size());

        for (final Element crl : crls) {
            crlStrings.add(crl.getTextContent());
        }

        builder.addPropertyValue("CRLs", crlStrings);
    }
}
