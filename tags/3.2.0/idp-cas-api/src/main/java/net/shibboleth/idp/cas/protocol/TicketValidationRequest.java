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

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import javax.annotation.Nonnull;

/**
 * Ticket validation request message.
 *
 * @author Marvin S. Addison
 */
public class TicketValidationRequest extends ServiceTicketResponse {

    /** CAS protocol renew flag. */
    private boolean renew;

    /** Proxy-granting ticket validation URL. */
    @Nonnull private String pgtUrl;

    /**
     * Creates a CAS ticket validation request message.
     *
     * @param service Service to which ticket was issued.
     * @param ticket Ticket to validate.
     */
    public TicketValidationRequest(@Nonnull final String service, @Nonnull final String ticket) {
        super(service, ticket);
    }

    public boolean isRenew() {
        return renew;
    }

    public void setRenew(final boolean renew) {
        this.renew = renew;
    }

    @Nonnull public String getPgtUrl() {
        return pgtUrl;
    }

    public void setPgtUrl(@Nonnull final String url) {
        this.pgtUrl = StringSupport.trimOrNull(url);
    }
}
