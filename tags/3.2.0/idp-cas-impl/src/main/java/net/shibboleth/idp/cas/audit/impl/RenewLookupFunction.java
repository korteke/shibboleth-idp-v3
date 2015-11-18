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

package net.shibboleth.idp.cas.audit.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.shibboleth.idp.cas.protocol.ProtocolContext;
import net.shibboleth.idp.cas.protocol.ServiceTicketRequest;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

/**
 * Looks up the value of the CAS renew parameter from the request to the /login URI.
 *
 * @author Marvin S. Addison
 */
public class RenewLookupFunction implements Function<ProfileRequestContext, Boolean> {
    @Nonnull
    private final Function<ProfileRequestContext, ProtocolContext> protocolContextFunction;

    public RenewLookupFunction() {
        this(new ChildContextLookup<ProfileRequestContext, ProtocolContext>(ProtocolContext.class));
    }

    public RenewLookupFunction(@Nonnull final Function<ProfileRequestContext, ProtocolContext> protocolLookup) {
        protocolContextFunction = Constraint.isNotNull(protocolLookup, "ProtocolContext lookup cannot be null");
    }

    @Nullable
    @Override
    public Boolean apply(@Nonnull final ProfileRequestContext input) {
        final ProtocolContext protocolContext = protocolContextFunction.apply(input);
        if (protocolContext == null || protocolContext.getRequest() ==  null) {
            return null;
        }
        final Object request = protocolContext.getRequest();
        if (request instanceof ServiceTicketRequest) {
            return ((ServiceTicketRequest) request).isRenew();
        }
        throw new IllegalArgumentException("Unsupported request type: " + request);
    }
}
