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
package net.shibboleth.idp.cas.protocol;

import net.shibboleth.utilities.java.support.logic.Constraint;

import javax.annotation.Nonnull;

/**
 * Describes a request for a ticket to access a service.
 *
 * @author Marvin S. Addison
 */
public class ServiceTicketRequest {
    /** Service URL */
    @Nonnull private final String service;

    /** CAS protocol renew flag. */
    private boolean renew;

    /** CAS protocol gateway flag. */
    private boolean gateway;

    /** Flag indicating whether ticket request is via SAML 1.1 protocol. */
    private boolean saml;


    public ServiceTicketRequest(@Nonnull final String service) {
        this.service = Constraint.isNotNull(service, "Service cannot be null");
    }

    @Nonnull public String getService() {
        return service;
    }

    public boolean isRenew() {
        return renew;
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(final boolean gateway) {
        this.gateway = gateway;
    }

    public boolean isSAML() {
        return saml;
    }

    public void setSAML(final boolean saml) {
        this.saml = saml;
    }
}
