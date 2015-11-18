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

package net.shibboleth.idp.consent.logic.impl;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.impl.Consent;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Predicate to determine whether global consent has been given by user.
 */
public class GlobalAttributeConsentPredicate implements Predicate<ProfileRequestContext> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(GlobalAttributeConsentPredicate.class);

    /** Strategy used to find the {@link ConsentContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ConsentContext> consentContextlookupStrategy;

    /** Constructor. */
    public GlobalAttributeConsentPredicate() {
        consentContextlookupStrategy = new ChildContextLookup<>(ConsentContext.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean apply(@Nullable final ProfileRequestContext input) {
        if (input == null) {
            return false;
        }

        final ConsentContext consentContext = consentContextlookupStrategy.apply(input);
        if (consentContext == null) {
            return false;
        }

        final Map<String, Consent> previousConsents = consentContext.getPreviousConsents();
        for (final Consent consent : previousConsents.values()) {
            if (consent.getId().equals(Consent.WILDCARD) && consent.isApproved()) {
                return true;
            }
        }

        return false;
    }

}
