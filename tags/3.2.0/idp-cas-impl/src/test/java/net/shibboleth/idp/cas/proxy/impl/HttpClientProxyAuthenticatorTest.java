/*
 * See LICENSE for licensing and NOTICE for copyright.
 */

package net.shibboleth.idp.cas.proxy.impl;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.idp.spring.IdPPropertiesApplicationContextInitializer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link HttpClientProxyAuthenticator} class.
 *
 * @author Marvin S. Addison
 */
@ContextConfiguration(
        locations = "/spring/proxy-authn-test.xml",
        initializers = IdPPropertiesApplicationContextInitializer.class)
@WebAppConfiguration
@TestPropertySource(properties = {"idp.initializer.failFast = false"})
public class HttpClientProxyAuthenticatorTest extends AbstractTestNGSpringContextTests {

    private HttpClientProxyAuthenticator authenticator = new HttpClientProxyAuthenticator();

    @Autowired
    private ApplicationContext context;

    @DataProvider(name = "data")
    public Object[][] buildTestData() {
        return new Object[][] {
                // Trusted cert and acceptable response code
                new Object[] { "testCase1", 200, null },

                // Trusted cert and unacceptable response code
                new Object[] { "testCase1", 404, new FailedLoginException() },

                // Untrusted cert
                new Object[] { "testCase2", 200, new CertificateException() },

                // Ensure cert is rejected when no trust engine is configured
                new Object[] { "doesNotExist", 200, new CertificateException() },
        };
    }

    @Test(dataProvider = "data")
    public void testAuthenticate(final String trustEngineBean, final int status, final Exception expected)
            throws Exception {
        Server server = null;
        try {
            server = startServer(new ConfigurableStatusHandler(status));
            TrustEngine<X509Credential> trustEngine;
            try {
                trustEngine = context.getBean(trustEngineBean, TrustEngine.class);
            } catch (NoSuchBeanDefinitionException e) {
                trustEngine = null;
            }
            authenticator.authenticate(new URI("https://localhost:8443/?pgtId=A&pgtIOU=B"), trustEngine);
            if (expected != null) {
                fail("Proxy authentication should have failed with " + expected);
            }
        } catch (Exception e) {
            if (expected == null) {
                throw e;
            }
            assertTrue(expected.getClass().isAssignableFrom(e.getClass()));
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    private Server startServer(final Handler handler) {
        final Server server = new Server();

        final SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreType("PKCS12");
        sslContextFactory.setKeyStorePath("src/test/resources/credentials/localhost.p12");
        sslContextFactory.setKeyStorePassword("changeit");
        final ServerConnector connector = new ServerConnector(server, sslContextFactory);
        connector.setHost("127.0.0.1");
        connector.setPort(8443);
        server.setConnectors(new Connector[] { connector });
        server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            try {
                server.stop();
            } catch (Exception e2) {}
            throw new RuntimeException("Jetty startup failed", e);
        }
        final Thread serverRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.join();
                } catch (InterruptedException e) {}
            }
        });
        serverRunner.start();
        return server;
    }

    private static class ConfigurableStatusHandler extends AbstractHandler {

        final int status;

        public ConfigurableStatusHandler(final int status) {
            this.status = status;
        }

        @Override
        public void handle(
                final String target,
                final Request request,
                final HttpServletRequest servletRequest,
                final HttpServletResponse servletResponse) throws IOException, ServletException {

            servletResponse.setContentType("text/plain;charset=utf-8");
            servletResponse.setStatus(status);
            request.setHandled(true);
            servletResponse.getWriter().println("OK");
        }
    }
}
