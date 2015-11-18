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

package net.shibboleth.idp.authn.context;

import javax.annotation.Nullable;
import javax.security.auth.kerberos.KerberosTicket;

import org.opensaml.messaging.context.BaseContext;

/**
 * Context, usually attached to {@link AuthenticationContext}, that carries a {@link KerberosTicket} to be
 * validated.
 */
public class KerberosTicketContext extends BaseContext {

    /** Kerberos ticket to be validated. */
    private KerberosTicket ticket;

    /**
     * Get the Kerberos ticket to be validated.
     * 
     * @return Kerberos ticket to be validated
     */
    @Nullable public KerberosTicket getTicket() {
        return ticket;
    }
    
    /**
     * Set the Kerberos ticket to be validated.
     * 
     * @param kerbTicket the Kerberos ticket to be validated
     * 
     * @return this context
     */
    public KerberosTicketContext setTicket(@Nullable final KerberosTicket kerbTicket){
        ticket = kerbTicket;
        return this;
    }
    
}