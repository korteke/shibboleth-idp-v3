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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.ad.impl.TemplateAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.spring.ad.BaseAttributeDefinitionParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser for scripted attribute configuration elements.
 */
public class TemplateAttributeDefinitionParser extends BaseAttributeDefinitionParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "Template");

    /** SourceValue element name. */
    @Nonnull public static final QName TEMPLATE_ELEMENT_NAME =
            new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "Template");

    /** SourceValue element name. */
    @Nonnull public static final QName SOURCE_ATTRIBUTE_ELEMENT_NAME =
            new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "SourceAttribute");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TemplateAttributeDefinitionParser.class);

    /** {@inheritDoc} */
    @Override
    protected Class<TemplateAttributeDefinition> getBeanClass(@Nullable Element element) {
        return TemplateAttributeDefinition.class;
    }

    /** {@inheritDoc} */
    @Override
    protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final List<Element> templateElements = ElementSupport.getChildElements(config, TEMPLATE_ELEMENT_NAME);

        if (null != templateElements && templateElements.size() >= 1) {
            final String templateText = StringSupport.trimOrNull(templateElements.get(0).getTextContent());
            log.debug("{} Template is '{}'", getLogPrefix(), templateText);

            builder.addPropertyValue("templateText", templateText);
        }

        final List<Element> sourceAttributeElements =
                ElementSupport.getChildElements(config, SOURCE_ATTRIBUTE_ELEMENT_NAME);
        if (null != sourceAttributeElements) {
            final List<String> sourceAttributes = new ManagedList<>(sourceAttributeElements.size());
            for (Element element : sourceAttributeElements) {
                sourceAttributes.add(StringSupport.trimOrNull(element.getTextContent()));
            }
            log.debug("{} Source attributes are '{}'.", getLogPrefix(), sourceAttributes);
            builder.addPropertyValue("sourceAttributes", sourceAttributes);
        }

        String velocityEngineRef = StringSupport.trimOrNull(config.getAttributeNS(null, "velocityEngine"));
        if (null == velocityEngineRef) {
            velocityEngineRef = "shibboleth.VelocityEngine";
        }
        log.debug("{} Velocity engine reference '{}'.", getLogPrefix(), velocityEngineRef);
        builder.addPropertyReference("velocityEngine", velocityEngineRef);
    }
    
    /** {@inheritDoc}. No input. */
    @Override protected boolean needsAttributeSourceID() {
        return false;
    }

}