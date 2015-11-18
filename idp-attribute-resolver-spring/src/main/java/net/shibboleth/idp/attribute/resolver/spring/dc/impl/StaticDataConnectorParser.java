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

package net.shibboleth.idp.attribute.resolver.spring.dc.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.dc.impl.StaticDataConnector;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Bean definition Parser for a {@link StaticDataConnector}. */
public class StaticDataConnectorParser extends AbstractDataConnectorParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE, "Static");

    /** Local name of attribute. */
    @Nonnull public static final QName ATTRIBUTE_ELEMENT_NAME =
            new QName(DataConnectorNamespaceHandler.NAMESPACE, "Attribute");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StaticDataConnectorParser.class);

    /** {@inheritDoc} */
    @Override protected Class<StaticDataConnector> getNativeBeanClass() {
        return StaticDataConnector.class;
    }

    /** {@inheritDoc} */
    @Override protected void doV2Parse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {

        final List<Element> children = ElementSupport.getChildElements(config, ATTRIBUTE_ELEMENT_NAME);
        final List<BeanDefinition> attributes = new ManagedList<>(children.size());

        for (final Element child : children) {

            final String attrId = StringSupport.trimOrNull(child.getAttributeNS(null, "id"));
            final BeanDefinitionBuilder attributeDefn = BeanDefinitionBuilder.genericBeanDefinition(IdPAttribute.class);
            attributeDefn.addConstructorArgValue(attrId);

            final List<Element> values =
                    ElementSupport.getChildElementsByTagNameNS(child, DataConnectorNamespaceHandler.NAMESPACE, "Value");
            final ManagedList<BeanDefinition> inValues = new ManagedList<>(values.size());
            for (final Element val : values) {
                final BeanDefinitionBuilder value =
                        BeanDefinitionBuilder.genericBeanDefinition(StringAttributeValue.class);
                value.addConstructorArgValue(val.getTextContent());
                log.trace("{} Attribute: {}, adding value {}",
                        new Object[] {getLogPrefix(), attrId, val.getTextContent(),});
                inValues.add(value.getBeanDefinition());
            }
            attributeDefn.addPropertyValue("values", inValues);
            log.debug("{} Adding Attribute: {} with {} values", new Object[] {getLogPrefix(), attrId, values.size(),});
            attributes.add(attributeDefn.getBeanDefinition());
        }

        builder.addPropertyValue("values", attributes);
    }

}