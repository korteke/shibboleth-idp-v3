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

package net.shibboleth.idp.profile;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Constants to use for audit logging fields stored in an {@link net.shibboleth.idp.profile.context.AuditContext}.
 */
public final class IdPAuditFields {

    /** Event timestamp field. */
    @Nonnull @NotEmpty public static final String EVENT_TIME = "T";

    /** Event type field. */
    @Nonnull @NotEmpty public static final String EVENT_TYPE = "e";

    /** URL field. */
    @Nonnull @NotEmpty public static final String URL = "URL";

    /** URI field. */
    @Nonnull @NotEmpty public static final String URI = "URI";

    /** Session ID field. */
    @Nonnull @NotEmpty public static final String SESSION_ID = "s";

    /** Remote address field. */
    @Nonnull @NotEmpty public static final String REMOTE_ADDR = "a";

    /** User Agent field. */
    @Nonnull @NotEmpty public static final String USER_AGENT = "UA";

    /** Profile field. */
    @Nonnull @NotEmpty public static final String PROFILE = "P";

    /** Username field. */
    @Nonnull @NotEmpty public static final String USERNAME = "u";

    /** Hashed username field. */
    @Nonnull @NotEmpty public static final String HASHED_USERNAME = "HASHEDu";

    /** Attributes field. */
    @Nonnull @NotEmpty public static final String ATTRIBUTES = "attr";
    
    /** Constructor. */
    private IdPAuditFields() {

    }

}