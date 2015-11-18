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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.profile.logic.EntityIdPredicate;
import org.opensaml.saml.metadata.resolver.filter.impl.EntityAttributesFilter;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Parser for a &lt;EntityAttributes&gt; filter. */
public class EntityAttributesFilterParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    public static final QName TYPE_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "EntityAttributes");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(EntityAttributesFilterParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return EntityAttributesFilter.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        final Unmarshaller unmarshaller = XMLObjectSupport.getUnmarshaller(Attribute.DEFAULT_ELEMENT_NAME);
        if (unmarshaller == null) {
            throw new BeanCreationException("Unable to obtain Unmarshaller for Attribute objects");
        }

        // Accumulate Attribute objects to attach as rule values.
        final List<Attribute> accumulator = new ArrayList<>();

        final ManagedMap<Object, ManagedList<Attribute>> ruleMap = new ManagedMap();

        Element child = ElementSupport.getFirstChildElement(element);
        while (child != null) {
            if (ElementSupport.isElementNamed(child, Attribute.DEFAULT_ELEMENT_NAME)) {
                try {
                    final XMLObject attribute = unmarshaller.unmarshall(child);
                    if (attribute instanceof Attribute) {
                        accumulator.add((Attribute) attribute);
                    }
                } catch (final UnmarshallingException e) {
                    log.error("Error unmarshalling Attribute", e);
                }
            } else if (ElementSupport
                    .isElementNamed(child, AbstractMetadataProviderParser.METADATA_NAMESPACE, "Entity")) {
                final BeanDefinitionBuilder entityIdBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(EntityIdPredicate.class);
                entityIdBuilder.addConstructorArgValue(ElementSupport.getElementContentAsString(child));
                final ManagedList<Attribute> forRule = new ManagedList(accumulator.size());
                forRule.addAll(accumulator);
                ruleMap.put(entityIdBuilder.getBeanDefinition(), forRule);
            } else if (ElementSupport.isElementNamed(child, AbstractMetadataProviderParser.METADATA_NAMESPACE,
                    "ConditionRef")) {
                final ManagedList<Attribute> forRule = new ManagedList(accumulator.size());
                forRule.addAll(accumulator);
                ruleMap.put(new RuntimeBeanReference(ElementSupport.getElementContentAsString(child)), forRule);
            }
            child = ElementSupport.getNextSiblingElement(child);
        }

        builder.addPropertyValue("rules", ruleMap);
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }

}