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

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.idp.attribute.resolver.spring.ResolverPluginDependencyParser;
import net.shibboleth.idp.attribute.resolver.spring.pc.impl.DirectConnectorParser;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

/** Namespace handler for the attribute resolver. */
public class AttributeResolverNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    @Nonnull @NotEmpty public static final String NAMESPACE = "urn:mace:shibboleth:2.0:resolver";

    /** {@inheritDoc} */
    @Override public void init() {
        BeanDefinitionParser parser = new AttributeResolverParser();
        registerBeanDefinitionParser(AttributeResolverParser.SCHEMA_TYPE, parser);
        registerBeanDefinitionParser(AttributeResolverParser.ELEMENT_NAME, parser);
        registerBeanDefinitionParser(DirectConnectorParser.TYPE_NAME, new DirectConnectorParser());
        registerBeanDefinitionParser(ResolverPluginDependencyParser.ELEMENT_NAME, new ResolverPluginDependencyParser());
    }
    
}