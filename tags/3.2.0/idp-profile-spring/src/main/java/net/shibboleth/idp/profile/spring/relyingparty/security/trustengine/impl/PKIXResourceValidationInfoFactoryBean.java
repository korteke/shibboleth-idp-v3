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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine.impl;

import java.io.IOException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.Resource;

/**
 * File system specific bean for PKIXValidationInfo.
 */
public class PKIXResourceValidationInfoFactoryBean extends AbstractBasicPKIXValidationInfoFactoryBean {

    /** log. */
    private Logger log = LoggerFactory.getLogger(PKIXResourceValidationInfoFactoryBean.class);

    /** The file to be turned into the certificates. */
    private List<Resource> certificateFiles;

    /** The file to be turned into the crls. */
    private List<Resource> crlFiles;

    /**
     * Set the file names which we will convert into certificates.
     * 
     * @param certs the file names.
     */
    public void setCertificates(@Nullable final List<Resource> certs) {
        certificateFiles = certs;
    }

    /**
     * Set the file names which we will convert into crls.
     * 
     * @param crls the file names.
     */
    public void setCRLs(@Nullable final List<Resource> crls) {
        crlFiles = crls;
    }

    /**
     * Get the configured certificates.
     * 
     * @return the certificates null
     */
    @Override @Nullable protected List<X509Certificate> getCertificates() {
        if (null == certificateFiles) {
            return null;
        }
        List<X509Certificate> certificates = new ArrayList<>(certificateFiles.size());
        for (Resource f : certificateFiles) {
            try {
                certificates.addAll(X509Support.decodeCertificates(f.getFile()));
            } catch (CertificateException | IOException e) {
                log.error("{}: Could not decode Certificate at {}", getConfigDescription(), f.getDescription(), e);
                throw new FatalBeanException("Could not decode provided CertificateFile: " + f.getDescription(), e);
            }
        }
        return certificates;
    }

    /**
     * Get the configured CRL list.
     * 
     * @return the crls or null
     */
    @Override @Nullable protected List<X509CRL> getCRLs() {
        if (null == crlFiles) {
            return null;
        }
        List<X509CRL> crls = new ArrayList<>(crlFiles.size());
        for (Resource crlFile : crlFiles) {
            try {
                crls.addAll(X509Support.decodeCRLs(crlFile.getFile()));
            } catch (CRLException | IOException e) {
                log.error("{}: Could not decode CRL file at {}: {}", getConfigDescription(), crlFile.getDescription(),
                        e);
                throw new FatalBeanException("Could not decode provided CRL file " + crlFile.getDescription(), e);
            }
        }
        return crls;
    }
}
