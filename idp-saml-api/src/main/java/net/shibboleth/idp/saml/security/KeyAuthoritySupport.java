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

package net.shibboleth.idp.saml.security;

import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.xmlobject.KeyAuthority;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.PKIXValidationInformation;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.KeyInfo;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Utility class for extracting {@link PKIXValidationInformation} from a {@link KeyAuthority}.
 */
public final class KeyAuthoritySupport {
    
    /** Default value for Shibboleth KeyAuthority verify depth. */
    public static final int KEY_AUTHORITY_VERIFY_DEPTH_DEFAULT = 1;
    
    /** Constructor. Private to prevent instantiation. */
    private KeyAuthoritySupport() {
        
    }
    
    /**
     * Extracts PKIX validation information from the Shibboleth KeyAuthority metadata extension element.
     * 
     * @param keyAuthority the Shibboleth KeyAuthority element from which to extract information
     * @return an instance of extracted PKIX validation information
     * @throws SecurityException thrown if the key, certificate, or CRL information is represented in an unsupported
     *             format
     */
    @Nullable public static PKIXValidationInformation extractPKIXValidationInfo(
            @Nullable final KeyAuthority keyAuthority) throws SecurityException {
        if (keyAuthority == null) {
            return null;
        }

        final List<X509Certificate> certs = new ArrayList<>();
        final List<X509CRL> crls = new ArrayList<>();
        Integer depth = keyAuthority.getVerifyDepth();
        if (depth == null) {
            depth = KEY_AUTHORITY_VERIFY_DEPTH_DEFAULT;
        }
        
        final List<KeyInfo> keyInfos = keyAuthority.getKeyInfos();
        if (keyInfos == null || keyInfos.isEmpty()) {
            return null;
        }
        
        for (KeyInfo keyInfo : keyInfos) {
            certs.addAll(getX509Certificates(keyInfo));
            crls.addAll(getX509CRLs(keyInfo));
        }
        
        // Unlikely, but go ahead and check.
        if (certs.isEmpty() && crls.isEmpty()) {
            return null;
        }

        return new KeyAuthorityPKIXValidationInformation(certs, crls, depth);
    }
    
    /**
     * Extract certificates from a KeyInfo element.
     * 
     * @param keyInfo the KeyInfo instance from which to extract certificates
     * @return a collection of X509 certificates, possibly empty
     * @throws SecurityException thrown if the certificate information is represented in an unsupported format
     */
    private static Collection<X509Certificate> getX509Certificates(KeyInfo keyInfo) throws SecurityException {
        try {
            return KeyInfoSupport.getCertificates(keyInfo);
        } catch (CertificateException e) {
            throw new SecurityException("Error extracting certificates from KeyAuthority KeyInfo", e);
        }

    }

    /**
     * Extract CRL's from a KeyInfo element.
     * 
     * @param keyInfo the KeyInfo instance from which to extract CRL's
     * @return a collection of X509 CRL's, possibly empty
     * @throws SecurityException thrown if the CRL information is represented in an unsupported format
     */
    private static Collection<X509CRL> getX509CRLs(KeyInfo keyInfo) throws SecurityException {
        try {
            return KeyInfoSupport.getCRLs(keyInfo);
        } catch (CRLException e) {
            throw new SecurityException("Error extracting CRL's from KeyAuthority KeyInfo", e);
        }

    }
    
    /**
     * Basic implementation of {@link PKIXValidationInformation}.
     */
    public static class KeyAuthorityPKIXValidationInformation implements PKIXValidationInformation {

        /** Certs used as the trust anchors. */
        @Nullable @NonnullElements private final Collection<X509Certificate> trustAnchors;

        /** CRLs used during validation. */
        @Nullable @NonnullElements private final Collection<X509CRL> trustedCRLs;

        /** Max verification depth during PKIX validation. */
        @Nonnull private final Integer verificationDepth;

        /**
         * Constructor.
         * 
         * @param anchors certs used as trust anchors during validation
         * @param crls CRLs used during validation
         * @param depth max verification path depth
         */
        public KeyAuthorityPKIXValidationInformation(@Nullable final Collection<X509Certificate> anchors,
                @Nullable final Collection<X509CRL> crls, @Nonnull final Integer depth) {

            verificationDepth = Constraint.isNotNull(depth, "Verification depth cannot be null");
            
            if (anchors != null) {
                trustAnchors = new ArrayList<>(Collections2.filter(anchors, Predicates.notNull()));
            } else {
                trustAnchors = null;
            }
            
            if (crls != null) {
                trustedCRLs = new ArrayList<>(Collections2.filter(crls, Predicates.notNull()));
            } else {
                trustedCRLs = null;
            }
        }

        /** {@inheritDoc} */
        @Nullable public Collection<X509CRL> getCRLs() {
            return trustedCRLs;
        }

        /** {@inheritDoc} */
        @Nullable public Collection<X509Certificate> getCertificates() {
            return trustAnchors;
        }

        /** {@inheritDoc} */
        @Nonnull public Integer getVerificationDepth() {
            return verificationDepth;
        }
    }

}