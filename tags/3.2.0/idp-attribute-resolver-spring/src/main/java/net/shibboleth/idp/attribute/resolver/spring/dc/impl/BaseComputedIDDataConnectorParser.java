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
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.spring.BaseResolverPluginParser;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser for configuring
 * {@link net.shibboleth.idp.saml.attribute.resolver.impl.ComputedIDDataConnector} and
 * {@link net.shibboleth.idp.saml.nameid.impl.StoredIDDataConnector}.
 */
public abstract class BaseComputedIDDataConnectorParser extends BaseResolverPluginParser {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(BaseComputedIDDataConnectorParser.class);

    /**
     * Parse the common definitions for {@link net.shibboleth.idp.saml.attribute.resolver.impl.ComputedIDDataConnector}
     * and {@link net.shibboleth.idp.saml.nameid.impl.StoredIDDataConnector}.
     * 
     * @param config the DOM element under consideration.
     * @param parserContext Spring's context.
     * @param builder Spring's bean builder.
     * @param generatedIdDefaultName the name to give the generated Attribute if none was provided.
     */
    protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder, @Nullable String generatedIdDefaultName) {
        super.doParse(config, parserContext, builder);
        final String generatedAttribute;
        if (config.hasAttributeNS(null, "generatedAttributeID")) {
            generatedAttribute = StringSupport.trimOrNull(config.getAttributeNS(null, "generatedAttributeID"));
        } else {
            generatedAttribute = generatedIdDefaultName;
        }

        final List<Element> failoverConnector = ElementSupport.getChildElements(config, 
                AbstractDataConnectorParser.FAILOVER_DATA_CONNECTOR_ELEMENT_NAME);
        if (failoverConnector != null && !failoverConnector.isEmpty()) {
            String connectorId = StringSupport.trimOrNull(failoverConnector.get(0).getAttributeNS(null, "ref"));
            log.debug("{} Setting the following failover data connector dependencies: {}", getLogPrefix(), connectorId);
            builder.addPropertyValue("failoverDataConnectorId", connectorId);
        }

        final String sourceAttribute = StringSupport.trimOrNull(config.getAttributeNS(null, "sourceAttributeID"));

        final String salt = StringSupport.trimOrNull(config.getAttributeNS(null, "salt"));
        if (null == salt) {
            log.debug("{} Generated Attribute: '{}', sourceAttribute = '{}', no salt provided", 
                    getLogPrefix(), generatedAttribute, sourceAttribute);
        } else {
            log.debug("{} Generated Attribute: '{}', sourceAttribute = '{}', salt (or property): '{}'", 
                    getLogPrefix(), generatedAttribute, sourceAttribute, salt);
        }

        builder.addPropertyValue("generatedAttributeId", generatedAttribute);
        builder.addPropertyValue("sourceAttributeId", sourceAttribute);
        builder.addPropertyValue("salt", salt);
    }
    /**
     * return a string which is to be prepended to all log messages.
     * 
     * @return "Attribute Definition: '<definitionID>' :"
     */
    @Nonnull @NotEmpty protected String getLogPrefix() {
        StringBuilder builder = new StringBuilder("Data Connector '").append(getDefinitionId()).append("':");
        return builder.toString();
    }
    
}