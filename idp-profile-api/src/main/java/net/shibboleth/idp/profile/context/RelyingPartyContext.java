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

package net.shibboleth.idp.profile.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.ProfileConfiguration;
import net.shibboleth.idp.relyingparty.RelyingPartyConfiguration;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.Function;

/**
 * {@link BaseContext} containing relying party specific information, usually a
 * subcontext of {@link org.opensaml.profile.context.ProfileRequestContext}.
 */
public final class RelyingPartyContext extends BaseContext {

    /** Optional flag indicating whether verification was done. */
    @Nullable private Boolean verified; 
    
    /** The identifier for the relying party. */
    @Nullable private String relyingPartyId;

    /** A pointer to a context tree containing identifying material for the relying party. */
    @Nullable private BaseContext relyingPartyIdContextTree;

    /** A lookup strategy for deriving verification based on the context. */
    @Nullable private Function<RelyingPartyContext,Boolean> verificationLookupStrategy;

    /** A lookup strategy for deriving a relying party ID based on contained information. */
    @Nullable private Function<RelyingPartyContext,String> relyingPartyIdLookupStrategy;
    
    /** The relying party configuration. */
    @Nullable private RelyingPartyConfiguration relyingPartyConfiguration;

    /** Profile configuration that is in use. */
    @Nullable private ProfileConfiguration profileConfiguration;
    
    /**
     * Get whether the relying party was verified in some fashion.
     * 
     * @return  true iff the relying party's identity was verified
     */
    public boolean isVerified() {
        if (verified != null) {
            return verified;
        } else if (verificationLookupStrategy != null) {
            final Boolean flag = verificationLookupStrategy.apply(this);
            if (flag != null) {
                return flag;
            }
        }
        return false;
    }
    
    /**
     * Set whether the relying party was verified in some fashion.
     * 
     * @param flag  explicit value for the verified setting
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setVerified(@Nullable final Boolean flag) {
        verified = flag;
        return this;
    }

    /**
     * Get the unique identifier of the relying party.
     * 
     * @return unique identifier of the relying party
     */
    @Nullable public String getRelyingPartyId() {
        
        if (relyingPartyId != null) {
            return relyingPartyId;
        } else if (relyingPartyIdLookupStrategy != null) {
            return relyingPartyIdLookupStrategy.apply(this);
        } else {
            return null;
        }
    }


    /**
     * Set the unique identifier of the relying party.
     * 
     * @param rpId the relying party identifier, or null
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setRelyingPartyId(@Nullable final String rpId) {
        relyingPartyId = StringSupport.trimOrNull(rpId);
        return this;
    }

    /**
     * Get the context tree containing identifying information for this relying party.
     * 
     * <p>The subtree root may, but need not, be an actual subcontext of this context.</p>
     * 
     * @return context tree
     */
    @Nullable public BaseContext getRelyingPartyIdContextTree() {
        return relyingPartyIdContextTree;
    }
    
    /**
     * Set the context tree containing identifying information for this relying party.
     * 
     * <p>The subtree root may, but need not, be an actual subcontext of this context.</p>
     * 
     * @param root  root of context tree
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setRelyingPartyIdContextTree(@Nullable final BaseContext root) {
        relyingPartyIdContextTree = root;
        return this;
    }
    
    /**
     * Get the lookup strategy for a non-explicit verification determination.
     * 
     * @return lookup strategy
     */
    @Nullable Function<RelyingPartyContext,Boolean> getVerificationLookupStrategy() {
        return verificationLookupStrategy;
    }
    
    /**
     * Set the lookup strategy for a non-explicit verification determination.
     * 
     * @param strategy  lookup strategy
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setVerificationLookupStrategy(
            @Nonnull final Function<RelyingPartyContext,Boolean> strategy) {
        verificationLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
        return this;
    }
    
    /**
     * Get the lookup strategy for a non-explicit relying party ID.
     * 
     * @return lookup strategy
     */
    @Nullable Function<RelyingPartyContext,String> getRelyingPartyIdLookupStrategy() {
        return relyingPartyIdLookupStrategy;
    }
    
    /**
     * Set the lookup strategy for a non-explicit relying party ID.
     * 
     * @param strategy  lookup strategy
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setRelyingPartyIdLookupStrategy(
            @Nonnull final Function<RelyingPartyContext,String> strategy) {
        relyingPartyIdLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
        return this;
    }
    
    /**
     * Get the relying party configuration.
     * 
     * @return the relying party configuration, or null
     */
    @Nullable public RelyingPartyConfiguration getConfiguration() {
        return relyingPartyConfiguration;
    }

    /**
     * Set the configuration to use when processing requests for this relying party.
     * 
     * @param config configuration to use when processing requests for this relying party, or null
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setConfiguration(@Nullable final RelyingPartyConfiguration config) {
        relyingPartyConfiguration = config;
        return this;
    }

    /**
     * Get the configuration for the request profile currently being processed.
     * 
     * @return profile configuration for the request profile currently being processed, or null
     */
    @Nullable public ProfileConfiguration getProfileConfig() {
        return profileConfiguration;
    }

    /**
     * Set the configuration for the request profile currently being processed.
     * 
     * @param config configuration for the request profile currently being processed, or null
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyContext setProfileConfig(@Nullable final ProfileConfiguration config) {
        profileConfiguration = config;
        return this;
    }
    
}