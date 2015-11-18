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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicInlineCredentialFactoryBean;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.X509ResourceCredentialFactoryBean;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for xsi:type="SignatureValidation".
 */
public class SignatureValidationParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "SignatureValidation");

    /** Element for embedded public keys. */
    public static final QName PUBLIC_KEY = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "PublicKey");

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(SignatureValidationParser.class);

    /** {@inheritDoc} */
    @Override protected Class getBeanClass(Element element) {
        return SignatureValidationFilter.class;
    }

    // Checkstyle: CyclomaticComplexity OFF
    // Checkstyle: MethodLength OFF
    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        final boolean hasEngineRef = element.hasAttributeNS(null, "trustEngineRef");
        final boolean hasCertFile = element.hasAttributeNS(null, "certificateFile");
        final List<Element> publicKeys = ElementSupport.getChildElements(element, PUBLIC_KEY);
        final List<Element> trustEngines =
                ElementSupport.getChildElements(element, AbstractMetadataProviderParser.TRUST_ENGINE_ELEMENT_NAME);

        super.doParse(element, parserContext, builder);

        if (hasEngineRef) {
            if (hasCertFile) {
                log.error("{}: trustEngineRef and certificateFile are mutually exclusive", parserContext
                        .getReaderContext().getResource().getDescription());
                throw new BeanCreationException("trustEngineRef and certificateFile are mutually exclusive");
            }
            if (trustEngines != null && !trustEngines.isEmpty()) {
                log.error("{}: trustEngineRef and Embedded <TrustEngine>  are mutually exclusive", parserContext
                        .getReaderContext().getResource().getDescription());
                throw new BeanCreationException("trustEngineRef and Embedded <TrustEngine> are mutually exclusive");
            }
            if (null != publicKeys && !publicKeys.isEmpty()) {
                log.error("{}: trustEngineRef and certificateFile are mutually exclusive", parserContext
                        .getReaderContext().getResource().getDescription());
                throw new BeanCreationException("trustEngineRef and embedded public keys are mutually exclusive");
            }
            builder.addConstructorArgReference(StringSupport.trimOrNull(element.getAttributeNS(null, 
                    "trustEngineRef")));
        } else if (hasCertFile) {
            if (null != publicKeys && !publicKeys.isEmpty()) {
                log.error("{}: certificateFile and embedded public keys are mutually exclusive", parserContext
                        .getReaderContext().getResource().getDescription());
                throw new BeanCreationException("certificateFile and embedded public keys are mutually exclusive");
            }
            if (trustEngines != null && !trustEngines.isEmpty()) {
                log.error("{}: certificateFile and Embedded <TrustEngine>  are mutually exclusive", parserContext
                        .getReaderContext().getResource().getDescription());
                throw new BeanCreationException("Embedded <TrustEngine> and certificateFile are mutually exclusive");
            }
            buildTrustEngine(builder, buildCertificateCredential(element.getAttributeNS(null, "certificateFile")));
        } else if (null != trustEngines && !trustEngines.isEmpty()) {
            if (trustEngines.size() > 1) {
                log.error("{}: Too many <TrustEngine>s", parserContext.getReaderContext().getResource()
                        .getDescription());
                throw new BeanCreationException("{}: Too many <TrustEngine>s");
            }
            ManagedList<BeanDefinition> engines = SpringSupport.parseCustomElements(trustEngines, parserContext);

            builder.addConstructorArgValue(engines.get(0));
        } else {
            buildTrustEngine(builder, buildPublicKeyCredential(parserContext, publicKeys));
        }

        if (element.hasAttributeNS(null, "requireSignedRoot")) {
            builder.addPropertyValue("requireSignedRoot", element.getAttributeNS(null, "requireSignedRoot"));
        } else if (element.hasAttributeNS(null, "requireSignedMetadata")) {
            log.warn("{} Use of the attribute 'requireSignedMetadata' is deprecated, " 
                    + "use 'requireSignedRoot' instead", 
                    parserContext.getReaderContext().getResource().getDescription());
            builder.addPropertyValue("requireSignedRoot", element.getAttributeNS(null, "requireSignedMetadata"));
        }

        if (element.hasAttributeNS(null, "defaultCriteriaRef")) {
            builder.addPropertyReference("defaultCriteria", element.getAttributeNS(null, "defaultCriteriaRef"));
        } else {
            // TODO refactor, if decide upon new strategy for (or against) well-known bean refs
            builder.addPropertyReference("defaultCriteria", "shibboleth.MetadataSignatureValidationStaticCriteria");
        }

        if (element.hasAttributeNS(null, "signaturePrevalidatorRef")) {
            builder.addPropertyReference("signaturePrevalidator",
                    element.getAttributeNS(null, "signaturePrevalidatorRef"));
        }

        if (element.hasAttributeNS(null, "dynamicTrustedNamesStrategyRef")) {
            builder.addPropertyReference("dynamicTrustedNamesStrategy",
                    element.getAttributeNS(null, "dynamicTrustedNamesStrategyRef"));
        }

    }

    // Checkstyle: CyclomaticComplexity ON
    // Checkstyle: MethodLength ON

    /**
     * Build a trust engine and populate it with the supplied credential (definition).
     * 
     * @param builder the builder for this bean.
     * @param credential the definition of a {@link org.opensaml.security.credential.Credential}
     */
    private void buildTrustEngine(BeanDefinitionBuilder builder, BeanDefinition credential) {
        final BeanDefinitionBuilder trustEngineBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(ExplicitKeySignatureTrustEngine.class);

        final BeanDefinitionBuilder resolver =
                BeanDefinitionBuilder.genericBeanDefinition(StaticCredentialResolver.class);
        // Casting a singleton to a list
        resolver.addConstructorArgValue(credential);

        trustEngineBuilder.addConstructorArgValue(resolver.getBeanDefinition());

        List<KeyInfoProvider> keyInfoProviders = new ArrayList<>();
        keyInfoProviders.add(new DSAKeyValueProvider());
        keyInfoProviders.add(new RSAKeyValueProvider());
        keyInfoProviders.add(new InlineX509DataProvider());
        trustEngineBuilder.addConstructorArgValue(new BasicProviderKeyInfoCredentialResolver(keyInfoProviders));

        builder.addConstructorArgValue(trustEngineBuilder.getBeanDefinition());
    }

    /**
     * Build (the definition) for a BasicInline Credential.
     * 
     * @param parserContext used for logging
     * @param publicKeys the list of &lt;PublicKey&gt; elements
     * @return the definition.
     */
    private BeanDefinition buildPublicKeyCredential(ParserContext parserContext, List<Element> publicKeys) {
        if (null == publicKeys || publicKeys.isEmpty()) {
            log.error("{}: SignatureValidation filter must have a 'trustEngineRef' attribute"
                    + ", a 'certificateFile' attribute or <PublicKey> elements", parserContext.getReaderContext()
                    .getResource().getDescription());
            throw new BeanCreationException("SignatureValidation filter must have a 'trustEngineRef' attribute"
                    + ", a 'certificateFile' attribute or <PublicKey> elements");
        }
        if (publicKeys.size() > 1) {
            log.error("{}: Only one <PublicKey> element may be specified", parserContext.getReaderContext()
                    .getResource().getDescription());
            throw new BeanCreationException("Only one <PublicKey> element may be specified");
        }

        final BeanDefinitionBuilder credentialBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(BasicInlineCredentialFactoryBean.class);

        final String keyAsString = StringSupport.trimOrNull(publicKeys.get(0).getTextContent());
        if (null == keyAsString) {
            log.error("{}: <PublicKey> must contain the public key", parserContext.getReaderContext().getResource()
                    .getDescription());
            throw new BeanCreationException("<PublicKey> must contain the public key");
        }

        List<String> keys = new ManagedList<>(1);
        keys.add(keyAsString);
        credentialBuilder.addPropertyValue("publicKeyInfo", keyAsString);
        return credentialBuilder.getBeanDefinition();
    }

    /**
     * Build (the definition) for a X509Filesystem Credential.
     * 
     * @param attribute the name of the certificate file
     * @return the bean definition.
     */
    private BeanDefinition buildCertificateCredential(String attribute) {
        final BeanDefinitionBuilder credentialBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(X509ResourceCredentialFactoryBean.class);
        final List<String> certs = new ManagedList<>(1);
        certs.add(attribute);
        credentialBuilder.addPropertyValue("certificates", certs);
        return credentialBuilder.getBeanDefinition();
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }

}
