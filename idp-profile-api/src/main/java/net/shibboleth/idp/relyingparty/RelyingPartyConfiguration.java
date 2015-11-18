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

package net.shibboleth.idp.relyingparty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

/** The configuration that applies to a given relying party. */
public class RelyingPartyConfiguration extends AbstractIdentifiableInitializableComponent implements
        IdentifiedComponent, Predicate<ProfileRequestContext> {

    /** The entity ID of the IdP. */
    @NonnullAfterInit @NotEmpty private String responderId;

    /** Controls whether detailed information about errors should be exposed. */
    private boolean detailedErrors;

    /** Registered and usable communication profile configurations for this relying party. */
    @Nonnull @NonnullElements private Map<String, ProfileConfiguration> profileConfigurations;

    /** Predicate that must be true for this configuration to be active for a given request. */
    @Nonnull private Predicate<ProfileRequestContext> activationCondition;

    /** Constructor. */
    public RelyingPartyConfiguration() {
        activationCondition = Predicates.alwaysTrue();
        profileConfigurations = Collections.emptyMap();
    }

    /**
     * Get the self-referential ID to use when responding to requests.
     * 
     * @return ID to use when responding
     */
    @Nonnull @NotEmpty public String getResponderId() {
        return responderId;
    }

    /**
     * Set the self-referential ID to use when responding to requests.
     * 
     * @param responder ID to use when responding
     */
    public void setResponderId(@Nonnull @NotEmpty final String responder) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        responderId =
                Constraint
                        .isNotNull(StringSupport.trimOrNull(responder), "Responder entity ID cannot be null or empty");
    }

    /**
     * Get whether detailed information about errors should be exposed.
     * 
     * @return true iff it is acceptable to expose detailed error information
     */
    public boolean isDetailedErrors() {
        return detailedErrors;
    }

    /**
     * Set whether detailed information about errors should be exposed.
     * 
     * @param flag flag to set
     */
    public void setDetailedErrors(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        detailedErrors = flag;
    }

    /**
     * Get the unmodifiable set of profile configurations for this relying party.
     * 
     * @return unmodifiable set of profile configurations for this relying party, never null
     */
    @Nonnull @NonnullElements @Unmodifiable @NotLive public Map<String, ProfileConfiguration>
            getProfileConfigurations() {
        return ImmutableMap.copyOf(profileConfigurations);
    }

    /**
     * Get the profile configuration, for the relying party, for the given profile. This is a convenience method and is
     * equivalent to calling {@link Map#get(Object)} on the return of {@link #getProfileConfigurations()}. This map
     * contains no null entries, keys, or values.
     * 
     * @param profileId the ID of the profile
     * 
     * @return the configuration for the profile or null if the profile ID was null or empty or there is no
     *         configuration for the given profile
     */
    @Nullable public ProfileConfiguration getProfileConfiguration(@Nullable final String profileId) {
        final String trimmedId = StringSupport.trimOrNull(profileId);
        if (trimmedId == null) {
            return null;
        }

        return profileConfigurations.get(trimmedId);
    }

    /**
     * Set the profile configurations for this relying party.
     * 
     * @param configs the configurations to set
     */
    public void setProfileConfigurations(@Nonnull @NonnullElements final Collection<ProfileConfiguration> configs) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(configs, "ProfileConfiguration collection cannot be null");

        profileConfigurations = new HashMap<>();
        for (ProfileConfiguration config : Collections2.filter(configs, Predicates.notNull())) {
            final String trimmedId =
                    Constraint.isNotNull(StringSupport.trimOrNull(config.getId()), "ID of profile configuration class "
                            + config.getClass().getName() + " cannot be null");
            profileConfigurations.put(trimmedId, config);
        }
    }

    /**
     * Set the condition under which the relying party configuration should be active.
     * 
     * @param condition the activation condition
     */
    public void setActivationCondition(@Nonnull final Predicate<ProfileRequestContext> condition) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        activationCondition =
                Constraint.isNotNull(condition, "Relying partying configuration activation condition cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (responderId == null) {
            throw new ComponentInitializationException("Responder ID cannot be null or empty");
        }
    }

    /** {@inheritDoc} */
    @Override public boolean apply(@Nullable final ProfileRequestContext input) {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        return activationCondition.apply(input);
    }
    
}