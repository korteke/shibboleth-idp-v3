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


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.trust.impl.ExplicitX509CertificateTrustEngine;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.X509Support;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ValidateX509Certificate} unit test. */
public class ValidateX509CertificateTest extends PopulateAuthenticationContextTest {
    
    private String entityCertBase64 = 
            "MIIDjDCCAnSgAwIBAgIBKjANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
            "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDQwOTA2MTIwOVoX" +
            "DTE3MDQwNjA2MTIwOVowMTESMBAGA1UEChMJSW50ZXJuZXQyMRswGQYDVQQDExJm" +
            "b29iYXIuZXhhbXBsZS5vcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB" +
            "AQDNWnkFmhy1vYa6gN/xBRKkZxFy3sUq2V0LsYb6Q3pe9Qlb6+BzaM5DrN8uIqqr" +
            "oBE3Wp0LtrgKuQTpDpNFBdS2p5afiUtOYLWBDtizTOzs3Z36MGMjIPUYQ4s03IP3" +
            "yPh2ud6EKpDPiYqzNbkRaiIwmYSit5r+RMYvd6fuKvTOn6h7PZI5AD7Rda7VWh5O" +
            "VSoZXlRx3qxFho+mZhW0q4fUfTi5lWwf4EhkfBlzgw/k5gf4cOi6rrGpRS1zxmbt" +
            "X1RAg+I20z6d04g0N2WsK5stszgYKoIROJCiXwjraa8/SoFcILolWQpttVHBIUYl" +
            "yDlm8mIFleZf4ReFpfm+nUYxAgMBAAGjgbIwga8wCQYDVR0TBAIwADAsBglghkgB" +
            "hvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYE" +
            "FDgRgTkjaKoK6DoZfUZ4g9LDJUWuMFUGA1UdIwROMEyAFNXuZVPeUdqHrULqQW7y" +
            "r9buRpQLoTGkLzAtMRIwEAYDVQQKEwlJbnRlcm5ldDIxFzAVBgNVBAMTDmNhLmV4" +
            "YW1wbGUub3JnggEBMA0GCSqGSIb3DQEBBQUAA4IBAQCPj3Si4Eiw9abNgPBUhBXW" +
            "d6eRYlIHaHcnez6j6g7foAOyuVIUso9Q5c6pvL87lmasK55l09YPXw1qmiH+bHMc" +
            "rwEPODpLx7xd3snlOCi7FyxahxwSs8yfTu8Pq95rWt0LNcfHxQK938Cpnav6jgDo" +
            "2uH/ywAOFFSnoBzGHAfScHMfj8asZ6THosYsklII7FSU8j49GV2utkvGB3mcu4ST" +
            "uLdeRCZmi93vq1D4JVGsXC4UaHjg114+a+9q0XZdz6a1UW4pt1ryXIPotCS62M71" +
            "pkJf5neHUinKAqgoRfPXowudZg1Zl8DjzoOBn+MNHRrR5KYbVGvdHcxoJLCwVB/v";
    
    private String otherCert1Base64 = 
            "MIIECTCCAvGgAwIBAgIBMzANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
            "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyNTIwMTYxMVoX" +
            "DTE3MDUyMjIwMTYxMVowGjEYMBYGA1UEAxMPaWRwLmV4YW1wbGUub3JnMIIBtjCC" +
            "ASsGByqGSM44BAEwggEeAoGBAI+ktw7R9m7TxjaCrT2MHwWNQUAyXPrqbFCcu+DC" +
            "irr861U6R6W/GyqWdcy8/D1Hh/I1U94POQn5yfqVPpVH2ZRS4OMFndHWaoo9V5LJ" +
            "oXTXHiDYB3W4t9tn0fm7It0n7VoUI5C4y9LG32Hq+UIGF/ktNTmo//mEqLS6aJNd" +
            "bMFpAhUArmKGh0hcpmjukYArWcMRvipB4CMCgYBuCiCrUaHBRRtqrk0P/Luq0l2M" +
            "2718GwSGeLPZip06gACDG7IctMrgH1J+ZIjsx6vffi977wnMDiktqacmaobV+SCR" +
            "W9ijJRdkYpUHmlLvuJGnDPjkvewpbGWJsCabpWEvWdYw3ma8RuHOPj4Jkrdd4VcR" +
            "aFwox/fPJ7cG6kBydgOBhAACgYBxQIPv9DCsmiMHG1FAxSARX0GcRiELJPJ+MtaS" +
            "tdTrVobNa2jebwc3npLiTvUR4U/CDo1mSZb+Sp/wian8kNZHmGcR6KbtJs9UDsa3" +
            "V0pbbgpUar4HcxV+NQJBbhn9RGu85g3PDILUrINiUAf26mhPN5Y0paM+HbM68nUf" +
            "1OLv16OBsjCBrzAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdl" +
            "bmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUIHFAEB/3jIIZzJEJ/qdsuI8v" +
            "N3kwVQYDVR0jBE4wTIAU1e5lU95R2oetQupBbvKv1u5GlAuhMaQvMC0xEjAQBgNV" +
            "BAoTCUludGVybmV0MjEXMBUGA1UEAxMOY2EuZXhhbXBsZS5vcmeCAQEwDQYJKoZI" +
            "hvcNAQEFBQADggEBAJt4Q34+pqjW5tHHhkdzTITSBjOOf8EvYMgxTMRzhagLSHTt" +
            "9RgO5i/G7ELvnwe1j6187m1XD9iEAWKeKbB//ljeOpgnwzkLR9Er5tr1RI3cbil0" +
            "AX+oX0c1jfRaQnR50Rfb5YoNX6G963iphlxp9C8VLB6eOk/S270XoWoQIkO1ioQ8" +
            "JY4HE6AyDsOpJaOmHpBaxjgsiko52ZWZeZyaCyL98BXwVxeml7pYnHlXWWidB0N/" +
            "Zy+LbvWg3urUkiDjMcB6nGImmEfDSxRdybitcMwbwL26z2WOpwL3llm3mcCydKXg" +
            "Xt8IQhfDhOZOHWckeD2tStnJRP/cqBgO62/qirw=";
    
