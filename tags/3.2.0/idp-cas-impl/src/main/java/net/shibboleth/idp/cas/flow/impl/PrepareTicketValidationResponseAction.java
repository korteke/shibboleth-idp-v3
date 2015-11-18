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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.cas.config.impl.ConfigLookupFunction;
import net.shibboleth.idp.cas.config.impl.ValidateConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.session.context.SessionContext;
import net.shibboleth.idp.session.context.navigate.SessionContextPrincipalLookupFunction;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Prepares {@link TicketValidationResponse} for use in CAS protocol response views. Possible outcomes:
 * <ul>
 *     <li><code>null</code> on success</li>
 *     <li>{@link ProtocolError#IllegalState IllegalState}</li>
 * </ul>
 *
 * @author Marvin S. Addison
 */
public class PrepareTicketValidationResponseAction extends
        AbstractCASProtocolAction<TicketValidationRequest, TicketValidationResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PrepareTicketValidationResponseAction.class);

    /** Function used to retrieve AttributeContext. */
    private Function<ProfileRequestContext,AttributeContext> attributeContextFunction =
            Functions.compose(
                    new ChildContextLookup<>(AttributeContext.class, true),
                    new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));

    /** Function used to retrieve subject principal. */
    private Function<ProfileRequestContext,String> principalLookupFunction =
            Functions.compose(
                    new SessionContextPrincipalLookupFunction(),
                    new ChildContextLookup<ProfileRequestContext, SessionContext>(SessionContext.class));

    /** Profile configuration lookup function. */
    private final ConfigLookupFunction<ValidateConfiguration> configLookupFunction =
            new ConfigLookupFunction<>(ValidateConfiguration.class);


    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final AttributeContext ac = attributeContextFunction.apply(profileRequestContext);
        if (ac == null) {
            throw new IllegalStateException("AttributeContext not found in profile request context.");
        }

        final ValidateConfiguration validateConfiguration = configLookupFunction.apply(profileRequestContext);
        if (validateConfiguration == null) {
            throw new IllegalArgumentException("Cannot locate ValidateConfiguration");
        }

        final String principal;
        if (validateConfiguration.getUserAttribute() != null) {
            log.debug("Using {} for CAS username", validateConfiguration.getUserAttribute());
            final IdPAttribute attribute = ac.getIdPAttributes().get(validateConfiguration.getUserAttribute());
            if (attribute != null && !attribute.getValues().isEmpty()) {
                principal = attribute.getValues().get(0).getValue().toString();
            } else {
                log.debug("Filtered attribute {} has no value", validateConfiguration.getUserAttribute());
                principal = null;
            }
        } else {
            principal = principalLookupFunction.apply(profileRequestContext);
        }

        if (principal == null) {
            throw new IllegalStateException("Principal cannot be null");
        }

        final TicketValidationResponse response = getCASResponse(profileRequestContext);
        response.setUserName(principal);
        for (IdPAttribute attribute : ac.getIdPAttributes().values()) {
            log.debug("Processing {}", attribute);
            for (IdPAttributeValue<?> value : attribute.getValues()) {
                response.addAttribute(attribute.getId(), value.getValue().toString());
            }
        }
        return null;
    }
}
