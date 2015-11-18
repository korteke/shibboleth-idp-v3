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

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.security.x509.impl.BasicX509CredentialNameEvaluator;
import org.opensaml.security.x509.impl.CertPathPKIXTrustEvaluator;
import org.opensaml.security.x509.impl.StaticPKIXValidationInformationResolver;
import org.opensaml.security.x509.impl.X509CredentialNameEvaluator;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base Parser for trust engines of type StaticPKIXKeySignature and StaticPKIXX509Credential.
 */
public abstract class AbstractStaticPKIXParser extends AbstractTrustEngineParser {

    /** Validation Information. */
    public static final QName VALIDATION_INFO = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "ValidationInfo");

    /** Trusted Names Information. */
    public static final QName TRUSTED_NAMES = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "TrustedName");

    /**
     * Get the definition for the {@link org.opensaml.security.x509.PKIXValidationInformationResolver}. This is
     * constructed from the Trusted names and the Validation Info.
     * 
     * @param element what to parse
     * @param parserContext the context to parse inside
     * @return the definition
     */
    protected BeanDefinition getPKIXValidationInformationResolver(@Nonnull final Element element,
            @Nonnull final ParserContext parserContext) {

        final List<Element> validationInfoElements = ElementSupport.getChildElements(element, VALIDATION_INFO);
        final List<Element> trustedNameElements = ElementSupport.getChildElements(element, TRUSTED_NAMES);

        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(StaticPKIXValidationInformationResolver.class);
        builder.addConstructorArgValue(SpringSupport.parseCustomElements(validationInfoElements, parserContext));
        builder.addConstructorArgValue(SpringSupport.getElementTextContentAsManagedList(trustedNameElements));

        return builder.getBeanDefinition();
    }

    /**
     * Get the definition for the {@link org.opensaml.security.x509.PKIXTrustEvaluator}. This is purely a
     * {@link CertPathPKIXTrustEvaluator} constructed with whatever ValidationOptions we provide.
     * 
     * @param element what to parse
     * @param parserContext the context to parse inside
     * @return the definition
     */
    protected BeanDefinition getPKIXTrustEvaluator(@Nonnull final Element element,
            @Nonnull final ParserContext parserContext) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CertPathPKIXTrustEvaluator.class);

        final List<Element> validationOptionsElements =
                ElementSupport.getChildElements(element, PKIXValidationOptionsParser.ELEMENT_NAME);

        if (null != validationOptionsElements && !validationOptionsElements.isEmpty()) {
            builder.addConstructorArgValue(SpringSupport.parseCustomElements(validationOptionsElements, parserContext));
        }
        return builder.getBeanDefinition();
    }

    /**
     * Get the effective X509CredentialNameEvaluator to use. Currently we return a literal value, but in the future a
     * BeanDefinition could be returned if necessary if we support toggling the name evaluator settings, hence the
     * Object return type.
     * 
     * @param element what to parse
     * @param parserContext the context to parse inside
     * @return an X509CredentialNameEvaluator instance or a BeanDefinition. May be null.
     */
    protected Object getX509CredentialNameEvaluator(@Nonnull final Element element,
            @Nonnull final ParserContext parserContext) {

        final BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(X509CredentialNameEvaluatorFactoryBean.class);
        String attrValue = StringSupport.trimOrNull(element.getAttributeNS(null, "trustedNameCheckEnabled"));
        if (attrValue != null) {
            builder.addPropertyValue("trustedNameCheckEnabled", attrValue);
        }
        return builder.getBeanDefinition();
    }

    /**
     * FactoryBean to do a deferred decision on whether to create a {@link X509CredentialNameEvaluator}. This is in a
     * factory bean to allow for property replacement. The default (no value setting) is true.
     */
    protected static class X509CredentialNameEvaluatorFactoryBean extends
            AbstractFactoryBean<X509CredentialNameEvaluator> {

        /** Do we emit a {@link X509CredentialNameEvaluator} or not. */
        private boolean trustedNameCheckEnabled = true;

        /**
         * Setter for {@link #trustedNameCheckEnabled}.
         * 
         * @param enabled whether we emit a the {@link X509CredentialNameEvaluator} .
         */
        public void setTrustedNameCheckEnabled(final boolean enabled) {
            trustedNameCheckEnabled = enabled;
        }

        /** {@inheritDoc} */
        @Override public Class<?> getObjectType() {
            return BasicX509CredentialNameEvaluator.class;
        }

        /** {@inheritDoc} */
        @Override protected BasicX509CredentialNameEvaluator createInstance() throws Exception {
            if (trustedNameCheckEnabled) {
                return new BasicX509CredentialNameEvaluator();
            } else {
                return null;
            }
        }
    }
}
