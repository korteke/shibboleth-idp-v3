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

package net.shibboleth.idp.attribute.resolver.spring;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Bean definition parser for a {@link ResolverPluginDependency}. */
public class ResolverPluginDependencyParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    @Nonnull public static final QName ELEMENT_NAME =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "Dependency");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ResolverPluginDependencyParser.class);

    /** {@inheritDoc} */
    @Override protected Class<ResolverPluginDependency> getBeanClass(@Nullable final Element element) {
        return ResolverPluginDependency.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        final String pluginId = StringSupport.trimOrNull(config.getAttributeNS(null, "ref"));
        log.info("Parsing configuration for {} with pluginId: {}", config.getLocalName(), pluginId);
        builder.addConstructorArgValue(pluginId);
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
    
}