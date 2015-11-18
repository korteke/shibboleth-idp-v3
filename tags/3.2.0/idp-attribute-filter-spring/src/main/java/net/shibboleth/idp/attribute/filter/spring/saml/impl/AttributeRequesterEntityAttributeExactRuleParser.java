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

package net.shibboleth.idp.attribute.filter.spring.saml.impl;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.AttributeRequesterEntityAttributeExactPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for {@link AttributeRequesterEntityAttributeExactPolicyRule}.
 */
public class AttributeRequesterEntityAttributeExactRuleParser extends AbstractEntityAttributeRuleParser {

    /** Schema type - v2. */
    public static final QName SCHEMA_TYPE_V2 = new QName(AttributeFilterSAMLNamespaceHandler.NAMESPACE,
            "AttributeRequesterEntityAttributeExactMatch");

    /** Schema type -saml. */
    public static final QName SCHEMA_TYPE = new QName(AttributeFilterSAMLNamespaceHandler.NAMESPACE,
            "EntityAttributeExactMatch");

    /** Schema type -saml. */
    public static final QName SCHEMA_TYPE_AFP = new QName(BaseFilterParser.NAMESPACE,
            "EntityAttributeExactMatch");

    /** log. */
    private final Logger log = LoggerFactory.getLogger(AttributeRequesterEntityAttributeExactRuleParser.class);

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<AttributeRequesterEntityAttributeExactPolicyRule> getNativeBeanClass() {

        return AttributeRequesterEntityAttributeExactPolicyRule.class;
    }

    /** {@inheritDoc} */
    @Override protected QName getAFPName() {
        return SCHEMA_TYPE_AFP;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(@Nonnull final Element element, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        if (SCHEMA_TYPE_V2.equals(DOMTypeSupport.getXSIType(element))) {
            log.info("Filter type '{}' has been deprecated, please use '{}'.", SCHEMA_TYPE_V2.getLocalPart(),
                    SCHEMA_TYPE.getLocalPart());
        }

        builder.addPropertyValue("value", element.getAttributeNS(null, "attributeValue"));
    }
}
