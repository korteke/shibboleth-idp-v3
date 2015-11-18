package net.shibboleth.idp.test.flows.cas;

import net.shibboleth.idp.cas.proxy.ProxyAuthenticator;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;

import javax.annotation.Nonnull;
import java.net.URI;
import java.security.GeneralSecurityException;

/**
 * @author Marvin S. Addison
 */
public class TestProxyAuthenticator implements ProxyAuthenticator<TrustEngine<X509Credential>> {

    /** Whether to fail or not. */
    private boolean failureFlag;

    public void setFailureFlag(final boolean isFail) {
        this.failureFlag = isFail;
    }

    @Override
    public void authenticate(@Nonnull URI uri, TrustEngine<X509Credential> criteria) throws GeneralSecurityException {
        if (failureFlag) {
            throw new GeneralSecurityException("Proxy callback authentication failed (failureFlag==true)");
        }
    }
}
