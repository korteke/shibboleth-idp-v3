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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract base class for protocol response messages.
 *
 * @author Marvin S. Addison
 * @since 3.2.0
 */
public class AbstractProtocolResponse {

    /** CAS protocol error code populated on failure. */
    @Nullable private String errorCode;

    /** CAS protocol error detail populated on failure. */
    @Nullable private String errorDetail;


    /** @return Non-null error code on a ticket validation failure condition. */
    @Nullable public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(@Nonnull String code) {
        this.errorCode = code;
    }

    /** @return Non-null error detail on a ticket validation failure condition. */
    @Nullable public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(@Nonnull String code) {
        this.errorDetail = code;
    }
}
