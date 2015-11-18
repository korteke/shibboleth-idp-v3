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

import java.io.IOException;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.collection.LazyList;

import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.Resource;

/**
 * A factory bean to understand X509Filesystem & X509ResourceBacked credentials.
 */
public class X509ResourceCredentialFactoryBean extends AbstractX509CredentialFactoryBean {

    /** log. */
    private final Logger log = LoggerFactory.getLogger(X509ResourceCredentialFactoryBean.class);

    /** The specification of where the entity Resource is to be found. */
    private Resource entityResource;

    /** Where the certificates are to be found. */
    private List<Resource> certificateResources;

    /** Where the private key is to be found. */
    private Resource privateKeyResource;

    /** Where the crls are to be found. */
    private List<Resource> crlResources;

    /**
     * Set the Resource with the entity certificate.
     * 
     * @param what The Resource to set.
     */
    public void setEntity(@Nonnull final Resource what) {
        entityResource = what;
    }

    /**
     * Sets the Resources which contain the certificates.
     * 
     * @param what The values to set.
     */
    public void setCertificates(@Nullable @NotEmpty final List<Resource> what) {
        certificateResources = what;
    }

    /**
     * Set the Resource with the entity certificate.
     * 
     * @param what The resource to set.
     */
    public void setPrivateKey(@Nullable final Resource what) {
        privateKeyResource = what;
    }

    /**
     * Sets the Resources which contain the crls.
     * 
     * @param what The value to set.
     */
    public void setCRLs(@Nullable @NotEmpty final List<Resource> what) {
        crlResources = what;
    }

    /** {@inheritDoc}. */
    @Override @Nullable protected X509Certificate getEntityCertificate() {

        if (null == entityResource) {
            return null;
        }
        try {
            final Collection<X509Certificate> certs = X509Support.decodeCertificates(entityResource.getFile());
            if (certs.size() > 1) {
                log.error("{}: Configuration element indicated an entityCertificate,"
                        + " but multiple certificates were decoded", getConfigDescription());
                throw new FatalBeanException("Configuration element indicated an entityCertificate,"
                        + " but multiple certificates were decoded");
            }
            return certs.iterator().next();
        } catch (CertificateException | IOException e) {
            log.error("{}: Could not decode provided Entity Certificate at {}: {}", getConfigDescription(),
                    entityResource.getDescription(), e);
            throw new FatalBeanException("Could not decode provided Entity Certificate file "
                    + entityResource.getDescription(), e);
        }
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected List<X509Certificate> getCertificates() {
        List<X509Certificate> certificates = new LazyList<>();
        for (Resource r : certificateResources) {
            try {
                certificates.addAll(X509Support.decodeCertificates(r.getFile()));
            } catch (CertificateException | IOException e) {
                log.error("{}: could not decode CertificateFile at {}: {}", getConfigDescription(),
                        r.getDescription(), e);
                throw new FatalBeanException("Could not decode provided CertificateFile: " + r.getDescription(), e);
            }
        }
        return certificates;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected PrivateKey getPrivateKey() {
        if (null == privateKeyResource) {
            return null;
        }
        try {
            return KeySupport.decodePrivateKey(privateKeyResource.getFile(), getPrivateKeyPassword());
        } catch (KeyException | IOException e) {
            log.error("{}: Could not decode KeyFile at {}: {}", getConfigDescription(),
                    privateKeyResource.getDescription(), e);
            throw new FatalBeanException("Could not decode provided KeyFile " + privateKeyResource.getDescription(), e);
        }
    }

    /** {@inheritDoc} */
    @Override @Nullable protected List<X509CRL> getCRLs() {
        if (null == crlResources) {
            return null;
        }
        List<X509CRL> crls = new LazyList<>();
        for (Resource crl : crlResources) {
            try {
                crls.addAll(X509Support.decodeCRLs(crl.getFile()));
            } catch (CRLException | IOException e) {
                log.error("{}: Could not decode CRL file: {}", getConfigDescription(), crl.getDescription(), e);
                throw new FatalBeanException("Could not decode provided CRL file " + crl.getDescription(), e);
            }
        }
        return crls;
    }
}
