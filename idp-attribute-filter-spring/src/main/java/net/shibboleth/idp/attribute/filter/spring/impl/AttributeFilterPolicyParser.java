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
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Bean definition parser for an {@link AttributeFilterPolicy}. */
public class AttributeFilterPolicyParser extends BaseFilterParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(BaseFilterParser.NAMESPACE,
            "AttributeFilterPolicy");

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(BaseFilterParser.NAMESPACE,
            "AttributeFilterPolicyType");

    /** The PolicyRequirementRuleReference QName. */
    public static final QName POLICY_REQUIREMENT_RULE_REF = new QName(BaseFilterParser.NAMESPACE,
            "PolicyRequirementRuleReference");

    /** The AttributeRule QName. */
    private static final QName ATTRIBUTE_RULE = new QName(BaseFilterParser.NAMESPACE, "AttributeRule");

    /** The AttributeRuleReference QName. */
    private static final QName ATTRIBUTE_RULE_REF = new QName(BaseFilterParser.NAMESPACE,
            "AttributeRuleReference");

    /** Class logger. */
    private Logger log = LoggerFactory.getLogger(AttributeFilterPolicyParser.class);

    /** {@inheritDoc} */
    @Override
    protected Class<?> getBeanClass(Element arg0) {
        return AttributeFilterPolicy.class;
    }

    /** {@inheritDoc} */
 // Checkstyle: CyclomaticComplexity OFF
    @Override
    protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        String policyId = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        if (null == policyId) {
            policyId =  builder.getBeanDefinition().getAttribute("qualifiedId").toString();
        }
        log.debug("Parsing configuration for attribute filter policy: {}", policyId);
        builder.addConstructorArgValue(policyId);

        // Get the policy requirement, either inline or referenced
        final List<Element> policyRequirements = ElementSupport.getChildElements(config,
                BaseFilterParser.POLICY_REQUIREMENT_RULE);
        if (policyRequirements != null && policyRequirements.size() > 0) {
            final ManagedList<BeanDefinition> requirements =
                    SpringSupport.parseCustomElements(policyRequirements, parserContext);
            builder.addConstructorArgValue(requirements.get(0));
        } else {
            final List<Element> policyRequirementsRef =
                    ElementSupport.getChildElements(config, POLICY_REQUIREMENT_RULE_REF);
            if (policyRequirementsRef != null && policyRequirementsRef.size() > 0) {

                final String referenceText = getReferenceText(policyRequirementsRef.get(0));
                if (null == referenceText) {
                    throw new BeanCreationException("Attribute Filter '" + policyId + "' no text or reference for "
                            + POLICY_REQUIREMENT_RULE_REF);
                }

                final String reference = getAbsoluteReference(config, "PolicyRequirementRule", referenceText);
                log.debug("Adding PolicyRequirementRule reference to {}", reference);
                builder.addConstructorArgValue(new RuntimeBeanReference(reference));
            } else {
                throw new BeanCreationException("Attribute Filter '" + policyId
                        + "' A PolicyRequirementRule or a PolicyRequirementRuleReference should be present");
            }
        }

        // Get the attribute rules, both inline or referenced.
        final ManagedList<BeanMetadataElement> attributeRules = new ManagedList<>();
        final List<Element> rules = ElementSupport.getChildElements(config, ATTRIBUTE_RULE);
        if (rules != null && rules.size() > 0) {
            attributeRules.addAll(SpringSupport.parseCustomElements(rules, parserContext));
        }

        final List<Element> rulesRef = ElementSupport.getChildElements(config, ATTRIBUTE_RULE_REF);
        if (rulesRef != null && rulesRef.size() > 0) {
            for (Element ruleRef : rulesRef) {
                final String reference = getAbsoluteReference(config, "AttributeRule", getReferenceText(ruleRef));
                attributeRules.add(new RuntimeBeanReference(reference));
            }
        }

        builder.addConstructorArgValue(attributeRules);
    }
    // Checkstyle: CyclomaticComplexity ON
}