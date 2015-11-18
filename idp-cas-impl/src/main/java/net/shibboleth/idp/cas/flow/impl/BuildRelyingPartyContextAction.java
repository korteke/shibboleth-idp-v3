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

package net.shibboleth.idp.cas.flow.impl;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.protocol.*;
import net.shibboleth.idp.cas.service.Service;
import net.shibboleth.idp.cas.service.ServiceRegistry;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Creates the {@link RelyingPartyContext} as a child of the {@link ProfileRequestContext}.
 *
 * @author Marvin S. Addison
 */
public class BuildRelyingPartyContextAction extends AbstractCASProtocolAction {

    /** Name of group to which unverified services belong. */
    public static final String UNVERIFIED_GROUP = "unverified";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BuildRelyingPartyContextAction.class);

    /** Repository for verified CAS services (relying parties). */
    @Nonnull
    private final ServiceRegistry serviceRegistry;

    /**
     * Creates a new instance.
     *
     * @param registry Service registry.
     */
    public BuildRelyingPartyContextAction(@Nonnull final ServiceRegistry registry) {
        this.serviceRegistry = Constraint.isNotNull(registry, "Service registry cannot be null");
    }

    @Nonnull
    @Override
    protected Event doExecute(
        final @Nonnull RequestContext springRequestContext,
        final @Nonnull ProfileRequestContext profileRequestContext) {

        final String serviceURL;
        final Object request = getCASRequest(profileRequestContext);
        if (request instanceof ServiceTicketRequest) {
            serviceURL = ((ServiceTicketRequest) request).getService();
        } else if (request instanceof ProxyTicketRequest) {
            serviceURL = ((ProxyTicketRequest) request).getTargetService();
        } else if (request instanceof TicketValidationRequest) {
            serviceURL = ((TicketValidationRequest) request).getService();
        } else {
            throw new IllegalStateException("Service URL not found in flow state");
        }
        Service service = serviceRegistry.lookup(serviceURL);
        final RelyingPartyContext rpc = new RelyingPartyContext();
        rpc.setVerified(service != null);
        rpc.setRelyingPartyId(serviceURL);
        if (service != null) {
            log.debug("Setting up RP context for verified relying party {}", service);
        } else {
            service = new Service(serviceURL, UNVERIFIED_GROUP, false);
            log.debug("Setting up RP context for unverified relying party {}", service);
        }
        profileRequestContext.addSubcontext(rpc);
        setCASService(profileRequestContext, service);
        return null;
    }
}
