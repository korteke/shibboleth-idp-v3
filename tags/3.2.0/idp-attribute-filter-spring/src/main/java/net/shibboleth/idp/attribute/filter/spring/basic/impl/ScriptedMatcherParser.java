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

package net.shibboleth.idp.attribute.filter.spring.basic.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.factory.EvaluableScriptFactoryBean;
import net.shibboleth.idp.attribute.filter.matcher.impl.ScriptedMatcher;
import net.shibboleth.idp.attribute.filter.policyrule.impl.ScriptedPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.BaseFilterParser;
import net.shibboleth.idp.attribute.filter.spring.impl.AbstractWarningFilterParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for {@link ScriptedPolicyRule} or {@link ScriptedMatcher} objects.
 */
public class ScriptedMatcherParser extends AbstractWarningFilterParser {

    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(AttributeFilterBasicNamespaceHandler.NAMESPACE, "Script");

    /** Schema type. */
    public static final QName SCHEMA_TYPE_AFP = new QName(BaseFilterParser.NAMESPACE, "Script");

    /** Script file element name - basic. */
    public static final QName SCRIPT_FILE_ELEMENT_NAME = new QName(AttributeFilterBasicNamespaceHandler.NAMESPACE,
            "ScriptFile");

    /** Script file element name - afp. */
    public static final QName SCRIPT_FILE_ELEMENT_NAME_AFP = new QName(BaseFilterParser.NAMESPACE, "ScriptFile");

    /** Inline Script element name - basic. */
    public static final QName SCRIPT_ELEMENT_NAME = new QName(AttributeFilterBasicNamespaceHandler.NAMESPACE, "Script");

    /** Inline Script element name - afp. */
    public static final QName SCRIPT_ELEMENT_NAME_AFP = new QName(BaseFilterParser.NAMESPACE, "Script");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeFilterBasicNamespaceHandler.class);

    /** {@inheritDoc} */
    @Override protected QName getAFPName() {
        return SCHEMA_TYPE_AFP;
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<?> getBeanClass(@Nonnull final Element element) {
        if (isPolicyRule(element)) {
            return ScriptedPolicyRule.class;
        }
        return ScriptedMatcher.class;
    }

    /**
     * {@inheritDoc} Both types of bean take the same constructor, so the parser is simplified.
     */
    // Checkstyle: CyclomaticComplexity OFF
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        final String myId = builder.getBeanDefinition().getAttribute("qualifiedId").toString();
        final String logPrefix = new StringBuilder("Scipted Filter '").append(myId).append("' :").toString();

        final BeanDefinitionBuilder scriptBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(EvaluableScriptFactoryBean.class);
        scriptBuilder.addPropertyValue("sourceId", logPrefix);
        if (config.hasAttributeNS(null, "language")) {
            final String scriptLanguage = StringSupport.trimOrNull(config.getAttributeNS(null, "language"));
            log.debug("{} scripting language: {}.", logPrefix, scriptLanguage);
            scriptBuilder.addPropertyValue("engineName", scriptLanguage);
        }

        List<Element> scriptElem = ElementSupport.getChildElements(config, SCRIPT_ELEMENT_NAME);
        if (scriptElem == null || scriptElem.isEmpty()) {
            scriptElem = ElementSupport.getChildElements(config, SCRIPT_ELEMENT_NAME_AFP);
        }

        List<Element> scriptFileElem = ElementSupport.getChildElements(config, SCRIPT_FILE_ELEMENT_NAME);
        if (scriptFileElem == null || scriptFileElem.isEmpty()) {
            scriptFileElem = ElementSupport.getChildElements(config, SCRIPT_FILE_ELEMENT_NAME_AFP);
        }

        if (scriptElem != null && scriptElem.size() > 0) {
            if (scriptFileElem != null && scriptFileElem.size() > 0) {
                log.info("Attribute definition {}: definition contains both <Script> "
                        + "and <ScriptFile> elements, taking the <Script> element", logPrefix);
            }
            final String script = scriptElem.get(0).getTextContent();
            log.debug("{} script {}.", logPrefix, script);
            scriptBuilder.addPropertyValue("script", script);
        } else if (scriptFileElem != null && scriptFileElem.size() > 0) {
            final String scriptFile = scriptFileElem.get(0).getTextContent();
            log.debug("{} script file {}.", logPrefix, scriptFile);
            scriptBuilder.addPropertyValue("resource", scriptFile);
        } else {
            log.error("{} No script specified for this attribute definition");
            throw new BeanCreationException("No script specified for this attribute definition");
        }

        final String customRef = StringSupport.trimOrNull(config.getAttributeNS(null, "customObjectRef"));
        if (null != customRef) {
            builder.addPropertyReference("customObject", customRef);
        }
        builder.addPropertyValue("id", myId);

        builder.addConstructorArgValue(scriptBuilder.getBeanDefinition());
    }
    // Checkstyle: CyclomaticComplexity ON
}