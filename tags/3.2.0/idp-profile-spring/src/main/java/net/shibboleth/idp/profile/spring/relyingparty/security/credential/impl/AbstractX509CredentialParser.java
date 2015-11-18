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

package net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Element;

/**
 * Specific parser for all X509Credentials.<br/>
 * This does the work of putting the element values into strings. The bean factory then does the correct thing - with
 * some help from Spring doing auto-conversion.
 */
public abstract class AbstractX509CredentialParser extends AbstractCredentialParser {

    /** &lt;PrivateKey&gt;. */
    public static final QName PRIVATE_KEY_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "PrivateKey");

    /** &lt;Certificate&gt;. */
    public static final QName CERTIFICATE_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "Certificate");

    /** &lt;CRL&gt;. */
    public static final QName CRL_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE, "CRL");

    /** log. */
    private Logger log = LoggerFactory.getLogger(AbstractX509CredentialParser.class);

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, BeanDefinitionBuilder builder) {
        super.doParse(element, builder);
        parsePrivateKey(ElementSupport.getChildElements(element, PRIVATE_KEY_ELEMENT_NAME), builder);
        parseCertificates(ElementSupport.getChildElements(element, CERTIFICATE_ELEMENT_NAME), builder);
        parseCRLs(ElementSupport.getChildElements(element, CRL_ELEMENT_NAME), builder);
    }

    /**
     * Parse the &lt;PrivateKey&gt; element.
     * 
     * @param childElements the elements containing the private key, may be null or empty.
     * @param builder the builder
     */
    private void parsePrivateKey(@Nullable final List<Element> childElements,
            @Nonnull final BeanDefinitionBuilder builder) {
        if (null == childElements || childElements.isEmpty()) {
            return;
        }
        if (childElements.size() > 1) {
            throw new BeanCreationException("More than one <PrivateKey> Elements present.");
        }
        final Element key = childElements.get(0);
        final String value = StringSupport.trimOrNull(key.getTextContent());
        if (null == value) {
            throw new BeanCreationException("<PrivateKey> Must contain text.");
        }
        log.debug("Found a private key <Supressed>");
        builder.addPropertyValue("privateKey", value);
        builder.addPropertyValue("privateKeyPassword", key.getAttributeNS(null, "password"));
    }

    /**
     * Parse the &lt;Certificate&gt; elements.
     * 
     * @param childElements the elements containing the certificates, Must have at least one element.
     * @param builder the builder
     */
    private void parseCertificates(@Nullable final List<Element> childElements,
            @Nonnull final BeanDefinitionBuilder builder) {
        if (null == childElements || childElements.isEmpty()) {
            throw new BeanCreationException("At least one <Certificate> should be present.");
        }

        List<String> certs = new ManagedList<>(childElements.size());

        for (Element elem : childElements) {
            final String cert = StringSupport.trimOrNull(elem.getTextContent());
            if (null == cert) {
                throw new BeanCreationException("All <Certificate> elements must contain text.");
            }
            if (elem.hasAttributeNS(null, "entityCertificate")
                    && AttributeSupport.getAttributeValueAsBoolean(elem.getAttributeNodeNS(null,
                            "entityCertificate"))) {
                // Note the loss of property replacement for this undocumented extension
                log.debug("Found a certficate marked as an entityCertificate {}", cert);
                builder.addPropertyValue("entity", cert);
            } else {
                log.debug("Found a certficate {}", cert);
            }
            certs.add(cert);
        }
        builder.addPropertyValue("certificates", certs);
    }

    /**
     * Parse the &lt;CRL&gt; elements.
     * 
     * @param childElements the elements containing the CRLs, Must have at least one element.
     * @param builder the builder
     */
    private void parseCRLs(@Nullable final List<Element> childElements, @Nonnull final BeanDefinitionBuilder builder) {
        if (null == childElements || childElements.isEmpty()) {
            return;
        }

        builder.addPropertyValue("CRLs", SpringSupport.getElementTextContentAsManagedList(childElements));
    }
}
