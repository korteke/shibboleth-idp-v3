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

package net.shibboleth.idp.attribute.resolver.spring.ad;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.attribute.resolver.spring.BaseResolverPluginParser;
import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base spring bean definition parser for attribute definitions. AttributeDefinition implementations should provide a
 * custom BeanDefinitionParser by extending this class and overriding the doParse() method to parse any additional
 * attributes or elements it requires. Standard attributes and elements defined by the ResolutionPlugIn and
 * AttributeDefinition schemas will automatically attempt to be parsed.
 */
public abstract class BaseAttributeDefinitionParser extends BaseResolverPluginParser {

    /** Element name. */
    @Nonnull public static final QName ELEMENT_NAME =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "AttributeDefinition");

    /** Local name of attribute encoder. */
    @Nonnull public static final QName ATTRIBUTE_ENCODER_ELEMENT_NAME =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "AttributeEncoder");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BaseAttributeDefinitionParser.class);

    /** {@inheritDoc} */
    // CheckStyle: CyclomaticComplexity OFF
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final List<Element> displayNames =
                ElementSupport.getChildElements(config, new QName(AttributeResolverNamespaceHandler.NAMESPACE,
                        "DisplayName"));
        if (displayNames != null && !displayNames.isEmpty()) {
            final Map<Locale, String> names = processLocalizedElement(displayNames);
            log.debug("{} Setting displayNames {}", getLogPrefix(), names);
            builder.addPropertyValue("displayNames", names);
        }

        final List<Element> displayDescriptions =
                ElementSupport.getChildElements(config, new QName(AttributeResolverNamespaceHandler.NAMESPACE,
                        "DisplayDescription"));
        if (displayDescriptions != null && !displayDescriptions.isEmpty()) {
            final Map<Locale, String> names = processLocalizedElement(displayDescriptions);
            log.debug("{} Setting displayDescriptions {}", getLogPrefix(), names);
            builder.addPropertyValue("displayDescriptions", names);
        }

        if (config.hasAttributeNS(null, "dependencyOnly")) {
            String dependencyOnly = StringSupport.trimOrNull(config.getAttributeNS(null, "dependencyOnly"));
            log.debug("{} Setting dependencyOnly {}", getLogPrefix(), dependencyOnly);
            builder.addPropertyValue("dependencyOnly", dependencyOnly);
        }

        if (config.hasAttributeNS(null, "sourceAttributeID")) {
            final String sourceAttributeId = config.getAttributeNodeNS(null, "sourceAttributeID").getValue();
            log.debug("{} Setting sourceAttributeID {}", getLogPrefix(), sourceAttributeId);
            builder.addPropertyValue("sourceAttributeId", sourceAttributeId);
            if (!needsAttributeSourceID()) {
                log.warn("{} sourceAttributeID was specified but is meaningless, add {} as a <Dependency> instead",
                        getLogPrefix(), sourceAttributeId);
            }
        } else if (needsAttributeSourceID()) {
            log.warn("{} sourceAttributeID was not specified but is required", getLogPrefix());
        }

        final List<Element> attributeEncoders =
                ElementSupport.getChildElements(config, new QName(AttributeResolverNamespaceHandler.NAMESPACE,
                        "AttributeEncoder"));

        if (attributeEncoders != null && !attributeEncoders.isEmpty()) {
            log.debug("{} Adding {} encoders", getLogPrefix(), attributeEncoders.size());
            builder.addPropertyValue("attributeEncoders",
                    SpringSupport.parseCustomElements(attributeEncoders, parserContext));
        }
    }
    // CheckStyle: CyclomaticComplexity ON


    /**
     * Used to process string elements that contain an xml:lang attribute expressing localization. returns a
     * {@link ManagedMap} to allow property replacement to work.
     * 
     * @param elements list of elements, must not be null, may be empty
     * 
     * @return the localized string indexed by locale
     */
    protected Map<Locale, String> processLocalizedElement(@Nonnull final List<Element> elements) {
        Map<Locale, String> localizedString = new ManagedMap<>(elements.size());
        for (Element element : elements) {
            localizedString.put(AttributeSupport.getXMLLangAsLocale(element), element.getTextContent());
        }

        return localizedString;
    }

    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Definition '<definitionID>' :"
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        StringBuilder builder = new StringBuilder("Attribute Definition '").append(getDefinitionId()).append("':");
        return builder.toString();
    }

    /**
     * Ask the specific parser of it needs attributeSourceID. We use this to log several misconfiguration possibilities.
     * 
     * @return Whether the attribute definition for this parser meeds attributeSourceID.
     */
    protected abstract boolean needsAttributeSourceID();
    
}