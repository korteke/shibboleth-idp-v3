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

import net.shibboleth.idp.saml.nameid.impl.TransientNameIDDecoder;
import net.shibboleth.idp.saml.nameid.impl.TransientNameIdentifierDecoder;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for Transient Principal Connector<br/>
 * &lt;PrincipalConnector xsi:type="pc:Transient"&gt;.
 */
public class TransientConnectorParser extends AbstractPrincipalConnectorParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(PrincipalConnectorNamespaceHandler.NAMESPACE, "Transient");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(TransientConnectorParser.class);

    /** {@inheritDoc} */
    @Override protected void addSAMLDecoders(@Nonnull Element config, @Nonnull ParserContext parserContext,
            @Nonnull BeanDefinitionBuilder builder) {

        String idStore = "shibboleth.StorageService";
        if (config.hasAttributeNS(null, "storageServiceRef")) {
            idStore = StringSupport.trimOrNull(config.getAttributeNS(null, "storageServiceRef"));
        }
        
        // NameID
        BeanDefinitionBuilder subBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransientNameIDDecoder.class);
        subBuilder.setInitMethodName("initialize");
        subBuilder.setDestroyMethodName("destroy");
        
        subBuilder.addPropertyReference("idStore", idStore);
        final String id = StringSupport.trimOrNull(config.getAttributeNS(null, "id"));
        subBuilder.addPropertyValue("id", id);
                
        builder.addConstructorArgValue(subBuilder.getBeanDefinition());
        
        if (config.hasAttributeNS(null, "noResultIsError")) {
            log.warn("Transient PrincipalConnector does not support 'noResultIsError' attribute");
        }
        
        // NameIdentifier
        subBuilder = BeanDefinitionBuilder.genericBeanDefinition(TransientNameIdentifierDecoder.class);
        subBuilder.setInitMethodName("initialize");
        subBuilder.setDestroyMethodName("destroy");
        
        subBuilder.addPropertyReference("idStore", idStore);
        subBuilder.addPropertyValue("id", id);
        builder.addConstructorArgValue(subBuilder.getBeanDefinition());
    }

}