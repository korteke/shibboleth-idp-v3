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

package net.shibboleth.idp.profile.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** Base class for {@link ProfileConfiguration} implementations. */
public abstract class AbstractProfileConfiguration implements ProfileConfiguration {

    /** ID of the profile configured. */
    @Nonnull @NotEmpty private final String profileId;

    /** Enables inbound interceptor flows. */
    @Nonnull @NonnullElements private List<String> inboundFlows;

    /** Enables outbound interceptor flows. */
    @Nonnull @NonnullElements private List<String> outboundFlows;
    
    /** The security configuration for this profile. */
    @Nullable private SecurityConfiguration securityConfiguration;

    /**
     * Constructor.
     * 
     * @param id ID of the communication profile, never null or empty
     */
    public AbstractProfileConfiguration(@Nonnull @NotEmpty final String id) {
        profileId = Constraint.isNotNull(StringSupport.trimOrNull(id), "Profile identifier cannot be null or empty");
        outboundFlows = Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getId() {
        return profileId;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    /**
     * Sets the security configuration for this profile.
     * 
     * @param configuration security configuration for this profile
     */
    public void setSecurityConfiguration(@Nullable final SecurityConfiguration configuration) {
        securityConfiguration = configuration;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getInboundInterceptorFlows() {
        return inboundFlows;
    }

    /**
     * Set the ordered collection of inbound interceptor flows to enable.
     * 
     * @param flows   flow identifiers to enable
     */
    public void setInboundInterceptorFlows(@Nonnull @NonnullElements final Collection<String> flows) {
        Constraint.isNotNull(flows, "Collection of flows cannot be null");
        
        inboundFlows = new ArrayList<>(StringSupport.normalizeStringCollection(flows));
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getOutboundInterceptorFlows() {
        return outboundFlows;
    }

    /**
     * Set the ordered collection of outbound interceptor flows to enable.
     * 
     * @param flows   flow identifiers to enable
     */
    public void setOutboundInterceptorFlows(@Nonnull @NonnullElements final Collection<String> flows) {
        Constraint.isNotNull(flows, "Collection of flows cannot be null");
        
        outboundFlows = new ArrayList<>(StringSupport.normalizeStringCollection(flows));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return profileId.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractProfileConfiguration)) {
            return false;
        }

        final AbstractProfileConfiguration other = (AbstractProfileConfiguration) obj;
        return Objects.equals(profileId, other.getId());
    }
    
}