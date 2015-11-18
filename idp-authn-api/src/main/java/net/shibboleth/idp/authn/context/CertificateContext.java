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

package net.shibboleth.idp.authn.context;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Context, usually attached to {@link AuthenticationContext}, that carries a {@link Certificate} to be
 * validated.
 */
public class CertificateContext extends BaseContext {

    /** The certificate to be validated. */
    @Nullable private Certificate certificate;
    
    /** Additional certificates as input to validation. */
    @Nonnull @NonnullElements private Collection<Certificate> intermediates;

    /** Constructor. */
    public CertificateContext() {
        intermediates = new ArrayList<>();
    }
    
    /**
     * Get the certificate to be validated.
     * 
     * @return the certificate to be validated
     */
    @Nullable public Certificate getCertificate() {
        return certificate;
    }

    /**
     * Set the certificate to be validated.
     * 
     * @param cert certificate to be validated
     * 
     * @return this context
     */
    public CertificateContext setCertificate(@Nullable final Certificate cert) {
        certificate = cert;
        return this;
    }
    
    /**
     * Get any additional certificates accompanying the end-entity certificate.
     * 
     * @return any additional certificates
     */
    @Nonnull @NonnullElements public Collection<Certificate> getIntermediates() {
        return intermediates;
    }

    /**
     * Set the additional certificates accompanying the end-entity certificate.
     * 
     * @param certs additional certificates
     * 
     * @return this context
     */
    public CertificateContext setIntermediates(@Nonnull @NonnullElements final Collection<Certificate> certs) {
        Constraint.isNotNull(certs, "Intermediate certificate collection cannot be null");
        
        intermediates.clear();
        intermediates.addAll(Collections2.filter(certs, Predicates.notNull()));
        
        return this;
    }
    
}