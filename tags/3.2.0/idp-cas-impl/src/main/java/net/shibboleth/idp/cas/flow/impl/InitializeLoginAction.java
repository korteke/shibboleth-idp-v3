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

import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.ProtocolParam;
import net.shibboleth.idp.cas.protocol.SamlParam;
import net.shibboleth.idp.cas.protocol.ServiceTicketRequest;
import net.shibboleth.idp.cas.protocol.ServiceTicketResponse;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Initializes the CAS protocol interaction at the <code>/login</code> URI. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#ServiceNotSpecified ServiceNotSpecified}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class InitializeLoginAction extends AbstractCASProtocolAction<ServiceTicketRequest, ServiceTicketResponse> {

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final ParameterMap params = springRequestContext.getRequestParameters();
        String service = params.get(ProtocolParam.Service.id());
        boolean isSAML= false;
        if (service == null) {
            service = params.get(SamlParam.TARGET.name());
            if (service == null) {
                return ProtocolError.ServiceNotSpecified.event(this);
            }
            isSAML = true;
        }
        final ServiceTicketRequest serviceTicketRequest = new ServiceTicketRequest(service);
        serviceTicketRequest.setSAML(isSAML);

        final String renew = params.get(ProtocolParam.Renew.id());
        if (renew != null) {
            serviceTicketRequest.setRenew(true);
        }

        // http://www.jasig.org/cas/protocol, section 2.1.1
        // It is RECOMMENDED that CAS implementations ignore the "gateway" parameter if "renew" is set.
        final String gateway = params.get(ProtocolParam.Gateway.id());
        if (gateway != null && renew == null) {
            serviceTicketRequest.setGateway(true);
        }

        setCASRequest(profileRequestContext, serviceTicketRequest);

        return null;
    }
}
