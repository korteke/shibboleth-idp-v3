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

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * {@link Function} that returns whether the current consents are approved from an {@link ConsentContext}.
 */
public class CurrentConsentIsApprovedAuditExtractor implements Function<ProfileRequestContext, Collection<Boolean>> {

    /** Strategy used to find the {@link ConsentContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ConsentContext> consentContextLookupStrategy;

    /** Constructor. */
    public CurrentConsentIsApprovedAuditExtractor() {
        consentContextLookupStrategy = new ChildContextLookup<>(ConsentContext.class, false);
    }

    /**
     * 
     * Constructor.
     *
     * @param strategy the consent context lookup strategy
     */
    public CurrentConsentIsApprovedAuditExtractor(
            @Nonnull final Function<ProfileRequestContext, ConsentContext> strategy) {
        consentContextLookupStrategy = Constraint.isNotNull(strategy, "Consent context lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable public Collection<Boolean> apply(@Nullable final ProfileRequestContext input) {
        final ConsentContext consentContext = consentContextLookupStrategy.apply(input);
        if (consentContext != null && !consentContext.getCurrentConsents().isEmpty()) {
            return Collections2.transform(consentContext.getCurrentConsents().values(),
                    new Function<Consent, Boolean>() {
                        public Boolean apply(final Consent input) {
                            return input.isApproved();
                        }
                    });
        } else {
            return Collections.emptyList();
        }
    }

}
