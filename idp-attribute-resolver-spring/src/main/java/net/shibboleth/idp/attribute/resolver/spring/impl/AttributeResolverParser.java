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

package net.shibboleth.idp.attribute.resolver.spring.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.spring.ad.BaseAttributeDefinitionParser;
import net.shibboleth.idp.attribute.resolver.spring.dc.impl.AbstractDataConnectorParser;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for an {@link net.shibboleth.idp.attribute.resolver.AttributeResolver}. <br/>
 * 
 * There is no bean being summoned up here. Rather we just parse all the children. Then over in the service all the
 * {@link net.shibboleth.idp.attribute.resolver.AttributeDefinition},
 * {@link net.shibboleth.idp.attribute.resolver.DataConnector} and
 * {@link net.shibboleth.idp.saml.attribute.principalconnector.impl.PrincipalConnector} beans are sucked out of Spring
 * by type and injected into a new {@link net.shibboleth.idp.attribute.resolver.impl.AttributeResolverImpl} via a
 * {@link net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverServiceStrategy}.
 */
public class AttributeResolverParser implements BeanDefinitionParser {

    /** Element name. */
    @Nonnull public static final QName ELEMENT_NAME = new QName(AttributeResolverNamespaceHandler.NAMESPACE,
            "AttributeResolver");

    /** Schema type. */
    @Nonnull public static final QName SCHEMA_TYPE = new QName(AttributeResolverNamespaceHandler.NAMESPACE,
            "AttributeResolverType");

    /**
     * {@inheritDoc}
     */
    @Override public BeanDefinition parse(Element config, ParserContext context) {

        final Map<QName, List<Element>> configChildren = ElementSupport.getIndexedChildElements(config);
        List<Element> children;

        children = configChildren.get(BaseAttributeDefinitionParser.ELEMENT_NAME);
        SpringSupport.parseCustomElements(children, context);

        children = configChildren.get(AbstractDataConnectorParser.ELEMENT_NAME);
        SpringSupport.parseCustomElements(children, context);

        children = configChildren.get(new QName(AttributeResolverNamespaceHandler.NAMESPACE, "PrincipalConnector"));
        SpringSupport.parseCustomElements(children, context);
        return null;
    }

}