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

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.FileCachingHttpClientFactoryBean;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.HttpClientFactoryBean;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.InMemoryCachingHttpClientFactoryBean;
import net.shibboleth.utilities.java.support.httpclient.HttpClientSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.security.httpclient.impl.SecurityEnhancedTLSSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for a &lt;FilesystemMetadataProvider&gt;.
 */
public class HTTPMetadataProviderParser extends AbstractReloadingMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE,
            "HTTPMetadataProvider");

    /** TLSTrustEngine element name. */
    public static final QName TLS_TRUST_ENGINE_ELEMENT_NAME = new QName(
            AbstractMetadataProviderParser.METADATA_NAMESPACE, "TLSTrustEngine");

    /** The URL for the metadata. */
    private static final String METADATA_URL = "metadataURL";

    /** BASIC auth username. */
    private static final String BASIC_AUTH_USER = "basicAuthUser";

    /** BASIC auth password. */
    private static final String BASIC_AUTH_PASSWORD = "basicAuthPassword";

    /** Default caching type. */
    private static final String DEFAULT_CACHING = "none";

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(HTTPMetadataProviderParser.class);

    /** {@inheritDoc} */
    @Override protected Class<? extends HTTPMetadataResolver> getNativeBeanClass(Element element) {
        return HTTPMetadataResolver.class;
    }

    /** {@inheritDoc} */
    // Checkstyle: CyclomaticComplexity OFF
    @Override protected void doNativeParse(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "cacheDuration")) {
            log.error("{}: cacheDuration is not supported", parserContext.getReaderContext().getResource()
                    .getDescription());
            throw new BeanDefinitionParsingException(new Problem("cacheDuration is not supported", new Location(
                    parserContext.getReaderContext().getResource())));
        }

        if (element.hasAttributeNS(null, "maintainExpiredMetadata")) {
            log.error("{}: maintainExpiredMetadata is not supported", parserContext.getReaderContext().getResource()
                    .getDescription());
            throw new BeanDefinitionParsingException(new Problem("maintainExpiredMetadata is not supported",
                    new Location(parserContext.getReaderContext().getResource())));
        }

        boolean haveTLSTrustEngine = false;
        if (element.hasAttributeNS(null, "tlsTrustEngineRef")) {
            builder.addPropertyReference("tLSTrustEngine",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "tlsTrustEngineRef")));
            haveTLSTrustEngine = true;
        } else {
            BeanDefinition tlsTrustEngine = parseTLSTrustEngine(element, parserContext);
            if (tlsTrustEngine != null) {
                builder.addPropertyValue("tLSTrustEngine", tlsTrustEngine);
                haveTLSTrustEngine = true;
            }
        }

        if (element.hasAttributeNS(null, "httpClientRef")) {
            builder.addConstructorArgReference(StringSupport.trimOrNull(element.getAttributeNS(null, "httpClientRef")));
            if (element.hasAttributeNS(null, "requestTimeout")
                    || element.hasAttributeNS(null, "disregardSslCertificate")
                    || element.hasAttributeNS(null, "disregardTLSCertificate")
                    || element.hasAttributeNS(null, "proxyHost") || element.hasAttributeNS(null, "proxyPort")
                    || element.hasAttributeNS(null, "proxyUser") || element.hasAttributeNS(null, "proxyPassword")) {
                log.warn("httpClientRef overrides settings for requestTimeout, disregardSslCertificate, "
                        + "disregardTLSCertificate, proxyHost, proxyPort, proxyUser and proxyPassword");
            }
        } else {
            builder.addConstructorArgValue(buildHttpClient(element, parserContext, haveTLSTrustEngine));
        }
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, METADATA_URL)));

        if (element.hasAttributeNS(null, BASIC_AUTH_USER) || element.hasAttributeNS(null, BASIC_AUTH_PASSWORD)) {
            builder.addPropertyValue("basicCredentials", buildBasicCredentials(element));
        }

    }

    // Checkstyle: CyclomaticComplexity ON

    /**
     * Build the definition of the HTTPClientBuilder which contains all our configuration.
     * 
     * @param element the HTTPMetadataProvider parser.
     * @param parserContext context
     * @param haveTLSTrustEngine whether have a TLS TrustEngine configured
     * @return the bean definition with the parameters.
     */
    // Checkstyle: CyclomaticComplexity OFF
    // Checkstyle: MethodLength OFF
    private BeanDefinition buildHttpClient(Element element, ParserContext parserContext, boolean haveTLSTrustEngine) {
        String caching = DEFAULT_CACHING;
        if (element.hasAttributeNS(null, "httpCaching")) {
            caching = StringSupport.trimOrNull(element.getAttributeNS(null, "httpCaching"));
        }

        BeanDefinitionBuilder clientBuilder = null;
        switch (caching) {
            case "none":
                clientBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpClientFactoryBean.class);
                break;
            case "file":
                clientBuilder = BeanDefinitionBuilder.genericBeanDefinition(FileCachingHttpClientFactoryBean.class);
                if (element.hasAttributeNS(null, "httpCacheDirectory")) {
                    clientBuilder.addPropertyValue("cacheDirectory",
                            StringSupport.trimOrNull(element.getAttributeNS(null, "httpCacheDirectory")));
                }
                if (element.hasAttributeNS(null, "httpMaxCacheEntries")) {
                    clientBuilder.addPropertyValue("maxCacheEntries",
                            StringSupport.trimOrNull(element.getAttributeNS(null, "httpMaxCacheEntries")));
                }
                if (element.hasAttributeNS(null, "httpMaxCacheEntrySize")) {
                    clientBuilder.addPropertyValue("maxCacheEntrySize",
                            StringSupport.trimOrNull(element.getAttributeNS(null, "httpMaxCacheEntrySize")));
                }
                break;
            case "memory":
                clientBuilder = BeanDefinitionBuilder.genericBeanDefinition(InMemoryCachingHttpClientFactoryBean.class);
                if (element.hasAttributeNS(null, "httpMaxCacheEntries")) {
                    clientBuilder.addPropertyValue("maxCacheEntries",
                            StringSupport.trimOrNull(element.getAttributeNS(null, "httpMaxCacheEntries")));
                }
                if (element.hasAttributeNS(null, "httpMaxCacheEntrySize")) {
                    clientBuilder.addPropertyValue("maxCacheEntrySize",
                            StringSupport.trimOrNull(element.getAttributeNS(null, "httpMaxCacheEntrySize")));
                }
                break;
            default:
                throw new BeanDefinitionParsingException(new Problem(String.format("Caching value '%s' is unsupported",
                        caching), new Location(parserContext.getReaderContext().getResource())));
        }

        clientBuilder.setLazyInit(true);

        if (element.hasAttributeNS(null, "requestTimeout")) {
            clientBuilder.addPropertyValue("connectionTimeout",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "requestTimeout")));
        }

        if (haveTLSTrustEngine) {
            clientBuilder.addPropertyValue("tLSSocketFactory",
                    new SecurityEnhancedTLSSocketFactory(HttpClientSupport.buildNoTrustTLSSocketFactory(),
                            new StrictHostnameVerifier()));
        }

        if (element.hasAttributeNS(null, "disregardTLSCertificate")) {
            clientBuilder.addPropertyValue("connectionDisregardTLSCertificate",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "disregardTLSCertificate")));
        } else if (element.hasAttributeNS(null, "disregardSslCertificate")) {
            log.warn("disregardSslCertificate is deprecated, please switch to disregardTLSCertificate");
            clientBuilder.addPropertyValue("connectionDisregardTLSCertificate",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "disregardSslCertificate")));
        }

        if (element.hasAttributeNS(null, "proxyHost")) {
            clientBuilder.addPropertyValue("connectionProxyHost",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "proxyHost")));
        }

        if (element.hasAttributeNS(null, "proxyPort")) {
            clientBuilder.addPropertyValue("connectionProxyPort",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "proxyPort")));
        }

        if (element.hasAttributeNS(null, "proxyUser")) {
            clientBuilder.addPropertyValue("connectionProxyUsername",
                    StringSupport.trimOrNull(element.getAttributeNS(null, "proxyUser")));
        }

        if (element.hasAttributeNS(null, "proxyPassword")) {
            clientBuilder.addPropertyValue("connectionProxyPassword", element.getAttributeNS(null, "proxyPassword"));
        }

        return clientBuilder.getBeanDefinition();
    }

    // Checkstyle: CyclomaticComplexity ON
    // Checkstyle: MethodLength OFF

    /**
     * Build the POJO with the username and password.
     * 
     * @param element the HTTPMetadataProvider parser.
     * @return the bean definition with the username and password.
     */
    private BeanDefinition buildBasicCredentials(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(UsernamePasswordCredentials.class);

        builder.setLazyInit(true);

        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, BASIC_AUTH_USER)));
        builder.addConstructorArgValue(StringSupport.trimOrNull(element.getAttributeNS(null, BASIC_AUTH_PASSWORD)));

        return builder.getBeanDefinition();
    }

    /**
     * Build the definition of the HTTPClientBuilder which contains all our configuration.
     * 
     * @param element the HTTPMetadataProvider element
     * @param parserContext context
     * @return the bean definition
     */
    private BeanDefinition parseTLSTrustEngine(Element element, ParserContext parserContext) {
        Element tlsTrustEngine = ElementSupport.getFirstChildElement(element, TLS_TRUST_ENGINE_ELEMENT_NAME);
        if (tlsTrustEngine != null) {
            Element trustEngine =
                    ElementSupport.getFirstChildElement(tlsTrustEngine,
                            AbstractMetadataProviderParser.TRUST_ENGINE_ELEMENT_NAME);
            if (trustEngine != null) {
                return SpringSupport.parseCustomElement(trustEngine, parserContext);
            } else {
                // This should be schema-invalid, but log a warning just in case.
                log.warn("{}:, Element {} did not contain a {} child element", parserContext.getReaderContext()
                        .getResource().getDescription(), TLS_TRUST_ENGINE_ELEMENT_NAME,
                        AbstractMetadataProviderParser.TRUST_ENGINE_ELEMENT_NAME);
            }
        }

        return null;
    }
}
