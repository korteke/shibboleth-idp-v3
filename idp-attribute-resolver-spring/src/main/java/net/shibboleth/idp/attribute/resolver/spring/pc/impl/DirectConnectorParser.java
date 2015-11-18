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

package net.shibboleth.idp.attribute.resolver.spring.pc.impl;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.saml.nameid.impl.TransformingNameIDDecoder;
import net.shibboleth.idp.saml.nameid.impl.TransformingNameIdentifierDecoder;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for Direct Principal Connector<br/>
 * &lt;PrincipalConnector xsi:type="pc:Direct"&gt;.
 */
public class DirectConnectorParser extends AbstractPrincipalConnectorParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(PrincipalConnectorNamespaceHandler.NAMESPACE, "Direct");

    /** {@inheritDoc} */
    @Override protected void addSAMLDecoders(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {

        BeanDefinitionBuilder subBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransformingNameIDDecoder.class);
        subBuilder.setInitMethodName("initialize");
        subBuilder.setDestroyMethodName("destroy");
        
        final String id = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        subBuilder.addPropertyValue("id", id);
        builder.addConstructorArgValue(subBuilder.getBeanDefinition());
        
        subBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransformingNameIdentifierDecoder.class);
        subBuilder.setInitMethodName("initialize");
        subBuilder.setDestroyMethodName("destroy");
        
        subBuilder.addPropertyValue("id", id);
        builder.addConstructorArgValue(subBuilder.getBeanDefinition());
    }

}