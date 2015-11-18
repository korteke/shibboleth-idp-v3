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
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for trust engines of types we no longer support.
 */
public class UnsupportedTrustEngineParser extends AbstractTrustEngineParser {

    /** Schema type for MetadataPKIXSignature. */
    public static final QName METADATA_PKIX_SIGNATURE_TYPE = new QName(
            AbstractMetadataProviderParser.SECURITY_NAMESPACE, "MetadataPKIXSignature");

    /** Schema type for MetadataExplicitKeySignature. */
    public static final QName METADATA_EXPLICIT_KEY_SIGNATURE_TYPE = new QName(
            AbstractMetadataProviderParser.SECURITY_NAMESPACE, "MetadataExplicitKeySignature");

    /** Schema type for MetadataPKIXX509Credential. */
    public static final QName METADATA_PKIX_CREDENTIAL_TYPE = new QName(
            AbstractMetadataProviderParser.SECURITY_NAMESPACE, "MetadataPKIXX509Credential");

    /** Schema type for MetadataExplicitKey. */
    public static final QName METADATA_EXPLICIT_KEY_TYPE = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "MetadataExplicitKey");

    /** Schema type for StaticPKIXX509Credential. */
    public static final QName PKIX_CREDENTIAL = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "StaticPKIXX509Credential");

    /** Schema type for StaticPKIXX509Credential. */
    public static final QName STATIC_EXPLICIT_KEY_TYPE = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "StaticExplicitKey");

    /** log. */
    private final Logger log = LoggerFactory.getLogger(UnsupportedTrustEngineParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return Object.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        log.warn("Configuration {} contained unsupported Trust Engine type {}. This has been ignored.", parserContext
                .getReaderContext().getResource().getDescription(), DOMTypeSupport.getXSIType(element).toString());
    }

}
