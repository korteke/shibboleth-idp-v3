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

import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for a &lt;FileBackedHTTPMetadataProvider&gt;.
 */
public class FileBackedHTTPMetadataProviderParser extends HTTPMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "FileBackedHTTPMetadataProvider");

    /** {@inheritDoc} */
    @Override protected Class<FileBackedHTTPMetadataResolver> getNativeBeanClass(Element element) {
        return FileBackedHTTPMetadataResolver.class;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "backingFile")));
    }
}
