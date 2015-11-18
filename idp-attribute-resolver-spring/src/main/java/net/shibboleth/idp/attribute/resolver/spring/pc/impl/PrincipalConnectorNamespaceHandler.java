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

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Namespace handler for the principal connector. This is a noop and is here purely to allow us to have a handler
 * declared (since all parsing is done inline).
 */
public class PrincipalConnectorNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for principal connector (which have not handlers). */
    @Nonnull @NotEmpty public static final String NAMESPACE = "urn:mace:shibboleth:2.0:resolver:pc";

    /** {@inheritDoc} */
    @Override public void init() {
        registerBeanDefinitionParser(DirectConnectorParser.TYPE_NAME, new DirectConnectorParser());

        registerBeanDefinitionParser(TransientConnectorParser.TYPE_NAME, new TransientConnectorParser());

        registerBeanDefinitionParser(CryptoTransientConnectorParser.TYPE_NAME, new CryptoTransientConnectorParser());

        registerBeanDefinitionParser(StoredIdConnectorParser.TYPE_NAME, new StoredIdConnectorParser());
    }
    
}