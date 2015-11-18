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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.context.impl.ConsentContext;
import net.shibboleth.idp.consent.flow.impl.ConsentFlowDescriptor;
import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Predicate that returns whether consent is required by comparing the previous and current consents from the consent
 * context.
 */
public class IsConsentRequiredPredicate implements Predicate<ProfileRequestContext> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(IsConsentRequiredPredicate.class);

    /** Consent context lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, ConsentContext> consentContextLookupStrategy;

    /** Consent flow descriptor lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, ConsentFlowDescriptor> consentFlowDescriptorLookupStrategy;

    /** Constructor. */
    public IsConsentRequiredPredicate() {
        consentContextLookupStrategy = new ChildContextLookup<>(ConsentContext.class);
        consentFlowDescriptorLookupStrategy =
                new FlowDescriptorLookupFunction<>(ConsentFlowDescriptor.class);
    }

    /**
     * Set the consent context lookup strategy.
     * 
     * @param strategy consent context lookup strategy
     */
    public void
            setConsentContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, ConsentContext> strategy) {
        consentContextLookupStrategy = Constraint.isNotNull(strategy, "Consent context lookup strategy cannot be null");
    }

    /**
     * Set the consent flow descriptor lookup strategy.
     * 
     * @param strategy consent flow descriptor lookup strategy
     */
    public void setConsentFlowDescriptorLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, ConsentFlowDescriptor> strategy) {
        consentFlowDescriptorLookupStrategy =
                Constraint.isNotNull(strategy, "Consent flow descriptor lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable public boolean apply(@Nullable final ProfileRequestContext input) {
        if (input == null) {
            log.debug("Consent is not required, no profile request context");
            return false;
        }

        final ConsentContext consentContext = consentContextLookupStrategy.apply(input);
        if (consentContext == null) {
            log.debug("Consent is not required, no consent context");
            return false;
        }

        final ConsentFlowDescriptor consentFlowDescriptor = consentFlowDescriptorLookupStrategy.apply(input);
        if (consentFlowDescriptor == null) {
            log.debug("Consent is not required, no consent flow descriptor");
            return false;
        }

        final Map<String, Consent> currentConsents = consentContext.getCurrentConsents();
        if (currentConsents.isEmpty()) {
            log.debug("Consent is not required, there are no current consents");
            return false;
        }

        final Map<String, Consent> previousConsents = consentContext.getPreviousConsents();
        if (previousConsents.isEmpty()) {
            log.debug("Consent is required, no previous consents");
            return true;
        }

        for (final Consent currentConsent : currentConsents.values()) {
            final Consent previousConsent = previousConsents.get(currentConsent.getId());
            if (previousConsent == null) {
                log.debug("Consent is required, no previous consent for '{}'", currentConsent);
                return true;
            }
            if (consentFlowDescriptor.compareValues()
                    && !Objects.equals(currentConsent.getValue(), previousConsent.getValue())) {
                log.debug("Consent is required, previous consent '{}' does not match current consent '{}'",
                        previousConsent, currentConsent);
                return true;
            }
        }

        log.debug("Consent is not required, previous consents match current consents");
        return false;
    }

}
