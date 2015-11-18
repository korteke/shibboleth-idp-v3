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

import java.security.PrivateKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.LazyList;

import org.cryptacular.util.KeyPairUtil;
import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;

/**
 * A factory bean to understand X509Inline credentials.
 */
public class X509InlineCredentialFactoryBean extends AbstractX509CredentialFactoryBean {

    /** log. */
    private final Logger log = LoggerFactory.getLogger(X509InlineCredentialFactoryBean.class);

    /** The entity certificate. */
    private String entityCertificate;

    /** The certificates. */
    private List<String> certificates;

    /** The private key. */
    private byte[] privateKey;

    /** The crls. */
    private List<String> crls;
    
    /**
     * Set the file with the entity certificate.
     * 
     * @param entityCert The file to set.
     */
    public void setEntity(@Nonnull final String entityCert) {
        entityCertificate = entityCert;
    }

    /**
     * Sets the files which contain the certificates.
     * 
     * @param certs The value to set.
     */
    public void setCertificates(@Nullable @NotEmpty final List<String> certs) {
        certificates = certs;
    }

    /**
     * Set the file with the entity certificate.
     * 
     * @param key The file to set.
     */
    public void setPrivateKey(@Nullable final byte[] key) {
        privateKey = key;
    }

    /**
     * Sets the files which contain the crls.
     * 
     * @param list The value to set.
     */
    public void setCRLs(@Nullable @NotEmpty final List<String> list) {
        crls = list;
    }

    /** {@inheritDoc}. */
    @Override @Nullable protected X509Certificate getEntityCertificate() {

        if (null == entityCertificate) {
            return null;
        }
        try {
            return X509Support.decodeCertificate(entityCertificate);
        } catch (CertificateException e) {
            log.error("{}: Could not decode provided Entity Certificate", getConfigDescription(), e);
            throw new FatalBeanException("Could not decode provided Entity Certificate", e);
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected List<X509Certificate> getCertificates() {
        List<X509Certificate> certs = new LazyList<>();
        for (String cert : certificates) {
            try {
                certs.add(X509Support.decodeCertificate(cert.trim()));
            } catch (CertificateException e) {
                log.error("{}: Could not decode provided Certificate", getConfigDescription(), e);
                throw new FatalBeanException("Could not decode provided Certificate", e);
            }
        }
        return certs;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected PrivateKey getPrivateKey() {
        if (null == privateKey) {
            return null;
        }
        return KeyPairUtil.decodePrivateKey(privateKey, getPrivateKeyPassword());
    }

    /** {@inheritDoc} */
    @Override @Nullable protected List<X509CRL> getCRLs() {
        if (null == crls) {
            return null;
        }
        List<X509CRL> result = new LazyList<>();
        for (String crl : crls) {
            try {
                result.add(X509Support.decodeCRL(crl));
            } catch (CRLException | CertificateException e) {
                log.error("{}: Could not decode provided CRL", getConfigDescription(), e);
                throw new FatalBeanException("Could not decode provided CRL", e);
            }
        }
        return result;
    }
}
