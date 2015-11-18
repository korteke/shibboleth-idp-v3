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

package net.shibboleth.idp.saml.profile.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.config.AbstractProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/** Base class for SAML profile configurations. */
public abstract class AbstractSAMLProfileConfiguration extends AbstractProfileConfiguration implements
        SAMLProfileConfiguration {

    /** Predicate used to determine if the received request should be signed. Default returns false. */
    @Nonnull private Predicate<ProfileRequestContext> signedRequestsPredicate;

    /** Predicate used to determine if the generated response should be signed. Default returns true. */
    @Nonnull private Predicate<ProfileRequestContext> signResponsesPredicate;

    /** Predicate used to determine if the generated assertion should be signed. Default returns false. */
    @Nonnull private Predicate<ProfileRequestContext> signAssertionsPredicate;

    /** Lifetime of an assertion in milliseconds. Default value: 5 minutes */
    @Positive @Duration private long assertionLifetime;

    /** Whether to include a NotBefore attribute in the Conditions of generated assertions. */
    private boolean includeConditionsNotBefore;

    /** Additional audiences to which an assertion may be released. Default value: empty */
    @Nonnull @NonnullElements private Set<String> assertionAudiences;

    /**
     * Constructor.
     * 
     * @param profileId ID of the communication profile
     */
    public AbstractSAMLProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);

        includeConditionsNotBefore = true;
        signedRequestsPredicate = Predicates.alwaysFalse();
        signResponsesPredicate = Predicates.alwaysFalse();
        signAssertionsPredicate = Predicates.alwaysFalse();
        assertionLifetime = 5 * 60 * 1000;
        assertionAudiences = Collections.emptySet();
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getSignAssertions() {
        return signAssertionsPredicate;
    }

    /**
     * Set the predicate used to determine if generated assertions should be signed.
     * 
     * @param predicate predicate used to determine if generated assertions should be signed
     */
    public void setSignAssertions(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        signAssertionsPredicate =
                Constraint.isNotNull(predicate, "Predicate to determine if assertions should be signed cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getSignRequests() {
        return signedRequestsPredicate;
    }

    /**
     * Set the predicate used to determine if generated requests should be signed.
     * 
     * @param predicate predicate used to determine if generated requests should be signed
     */
    public void setSignRequests(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        signedRequestsPredicate =
                Constraint.isNotNull(predicate,
                        "Predicate to determine if received requests should be signed cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nonnull public Predicate<ProfileRequestContext> getSignResponses() {
        return signResponsesPredicate;
    }

    /**
     * Set the predicate used to determine if generated responses should be signed.
     * 
     * @param predicate predicate used to determine if generated responses should be signed
     */
    public void setSignResponses(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        signResponsesPredicate =
                Constraint.isNotNull(predicate, "Predicate to determine if responses should be signed cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Positive public long getAssertionLifetime() {
        return assertionLifetime;
    }

    /**
     * Set the lifetime of an assertion.
     * 
     * @param lifetime lifetime of an assertion in milliseconds
     */
    public void setAssertionLifetime(@Positive @Duration final long lifetime) {
        assertionLifetime = Constraint.isGreaterThan(0, lifetime, "Assertion lifetime must be greater than 0");
    }

    /** {@inheritDoc} */
    @Override public boolean includeConditionsNotBefore() {
        return includeConditionsNotBefore;
    }

    /**
     * Set whether to include a NotBefore attribute in the Conditions of generated assertions.
     * 
     * @param include whether to include a NotBefore attribute in the Conditions of generated assertions
     */
    public void setIncludeConditionsNotBefore(final boolean include) {
        includeConditionsNotBefore = include;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements @NotLive public Set<String> getAdditionalAudiencesForAssertion() {
        return ImmutableSet.copyOf(assertionAudiences);
    }

    /**
     * Set the set of audiences, in addition to the relying party(ies) to which the IdP is issuing the assertion, with
     * which an assertion may be shared.
     * 
     * @param audiences the additional audiences
     * 
     * @deprecated
     */
    public void setAdditionalAudienceForAssertion(@Nonnull @NonnullElements final Collection<String> audiences) {
        LoggerFactory.getLogger(AbstractSAMLProfileConfiguration.class).warn(
                "Use of deprecated property name 'additionalAudienceForAssertion', please correct to "
                    + "'additionalAudiencesForAssertion'");
        setAdditionalAudiencesForAssertion(audiences);
    }

    /**
     * Set the set of audiences, in addition to the relying party(ies) to which the IdP is issuing the assertion, with
     * which an assertion may be shared.
     * 
     * @param audiences the additional audiences
     */
    public void setAdditionalAudiencesForAssertion(@Nonnull @NonnullElements final Collection<String> audiences) {
        if (audiences == null || audiences.isEmpty()) {
            assertionAudiences = Collections.emptySet();
            return;
        }

        assertionAudiences = new HashSet<>();
        for (final String audience : audiences) {
            final String trimmedAudience = StringSupport.trimOrNull(audience);
            if (trimmedAudience != null) {
                assertionAudiences.add(trimmedAudience);
            }
        }
    }
}