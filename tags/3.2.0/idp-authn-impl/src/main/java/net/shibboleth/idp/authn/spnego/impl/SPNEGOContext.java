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

package net.shibboleth.idp.authn.spnego.impl;

import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;

/**
 * Context, usually attached to {@link AuthenticationContext}, that carries configuration data
 * and request state for SPNEGO authentication.
 */
public class SPNEGOContext extends BaseContext {
    
    /** The Kerberos settings. */
    @Nullable private KerberosSettings kerberosSettings;

    /** Utility class that manages credentials and state for GSS loop. */
    @Nullable private GSSContextAcceptor contextAcceptor;
    
    /**
     * Get the Kerberos settings.
     * 
     * @return the Kerberos settings
     */
    @Nullable public KerberosSettings getKerberosSettings() {
        return kerberosSettings;
    }

    /**
     * Set the Kerberos settings.
     * 
     * @param settings the Kerberos settings
     */
    public void setKerberosSettings(@Nullable final KerberosSettings settings) {
        kerberosSettings = settings;
    }
    
    /**
     * Get the context acceptor for the current request.
     * 
     * @return context acceptor
     */
    @Nullable public GSSContextAcceptor getContextAcceptor() {
        return contextAcceptor;
    }
    
    /**
     * Set the context acceptor for the current request.
     * 
     * @param acceptor context acceptor
     */
    public void setContextAcceptor(@Nullable final GSSContextAcceptor acceptor) {
        contextAcceptor = acceptor;
    }
    
}