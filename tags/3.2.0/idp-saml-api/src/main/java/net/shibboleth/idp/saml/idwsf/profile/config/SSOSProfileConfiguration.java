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

package net.shibboleth.idp.saml.idwsf.profile.config;

import javax.annotation.Nonnull;

import net.shibboleth.idp.saml.saml2.profile.config.BrowserSSOProfileConfiguration;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/** Configuration support for the Liberty ID-WSF SSOS profile. */
public class SSOSProfileConfiguration extends BrowserSSOProfileConfiguration {

    /** ID for this profile configuration. */
    @Nonnull @NotEmpty public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/liberty/ssos";

    /** Predicate used to determine if a token may be delegated to a relying party. */
    @Nonnull private Predicate<ProfileRequestContext> delegationPredicate;

    /** Constructor. */
    public SSOSProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected SSOSProfileConfiguration(final String profileId) {
        super(profileId);
        
        delegationPredicate = Predicates.alwaysFalse();
    }

    /**
     * Gets the predicate used to determine whether a delegated token presented
     * to the IdP by another non-user entity may be used to complete SAML 2 SSO
     * to this relying party.
     * 
     * @return predicate the delegation predicate
     */
    @Nonnull public Predicate<ProfileRequestContext> getDelegationPredicate() {
        return delegationPredicate;
    }

    /**
     * Sets the predicate used to determine whether a delegated token presented
     * to the IdP by another non-user entity may be used to complete SAML 2 SSO
     * to this relying party.
     * 
     * @param predicate the new delegation predicate
     */
    public void setDelegationPredicate(
            @Nonnull final Predicate<ProfileRequestContext> predicate) {
        delegationPredicate = Constraint.isNotNull(predicate, "Delegation predicate cannot be null");
    }
    
}