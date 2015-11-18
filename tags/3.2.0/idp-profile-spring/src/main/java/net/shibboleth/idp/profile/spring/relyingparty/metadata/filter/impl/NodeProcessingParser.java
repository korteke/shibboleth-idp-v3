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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.impl.MetadataNamespaceHandler;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.filter.impl.NodeProcessingMetadataFilter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for a &lt;NodeProcessing&gt; filter.
 */
public class NodeProcessingParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    public static final QName TYPE_NAME =
            new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "NodeProcessing");

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(Element element) {
        return NodeProcessingMetadataFilter.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");
        builder.setLazyInit(true);

        final List<Element> children =
                ElementSupport.getChildElements(element, MetadataNamespaceHandler.METADATA_NODE_PROC_ELEMENT_NAME);

        if (null != children && !children.isEmpty()) {
            builder.addPropertyValue("nodeProcessors", SpringSupport.parseCustomElements(children, parserContext));
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }

}