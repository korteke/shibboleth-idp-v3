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

package net.shibboleth.idp.authn;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Constants to use for {@link org.opensaml.profile.action.ProfileAction}
 * {@link org.opensaml.profile.context.EventContext} results related to
 * authentication and subject c14n.
 */
public final class AuthnEventIds {

    /**
     * ID of event returned if the {@link net.shibboleth.idp.authn.context.AuthenticationContext} is missing or invalid.
     */
    @Nonnull @NotEmpty public static final String INVALID_AUTHN_CTX = "InvalidAuthenticationContext";
    
    /** ID of event returned if there are no flows that could be used for authentication or c14n. */
    @Nonnull @NotEmpty public static final String NO_POTENTIAL_FLOW = "NoPotentialFlow";

    /** ID of the event returned if passive authentication isn't possible. */
    @Nonnull @NotEmpty public static final String NO_PASSIVE = "NoPassive";

    /** ID of event returned if the request's authentication requirements can't be met by an action or flow. */
    @Nonnull @NotEmpty public static final String REQUEST_UNSUPPORTED = "RequestUnsupported";
    
    /** ID of event returned if there are no credentials available in the request. */
    @Nonnull @NotEmpty public static final String NO_CREDENTIALS = "NoCredentials";
    
    /** ID of event returned if the given credentials are invalid. */
    @Nonnull @NotEmpty public static final String INVALID_CREDENTIALS = "InvalidCredentials";
    
    /** ID of event returned if the subject's account is in an invalid state. */
    @Nonnull @NotEmpty public static final String ACCOUNT_ERROR = "AccountError";

    /** ID of event returned if the subject's account has non-fatal but potentially useful state to report. */
    @Nonnull @NotEmpty public static final String ACCOUNT_WARNING = "AccountWarning";
    
    /** ID of event returned if a flow wishes to indicate that another flow should be selected instead. */
    @Nonnull @NotEmpty public static final String RESELECT_FLOW = "ReselectFlow";
    
    /**
     * ID of event returned if the {@link net.shibboleth.idp.authn.context.SubjectCanonicalizationContext}
     * is missing or invalid.
     */
    @Nonnull @NotEmpty public static final String INVALID_SUBJECT_C14N_CTX = "InvalidSubjectCanonicalizationContext";

    /** ID of event returned if a Subject cannot be canonicalized. */
    @Nonnull @NotEmpty public static final String INVALID_SUBJECT = "InvalidSubject";

    /** ID of event returned if an error occurs canonicalizing a Subject. */
    @Nonnull @NotEmpty public static final String SUBJECT_C14N_ERROR = "SubjectCanonicalizationError";

    /** ID of event returned if an authenticated subject doesn't match the identity in an existing session. */
    @Nonnull @NotEmpty public static final String IDENTITY_SWITCH = "IdentitySwitch";
    
    /** ID of event returned if authentication throws an exception unrelated to credential validation. */
    @Nonnull @NotEmpty public static final String AUTHN_EXCEPTION = "AuthenticationException";

    /** Constructor. */
    private AuthnEventIds() {
    }
    
}