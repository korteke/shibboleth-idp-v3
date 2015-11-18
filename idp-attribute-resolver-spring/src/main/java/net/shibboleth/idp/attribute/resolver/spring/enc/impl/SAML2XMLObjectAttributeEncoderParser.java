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

import net.shibboleth.idp.attribute.resolver.spring.enc.BaseAttributeEncoderParser;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2XMLObjectAttributeEncoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Spring Bean Definition Parser for SAML2 XMLObject attribute encoder. */
public class SAML2XMLObjectAttributeEncoderParser extends BaseAttributeEncoderParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(AttributeEncoderNamespaceHandler.NAMESPACE,
            "SAML2XMLObject");

    /** Local name of name format attribute. */
    @Nonnull @NotEmpty public static final String NAME_FORMAT_ATTRIBUTE_NAME = "nameFormat";

    /** Local name of friendly name attribute. */
    @Nonnull @NotEmpty public static final String FRIENDLY_NAME_ATTRIBUTE_NAME = "friendlyName";

    /** Constructor. */
    public SAML2XMLObjectAttributeEncoderParser() {
        setNameRequired(true);
    }

    /** {@inheritDoc} */
    @Override protected Class<SAML2XMLObjectAttributeEncoder> getBeanClass(@Nullable final Element element) {
        return SAML2XMLObjectAttributeEncoder.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        if (config.hasAttributeNS(null, NAME_FORMAT_ATTRIBUTE_NAME)) {
            final String nameFormat = StringSupport.trimOrNull(config.getAttributeNS(null, NAME_FORMAT_ATTRIBUTE_NAME));
            builder.addPropertyValue("nameFormat", nameFormat);
        }

        builder.addPropertyValue("friendlyName",
                StringSupport.trimOrNull(config.getAttributeNS(null, FRIENDLY_NAME_ATTRIBUTE_NAME)));
    }

}