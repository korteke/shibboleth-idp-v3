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

package net.shibboleth.idp.authn.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resource.Resource;

import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.SSLContextInitializer;
import org.ldaptive.ssl.X509SSLContextInitializer;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.X509Support;

/**
 * Implementation of {@link CredentialConfig} that loads trust and key material using a {@link Resource}.
 */
public class X509ResourceCredentialConfig implements CredentialConfig {

    /** Name of the trust certificates to use for the SSL connection. */
    private Resource trustCertificates;

    /** Name of the authentication certificate to use for the SSL connection. */
    private Resource authenticationCertificate;

    /** Name of the key to use for the SSL connection. */
    private Resource authenticationKey;

    /** Password to decrypt the authentication key. */
    private String authenticationKeyPassword;

    /**
     * Set the trust certificates resource.
     * 
     * @param resource the trust certificates resource
     */
    public void setTrustCertificates(@Nonnull @NotEmpty final Resource resource) {
        trustCertificates = Constraint.isNotNull(resource, "Trust certificates resource cannot be null");
    }

    /**
     * Set the authentication certificate resource.
     * 
     * @param resource the authentication certificate resource
     */
    public void setAuthenticationCertificate(@Nonnull @NotEmpty final Resource resource) {
        authenticationCertificate =
                Constraint.isNotNull(resource, "Authentication certificate resource cannot be null");
    }

    /**
     * Set the authentication key resource.
     * 
     * @param resource the authentication key resource
     */
    public void setAuthenticationKey(@Nonnull @NotEmpty final Resource resource) {
        authenticationKey = Constraint.isNotNull(resource, "Authentication key resource cannot be null");
    }

    /**
     * Set the authentication key password.
     * 
     * @param password the authentication key password
     */
    public void setAuthenticationKeyPassword(@Nonnull @NotEmpty final String password) {
        authenticationKeyPassword = Constraint.isNotNull(password, "Authentication key password cannot be null");
    }

    /** {@inheritDoc} */
    @Override public SSLContextInitializer createSSLContextInitializer() throws GeneralSecurityException {
        final X509SSLContextInitializer sslInit = new X509SSLContextInitializer();
        try {
            if (trustCertificates != null) {
                sslInit.setTrustCertificates(X509Support.decodeCertificates(trustCertificates.getFile()).toArray(
                        new X509Certificate[0]));
            }
            if (authenticationCertificate != null) {
                sslInit.setAuthenticationCertificate(
                        X509Support.decodeCertificate(authenticationCertificate.getFile()));
            }
            if (authenticationKey != null) {
                sslInit.setAuthenticationKey(KeySupport.decodePrivateKey(authenticationKey.getFile(),
                        authenticationKeyPassword != null ? authenticationKeyPassword.toCharArray() : null));
            }
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
        return sslInit;
    }
}
