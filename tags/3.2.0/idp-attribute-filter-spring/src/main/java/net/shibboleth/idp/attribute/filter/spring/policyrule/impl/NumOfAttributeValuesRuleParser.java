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

package net.shibboleth.idp.attribute.filter.spring.policyrule.impl;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.filter.policyrule.filtercontext.impl.NumOfAttributeValuesPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.idp.attribute.filter.spring.basic.impl.AttributeFilterBasicNamespaceHandler;
import net.shibboleth.idp.attribute.filter.spring.policyrule.BasePolicyRuleParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for {@link NumOfAttributeValuesPolicyRule}.
 */
public class NumOfAttributeValuesRuleParser extends BasePolicyRuleParser {

    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(AttributeFilterBasicNamespaceHandler.NAMESPACE,
            "NumberOfAttributeValues");

    /** Schema type. */
    public static final QName SCHEMA_TYPE_AFP = new QName(BaseFilterParser.NAMESPACE, "NumberOfAttributeValues");

    /** {@inheritDoc} */
    @Override protected QName getAFPName() {
        return SCHEMA_TYPE_AFP;
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<NumOfAttributeValuesPolicyRule> getNativeBeanClass() {
        return NumOfAttributeValuesPolicyRule.class;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, builder);

        final String attributeId = StringSupport.trimOrNull(config.getAttributeNS(null, "attributeID"));
        builder.addPropertyValue("attributeId", attributeId);

        final String minimum = StringSupport.trimOrNull(config.getAttributeNS(null, "minimum"));
        builder.addPropertyValue("minimum", minimum);

        final String maximum = StringSupport.trimOrNull(config.getAttributeNS(null, "maximum"));
        builder.addPropertyValue("maximum", maximum);
    }
}
