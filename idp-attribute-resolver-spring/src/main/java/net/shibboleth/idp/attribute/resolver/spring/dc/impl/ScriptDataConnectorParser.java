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

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.factory.EvaluableScriptFactoryBean;
import net.shibboleth.idp.attribute.resolver.dc.impl.ScriptedDataConnector;
import net.shibboleth.idp.attribute.resolver.spring.ad.impl.ScriptedAttributeDefinitionParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Bean definition Parser for a {@link ScriptedDataConnector}. */
public class ScriptDataConnectorParser extends AbstractDataConnectorParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE, "Script");

    /** Script file element name. */
    @Nonnull public static final QName SCRIPT_FILE_ELEMENT_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE,
            "ScriptFile");

    /** Inline Script element name. */
    @Nonnull public static final QName SCRIPT_ELEMENT_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE,
            "Script");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedAttributeDefinitionParser.class);

    /** {@inheritDoc} */
    @Override protected Class<ScriptedDataConnector> getNativeBeanClass() {
        return ScriptedDataConnector.class;
    }

    /** {@inheritDoc} */
    @Override protected void doV2Parse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {

        BeanDefinitionBuilder scriptBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(EvaluableScriptFactoryBean.class);
        scriptBuilder.addPropertyValue("sourceId", getLogPrefix());
        if (config.hasAttributeNS(null, "language")) {
            final String scriptLanguage = StringSupport.trimOrNull(config.getAttributeNS(null, "language"));
            log.debug("{} Scripting language: {}", getLogPrefix(), scriptLanguage);
            scriptBuilder.addPropertyValue("engineName", scriptLanguage);
        }

        final List<Element> scriptElem = ElementSupport.getChildElements(config, SCRIPT_ELEMENT_NAME);
        final List<Element> scriptFileElem = ElementSupport.getChildElements(config, SCRIPT_FILE_ELEMENT_NAME);
        if (scriptElem != null && scriptElem.size() > 0) {
            if (scriptFileElem != null && scriptFileElem.size() > 0) {
                log.warn("{} Data connector {}: definition contains both <Script> "
                        + "and <ScriptFile> elements, taking the <Script> element", getLogPrefix(), getDefinitionId());
            }
            final String script = scriptElem.get(0).getTextContent();
            log.debug("{} Script: {}", getLogPrefix(), script);
            scriptBuilder.addPropertyValue("script", script);
        } else if (scriptFileElem != null && scriptFileElem.size() > 0) {
            final String scriptFile = scriptFileElem.get(0).getTextContent();
            log.debug("{} Script file: {}", getLogPrefix(), scriptFile);
            scriptBuilder.addPropertyValue("resource", scriptFile);
        } else {
            log.error("{} No script or script file specified for this data connector", getLogPrefix());
            throw new BeanCreationException("No script or script file specified for this attribute definition");
        }

        String customRef = StringSupport.trimOrNull(config.getAttributeNS(null, "customObjectRef"));
        if (null != customRef) {
            builder.addPropertyReference("customObject", customRef);
        }

        builder.addPropertyValue("script", scriptBuilder.getBeanDefinition());
    }

}