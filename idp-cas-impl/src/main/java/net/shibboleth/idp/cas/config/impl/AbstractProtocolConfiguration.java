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

package net.shibboleth.idp.cas.config.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.shibboleth.idp.cas.ticket.impl.TicketIdentifierGenerationStrategy;
import net.shibboleth.idp.profile.config.AbstractProfileConfiguration;
import net.shibboleth.idp.profile.config.SecurityConfiguration;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.InitializableComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Base class for CAS protocol configuration.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractProtocolConfiguration extends AbstractProfileConfiguration
        implements InitializableComponent {

    /** CAS base protocol URI. */
    public static final String PROTOCOL_URI = "https://www.apereo.org/cas/protocol";

    /** Initialization flag. */
    private boolean initialized;

    /** Validity time period of tickets. */
    @Duration
    @Positive
    private long ticketValidityPeriod;

    /** Flag to indicate whether attributes should be resolved for this profile. */
    private boolean resolveAttributes = true;

    /**
     * Creates a new configuration instance.
     *
     * @param profileId Unique profile identifier.
     */
    public AbstractProtocolConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSecurityConfiguration(
                new SecurityConfiguration(
                    TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES),
                    new TicketIdentifierGenerationStrategy(getDefaultTicketPrefix(), getDefaultTicketLength())));
    }

    @Override
    public void initialize() throws ComponentInitializationException {
        Constraint.isNotNull(getSecurityConfiguration(), "Security configuration cannot be null.");
        Constraint.isNotNull(getSecurityConfiguration().getIdGenerator(),
                "Security configuration ID generator cannot be null.");
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * @return Ticket validity period in milliseconds.
     */
    @Positive
    public long getTicketValidityPeriod() {
        return ticketValidityPeriod;
    }

    /**
     * Sets the ticket validity period.
     *
     * @param millis Ticket validity period in milliseconds.
     */
    public void setTicketValidityPeriod(@Duration @Positive final long millis) {
        this.ticketValidityPeriod = Constraint.isGreaterThan(0, millis, "Ticket validity period must be positive.");
    }

    /** @return True if attribute resolution enabled for this profile, false otherwise. */
    public boolean isResolveAttributes() {
        return resolveAttributes;
    }

    /**
     * Enables or disables attribute resolution.
     *
     * @param resolveAttributes True to enable attribute resolution (default), false otherwise.
     */
    public void setResolveAttributes(final boolean resolveAttributes) {
        this.resolveAttributes = resolveAttributes;
    }

    @Nonnull
    protected abstract String getDefaultTicketPrefix();

    @Nonnull
    protected abstract int getDefaultTicketLength();
}
