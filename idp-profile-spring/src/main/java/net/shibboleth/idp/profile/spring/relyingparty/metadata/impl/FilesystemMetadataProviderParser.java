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

import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for a &lt;FilesystemMetadataProvider&gt;.
 */
public class FilesystemMetadataProviderParser extends AbstractReloadingMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME =
            new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "FilesystemMetadataProvider");

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(FilesystemMetadataProviderParser.class);

    /** {@inheritDoc} */
    @Override protected Class<FilesystemMetadataResolver> getNativeBeanClass(Element element) {
        return FilesystemMetadataResolver.class;
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "maintainExpiredMetadata")) {
            log.error("{}: maintainExpiredMetadata is not supported", parserContext.getReaderContext().getResource()
                    .getDescription());
            throw new BeanDefinitionParsingException(new Problem("maintainExpiredMetadata is not supported",
                    new Location(parserContext.getReaderContext().getResource())));
        }

        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "metadataFile")));
    }
}
