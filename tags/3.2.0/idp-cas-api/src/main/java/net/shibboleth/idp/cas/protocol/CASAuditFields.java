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

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

import javax.annotation.Nonnull;

/**
 * Constants to use for audit logging fields stored in an {@link net.shibboleth.idp.profile.context.AuditContext}.
 *
 * @author Marvin S. Addison
 * @since 3.2.0
 */
public final class CASAuditFields {

    /** CAS client URL. */
    @Nonnull @NotEmpty public static final String SERVICE_URL = "SP";

    /** CAS protocol gateway flag. */
    @Nonnull @NotEmpty public static final String GATEWAY = "pasv";

    /** CAS protocol renew flag. */
    @Nonnull @NotEmpty public static final String RENEW = "fauth";

    /** CAS protocol ticket-granting ticket (TGT/PGT). */
    @Nonnull @NotEmpty public static final String GRANTING_TICKET = "I";

    /** CAS protocol service ticket (ST/PT). */
    @Nonnull @NotEmpty public static final String SERVICE_TICKET = "i";

    /** User name released to CAS client. */
    @Nonnull @NotEmpty public static final String USER = "n";

    /** CAS ticket validation status code. */
    @Nonnull @NotEmpty public static final String STATUS_CODE = "S";

    /** CAS ticket validation status detail. */
    @Nonnull @NotEmpty public static final String STATUS_DETAIL = "SM";


    /** Constructor. */
    private CASAuditFields() {

    }

}