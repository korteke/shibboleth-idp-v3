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

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.shibboleth.idp.authn.AbstractUsernamePasswordValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action that checks for a {@link UsernamePasswordContext} and directly produces an
 * {@link net.shibboleth.idp.authn.AuthenticationResult} based on that identity by acquiring
 * a TGT and optional service ticket from Kerberos.
 *  
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link AuthnEventIds#AUTHN_EXCEPTION}
 * @event {@link AuthnEventIds#INVALID_CREDENTIALS}
 * @pre <pre>ProfileRequestContext.getSubcontext(AuthenticationContext.class, false).getAttemptedFlow() != null</pre>
 * @post If AuthenticationContext.getSubcontext(UsernamePasswordContext.class, false) != null, then
 * an {@link net.shibboleth.idp.authn.AuthenticationResult} is saved to the {@link AuthenticationContext} on a
 * successful login. On a failed login, the {@link net.shibboleth.idp.authn.AbstractValidationAction#handleError(
 * ProfileRequestContext, AuthenticationContext, Exception, String)} method is called.
 */
public class ValidateUsernamePasswordAgainstKerberos extends AbstractUsernamePasswordValidationAction {
    
    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ValidateUsernamePasswordAgainstKerberos.class);

    /** Class name of JAAS LoginModule to acquire Kerberos credentials. */
    @NonnullAfterInit @NotEmpty private String loginModuleClassName;
    
    /** Refresh the Kerberos config before running? */
    private boolean refreshKrb5Config;
    
    /** Save the TGT in the resulting Subject? */
    private boolean preserveTicket;
    
    /** Service principal to acquire a ticket for to verify KDC. */
    private String servicePrincipal;
    
    /** Path to keytab for service principal. */
    private String keytabPath;

    /** JAAS options for client login. */
    @NonnullAfterInit private Map<String,String> clientOptions;

    /** JAAS options for server login. */
    @NonnullAfterInit private Map<String,String> serverOptions;
    
    /** Constructor. */
    public ValidateUsernamePasswordAgainstKerberos() {
        loginModuleClassName = "com.sun.security.auth.module.Krb5LoginModule";
    }
    
    /**
     * Set the name of the JAAS LoginModule to use to acquire Kerberos credentials.
     * 
     * @param name  name of login module class
     */
    public void setLoginModuleClassName(@Nonnull final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        loginModuleClassName = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "Class name cannot be null or empty");
    }
    
    /**
     * Set whether to refresh the Kerberos configuration before running.
     * 
     * @param flag  flag to set
     */
    public void setRefreshKrb5Config(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        refreshKrb5Config = flag;
    }

    /**
     * Set whether to save the TGT in the Subject.
     * 
     * @param flag  flag to set
     */
    public void setPreserveTicket(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        preserveTicket = flag;
    }
    
    /**
     * Set the name of a service principal to use to verify the KDC.
     * 
     * <p>If non-null, a keytab resource must also be set.</p>
     * 
     * @param name name of service principal
     */
    public void setServicePrincipal(@Nullable final String name) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        servicePrincipal = StringSupport.trimOrNull(name);
    }

    /**
     * Provides a keytab for the service principal to use to verify the KDC.
     * 
     * @param path path to file containing a keytab
     */
    public void setKeytabPath(@Nullable final String path) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        keytabPath = path;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (servicePrincipal != null && keytabPath == null) {
            throw new ComponentInitializationException("A keytab path is required if a service principal is set");
        }
        
        clientOptions = new HashMap<>();
        clientOptions.put("refreshKrb5Config", Boolean.valueOf(refreshKrb5Config).toString());

        if (servicePrincipal != null) {
            // This set of options is from a lot of trial and error, but a couple of important points:
            // - setting isInitiator to false prevents an extra AS exchange to get a TGT for the service
            // - setting storeKey to true is essential or it can't create a GSSCredential for the service
            serverOptions = new HashMap<>();
            serverOptions.put("refreshKrb5Config", Boolean.valueOf(refreshKrb5Config).toString());
            serverOptions.put("useKeyTab", "true");
            serverOptions.put("keyTab", keytabPath);
            serverOptions.put("principal", servicePrincipal);
            serverOptions.put("doNotPrompt", "true");
            serverOptions.put("isInitiator", "false");
            serverOptions.put("storeKey", "true");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        
        try {
            final LoginModule clientLoginModule = (LoginModule) Class.forName(loginModuleClassName).newInstance();
            clientLoginModule.initialize(getSubject(), new SimpleCallbackHandler(), new HashMap(), clientOptions);
            if (!clientLoginModule.login() || !clientLoginModule.commit()) {
                clientLoginModule.abort();
                throw new LoginException("Login module reported failure");
            }
            
            // We don't call logout, since that would destroy the contents of the Subject.
            
            if (servicePrincipal != null) {
                verifyKDC();
            }
            
            log.info("{} Login by '{}' succeeded", getLogPrefix(), getUsernamePasswordContext().getUsername());
            
            buildAuthenticationResult(profileRequestContext, authenticationContext);
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error("{} Unable to instantiate JAAS module for Kerberos", getLogPrefix(), e);
            handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.AUTHN_EXCEPTION);
        } catch (final LoginException e) {
            log.info("{} Login by {} failed", getLogPrefix(), getUsernamePasswordContext().getUsername(), e);
            handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.INVALID_CREDENTIALS);
        } catch(final GSSException e) {
            log.warn("{} Login by {} failed during GSS context establishment to verify KDC", getLogPrefix(),
                    getUsernamePasswordContext().getUsername(), e);
            handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.INVALID_CREDENTIALS);
        } catch (final Exception e) {
            log.warn("{} Login by {} produced unknown exception", getLogPrefix(),
                    getUsernamePasswordContext().getUsername(), e);
            handleError(profileRequestContext, authenticationContext, e, AuthnEventIds.AUTHN_EXCEPTION);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull protected Subject populateSubject(@Nonnull final Subject subject) {

        if (!preserveTicket) {
            subject.getPrivateCredentials().clear();
        }
        
        return super.populateSubject(subject);
    }

    /**
     * Use credentials to acquire and verify a service ticket.
     * 
     * @throws Exception if an error occurs
     */
    private void verifyKDC() throws Exception {
        log.debug("{} TGT acquired for {}, attempting to verify authenticity of TGT using service principal {}",
                getLogPrefix(), getUsernamePasswordContext().getUsername(), servicePrincipal);
        
        final Oid mechOid = new Oid("1.2.840.113554.1.2.2");
        
        LoginModule serverLoginModule = null;
        try {
            serverLoginModule = (LoginModule) Class.forName(loginModuleClassName).newInstance();
            final Subject serverSubject = new Subject();
            serverLoginModule.initialize(serverSubject, null, new HashMap(), serverOptions);
            if (!serverLoginModule.login() || !serverLoginModule.commit()) {
                serverLoginModule.abort();
                throw new LoginException("Login module reported failure");
            }
            
            final GSSManager manager = GSSManager.getInstance();
            
            // Note the use of NT_USER_NAME; using NT_HOSTBASED_SERVICE does not work and results in a TGS request
            // to the KDC for an unknown service name that isn't even logged there.
            final GSSName serviceName = manager.createName(servicePrincipal, GSSName.NT_USER_NAME);
            final GSSContext context = manager.createContext(serviceName, mechOid, null, GSSContext.DEFAULT_LIFETIME);
            
            // The GSS context initiation has to be performed as a privileged action with the client subject
            // so that the null credential above indicating the default credentials pulls from the JAAS subject.
            final byte[] token = Subject.doAs(getSubject(), new PrivilegedExceptionAction<byte[]>() {
                public byte[] run() throws GSSException {
                    byte[] token = new byte[0];
                    // This is a one pass context initialization.
                    context.requestMutualAuth(false);
                    context.requestCredDeleg(false);
                    return context.initSecContext(token, 0, token.length);
                }
            });
            
            // At this point the KDC has logged the additional TGS exchange to get the service ticket.
            // Because we used the storeKeys option on the server-side JAAS module call, the keytab
            // entries have been loaded, and the KerberosKey objects in the private credential set are
            // the credentials needed by GSS to accept the context token, namely to decrypt the service
            // ticket. So we can use a null GSSCredential again by running the context creation call
            // under the subject's privilege context.
            final String verifiedName = Subject.doAs(serverSubject, new PrivilegedExceptionAction<String>() {
                public String run() throws GSSException {
                    final GSSContext serverCtx = manager.createContext((GSSCredential) null);
                    serverCtx.acceptSecContext(token, 0, token.length);
                    final String s = serverCtx.getSrcName().toString();
                    serverCtx.dispose();
                    return s;
                }
            });
            
            context.dispose();
            
            log.debug("{} GSS context established between {} and {}", getLogPrefix(), verifiedName, servicePrincipal);
        } catch (final LoginException e) {
            throw new LoginException("Unable to obtain service credentials for KDC verification");
        } catch (final PrivilegedActionException e) {
            if (e.getException() != null) {
                throw e.getException();
            } else {
                throw e;
            }
        } finally {
            if (serverLoginModule != null) {
                serverLoginModule.logout();
            }
        }
    }
    
    /**
     * A callback handler that provides static name and password data to a JAAS login process.
     * 
     * This handler only supports {@link NameCallback} and {@link PasswordCallback}.
     */
    private class SimpleCallbackHandler implements CallbackHandler {

        /**
         * Handle a callback.
         * 
         * @param callbacks The list of callbacks to process.
         * 
         * @throws UnsupportedCallbackException If callbacks has a callback other than {@link NameCallback} or
         *             {@link PasswordCallback}.
         */
        public void handle(final Callback[] callbacks) throws UnsupportedCallbackException {

            if (callbacks == null || callbacks.length == 0) {
                return;
            }

            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) cb;
                    ncb.setName(getUsernamePasswordContext().getUsername());
                } else if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    pcb.setPassword(getUsernamePasswordContext().getPassword().toCharArray());
                }
            }
        }
    }

}