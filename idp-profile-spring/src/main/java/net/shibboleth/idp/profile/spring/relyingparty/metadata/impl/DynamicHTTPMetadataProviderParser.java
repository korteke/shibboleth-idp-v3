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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.impl;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.HTTPEntityIDRequestURLBuilder;
import org.opensaml.saml.metadata.resolver.impl.MetadataQueryProtocolRequestURLBuilder;
import org.opensaml.saml.metadata.resolver.impl.RegexRequestURLBuilder;
import org.opensaml.saml.metadata.resolver.impl.TemplateRequestURLBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for concrete dynamic HTTP metadata resolvers, based on {@link FunctionDrivenDynamicHTTPMetadataResolver}.
 */
public class DynamicHTTPMetadataProviderParser extends AbstractDynamicHTTPMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "DynamicHTTPMetadataProvider");

    /** Template child element name. */
    private static final QName TEMPLATE = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "Template");

    /** Regex child element name. */
    private static final QName REGEX = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "Regex");

    /** MetadataQueryProtocol child element name. */
    private static final QName METADATA_QUERY_PROTOCOL = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "MetadataQueryProtocol");

    /** Name of default VelocityEngine bean to use. */
    private static final String DEFAULT_VELOCITY_ENGINE_REF = "shibboleth.VelocityEngine";

    /** {@inheritDoc} */
    @Override
    protected Class<? extends MetadataResolver> getNativeBeanClass(Element element) {
        return FunctionDrivenDynamicHTTPMetadataResolver.class;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        builder.addPropertyValue("requestURLBuilder", getRequestURLBuilder(element));

    }

    /**
     * Build and return an instance of the {@link com.google.common.base.Function} used as the request URL builder.
     * 
     * @param element the parent metadata provider element
     * @return the function
     */
    protected BeanDefinition getRequestURLBuilder(Element element) {
        // Note: we have to do this BeanDefinitionBuilder business b/c for the template one, we need to
        // inject the VelocityEngine. Otherwise would be easier to just return the Function directly.

        // Template child
        Element template = ElementSupport.getFirstChildElement(element, TEMPLATE);
        if (template != null) {
            String templateString = StringSupport.trimOrNull(ElementSupport.getElementContentAsString(template));
            Boolean encoded = null;
            if (template.hasAttributeNS(null, "encoded")) {
                encoded = AttributeSupport.getAttributeValueAsBoolean(template.getAttributeNodeNS(null, "encoded"));
            }
            if (encoded == null) {
                encoded = true;
            }
            String velocityEngineRef =
                    StringSupport.trimOrNull(StringSupport.trimOrNull(template.getAttributeNS(null, "velocityEngine")));
            if (null == velocityEngineRef) {
                velocityEngineRef = DEFAULT_VELOCITY_ENGINE_REF;
            }
            String transformRef =
                    StringSupport.trimOrNull(StringSupport.trimOrNull(template.getAttributeNS(null, "transformRef")));

            BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.genericBeanDefinition(TemplateRequestURLBuilder.class);
            builder.addConstructorArgReference(velocityEngineRef);
            builder.addConstructorArgValue(templateString);
            builder.addConstructorArgValue(encoded);
            if (transformRef != null) {
                builder.addConstructorArgReference(transformRef);
            }
            return builder.getBeanDefinition();
        }

        // Regex child
        Element regex = ElementSupport.getFirstChildElement(element, REGEX);
        if (regex != null) {
            String replacement = StringSupport.trimOrNull(ElementSupport.getElementContentAsString(regex));
            String match = StringSupport.trimOrNull(StringSupport.trimOrNull(regex.getAttributeNS(null, "match")));

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RegexRequestURLBuilder.class);
            builder.addConstructorArgValue(match);
            builder.addConstructorArgValue(replacement);
            return builder.getBeanDefinition();
        }

        // MetadataQueryProtocol child
        Element mdq = ElementSupport.getFirstChildElement(element, METADATA_QUERY_PROTOCOL);
        if (mdq != null) {
            String baseURL = ElementSupport.getElementContentAsString(mdq);
            String transformRef =
                    StringSupport.trimOrNull(StringSupport.trimOrNull(mdq.getAttributeNS(null, "transformRef")));

            BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.genericBeanDefinition(MetadataQueryProtocolRequestURLBuilder.class);
            builder.addConstructorArgValue(baseURL);
            if (transformRef != null) {
                builder.addConstructorArgReference(transformRef);
            }
            return builder.getBeanDefinition();
        }

        // None of the above, so return the well-known location one. Takes no args or properties.
        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(HTTPEntityIDRequestURLBuilder.class);
        return builder.getBeanDefinition();
    }

}
