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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.shibboleth.idp.authn.config.AuthenticationProfileConfiguration;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * CAS protocol configuration that applies to the <code>/login</code> URI.
 *
 * @author Marvin S. Addison
 */
public class LoginConfiguration extends AbstractProtocolConfiguration
        implements AuthenticationProfileConfiguration {

    /** Proxy ticket profile URI. */
    public static final String PROFILE_ID = PROTOCOL_URI + "/login";

    /** Default ticket prefix. */
    public static final String DEFAULT_TICKET_PREFIX = "ST";

    /** Default ticket length (random part). */
    public static final int DEFAULT_TICKET_LENGTH = 25;

    /** Filters the usable authentication flows. */
    @Nonnull
    @NonnullElements
    private Set<String> authenticationFlows = Collections.emptySet();

    /** Enables post-authentication interceptor flows. */
    @Nonnull
    @NonnullElements
    private List<String> postAuthenticationFlows = Collections.emptyList();
    
    /** Selects, and limits, the authentication contexts to use for requests. */
    @Nonnull
    @NonnullElements
    private List<AuthnContextClassRefPrincipal> defaultAuthenticationContexts = Collections.emptyList();

    /** Precedence of name identifier formats to use for requests. */
    @Nonnull
    @NonnullElements
    private List<String> nameIDFormatPrecedence = Collections.emptyList();

    /** Creates a new instance. */
    public LoginConfiguration() {
        super(PROFILE_ID);
        // Service tickets valid for 15s by default
        setTicketValidityPeriod(15000);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements
    @NotLive
    @Unmodifiable
    public List<Principal> getDefaultAuthenticationMethods() {
        return ImmutableList.<Principal> copyOf(defaultAuthenticationContexts);
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
    @Nonnull @NonnullElements
    @NotLive
    @Unmodifiable
    public Set<String> getAuthenticationFlows() {
        return ImmutableSet.copyOf(authenticationFlows);
    }

    /**
     * Set the authentication flows to use.
     *
     * @param flows   flow identifiers to use
     */
    public void setAuthenticationFlows(@Nonnull @NonnullElements final Collection<String> flows) {
        Constraint.isNotNull(flows, "Collection of flows cannot be null");

        authenticationFlows = new HashSet<>(Collections2.filter(flows, Predicates.notNull()));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements
    @NotLive
    @Unmodifiable
    public List<String> getPostAuthenticationFlows() {
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
    @Override
    @Nonnull @NonnullElements
    @NotLive
    @Unmodifiable
    public List<String> getNameIDFormatPrecedence() {
        return ImmutableList.copyOf(nameIDFormatPrecedence);
    }

    /**
     * Set the name identifier formats to use.
     *
     * @param formats name identifier formats to use
     */
    public void setNameIDFormatPrecedence(@Nonnull @NonnullElements final List<String> formats) {
        Constraint.isNotNull(formats, "List of formats cannot be null");

        nameIDFormatPrecedence = new ArrayList<>(Collections2.filter(formats, Predicates.notNull()));
    }



    @Override
    @Nonnull
    protected String getDefaultTicketPrefix() {
        return DEFAULT_TICKET_PREFIX;
    }

    @Override
    @Nonnull
    protected int getDefaultTicketLength() {
        return DEFAULT_TICKET_LENGTH;
    }
}
