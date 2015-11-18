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

package net.shibboleth.idp.consent.flow.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consent action which extracts user input and updates current consent objects in the consent context accordingly.
 * 
 * For every consent id passed via the {@link #CONSENT_IDS_REQUEST_PARAMETER} request parameter, this action sets
 * {@link Consent#isApproved()} to true for the current consent object whose id matches the request parameter value. For
 * every current consent object whose id is not passed as a request parameter, this action sets
 * {@link Consent#isApproved()} to false.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @event {@link org.opensaml.profile.action.EventIds#INVALID_PROFILE_CTX}
 * @post See above.
 */
public class ExtractConsent extends AbstractConsentAction {

    /** Parameter name for consent IDs. */
    @Nonnull @NotEmpty public static final String CONSENT_IDS_REQUEST_PARAMETER = "_shib_idp_consentIds";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ExtractConsent.class);

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final ConsentContext consentContext = getConsentContext();

        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.debug("{} Profile action does not contain an HttpServletRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return;
        }

        final String[] consentIdsRequestParameterValues = request.getParameterValues(CONSENT_IDS_REQUEST_PARAMETER);
        if (consentIdsRequestParameterValues == null) {
            log.debug("{} No consent choices available from user input", getLogPrefix());
            return;
        }

        final Collection<String> consentIds =
                StringSupport.normalizeStringCollection(Arrays.asList(consentIdsRequestParameterValues));
        log.debug("{} Extracted consent ids '{}' from request parameter '{}'", getLogPrefix(), consentIds,
                CONSENT_IDS_REQUEST_PARAMETER);

        final Map<String, Consent> currentConsents = getConsentContext().getCurrentConsents();
        for (final Consent consent : currentConsents.values()) {
            if (consentIds.contains(consent.getId())) {
                consent.setApproved(Boolean.TRUE);
            } else {
                consent.setApproved(Boolean.FALSE);
            }
        }

        log.debug("{} Consent context '{}'", getLogPrefix(), consentContext);
    }

}
