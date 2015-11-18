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

import net.shibboleth.idp.attribute.resolver.ad.impl.PrescopedAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.ad.BaseAttributeDefinitionParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Spring bean definition parser for prescoped attributes. */
public class PrescopedAttributeDefinitionParser extends BaseAttributeDefinitionParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME =
            new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "Prescoped");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrescopedAttributeDefinitionParser.class);

    /** {@inheritDoc} */
    @Override protected Class<PrescopedAttributeDefinition> getBeanClass(@Nullable final Element element) {
        return PrescopedAttributeDefinition.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        if (config.hasAttributeNS(null, "scopeDelimiter")) {
            final String scopeDelimiter = StringSupport.trimOrNull(config.getAttributeNS(null, "scopeDelimiter"));
            log.debug("{} Scope delimiter of {} specified.", getLogPrefix(), scopeDelimiter);
            builder.addPropertyValue("scopeDelimiter", scopeDelimiter);
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean needsAttributeSourceID() {
        return true;
    }
    
}