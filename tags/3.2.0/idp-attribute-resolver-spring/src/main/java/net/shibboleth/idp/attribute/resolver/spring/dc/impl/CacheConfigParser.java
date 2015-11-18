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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/** Utility class for parsing v2 cache configuration. */
public class CacheConfigParser {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(CacheConfigParser.class);

    /** Base XML element. */
    @Nonnull private final Element configElement;

    /**
     * Creates a new cache config parser with the supplied ResultsCache element.
     * 
     * @param config LDAPDirectory element
     */
    public CacheConfigParser(@Nonnull final Element config) {
        Constraint.isNotNull(config, "Element cannot be null");
        configElement = config;
    }

    /**
     * Creates a new cache bean definition from a v2 XML configuration.
     * 
     * @return cache bean definition
     */
    @Nonnull public BeanDefinition createCache() {

        final String defaultCache = AttributeSupport.getAttributeValue(configElement, new QName("cacheResults"));
        if (defaultCache != null) {
            log.warn("The cacheResults attribute is no longer supported, please create a dc:ResultCache element");
            return null;
        }
        
        final Element cacheElement =
                ElementSupport.getFirstChildElement(configElement, new QName(DataConnectorNamespaceHandler.NAMESPACE,
                        "ResultCache"));
        if (cacheElement == null) {
            return null;
        }
        
        final BeanDefinitionBuilder cache =
                BeanDefinitionBuilder.rootBeanDefinition(CacheConfigParser.class, "buildCache");
        cache.addConstructorArgValue(AttributeSupport.getAttributeValue(cacheElement, new QName("elementTimeToLive")));
        cache.addConstructorArgValue(
                AttributeSupport.getAttributeValue(cacheElement, new QName("maximumCachedElements")));
        return cache.getBeanDefinition();
    }

    /**
     * Factory method to leverage spring property replacement functionality. The default settings are a max size
     * of 500 and an expiration time of 4 hours.
     * 
     * @param timeToLive duration string
     * @param maximumSize long string
     * 
     * @return cache
     */
    @Nullable public static Cache<String, Map<String, IdPAttribute>> buildCache(@Nullable final String timeToLive,
            @Nullable final String maximumSize) {
        return CacheBuilder.newBuilder().maximumSize(maximumSize != null ? Long.parseLong(maximumSize) : 500)
                .expireAfterAccess(timeToLive != null ? DOMTypeSupport.durationToLong(timeToLive)
                        : 4 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS).build();
    }
    
}