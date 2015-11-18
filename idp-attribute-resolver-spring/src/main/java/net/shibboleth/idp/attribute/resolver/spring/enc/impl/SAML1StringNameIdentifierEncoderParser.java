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

package net.shibboleth.idp.attribute.resolver.spring.enc.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1StringNameIdentifierEncoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser for {@link SAML1StringNameIdentifierEncoder}.
 */
public class SAML1StringNameIdentifierEncoderParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type. */
    @Nonnull public static final QName SCHEMA_TYPE = new QName(AttributeEncoderNamespaceHandler.NAMESPACE,
            "SAML1StringNameIdentifier");

    /** Local name of name format attribute. */
    @Nonnull @NotEmpty public static final String FORMAT_ATTRIBUTE_NAME = "nameFormat";

    /** Local name of name qualifier attribute. */
    @Nonnull @NotEmpty public static final String NAMEQUALIFIER_ATTRIBUTE_NAME = "nameQualifier";

    /** {@inheritDoc} */
    @Override protected Class<SAML1StringNameIdentifierEncoder> getBeanClass(@Nullable final Element element) {
        return SAML1StringNameIdentifierEncoder.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        if (config.hasAttributeNS(null, FORMAT_ATTRIBUTE_NAME)) {
            final String format = StringSupport.trimOrNull(config.getAttributeNS(null, FORMAT_ATTRIBUTE_NAME));
            builder.addPropertyValue("nameFormat", format);
        }
        builder.setInitMethodName(null);

        builder.addPropertyValue("nameQualifier",
                StringSupport.trimOrNull(config.getAttributeNS(null, NAMEQUALIFIER_ATTRIBUTE_NAME)));
    }

    /** {@inheritDoc} */
    @Override public boolean shouldGenerateId() {
        return true;
    }

}