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

package net.shibboleth.idp.saml.saml2.profile.config;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.config.AuthenticationProfileConfiguration;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactAwareProfileConfiguration;
import net.shibboleth.idp.saml.profile.config.SAMLArtifactConfiguration;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/** Configuration support for SAML 2 Browser SSO. */
public class BrowserSSOProfileConfiguration extends AbstractSAML2ProfileConfiguration
        implements SAMLArtifactAwareProfileConfiguration, AuthenticationProfileConfiguration {

    /** ID for this profile configuration. */
    public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/saml2/sso/browser";

    /** SAML artifact configuration. */
    @Nullable private SAMLArtifactConfiguration artifactConfig;
    
    /**
     * Whether attributes should be resolved in the course of the profile.
     * 
     * <p>Default value: true</p>
     */
    private boolean resolveAttributes;
    
    /** Whether responses to the authentication request should include an attribute statement. Default value: true */
    private boolean includeAttributeStatement;

    /** Whether the response endpoint should be validated if the request is signed. */
    private boolean skipEndpointValidationWhenSigned;

    /**
     * The maximum amount of time, in milliseconds, the service provider should maintain a session for the user. A value
     * of 0 (the default) indicates no cap is put on the SP's session lifetime.
     */
    @Duration @NonNegative private long maximumSPSessionLifetime;

    /** 
     * The predicate used to determine if produced assertions may be delegated.
     */
    @Nonnull private Predicate<ProfileRequestContext> allowDelegationPredicate;
    
    /** 
     * Whether produced assertions may be delegated. Default value: null.
     * @deprecated use {@link #allowDelegationPredicate} instead
     */
    private Boolean allowingDelegation;
    
    /** Limits the total number of delegates that may be derived from the initial SAML token. Default value: 1. */
    @NonNegative private long maximumTokenDelegationChainLength;

    /** Selects, and limits, the authentication contexts to use for requests. */
    @Nonnull @NonnullElements private List<AuthnContextClassRefPrincipal> defaultAuthenticationContexts;

    /** Filters the usable authentication flows. */
    @Nonnull @NonnullElements private Set<String> authenticationFlows;
    
    /** Enables post-authentication interceptor flows. */
    @Nonnull @NonnullElements private List<String> postAuthenticationFlows;
    
    /** Precedence of name identifier formats to use for requests. */
    @Nonnull @NonnullElements private List<String> nameIDFormatPrecedence;

    /** Constructor. */
    public BrowserSSOProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected BrowserSSOProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSignResponses(Predicates.<ProfileRequestContext>alwaysTrue());
        setEncryptAssertions(Predicates.<ProfileRequestContext>alwaysTrue());
        resolveAttributes = true;
        includeAttributeStatement = true;
        skipEndpointValidationWhenSigned = false;
        maximumSPSessionLifetime = 0;
        allowingDelegation = null;
        maximumTokenDelegationChainLength = 1;
        allowDelegationPredicate = Predicates.<ProfileRequestContext>alwaysFalse();
        defaultAuthenticationContexts = Collections.emptyList();
        authenticationFlows = Collections.emptySet();
        postAuthenticationFlows = Collections.emptyList();
        nameIDFormatPrecedence = Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override @Nullable public SAMLArtifactConfiguration getArtifactConfiguration() {
        return artifactConfig;
    }

    /**
     * Set the SAML artifact configuration, if any.
     * 
     * @param config configuration to set
     */
    public void setArtifactConfiguration(@Nullable final SAMLArtifactConfiguration config) {
        artifactConfig = config;
    }
    
    /**
     * Get whether attributes should be resolved during the profile.
     * 
     * @return true iff attributes should be resolved
     */
    public boolean resolveAttributes() {
        return resolveAttributes;
    }
    
    /**
     * Set whether attributes should be resolved during the profile.
     * 
     * @param flag flag to set
     */
    public void setResolveAttributes(final boolean flag) {
        resolveAttributes = flag;
    }
    
    /**
     * Get whether responses to the authentication request should include an attribute statement.
     * 
     * @return whether responses to the authentication request should include an attribute statement
     */
    public boolean includeAttributeStatement() {
        return includeAttributeStatement;
    }

    /**
     * Set whether responses to the authentication request should include an attribute statement.
     * 
     * @param include whether responses to the authentication request should include an attribute statement
     */
    public void setIncludeAttributeStatement(final boolean include) {
        includeAttributeStatement = include;
    }

    /**
     * Get whether the response endpoint should be validated if the request is signed.
     * 
     * @return whether the response endpoint should be validated if the request is signed
     */
    public boolean skipEndpointValidationWhenSigned() {
        return skipEndpointValidationWhenSigned;
    }

    /**
     * Set whether the response endpoint should be validated if the request is signed.
     * 
     * @param skip whether the response endpoint should be validated if the request is signed
     */
    public void setSkipEndpointValidationWhenSigned(final boolean skip) {
        skipEndpointValidationWhenSigned = skip;
    }

    /**
     * Get the maximum amount of time, in milliseconds, the service provider should maintain a session for the user
     * based on the authentication assertion. A value of 0 is interpreted as an unlimited lifetime.
     * 
     * @return max lifetime of service provider should maintain a session
     */
    @NonNegative public long getMaximumSPSessionLifetime() {
        return maximumSPSessionLifetime;
    }

    /**
     * Set the maximum amount of time, in milliseconds, the service provider should maintain a session for the user
     * based on the authentication assertion. A value of 0 is interpreted as an unlimited lifetime.
     * 
     * @param lifetime max lifetime of service provider should maintain a session
     */
    public void setMaximumSPSessionLifetime(@Duration @NonNegative final long lifetime) {
        maximumSPSessionLifetime =
                Constraint.isGreaterThanOrEqual(0, lifetime,
                        "Maximum SP session lifetime must be greater than or equal to 0");
    }
    
    /**
     * Get the predicate used to determine if produced assertions may be delegated.
     * 
     * @return predicate used to determine if produced assertions may be delegated
     */

    @Nonnull public Predicate<ProfileRequestContext> getAllowDelegation() {
        return allowDelegationPredicate;
    }

    /**
     * Set the predicate used to determine if produced assertions may be delegated.
     * 
     * @param  predicate used to determine if produced assertions may be delegated
     */

    public void setAllowDelegation(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        allowDelegationPredicate = Constraint.isNotNull(predicate, "Allow delegation predicate may not be null");
    }

    /**
     * Get whether produced assertions may be delegated.
     * 
     * @return whether produced assertions may be delegated, as a {@link Boolean}.  May be null.
     * 
     * @deprecated use instead {@link #getAllowDelegation()} predicate
     */
    @Deprecated
    public Boolean getAllowingDelegation() {
        return allowingDelegation;
    }
    
    /**
     * Get whether produced assertions may be delegated.
     * 
     * @return whether produced assertions may be delegated
     * 
     * @deprecated use instead {@link #getAllowDelegation()} predicate
     */
    @Deprecated
    public boolean isAllowingDelegation() {
        if (allowingDelegation != null) {
            return allowingDelegation;
        } else {
            return false;
        }
    }

    /**
     * Set whether produced assertions may be delegated.
     * 
     * @param isAllowed whether produced assertions may be delegated
     * 
     * @deprecated use instead {@link #setAllowingDelegation(Boolean)} predicate
     */
    @Deprecated
    public void setAllowingDelegation(final Boolean isAllowed) {
        allowingDelegation = isAllowed;
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements @NotLive @Unmodifiable public List<Principal> 
        getDefaultAuthenticationMethods() {
        return ImmutableList.<Principal> copyOf(defaultAuthenticationContexts);
    }
    
    /**
     * Get the limits on the total number of delegates that may be derived from the initial SAML token.
     * 
     * @return the limit on the total number of delegates that may be derived from the initial SAML token
     */
    public long getMaximumTokenDelegationChainLength() {
        return maximumTokenDelegationChainLength;
    }

    /**
     * Set the limits on the total number of delegates that may be derived from the initial SAML token.
     * 
     * @param length the limit on the total number of delegates that may be derived from the initial SAML token
     */
    public void setMaximumTokenDelegationChainLength(@NonNegative final long length) {
        maximumTokenDelegationChainLength = Constraint.isGreaterThanOrEqual(0, length,
                "Delegation chain length must be greater than or equal to 0");
    }

    /**
     * Set the default authentication contexts to use, expressed as custom principals.
     * 
     * @param contexts default authentication contexts to use
     */
    public void setDefaultAuthenticationMethods(
            @Nonnull @NonnullElements final List<AuthnContextClassRefPrincipal> contexts) {
        Constraint.isNotNull(contexts, "List of contexts cannot be null");

        defaultAuthenticationContexts = new ArrayList<>(Collections2.filter(contexts, Predicates.notNull()));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Set<String> getAuthenticationFlows() {
        return ImmutableSet.copyOf(authenticationFlows);
    }

    /**
     * Set the authentication flows to use.
     * 
     * @param flows   flow identifiers to use
     */
    public void setAuthenticationFlows(@Nonnull @NonnullElements final Collection<String> flows) {
        Constraint.isNotNull(flows, "Collection of flows cannot be null");
        
        authenticationFlows = new HashSet<>(StringSupport.normalizeStringCollection(flows));
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getPostAuthenticationFlows() {
        return postAuthenticationFlows;
    }

    /**
     * Set the ordered collection of post-authentication interceptor flows to enable.
     * 
     * @param flows   flow identifiers to enable
     */
    public void setPostAuthenticationFlows(@Nonnull @NonnullElements final Collection<String> flows) {
        Constraint.isNotNull(flows, "Collection of flows cannot be null");
        
        postAuthenticationFlows = new ArrayList<>(StringSupport.normalizeStringCollection(flows));
    }
    
    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getNameIDFormatPrecedence() {
        return ImmutableList.copyOf(nameIDFormatPrecedence);
    }

    /**
     * Set the name identifier formats to use.
     * 
     * @param formats name identifier formats to use
     */
    public void setNameIDFormatPrecedence(@Nonnull @NonnullElements final List<String> formats) {
        Constraint.isNotNull(formats, "List of formats cannot be null");

        nameIDFormatPrecedence = new ArrayList<>(StringSupport.normalizeStringCollection(formats));
    }

}