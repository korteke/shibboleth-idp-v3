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

package net.shibboleth.idp.profile.spring.resource.impl;

import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Element;

/**
 * Parser for resources of type ClasspathResource.
 */
public class ClasspathResourceParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type. */
    public static final QName ELEMENT_NAME = new QName(ResourceNamespaceHandler.NAMESPACE, "ClasspathResource");

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return ClassPathResource.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setLazyInit(true);
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "file")));
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
}
