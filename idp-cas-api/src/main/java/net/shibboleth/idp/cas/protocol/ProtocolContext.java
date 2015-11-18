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

import org.opensaml.messaging.context.BaseContext;

/**
 * Context container for CAS protocol request and response messages.
 *
 * @author Marvin S. Addison
 */
public class ProtocolContext<RequestType, ResponseType> extends BaseContext {
    /** CAS protocol request. */
    private RequestType request;

    /** CAS protocol response. */
    private ResponseType response;

    /** @return CAS protocol request. */
    public RequestType getRequest() {
        return request;
    }

    /**
     * Sets the CAS protocol request.
     *
     * @param request CAS protocol request.
     */
    public void setRequest(final RequestType request) {
        this.request = request;
    }

    /** @return CAS protocol response. */
    public ResponseType getResponse() {
        return response;
    }

    /**
     * Sets the CAS protocol request.
     *
     * @param response CAS protocol response.
     */
    public void setResponse(final ResponseType response) {
        this.response = response;
    }
}
