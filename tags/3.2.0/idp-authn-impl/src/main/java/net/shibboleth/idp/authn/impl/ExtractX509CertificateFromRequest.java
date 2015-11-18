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

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.CertificateContext;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that extracts an X.509 certificate from the standard servlet request attribute,
 * creates a {@link CertificateContext}, and attaches it to the {@link AuthenticationContext}.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#NO_CREDENTIALS}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class) != null</pre>
 * @post If getHttpServletRequest() != null, the content of the "javax.servlet.request.X509Certificate"
 * request attribute is attached to a {@link CertificateContext}.
 */
public class ExtractX509CertificateFromRequest extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExtractX509CertificateFromRequest.class);
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        final CertificateContext certCtx = new CertificateContext();
        authenticationContext.addSubcontext(certCtx, true);
        
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        
        final X509Certificate[] certs =
                (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        log.debug("{} {} X.509 Certificate(s) found in request", getLogPrefix(), certs != null ? certs.length : 0);

        if (certs == null || certs.length < 1) {
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }

        final X509Certificate cert = certs[0];
        log.debug("{} End-entity X.509 certificate found with subject '{}', issued by '{}'", getLogPrefix(),
                cert.getSubjectDN().getName(), cert.getIssuerDN().getName());

        certCtx.setCertificate(cert);

        // Add the rest starting at index 1.
        for (int i = 1; i < certs.length; i++) {
            certCtx.getIntermediates().add(certs[i]);
        }
    }

}