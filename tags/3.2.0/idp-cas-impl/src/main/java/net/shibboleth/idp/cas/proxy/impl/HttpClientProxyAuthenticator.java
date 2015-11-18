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

package net.shibboleth.idp.cas.proxy.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import com.beust.jcommander.internal.Nullable;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.opensaml.security.SecurityException;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates a CAS proxy callback endpoint using an {@link org.apache.http.client.HttpClient} instance.
 *
 * @author Marvin S. Addison
 */
public class HttpClientProxyAuthenticator extends AbstractProxyAuthenticator {

    /**
     * Delegates X.509 certificate trust to an underlying OpenSAML <code>TrustEngine</code>.
     */
    private static class TrustEngineTrustStrategy implements TrustStrategy {

        private final TrustEngine<? super X509Credential> trustEngine;

        /** Class logger. */
        private final Logger log = LoggerFactory.getLogger(TrustEngineTrustStrategy.class);

        public TrustEngineTrustStrategy(final TrustEngine<? super X509Credential> engine) {
            trustEngine = engine;
        }

        @Override
        public boolean isTrusted(final X509Certificate[] certificates, final String authType)
                throws CertificateException {
            if (trustEngine == null || certificates == null || certificates.length < 1) {
                return false;
            }
            // Assume the first certificate is the end-entity cert
            try {
                log.debug("Validating cert {} issued by {}",
                        certificates[0].getSubjectDN().getName(),
                        certificates[0].getIssuerDN().getName());
                return trustEngine.validate(new BasicX509Credential(certificates[0]), new CriteriaSet());
            } catch (SecurityException e) {
                throw new CertificateException("X509 validation error", e);
            }
        }
    }

    /** Default connection and socket timeout in ms. */
    private static final int DEFAULT_TIMEOUT = 800;

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(HttpClientProxyAuthenticator.class);

    /** Connection and socket timeout. */
    @Positive
    private int timeout = DEFAULT_TIMEOUT;


    /**
     * Sets connect and socket timeouts for HTTP connection to proxy callback endpoint.
     *
     * @param timeout Non-zero timeout in milliseconds for both connection and socket timeouts.
     */
    public void setTimeout(@Positive final int timeout) {
        this.timeout = (int) Constraint.isGreaterThan(timeout, 0, "Timeout must be positive");
    }

    @Override
    protected int authenticateProxyCallback(
            @Nonnull final URI callbackUri,
            @Nullable final TrustEngine<? super X509Credential> x509TrustEngine)
            throws GeneralSecurityException {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = createHttpClient(x509TrustEngine);
            log.debug("Attempting to connect to {}", callbackUri);
            final HttpGet request = new HttpGet(callbackUri);
            request.setConfig(
                    RequestConfig.custom()
                            .setConnectTimeout(timeout)
                            .setSocketTimeout(timeout)
                            .build());
            response = httpClient.execute(request);
            return response.getStatusLine().getStatusCode();
        } catch (ClientProtocolException e) {
            throw new GeneralSecurityException("HTTP protocol error", e);
        } catch (SSLException e) {
            if (e.getCause() instanceof CertificateException) {
                throw (CertificateException) e.getCause();
            }
            throw new GeneralSecurityException("SSL connection error", e);
        } catch (IOException e) {
            throw new GeneralSecurityException("IO error", e);
        } finally {
            close(response);
            close(httpClient);
        }
    }

    private CloseableHttpClient createHttpClient(final TrustEngine<? super X509Credential> x509TrustEngine) {
        final SSLConnectionSocketFactory socketFactory;
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .useTLS()
                    .loadTrustMaterial(null, new TrustEngineTrustStrategy(x509TrustEngine))
                    .build();
            socketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            throw new RuntimeException("SSL initialization error", e);
        }
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTPS_SCHEME, socketFactory).build();
        final BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
        return HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    private void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                log.warn("Error closing " + resource, e);
            }
        }
    }
}
