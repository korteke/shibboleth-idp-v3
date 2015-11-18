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

package net.shibboleth.idp.attribute.resolver.spring.ad.mapped.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.ad.mapped.impl.SourceValue;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.AttributeDefinitionNamespaceHandler;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Bean definition parser for a {@link SourceValue}. */
public class SourceValueParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME =
            new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "SourceValue");

    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(SourceValueParser.class);

    /** {@inheritDoc} */
    @Override protected Class<SourceValue> getBeanClass(@Nullable final Element element) {
        return SourceValue.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final String value = config.getTextContent();
        builder.addConstructorArgValue(value);

        String ignoreCase = null;
        if (config.hasAttributeNS(null, "ignoreCase")) {
            ignoreCase = StringSupport.trimOrNull(config.getAttributeNS(null, "ignoreCase"));
            builder.addConstructorArgValue(ignoreCase);
        } else {
            builder.addConstructorArgValue(null);
        }

        String partialMatch = null;
        if (config.hasAttributeNS(null, "partialMatch")) {
            partialMatch = StringSupport.trimOrNull(config.getAttributeNS(null, "partialMatch"));
            builder.addConstructorArgValue(partialMatch);
        } else {
            builder.addConstructorArgValue(null);
        }


        log.debug("SourceValue value: {}, ignoreCase: {}, partialMatch: {}", value, ignoreCase, partialMatch);

    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
    
}