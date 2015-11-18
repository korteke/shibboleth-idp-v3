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

package net.shibboleth.idp.attribute.resolver.spring.pc.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.idp.saml.attribute.principalconnector.impl.PrincipalConnector;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base Parser for Direct &lt;PrincipalConnector&gt;.<br/>
 * Concrete implementations just need to add the SAML decoders via the
 * {@link #addSAMLDecoders(Element, ParserContext, BeanDefinitionBuilder)} method.
 */
public abstract class AbstractPrincipalConnectorParser extends AbstractSingleBeanDefinitionParser {

    /** Relying Parties. */
    @Nonnull public static final QName RELYING_PARTY =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "RelyingParty");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractPrincipalConnectorParser.class);

    /** {@inheritDoc} */
    @Override protected Class<PrincipalConnector> getBeanClass(Element element) {
        return PrincipalConnector.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        
        log.warn("PrincipalConnector feature is DEPRECATED in favor of subject c14n flows");
        
        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");
        
        super.doParse(config, parserContext, builder);

        // First up, add the per type decoders
        addSAMLDecoders(config, parserContext, builder);

        final String format = StringSupport.trimOrNull(config.getAttributeNS(null, "nameIDFormat"));
        builder.addConstructorArgValue(format);


        final String id = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        builder.addPropertyValue("id", id);

        final List<Element> children = ElementSupport.getChildElements(config, RELYING_PARTY);
        final List<String> relyingParties = new ManagedList<>(children.size());

        for (Element child : children) {
            relyingParties.add(child.getTextContent());
        }

        builder.addPropertyValue("relyingParties", relyingParties);
    }
    
    /**
     * Add in the two SAML decoders. <br/>
     * In order complete the construction of the principal decoder we need to supply a
     * {@link net.shibboleth.idp.saml.nameid.NameIDDecoder} and a
     * {@link net.shibboleth.idp.saml.nameid.NameIdentifierDecoder}. This is done via the
     * {@link BeanDefinitionBuilder#addConstructorArgValue(Object)} method, supplying the SAML2 decoder first
     * 
     * @param config The configuration
     * @param parserContext The context
     * @param builder The builder upon which this method has to call
     *            {@link BeanDefinitionBuilder#addConstructorArgValue(Object)}
     */
    protected abstract void addSAMLDecoders(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder);

}