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

package net.shibboleth.idp.attribute.resolver.spring.dc.ldap.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.ext.spring.factory.AbstractComponentAwareFactoryBean;

import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.CredentialConfigFactory;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

/**
 * A Factory bean to summon up {@link CredentialConfig} from supplied &lt;Credential&gt; statements.
 */
public class CredentialConfigFactoryBean extends AbstractComponentAwareFactoryBean<CredentialConfig> {

    /** Class logger. */
    @Nonnull private static Logger log = LoggerFactory.getLogger(CredentialConfigFactoryBean.class);

    /** The credential of the LDAP server. */
    @Nullable private Credential trustCredential;

    /** Our authentication credential for the LDAP connection. */
    @Nullable private Credential authCredential;

    /** {@inheritDoc} */
    @Override public Class<?> getObjectType() {
        return CredentialConfig.class;
    }

    /** {@inheritDoc} */
    @Override protected CredentialConfig doCreateInstance() throws Exception {
        X509Certificate[] trustCerts = null;

        if (trustCredential != null) {
            if (trustCredential instanceof X509Credential) {
                X509Credential cred = (X509Credential) trustCredential;
                trustCerts =
                        cred.getEntityCertificateChain().toArray(
                                new X509Certificate[cred.getEntityCertificateChain().size()]);
            } else {
                log.error("Supplied StartTLSTrustCredential was of type {}, not {}", trustCredential.getClass()
                        .getName(), X509Credential.class.getName());
                throw new BeanCreationException("Supplied StartTLSTrustCredential was of wrong type");
            }
        }
        X509Certificate authCert = null;
        PrivateKey authKey = null;

        if (authCredential != null) {
            if (authCredential instanceof X509Credential) {
                X509Credential cred = (X509Credential) authCredential;
                authCert = cred.getEntityCertificate();
                authKey = cred.getPrivateKey();
            } else {
                log.error("Supplied StartTLSAuthenticationCredential was of type {}, not {}", 
                        authCredential.getClass().getName(), X509Credential.class.getName());
                throw new BeanCreationException("Supplied StartTLSAuthenticationCredential was of wrong type");
            }
        }
        return CredentialConfigFactory.createX509CredentialConfig(trustCerts, authCert, authKey);
    }

    /**
     * Get the authentication credential for the LDAP connection.
     * 
     * @return Returns the authnCredential.
     */
    @Nullable public Credential getAuthCredential() {
        return authCredential;
    }

    /**
     * Set the authentication credential for the LDAP connection.
     * 
     * @param credential What to set.
     */
    public void setAuthCredential(@Nullable final Credential credential) {
        authCredential = credential;
    }

    /**
     * Get the credential of the LDAP server.
     * 
     * @return Returns the trustCredential.
     */
    @Nullable public Credential getTrustCredential() {
        return trustCredential;
    }

    /**
     * Set the credential of the LDAP server.
     * 
     * @param credential What to set.
     */
    public void setTrustCredential(@Nullable final Credential credential) {
        trustCredential = credential;
    }
}
