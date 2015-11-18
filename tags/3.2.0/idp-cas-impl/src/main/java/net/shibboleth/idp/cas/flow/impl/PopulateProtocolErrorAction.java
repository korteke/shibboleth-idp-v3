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
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Populates error information needed for protocol error messages.
 *
 * @author Marvin S. Addison
 */
public class PopulateProtocolErrorAction extends AbstractCASProtocolAction {

    @Nonnull
    @Override
    protected Event doExecute(
            @Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext profileRequestContext) {
        final Object request = getCASRequest(profileRequestContext);
        final AbstractProtocolResponse response;
        if (request instanceof ProxyTicketRequest) {
            response = new ProxyTicketResponse();
        } else if (request instanceof TicketValidationRequest) {
            response = new TicketValidationResponse();
        } else {
            throw new IllegalArgumentException("Invalid request type: " + request);
        }
        String code = (String) springRequestContext.getCurrentEvent().getAttributes().get("code");
        String detail = (String) springRequestContext.getCurrentEvent().getAttributes().get("detailCode");
        if (code == null) {
            code = ProtocolError.IllegalState.getCode();
        }
        if (detail == null) {
            detail = ProtocolError.IllegalState.getDetailCode();
        }
        response.setErrorCode(code);
        response.setErrorDetail(detail);
        setCASResponse(profileRequestContext, response);
        return null;
    }
}
