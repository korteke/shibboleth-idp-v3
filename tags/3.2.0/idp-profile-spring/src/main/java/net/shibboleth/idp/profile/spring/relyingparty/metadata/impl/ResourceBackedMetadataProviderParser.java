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

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.HttpClientFactoryBean;
import net.shibboleth.idp.profile.spring.resource.impl.ClasspathResourceParser;
import net.shibboleth.idp.profile.spring.resource.impl.ResourceNamespaceHandler;
import net.shibboleth.idp.profile.spring.resource.impl.SVNResourceParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for a &lt;ResourceBackedMetadataProvider;gt;. <br/>
 * This is the most complicated of the parsers. We reach into the resource and find out what sort it is and them summon
 * up an appropriate provider.
 */
public class ResourceBackedMetadataProviderParser extends AbstractReloadingMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "ResourceBackedMetadataProvider");

    /** Element name for the resource elements. */
    public static final QName RESOURCES_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "MetadataResource");

    /** Log. */
    private final Logger log = LoggerFactory.getLogger(ResourceBackedMetadataProviderParser.class);

    /** {@inheritDoc} */
    @Override protected Class<? extends AbstractBatchMetadataResolver> getNativeBeanClass(Element element) {

        final List<Element> resources = ElementSupport.getChildElements(element, RESOURCES_NAME);
        if (null == resources || resources.isEmpty()) {
            throw new BeanCreationException("No <Resource> specified for ResourceBackedMetadataProvider");
        }
        final QName qName = DOMTypeSupport.getXSIType(resources.get(0));
        if (null == qName) {
            log.error("No type specified for a <Resource> within a ResourceBackedMetadataProvider");
            throw new BeanCreationException(
                    "No type specified for a <Resource> within a ResourceBackedMetadataProvider");
        }
        log.debug("comparing type '{}' against known Resources", qName.getLocalPart());

        if (ClasspathResourceParser.ELEMENT_NAME.equals(qName)) {
            return ResourceBackedMetadataResolver.class;
        } else if (SVNResourceParser.ELEMENT_NAME.equals(qName)) {
            return ResourceBackedMetadataResolver.class;
        } else if (ResourceNamespaceHandler.HTTP_ELEMENT_NAME.equals(qName)) {
            return HTTPMetadataResolver.class;
        } else if (ResourceNamespaceHandler.FILE_HTTP_ELEMENT_NAME.equals(qName)) {
            return FileBackedHTTPMetadataResolver.class;
        } else if (ResourceNamespaceHandler.FILESYSTEM_ELEMENT_NAME.equals(qName)) {
            return FilesystemMetadataResolver.class;
        }

        log.error("ResourceBackedMetadataProvider : Unrecognised resource type: {} ", qName.getLocalPart());
        throw new BeanCreationException("ResourceBackedMetadataProvider : Unrecognised resource type: "
                + qName.getLocalPart());
    }

    /** {@inheritDoc} */
    @Override protected void doNativeParse(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "maxCacheDuration")) {
            log.error("{}: maxCacheDuration is not supported", parserContext.getReaderContext().getResource()
                    .getDescription());
            throw new BeanDefinitionParsingException(new Problem("maxCacheDuration is not supported", new Location(
                    parserContext.getReaderContext().getResource())));
        }

        final List<Element> resources = ElementSupport.getChildElements(element, RESOURCES_NAME);
        if (resources.size() != 1) {
            log.error("{}: Only one Resource may be supplied to a ResourceBackedMetadataProvider", parserContext
                    .getReaderContext().getResource().getDescription());
            throw new BeanDefinitionParsingException(new Problem(
                    "Only one Resource may be supplied to a ResourceBackedMetadataProvider", new Location(parserContext
                            .getReaderContext().getResource())));
        }

        ResourceNamespaceHandler.noFilters(resources.get(0), parserContext.getReaderContext());

        final QName qName = DOMTypeSupport.getXSIType(resources.get(0));
        log.debug("Dispatching based on type '{}'", qName.getLocalPart());

        if (ClasspathResourceParser.ELEMENT_NAME.equals(qName)) {

            parseResource(resources.get(0), parserContext, builder);

        } else if (SVNResourceParser.ELEMENT_NAME.equals(qName)) {

            parseResource(resources.get(0), parserContext, builder);

        } else if (ResourceNamespaceHandler.HTTP_ELEMENT_NAME.equals(qName)) {

            log.warn("{}: {} is deprecated. consider using {}", parserContext.getReaderContext().getResource()
                    .getDescription(), ResourceNamespaceHandler.HTTP_ELEMENT_NAME.getLocalPart(),
                    HTTPMetadataProviderParser.ELEMENT_NAME.getLocalPart());
            parseHTTPResource(resources.get(0), parserContext, builder);

        } else if (ResourceNamespaceHandler.FILE_HTTP_ELEMENT_NAME.equals(qName)) {

            log.warn("{}: {} is deprecated. consider using {}", parserContext.getReaderContext().getResource()
                    .getDescription(), ResourceNamespaceHandler.FILE_HTTP_ELEMENT_NAME.getLocalPart(),
                    FileBackedHTTPMetadataProviderParser.ELEMENT_NAME.getLocalPart());
            parseFileBackedHTTPResource(resources.get(0), parserContext, builder);

        } else if (ResourceNamespaceHandler.FILESYSTEM_ELEMENT_NAME.equals(qName)) {

            log.warn("{}: {} is deprecated. consider using {}", parserContext.getReaderContext().getResource()
                    .getDescription(), ResourceNamespaceHandler.FILESYSTEM_ELEMENT_NAME.getLocalPart(),
                    FilesystemMetadataProviderParser.ELEMENT_NAME.getLocalPart());
            parseFilesystemResource(resources.get(0), parserContext, builder);
        }
    }

    /**
     * Parse the provided &lt;Resource&gt; and populate an appropriate {@link ResourceBackedMetadataResolver}.
     * 
     * @param element the &lt;Resource&gt; element
     * @param parserContext the parser context
     * @param builder the builder for the {@link ResourceBackedMetadataResolver}.
     */
    private void parseResource(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        BeanDefinitionBuilder resourceConverter = BeanDefinitionBuilder.genericBeanDefinition(ResourceHelper.class);
        resourceConverter.setLazyInit(true);
        resourceConverter.setFactoryMethod("of");
        resourceConverter.addConstructorArgValue(parserContext.getDelegate().parseCustomElement(element));

        builder.addConstructorArgValue(resourceConverter.getBeanDefinition());
    }

    /**
     * Parse the provided &lt;Resource&gt; and populate an appropriate {@link HTTPMetadataResolver}.
     * 
     * <br/>
     * See {@link HTTPMetadataProviderParser#doParse(Element, ParserContext, BeanDefinitionBuilder)}.
     * 
     * @param element the &lt;Resource&gt; element
     * @param parserContext the parser context
     * @param builder the builder for the {@link HTTPMetadataResolver}.
     */
    private void parseHTTPResource(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        BeanDefinitionBuilder clientBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpClientFactoryBean.class);

        clientBuilder.setLazyInit(true);

        builder.addConstructorArgValue(clientBuilder.getBeanDefinition());
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "url")));
    }

    /**
     * Parse the provided &lt;Resource&gt; and populate an appropriate {@link FileBackedHTTPMetadataResolver}. <br/>
     * See {@link FileBackedHTTPMetadataProviderParser#doParse(Element, ParserContext, BeanDefinitionBuilder)}.
     * 
     * @param element the &lt;Resource&gt; element
     * @param parserContext the parser context
     * @param builder the builder for the {@link FileBackedHTTPMetadataResolver}.
     */
    private void
            parseFileBackedHTTPResource(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        parseHTTPResource(element, parserContext, builder);
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "file")));
    }

    /**
     * Parse the provided &lt;Resource&gt; and populate an appropriate {@link FilesystemMetadataResolver}.
     * 
     * <br/>
     * See {@link FilesystemMetadataProviderParser#doParse(Element, ParserContext, BeanDefinitionBuilder)}.
     * 
     * @param element the &lt;Resource&gt; element
     * @param parserContext the parser context
     * @param builder the builder for the {@link FilesystemMetadataResolver}.
     */
    private void parseFilesystemResource(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, "file")));
    }

}
