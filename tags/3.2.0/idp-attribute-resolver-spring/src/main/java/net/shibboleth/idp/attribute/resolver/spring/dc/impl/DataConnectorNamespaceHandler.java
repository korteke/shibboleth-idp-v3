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

package net.shibboleth.idp.attribute.resolver.spring.dc.impl;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import net.shibboleth.idp.attribute.resolver.spring.dc.ldap.impl.LDAPDataConnectorParser;
import net.shibboleth.idp.attribute.resolver.spring.dc.rdbms.impl.RDBMSDataConnectorParser;

/** Namespace handler for the Shibboleth static data connector namespace. */
public class DataConnectorNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    @Nonnull public static final String NAMESPACE = "urn:mace:shibboleth:2.0:resolver:dc";

    /** {@inheritDoc} */
    @Override public void init() {
        registerBeanDefinitionParser(StaticDataConnectorParser.TYPE_NAME, new StaticDataConnectorParser());
        registerBeanDefinitionParser(ComputedIDDataConnectorParser.TYPE_NAME, new ComputedIDDataConnectorParser());
        registerBeanDefinitionParser(StoredIDDataConnectorParser.TYPE_NAME, new StoredIDDataConnectorParser());
        registerBeanDefinitionParser(RDBMSDataConnectorParser.TYPE_NAME, new RDBMSDataConnectorParser());
        registerBeanDefinitionParser(LDAPDataConnectorParser.TYPE_NAME, new LDAPDataConnectorParser());
        registerBeanDefinitionParser(ScriptDataConnectorParser.TYPE_NAME, new ScriptDataConnectorParser());
    }
    
}