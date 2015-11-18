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

package net.shibboleth.idp.consent.audit.impl;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Constants to use for audit logging fields stored in an {@link net.shibboleth.idp.profile.context.AuditContext} as a
 * child of an {@link net.shibboleth.idp.consent.context.impl.ConsentContext}.
 */
public final class ConsentAuditFields {

    /** Current consents ID field. */
    @Nonnull @NotEmpty public static final String CURRENT_CONSENT_IDS = "CCI";

    /** Current consents value field. */
    @Nonnull @NotEmpty public static final String CURRENT_CONSENT_VALUES = "CCV";

    /** Current consents isApproved field. */
    @Nonnull @NotEmpty public static final String CURRENT_CONSENT_IS_APPROVED = "CCA";

    /** Constructor. */
    private ConsentAuditFields() {

    }
}
