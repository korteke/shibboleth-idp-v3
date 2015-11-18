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

package net.shibboleth.idp.attribute.resolver.spring.ad.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.ad.impl.RegexSplitAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.ad.BaseAttributeDefinitionParser;
import net.shibboleth.idp.attribute.resolver.spring.ad.PatternFactoryBean;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring Bean Definition Parser for static data connector.
 */
public class RegexSplitAttributeDefinitionParser extends BaseAttributeDefinitionParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME =
            new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "RegexSplit");

    /** Logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RegexSplitAttributeDefinitionParser.class);

    /** {@inheritDoc} */
    @Override protected Class<RegexSplitAttributeDefinition> getBeanClass(@Nullable Element element) {
        return RegexSplitAttributeDefinition.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final String regexp = StringSupport.trimOrNull(config.getAttributeNS(null, "regex"));

        if (null == regexp) {
            log.error("{} No regexp specified.", getLogPrefix());
            throw new BeanCreationException(getLogPrefix() + " No regexp text provided.");
        }

        BeanDefinitionBuilder pattern = BeanDefinitionBuilder.genericBeanDefinition(PatternFactoryBean.class);
        
        pattern.addPropertyValue("regexp", regexp);

        if (config.hasAttributeNS(null, "caseSensitive")) {
            pattern.addPropertyValue("caseSensitive", 
                    StringSupport.trimOrNull(config.getAttributeNS(null, "caseSensitive")));
        }

        builder.addPropertyValue("regularExpression", pattern.getBeanDefinition());
    }

    /** {@inheritDoc} */
    @Override protected boolean needsAttributeSourceID() {
        return true;
    }
    
}