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

package net.shibboleth.idp.cas.proxy.impl;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.FailedLoginException;

import net.shibboleth.idp.cas.proxy.ProxyAuthenticator;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;

/**
 * Base class for CAS proxy authenticators that make an HTTPS connection to the endpoint at the callback URI.
 * The response code and TLS certificate are examined as the basis for authentication.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractProxyAuthenticator implements ProxyAuthenticator<TrustEngine<? super X509Credential>> {

    /** Required https scheme for proxy callbacks. */
    protected static final String HTTPS_SCHEME = "https";

    /** List of HTTP response codes permitted for successful proxy callback. */
    @NotEmpty
    @NonnullElements
    private Set<Integer> allowedResponseCodes = Collections.singleton(200);

    /**
     * Sets the HTTP response codes permitted for successful authentication of the proxy callback URL.
     *
     * @param responseCodes One or more HTTP response codes.
     */
    public void setAllowedResponseCodes(@NotEmpty @NonnullElements final Set<Integer> responseCodes) {
        Constraint.isNotEmpty(responseCodes, "Response codes cannot be null or empty.");
        Constraint.noNullItems(responseCodes.toArray(), "Response codes cannot contain null elements.");
        this.allowedResponseCodes = responseCodes;
    }

    @Override
    public final void authenticate(@Nonnull final URI credential, @Nullable TrustEngine<? super X509Credential> criteria)
            throws GeneralSecurityException {

        Constraint.isNotNull(credential, "URI to authenticate cannot be null.");
        if (!HTTPS_SCHEME.equalsIgnoreCase(credential.getScheme())) {
            throw new GeneralSecurityException(credential + " is not an https URI as required.");
        }
        final int status = authenticateProxyCallback(credential, criteria);
        if (!allowedResponseCodes.contains(status)) {
            throw new FailedLoginException(credential + " returned unacceptable HTTP status code " + status);
        }
    }

    /**
     * Authenticates the proxy callback URI by making an HTTP GET request and returning the HTTP response code.
     * The TLS trust evaluation on the certificate at the HTTPS endpoint MUST be performed as part of the request
     * process.
     *
     * @param callbackUri Proxy callback URI containing requisite CAS protocol parameters, <code>pgtId</code> and
     *                    <code>pgtIou</code>.
     * @param x509TrustEngine X.509 trust engine used to perform trust calcluation on TLS certificate of URI endpoint.
     *
     * @return Status code from HTTP GET request.
     *
     * @throws GeneralSecurityException On a failure related to establishing the HTTP connection due to SSL/TLS errors.
     * @throws RuntimeException On networking errors (IO, HTTP protocol).
     */
    protected abstract int authenticateProxyCallback(
            @Nonnull URI callbackUri,
            @Nullable TrustEngine<? super X509Credential> x509TrustEngine)
            throws GeneralSecurityException;

}
