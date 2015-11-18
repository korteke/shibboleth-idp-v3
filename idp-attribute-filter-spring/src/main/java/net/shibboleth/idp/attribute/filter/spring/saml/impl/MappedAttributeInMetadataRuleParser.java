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

import net.shibboleth.idp.attribute.filter.matcher.saml.impl.MappedAttributeInMetadataMatcher;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.idp.attribute.filter.spring.matcher.BaseAttributeValueMatcherParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for {@link MappedAttributeInMetadataMatcher}.
 */
public class MappedAttributeInMetadataRuleParser extends BaseAttributeValueMatcherParser {

    /** Schema type. */
    public static final QName MAPPED_ATTRIBUTE_IN_METADATA = new QName(AttributeFilterSAMLNamespaceHandler.NAMESPACE,
            "MappedAttributeInMetadata");

    /** Schema type. */
    public static final QName MAPPED_ATTRIBUTE_IN_METADATA_AFP = new QName(BaseFilterParser.NAMESPACE,
            "MappedAttributeInMetadata");

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<MappedAttributeInMetadataMatcher> getNativeBeanClass() {
        return MappedAttributeInMetadataMatcher.class;
    }

    /** {@inheritDoc} */
    @Override protected QName getAFPName() {
        return MAPPED_ATTRIBUTE_IN_METADATA_AFP;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, builder);

        if (config.hasAttributeNS(null, "onlyIfRequired")) {
            builder.addPropertyValue("onlyIfRequired",
                    StringSupport.trimOrNull(config.getAttributeNS(null, "onlyIfRequired")));
        }

        if (config.hasAttributeNS(null, "matchIfMetadataSilent")) {
            builder.addPropertyValue("matchIfMetadataSilent",
                    StringSupport.trimOrNull(config.getAttributeNS(null, "matchIfMetadataSilent")));
        }
    }

}