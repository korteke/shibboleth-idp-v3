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

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.AbstractCredentialParser;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.trust.impl.ExplicitKeyTrustEngine;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for trust engines of type StaticExplicitKey TrustEngine.
 */
public class StaticExplicitKeyParser extends AbstractTrustEngineParser {

    /** Schema type. */
    public static final QName TYPE_NAME =
            new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE, "StaticExplicitKey");

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return ExplicitKeyTrustEngine.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        final List<Element> credentials =
                ElementSupport.getChildElements(element, AbstractCredentialParser.CREDENTIAL_ELEMENT_NAME);

        final BeanDefinitionBuilder resolver =
                BeanDefinitionBuilder.genericBeanDefinition(StaticCredentialResolver.class);
        resolver.addConstructorArgValue(SpringSupport.parseCustomElements(credentials, parserContext));

        builder.addConstructorArgValue(resolver.getBeanDefinition());
    }
}
