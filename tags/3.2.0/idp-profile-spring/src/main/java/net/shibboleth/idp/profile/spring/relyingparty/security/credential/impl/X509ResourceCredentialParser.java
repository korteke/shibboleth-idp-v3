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

package net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl;

import javax.xml.namespace.QName;

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Parser for X509Filesystem credentials.
 */
public class X509ResourceCredentialParser extends AbstractX509CredentialParser {

    /** Element Name for Filesystem. */
    public static final QName TYPE_NAME_FILESYSTEM = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "X509Filesystem");

    /** Type for X509 credentials. */
    public static final QName TYPE_NAME_RESOURCE = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "X509ResourceBacked");

    /** log. */
    private final Logger log = LoggerFactory.getLogger(X509ResourceCredentialParser.class);

    @Override protected Class<?> getBeanClass(Element element) {
        return X509ResourceCredentialFactoryBean.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, BeanDefinitionBuilder builder) {
        if (TYPE_NAME_FILESYSTEM.equals(DOMTypeSupport.getXSIType(element))) {
            log.warn("Credential type '{}' has been deprecated; use the compatible Credential type '{}'",
                    TYPE_NAME_FILESYSTEM.getLocalPart(), TYPE_NAME_RESOURCE.getLocalPart());
        }
        super.doParse(element, builder);
    }

}
