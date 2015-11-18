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

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base parser for all &lt;Credential&gt; elements.
 */
public abstract class AbstractCredentialParser extends AbstractSingleBeanDefinitionParser {

    /** &lt;Credential&gt;. */
    public static final QName CREDENTIAL_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "Credential");

    /** &lt;KeyName&gt;. */
    public static final QName KEY_NAME_ELEMENT_NAME = new QName(AbstractMetadataProviderParser.SECURITY_NAMESPACE,
            "KeyName");

    /** {@inheritDoc} */
    @Override protected String resolveId(final Element element, final AbstractBeanDefinition definition,
            final ParserContext parserContext) {
        return StringSupport.trimOrNull(element.getAttributeNS(null, "id"));
    }

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        builder.setLazyInit(true);

        parseAttributes(element, builder);
        parseKeyNames(ElementSupport.getChildElements(element, KEY_NAME_ELEMENT_NAME), builder);
    }

    /**
     * Parse the credential element attributes.
     * 
     * @param element credential element
     * @param builder bean definition builder
     */
    protected void parseAttributes(final Element element, final BeanDefinitionBuilder builder) {
        final String usage = StringSupport.trimOrNull(element.getAttributeNS(null, "usage"));
        builder.addPropertyValue("usageType", usage);

        final String entityID = StringSupport.trimOrNull(element.getAttributeNS(null, "entityID"));
        if (entityID != null) {
            builder.addPropertyValue("entityID", entityID);
        }
    }

    /**
     * Parses the key names from the credential configuration.
     * 
     * @param keyNameElems the elements to parse
     * @param builder credential build
     */
    protected void parseKeyNames(List<Element> keyNameElems, BeanDefinitionBuilder builder) {
        if (keyNameElems == null || keyNameElems.isEmpty()) {
            return;
        }

        builder.addPropertyValue("keyNames", SpringSupport.getElementTextContentAsManagedList(keyNameElems));
    }
}
