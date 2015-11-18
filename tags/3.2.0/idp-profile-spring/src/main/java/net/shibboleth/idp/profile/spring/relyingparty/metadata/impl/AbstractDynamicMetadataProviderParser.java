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

import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for {@link org.opensaml.saml.metadata.resolver.impl.AbstractDynamicMetadataResolver}.
 */
public abstract class AbstractDynamicMetadataProviderParser extends AbstractMetadataProviderParser {

    /** The reference to the system parser pool that we set up. */
    private static final String DEFAULT_PARSER_POOL_REF = "shibboleth.ParserPool";

    /**
     * 
     * {@inheritDoc}
     * 
     * We assume that we will be summoning up a class which extends an
     * {@link org.opensaml.saml.metadata.resolver.impl.AbstractDynamicMetadataResolver}.
     */
    @Override protected void doNativeParse(Element element, ParserContext parserContext, 
            BeanDefinitionBuilder builder) {

        super.doNativeParse(element, parserContext, builder);

        // If there's a timer bean reference, that's the first c'tor argument.
        final String timerRef = getTaskTimerRef(element);
        if (timerRef != null) {
            builder.addConstructorArgReference(timerRef);
        }

        if (element.hasAttributeNS(null, "refreshDelayFactor")) {
            builder.addPropertyValue("refreshDelayFactor",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "refreshDelayFactor")));
        }

        if (element.hasAttributeNS(null, "minCacheDuration")) {
            builder.addPropertyValue("minCacheDuration",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "minCacheDuration")));
        }

        if (element.hasAttributeNS(null, "maxCacheDuration")) {
            builder.addPropertyValue("maxCacheDuration",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "maxCacheDuration")));
        }

        if (element.hasAttributeNS(null, "maxIdleEntityData")) {
            builder.addPropertyValue("maxIdleEntityData",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "maxIdleEntityData")));
        }

        if (element.hasAttributeNS(null, "removeIdleEntityData")) {
            builder.addPropertyValue("removeIdleEntityData",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "removeIdleEntityData")));
        }

        if (element.hasAttributeNS(null, "cleanupTaskInterval")) {
            builder.addPropertyValue("cleanupTaskInterval",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "cleanupTaskInterval")));
        }

        builder.addPropertyReference("parserPool", getParserPoolRef(element));
    }

    /**
     * Gets the default task timer reference for the metadata provider.
     * 
     * @param element metadata provider configuration element
     * 
     * @return task timer reference
     */
    protected String getTaskTimerRef(Element element) {

        if (element.hasAttributeNS(null, "taskTimerRef")) {
            return StringSupport.trimOrNull(StringSupport.trimOrNull(element.getAttributeNS(null, "taskTimerRef")));
        } else {
            return null;
        }
    }

    /**
     * Gets the default parser pool reference for the metadata provider.
     * 
     * @param element metadata provider configuration element
     * 
     * @return parser pool reference
     */
    protected String getParserPoolRef(Element element) {
        String parserPoolRef = null;
        if (element.hasAttributeNS(null, "parserPoolRef")) {
            parserPoolRef =
                    StringSupport.trimOrNull(StringSupport.trimOrNull(element.getAttributeNS(null, "parserPoolRef")));
        }

        if (parserPoolRef == null) {
            parserPoolRef = DEFAULT_PARSER_POOL_REF;
        }

        return parserPoolRef;
    }

}
