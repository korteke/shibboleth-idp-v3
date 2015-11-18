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

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.CertificateContext;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.SecurityException;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that checks for a {@link CertificateContext} containing {@link X509Certificate} objects, and
 * directly produces an {@link net.shibboleth.idp.authn.AuthenticationResult} based on that identity, after
 * optionally validating the certificate(s) against a {@link TrustEngine}.
 *  
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_PROFILE_CTX}
 * @event {@link AuthnEventIds#INVALID_CREDENTIALS}
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class).getAttemptedFlow() != null</pre>
 * @post If AuthenticationContext.getSubcontext(CertificateContext.class) != null, then
 * an {@link net.shibboleth.idp.authn.AuthenticationResult} is saved to the {@link AuthenticationContext} on a
 * successful validation. On a failure, the
 * {@link AbstractValidationAction#handleError(ProfileRequestContext, AuthenticationContext, Exception, String)}
 * method is called.
 */
public class ValidateX509Certificate extends AbstractValidationAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ValidateX509Certificate.class);

    /** Optional trust engine to validate certificates against. */
    @Nullable private TrustEngine<? super X509Credential> trustEngine;
    
    /** CertificateContext containing the credentials to validate. */
    @Nullable private CertificateContext certContext;
    
    /**
     * Set a {@link TrustEngine} to use.
     * 
     * @param tm trust engine to use  
     */
    public void setTrustEngine(@Nullable final TrustEngine<? super X509Credential> tm) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        trustEngine = tm;
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        }
        
        if (authenticationContext.getAttemptedFlow() == null) {
            log.info("{} No attempted flow within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }
        
        certContext = authenticationContext.getSubcontext(CertificateContext.class);
        if (certContext == null) {
            log.info("{} No CertificateContext available within authentication context", getLogPrefix());
            handleError(profileRequestContext, authenticationContext, AuthnEventIds.NO_CREDENTIALS,
                    AuthnEventIds.NO_CREDENTIALS);
            return false;
        } else if (certContext.getCertificate() == null || !(certContext.getCertificate() instanceof X509Certificate)) {
            log.info("{} No X.509 certificate available within CertificateContext", getLogPrefix());
            handleError(profileRequestContext, authenticationContext, AuthnEventIds.NO_CREDENTIALS,
                    AuthnEventIds.NO_CREDENTIALS);
            return false;
        }
        
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        if (trustEngine != null) {
            log.debug("{} Attempting to validate certificate using trust engine", getLogPrefix());
            try {
                final BasicX509Credential cred =
                        new BasicX509Credential((X509Certificate) certContext.getCertificate());
                if (!certContext.getIntermediates().isEmpty()) {
                    cred.getEntityCertificateChain().add((X509Certificate) certContext.getCertificate());
                    for (final Certificate extra : certContext.getIntermediates()) {
                        if (extra instanceof X509Certificate) {
                            cred.getEntityCertificateChain().add((X509Certificate) extra);
                        }
                    }
                }
                if (trustEngine.validate(cred, new CriteriaSet())) {
                    log.debug("{} Trust engine validated X.509 certificate", getLogPrefix());
                } else {
                    log.warn("{} Trust engine failed to validate X.509 certificate", getLogPrefix());
                    handleError(profileRequestContext, authenticationContext, AuthnEventIds.INVALID_CREDENTIALS,
                            AuthnEventIds.INVALID_CREDENTIALS);
                    return;
                }
            } catch (final SecurityException e) {
                log.error("{} Exception raised by trust engine", getLogPrefix(), e);
                handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.INVALID_CREDENTIALS);
                return;
            }
        }
        
        log.debug("{} No trust engine configured, certificate will be trusted", getLogPrefix());

        log.info("{} Login by '{}' succeeded", getLogPrefix(),
                ((X509Certificate) certContext.getCertificate()).getSubjectX500Principal().getName());
        buildAuthenticationResult(profileRequestContext, authenticationContext);
        ActionSupport.buildProceedEvent(profileRequestContext);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull protected Subject populateSubject(@Nonnull final Subject subject) {
        subject.getPrincipals().add(((X509Certificate) certContext.getCertificate()).getSubjectX500Principal());
        subject.getPublicCredentials().add(certContext.getCertificate());
        return subject;
    }

}