    private ValidateX509Certificate action; 
    
    @BeforeMethod public void setUp() throws Exception {
        super.setUp();
        
        action = new ValidateX509Certificate();
        action.setHttpServletRequest((HttpServletRequest) src.getExternalContext().getNativeRequest());
        action.initialize();
    }

    @Test public void testMissingFlow() {
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }
    
    @Test public void testMissingCert() {
        prc.getSubcontext(AuthenticationContext.class).setAttemptedFlow(authenticationFlows.get(0));
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testNoTrustEngine() throws ComponentInitializationException, CertificateException {
        final X509Certificate entityCert = X509Support.decodeCertificate(entityCertBase64);
        final X509Certificate[] certs = new X509Certificate[]{entityCert};
        ((MockHttpServletRequest) action.getHttpServletRequest()).setAttribute("javax.servlet.request.X509Certificate", certs);

        
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(
                X500Principal.class).iterator().next().getName(), "CN=foobar.example.org,O=Internet2");
    }

    @Test public void testTrustEngineSuccess() throws ComponentInitializationException, CertificateException {
        final X509Certificate entityCert = X509Support.decodeCertificate(entityCertBase64);
        final X509Certificate[] certs = new X509Certificate[]{entityCert};

        final CredentialResolver resolver = new StaticCredentialResolver(new BasicX509Credential(entityCert));
        final TrustEngine<X509Credential> engine = new ExplicitX509CertificateTrustEngine(resolver);
        
        action = new ValidateX509Certificate();
        action.setTrustEngine(engine);
        action.setHttpServletRequest((HttpServletRequest) src.getExternalContext().getNativeRequest());
        
        action.initialize();

        ((MockHttpServletRequest) action.getHttpServletRequest()).setAttribute("javax.servlet.request.X509Certificate", certs);

        
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(
                X500Principal.class).iterator().next().getName(), "CN=foobar.example.org,O=Internet2");
    }

    @Test public void testTrustEngineFailure() throws ComponentInitializationException, CertificateException {
        final X509Certificate entityCert = X509Support.decodeCertificate(entityCertBase64);
        final X509Certificate[] certs = new X509Certificate[]{entityCert};

        final X509Certificate otherCert1 = X509Support.decodeCertificate(otherCert1Base64);
        
        final CredentialResolver resolver = new StaticCredentialResolver(new BasicX509Credential(otherCert1));
        final TrustEngine<X509Credential> engine = new ExplicitX509CertificateTrustEngine(resolver);
        
        action = new ValidateX509Certificate();
        action.setTrustEngine(engine);
        action.setHttpServletRequest((HttpServletRequest) src.getExternalContext().getNativeRequest());
        
        action.initialize();

        ((MockHttpServletRequest) action.getHttpServletRequest()).setAttribute("javax.servlet.request.X509Certificate", certs);

        
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        doExtract(prc);
        
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
        Assert.assertNull(ac.getAuthenticationResult());
    }
    
    private void doExtract(ProfileRequestContext prc) throws ComponentInitializationException, CertificateException {
        final ExtractX509CertificateFromRequest extract = new ExtractX509CertificateFromRequest();
        extract.setHttpServletRequest(action.getHttpServletRequest());

        extract.initialize();
        extract.execute(src);
    }

}