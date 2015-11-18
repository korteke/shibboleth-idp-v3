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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate.Candidate;
import org.opensaml.saml.common.profile.logic.EntityGroupNamePredicate;
import org.opensaml.saml.common.profile.logic.EntityIdPredicate;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.google.common.base.Predicates;

/**
 * Parser for a &lt;Predicate&gt; filter.
 */
public class PredicateFilterParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    public static final QName TYPE_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "Predicate");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PredicateFilterParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return PredicateFilter.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        if (element.hasAttributeNS(null, "removeEmptyEntitiesDescriptors")) {
            builder.addPropertyValue("removeEmptyEntitiesDescriptors",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "removeEmptyEntitiesDescriptors")));
        }

        final BeanDefinitionBuilder directionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(PredicateFilterDirectionFactoryBean.class);
        directionBuilder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "direction")));
        builder.addConstructorArgValue(directionBuilder.getBeanDefinition());

        if (element.hasAttributeNS(null, "conditionRef")) {
            log.info("Found conditionRef attribute, ignoring embedded Entity/Group/Tag elements");
            builder.addConstructorArgReference(StringSupport.trimOrNull(element.getAttributeNS(null, "conditionRef")));
        } else {
            builder.addConstructorArgValue(parseCustomElements(element));
        }
    }

    // Checkstyle: CyclomaticComplexity OFF
    /**
     * Parser custom element content into a {@link com.google.common.base.Predicate} to pass to the filter constructor.
     * 
     * @param element root element to parse
     * 
     * @return the bean definition of the Predicate to install
     */
    @Nonnull public BeanDefinition parseCustomElements(@Nonnull final Element element) {

        // Track which predicates have to be built.
        final BeanDefinitionBuilder entityIdPredicateBuilder = parseEntityPredicate(element);
        final BeanDefinitionBuilder groupPredicateBuilder = parseGroupPredicate(element);
        final BeanDefinitionBuilder tagPredicateBuilder = parseTagPredicate(element);

        int count = 0;
        if (entityIdPredicateBuilder != null) {
            count++;
        }
        if (groupPredicateBuilder != null) {
            count++;
        }
        if (tagPredicateBuilder != null) {
            count++;
        }

        if (count == 0) {
            throw new BeanCreationException("No Entity, Group, or Tag element found");
        } else if (count == 1) {
            if (entityIdPredicateBuilder != null) {
                return entityIdPredicateBuilder.getBeanDefinition();
            } else if (groupPredicateBuilder != null) {
                return groupPredicateBuilder.getBeanDefinition();
            } else {
                return tagPredicateBuilder.getBeanDefinition();
            }
        } else {
            final BeanDefinitionBuilder orBuilder = BeanDefinitionBuilder.rootBeanDefinition(Predicates.class, "or");
            final ManagedList<BeanDefinition> managedList = new ManagedList<>(count);
            if (entityIdPredicateBuilder != null) {
                managedList.add(entityIdPredicateBuilder.getBeanDefinition());
            }
            if (groupPredicateBuilder != null) {
                managedList.add(groupPredicateBuilder.getBeanDefinition());
            }
            if (tagPredicateBuilder != null) {
                managedList.add(tagPredicateBuilder.getBeanDefinition());
            }
            orBuilder.addConstructorArgValue(managedList);
            return orBuilder.getBeanDefinition();
        }
    }

    // Checkstyle: CyclomaticComplexity ON

    /**
     * Parse Entity elements into a builder for an {@link EntityIdPredicate}.
     * 
     * @param element root element to parse under
     * 
     * @return builder for the predicate, or null if none needed
     */
    @Nullable public BeanDefinitionBuilder parseEntityPredicate(@Nonnull final Element element) {
        final List<Element> entityList =
                ElementSupport.getChildElementsByTagNameNS(element, AbstractMetadataProviderParser.METADATA_NAMESPACE,
                        "Entity");
        if (!entityList.isEmpty()) {
            final ManagedList<String> managedEntityList = SpringSupport.getElementTextContentAsManagedList(entityList);
            final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EntityIdPredicate.class);
            builder.addConstructorArgValue(managedEntityList);
            return builder;
        }

        return null;
    }

    /**
     * Parse Group elements into a builder for an {@link EntityGroupNamePredicate}.
     * 
     * @param element root element to parse under
     * 
     * @return builder for the predicate, or null if none needed
     */
    @Nullable public BeanDefinitionBuilder parseGroupPredicate(@Nonnull final Element element) {

        final List<Element> groupList =
                ElementSupport.getChildElementsByTagNameNS(element, AbstractMetadataProviderParser.METADATA_NAMESPACE,
                        "Group");
        if (!groupList.isEmpty()) {
            final ManagedList<String> managedGroupList = SpringSupport.getElementTextContentAsManagedList(groupList);
            final BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.genericBeanDefinition(EntityGroupNamePredicate.class);
            builder.addConstructorArgValue(managedGroupList);
            return builder;
        }

        return null;
    }

    /**
     * Parse Tag elements into a builder for an {@link EntityAttributesPredicate}.
     * 
     * @param element root element to parse under
     * 
     * @return builder for the predicate, or null if none needed
     */
    @Nullable public BeanDefinitionBuilder parseTagPredicate(@Nonnull final Element element) {
        final List<Element> tagList =
                ElementSupport.getChildElementsByTagNameNS(element, AbstractMetadataProviderParser.METADATA_NAMESPACE,
                        "Tag");
        if (!tagList.isEmpty()) {
            final ManagedList<BeanDefinition> managedTagList = new ManagedList<>(tagList.size());
            for (final Element tag : tagList) {
                final BeanDefinitionBuilder tagBuilder = BeanDefinitionBuilder.genericBeanDefinition(Candidate.class);
                tagBuilder.addConstructorArgValue(StringSupport.trimOrNull(tag.getAttributeNS(null, "name")));
                tagBuilder.addConstructorArgValue(StringSupport.trimOrNull(tag.getAttributeNS(null, "nameFormat")));
                final List<Element> valueList =
                        ElementSupport.getChildElementsByTagNameNS(tag,
                                AbstractMetadataProviderParser.METADATA_NAMESPACE, "Value");
                if (!valueList.isEmpty()) {
                    final ManagedList<String> managedValueList =
                            SpringSupport.getElementTextContentAsManagedList(valueList);
                    tagBuilder.addPropertyValue("values", managedValueList);
                }
                managedTagList.add(tagBuilder.getBeanDefinition());
            }
            final BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.genericBeanDefinition(EntityAttributesPredicate.class);
            builder.addConstructorArgValue(managedTagList);
            builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "trim")));
            return builder;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
}