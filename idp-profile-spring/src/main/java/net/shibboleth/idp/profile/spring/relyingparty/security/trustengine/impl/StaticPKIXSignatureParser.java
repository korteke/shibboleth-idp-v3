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

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;

import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.impl.PKIXSignatureTrustEngine;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for trust engines of type StaticPKIXKeySignature.
 */
public class StaticPKIXSignatureParser extends AbstractStaticPKIXParser {

    /** Schema type. */
    public static final QName TYPE_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "StaticPKIXSignature");

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return PKIXSignatureTrustEngine.class;
    }

    /**
     * {@inheritDoc} <br/>
     * We call into
     * {@link 
     * PKIXSignatureTrustEngine#PKIXSignatureTrustEngine(
     * org.opensaml.security.x509.PKIXValidationInformationResolver, 
     * org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver, 
     * org.opensaml.security.x509.PKIXTrustEvaluator, 
     * org.opensaml.security.x509.impl.X509CredentialNameEvaluator)}
     * .
     */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        builder.addConstructorArgValue(getPKIXValidationInformationResolver(element, parserContext));
        builder.addConstructorArgValue(DefaultSecurityConfigurationBootstrap
                .buildBasicInlineKeyInfoCredentialResolver());
        builder.addConstructorArgValue(getPKIXTrustEvaluator(element, parserContext));
        builder.addConstructorArgValue(getX509CredentialNameEvaluator(element, parserContext));
    }

}
