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
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import org.opensaml.security.SecurityException;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet compatible with the {@link ExternalAuthentication} interface that extracts and validates
 * an X.509 client certificate for user authentication.
 */
public class X509AuthServlet extends HttpServlet {
    
    /** Serial UUID. */
    private static final long serialVersionUID = 7466474175700654990L;
    
    /** Init parameter identifying optional {@link TrustEngine} bean name. */
    @Nonnull @NotEmpty private static final String TRUST_ENGINE_PARAM = "trustEngine";

    /** Parameter/cookie for bypassing prompt page. */
    @Nonnull @NotEmpty private static final String PASSTHROUGH_PARAM = "x509passthrough";
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(X509AuthServlet.class);

    /** Trust engine. */
    @Nullable private TrustEngine<? super X509Credential> trustEngine;

    /**
     * Set the {@link TrustEngine} to use.
     * 
     * @param tm trust engine to use  
     */
    public void setTrustEngine(@Nullable final TrustEngine<? super X509Credential> tm) {
        trustEngine = tm;
    }
    
    /** {@inheritDoc} */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        final WebApplicationContext springContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        
        final String param = config.getInitParameter(TRUST_ENGINE_PARAM);
        if (param != null) {
            log.debug("Looking up TrustEngine bean: {}", param);
            final Object bean = springContext.getBean(param);
            if (bean instanceof TrustEngine) {
                trustEngine = (TrustEngine) bean;
            } else {
                throw new ServletException("Bean " + param + " was missing, or not a TrustManager");
            }
        }
    }

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    protected void service(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
            throws ServletException, IOException {
        
        try {
            final String key = ExternalAuthentication.startExternalAuthentication(httpRequest);
            
            final X509Certificate[] certs =
                    (X509Certificate[]) httpRequest.getAttribute("javax.servlet.request.X509Certificate");
            log.debug("{} X.509 Certificate(s) found in request", certs != null ? certs.length : 0);

            if (certs == null || certs.length < 1) {
                log.error("No X.509 Certificates found in request");
                httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_ERROR_KEY, AuthnEventIds.NO_CREDENTIALS);
                ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
                return;
            }

            final X509Certificate cert = certs[0];
            log.debug("End-entity X.509 certificate found with subject '{}', issued by '{}'",
                    cert.getSubjectDN().getName(), cert.getIssuerDN().getName());
            
            if (trustEngine != null) {
                try {
                    final BasicX509Credential cred = new BasicX509Credential(cert);
                    cred.setEntityCertificateChain(Arrays.asList(certs));
                    if (trustEngine.validate(cred, new CriteriaSet())) {
                        log.debug("Trust engine validated X.509 certificate");
                    } else {
                        log.warn("Trust engine failed to validate X.509 certificate");
                        httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_ERROR_KEY,
                                AuthnEventIds.INVALID_CREDENTIALS);
                        ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
                        return;
                    }
                } catch (final SecurityException e) {
                    log.error("Exception raised by trust engine", e);
                    httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_EXCEPTION_KEY, e);
                    ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
                    return;
                }
            }
            
            final String passthrough = httpRequest.getParameter(PASSTHROUGH_PARAM);
            if (passthrough != null && Boolean.parseBoolean(passthrough)) {
                log.debug("Setting UI passthrough cookie");
                final Cookie cookie = new Cookie(PASSTHROUGH_PARAM, "1");
                cookie.setPath(httpRequest.getContextPath());
                cookie.setMaxAge(60 * 60 * 24 * 365);
                cookie.setSecure(true);
                httpResponse.addCookie(cookie);
            }
            
            final Subject subject = new Subject();
            subject.getPublicCredentials().add(cert);
            subject.getPrincipals().add(cert.getSubjectX500Principal());

            httpRequest.setAttribute(ExternalAuthentication.SUBJECT_KEY, subject);

            final String revokeConsent =
                    httpRequest.getParameter(ExternalAuthentication.REVOKECONSENT_KEY);
            if (revokeConsent != null && ("1".equals(revokeConsent) || "true".equals(revokeConsent))) {
                httpRequest.setAttribute(ExternalAuthentication.REVOKECONSENT_KEY, Boolean.TRUE);
            }

            ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
            
        } catch (final ExternalAuthenticationException e) {
            throw new ServletException("Error processing external authentication request", e);
        }
    }
// Checkstyle: CyclomaticComplexity ON
    
}