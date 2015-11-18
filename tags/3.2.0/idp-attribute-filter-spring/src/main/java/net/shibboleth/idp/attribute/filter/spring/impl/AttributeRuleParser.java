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

package net.shibboleth.idp.attribute.filter.spring.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser to configure an {@link AttributeRule}.
 */
public class AttributeRuleParser extends BaseFilterParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(BaseFilterParser.NAMESPACE, "AttributeRule");

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(BaseFilterParser.NAMESPACE, "AttributeRuleType");

    /** PermitValueRuleReference. */
    public static final QName PERMIT_VALUE_REF = new QName(BaseFilterParser.NAMESPACE,
            "PermitValueRuleReference");

    /** DenyValueRuleReference. */
    public static final QName DENY_VALUE_REF = new QName(BaseFilterParser.NAMESPACE,
            "DenyValueRuleReference");

    /** permitAny Attribute. */
    public static final String PERMIT_ANY_ATTRIBUTE = "permitAny";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeRuleParser.class);

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<?> getBeanClass(@Nullable Element arg) {
        return AttributeRule.class;
    }

    /** {@inheritDoc} */
    // Checkstyle: CyclomaticComplexity OFF
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final String id = builder.getBeanDefinition().getAttribute("qualifiedId").toString();

        builder.addPropertyValue("id", id);

        final String attributeId = StringSupport.trimOrNull(config.getAttributeNS(null, "attributeID"));
        builder.addPropertyValue("attributeId", attributeId);

        final List<Element> permitValueRule = ElementSupport.getChildElements(config,
                BaseFilterParser.PERMIT_VALUE_RULE);
        final List<Element> permitValueReference = ElementSupport.getChildElements(config, PERMIT_VALUE_REF);
        final List<Element> denyValueRule = ElementSupport.getChildElements(config, BaseFilterParser.DENY_VALUE_RULE);
        final List<Element> denyValueReference = ElementSupport.getChildElements(config, DENY_VALUE_REF);

        if (permitValueRule != null && !permitValueRule.isEmpty()) {

            final ManagedList<BeanDefinition> permitValueRules =
                    SpringSupport.parseCustomElements(permitValueRule, parserContext);
            log.debug("permitValueRules {}", permitValueRules);
            builder.addPropertyValue("matcher", permitValueRules.get(0));
            builder.addPropertyValue("isDenyRule", false);

        } else if (permitValueReference != null && !permitValueReference.isEmpty()) {

            final String referenceText = getReferenceText(permitValueReference.get(0));
            if (null == referenceText) {
                throw new BeanCreationException("Attribute Rule '" + id + "' no text or reference for "
                        + PERMIT_VALUE_REF);
            }

            final String reference = getAbsoluteReference(config, "PermitValueRule", referenceText);
            log.debug("Adding PermitValueRule reference to {}", reference);
            builder.addPropertyValue("matcher", new RuntimeBeanReference(reference));
            builder.addPropertyValue("isDenyRule", false);

        } else if (denyValueRule != null && !denyValueRule.isEmpty()) {

            final ManagedList<BeanDefinition> denyValueRules =
                    SpringSupport.parseCustomElements(denyValueRule, parserContext);
            log.debug("denyValueRules {}", denyValueRules);
            builder.addPropertyValue("matcher", denyValueRules.get(0));
            builder.addPropertyValue("isDenyRule", true);

        } else if (denyValueReference != null && !denyValueReference.isEmpty()) {

            final String referenceText = getReferenceText(denyValueReference.get(0));
            if (null == referenceText) {
                throw new BeanCreationException("Attribute Rule '" + id + "' no text or reference for "
                        + DENY_VALUE_REF);
            }
            final String reference = getAbsoluteReference(config, "DenyValueRule", referenceText);
            log.debug("Adding DenyValueRule reference to {}", reference);
            builder.addPropertyValue("matcher", new RuntimeBeanReference(reference));
            builder.addPropertyValue("isDenyRule", true);
        } else if (config.hasAttributeNS(null, PERMIT_ANY_ATTRIBUTE)
                && AttributeSupport.getAttributeValueAsBoolean(config.getAttributeNodeNS(null, PERMIT_ANY_ATTRIBUTE))) {
            // Note the documented restriction that permitAny cannot be property replaced.
            builder.addPropertyValue("isDenyRule", false);
            builder.addPropertyValue("matcher", Matcher.MATCHES_ALL);
        } else {
            log.warn("Attribute rule must have one of PermitValueRule, "
                    + "DenyValueRule or have attribute permitAny=\"true\"");
        }
    }
    // Checkstyle: CyclomaticComplexity ON
}