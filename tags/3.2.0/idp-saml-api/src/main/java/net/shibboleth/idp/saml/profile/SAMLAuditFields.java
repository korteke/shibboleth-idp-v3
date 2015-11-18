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

package net.shibboleth.idp.saml.profile;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Constants to use for audit logging fields stored in an {@link net.shibboleth.idp.profile.context.AuditContext}.
 */
public final class SAMLAuditFields {

    /** Service Provider field. */
    @Nonnull @NotEmpty public static final String SERVICE_PROVIDER = "SP";

    /** Identity Provider field. */
    @Nonnull @NotEmpty public static final String IDENTITY_PROVIDER = "IDP";

    /** Protocol field. */
    @Nonnull @NotEmpty public static final String PROTOCOL = "p";

    /** Request binding field. */
    @Nonnull @NotEmpty public static final String REQUEST_BINDING = "b";

    /** Response binding field. */
    @Nonnull @NotEmpty public static final String RESPONSE_BINDING = "bb";
    
    /** Name identifier field. */
    @Nonnull @NotEmpty public static final String NAMEID = "n";

    /** Name identifier Format field. */
    @Nonnull @NotEmpty public static final String NAMEID_FORMAT = "f";
    
    /** Assertion ID field. */
    @Nonnull @NotEmpty public static final String ASSERTION_ID = "i";

    /** Assertion IssueInstant field. */
    @Nonnull @NotEmpty public static final String ASSERTION_ISSUE_INSTANT = "d";
    
    /** Request message ID field. */
    @Nonnull @NotEmpty public static final String REQUEST_ID = "I";
    
    /** Request message IssueInstant field. */
    @Nonnull @NotEmpty public static final String REQUEST_ISSUE_INSTANT = "D";

    /** InResponseTo field. */
    @Nonnull @NotEmpty public static final String IN_RESPONSE_TO = "II";

    /** Response message ID field. */
    @Nonnull @NotEmpty public static final String RESPONSE_ID = "III";

    /** Response message IssueInstant field. */
    @Nonnull @NotEmpty public static final String RESPONSE_ISSUE_INSTANT = "DD";
    
    /** Authentication timestamp field. */
    @Nonnull @NotEmpty public static final String AUTHN_INSTANT = "t";

    /** SessionIndex field. */
    @Nonnull @NotEmpty public static final String SESSION_INDEX = "x";

    /** Authentication method/context/decl field. */
    @Nonnull @NotEmpty public static final String AUTHN_CONTEXT = "ac";

    /** Status code field. */
    @Nonnull @NotEmpty public static final String STATUS_CODE = "S";

    /** Sub-status code field. */
    @Nonnull @NotEmpty public static final String SUBSTATUS_CODE = "SS";

    /** Status message field. */
    @Nonnull @NotEmpty public static final String STATUS_MESSAGE = "SM";

    /** IsPassive requested field. */
    @Nonnull @NotEmpty public static final String IS_PASSIVE = "pasv";

    /** ForceAuthn requested field. */
    @Nonnull @NotEmpty public static final String FORCE_AUTHN = "fauth";
    
    /** Constructor. */
    private SAMLAuditFields() {

    }

}