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

import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to publish the CAS protocol request or response messages, i.e.
 * {@link net.shibboleth.idp.cas.protocol.ProtocolContext#getResponse()}, in Spring Webflow
 * flow scope to make available in views. The key name is the protocol object simple class name
 * converted to variable case, e.g. <code>TicketValidationResponse</code> is accessible as
 * <code>flowScope.ticketValidationResponse</code>.
 *
 * @author Marvin S. Addison
 */
public class PublishProtocolMessageAction extends AbstractCASProtocolAction {

    /** Request/response flag. */
    private boolean requestFlag;

    /**
     * Creates a new instance to publish request or response messages to Webflow request scope.
     *
     * @param isRequest True for request messages, false for response messages.
     */
    public PublishProtocolMessageAction(final boolean isRequest) {
        requestFlag = isRequest;
    }

    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final Object message;
        if (requestFlag) {
            message = getCASRequest(profileRequestContext);
        } else {
            message = getCASResponse(profileRequestContext);
        }
        final String className = message.getClass().getSimpleName();
        final String keyName = className.substring(0, 1).toLowerCase() + className.substring(1);
        springRequestContext.getFlowScope().put(keyName, message);
        return null;
    }
}
