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

import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;

import javax.annotation.Nonnull;

/**
 * CAS protocol errors.
 *
 * @author Marvin S. Addison
 */
public enum ProtocolError {

    /** One or more proxy-granting tickets in proxy chain have expired. */
    BrokenProxyChain("INVALID_TICKET", "E_BROKEN_PROXY_CHAIN"),

    /** Illegal state error. */
    IllegalState("INTERNAL_ERROR", "E_ILLEGAL_STATE"),

    /** Ticket parameter provided but has invalid format. */
    InvalidTicketFormat("INVALID_TICKET", "E_INVALID_TICKET_FORMAT"),

    /** A valid ticket of an unsupported type was provided. */
    InvalidTicketType("INVALID_TICKET", "E_INVALID_TICKET_TYPE"),

    /** Generic protocol violation error. */
    ProtocolViolation("INVALID_REQUEST", "E_PROTOCOL_VIOLATION"),

    /** Proxy callback authentication failed. */
    ProxyCallbackAuthenticationFailure("INVALID_REQUEST", "E_PROXY_CALLBACK_AUTH_FAILURE"),

    /** Unauthorized attempt to request proxy-granting ticket. */
    ProxyNotAuthorized("INVALID_REQUEST", "E_PROXY_NOT_AUTHORIZED"),

    /** Unsupported condition where a proxy ticket validation occurs with the renew flag set. */
    RenewIncompatibleWithProxy("INVALID_REQUEST", "E_RENEW_INCOMPATIBLE_WITH_PROXY"),

    /** Service parameter required but not specified. */
    ServiceNotSpecified("INVALID_REQUEST", "E_SERVICE_NOT_SPECIFIED"),

    /** Validating service does not match service to which ticket was issued. */
    ServiceMismatch("INVALID_SERVICE", "E_SERVICE_MISMATCH"),

    /** IdP session that issued ticket has expired which invalidates ticket. */
    SessionExpired("INVALID_TICKET", "E_SESSION_EXPIRED"),

    /** Error retrieving IdP session. */
    SessionRetrievalError("INVALID_TICKET", "E_SESSION_RETRIEVAL_ERROR"),

    /** Ticket parameter required but not specified. */
    TicketNotSpecified("INVALID_REQUEST", "E_TICKET_NOT_SPECIFIED"),

    /** Ticket not found or expired. */
    TicketExpired("INVALID_TICKET", "E_TICKET_EXPIRED"),

    /** Validation specifies renew protocol flag but ticket was not issued from a forced authentication. */
    TicketNotFromRenew("INVALID_TICKET", "E_TICKET_NOT_FROM_RENEW"),

    /** Error creating ticket. */
    TicketCreationError("INTERNAL_ERROR", "E_TICKET_CREATION_ERROR"),

    /** Error retrieving ticket. */
    TicketRetrievalError("INTERNAL_ERROR", "E_TICKET_RETRIEVAL_ERROR"),

    /** Error removing ticket. */
    TicketRemovalError("INTERNAL_ERROR", "E_TICKET_REMOVAL_ERROR");

    /** Error code. */
    private final String code;

    /** Error detail code. */
    private final String detailCode;

    ProtocolError(final String code, final String detailCode) {
        this.code = code;
        this.detailCode = detailCode;
    }

    @Nonnull public String getCode() {
        return code;
    }

    @Nonnull public String getDetailCode() {
        return detailCode;
    }

    /**
     * Creates a Spring webflow event whose ID is given by {@link #name()}} and contains the following attributes:
     *
     * <ul>
     *     <li>code</li>
     *     <li>detailCode</li>
     * </ul>
     *
     * The values of attributes correspond to fields of the same names.
     *
     * @param source Event source.
     *
     * @return Spring webflow event.
     */
    @Nonnull public Event event(final Object source) {
        final LocalAttributeMap attributes = new LocalAttributeMap();
        attributes.put("code", this.code);
        attributes.put("detailCode", this.detailCode);
        return new Event(source, name(), attributes);
    }
}
