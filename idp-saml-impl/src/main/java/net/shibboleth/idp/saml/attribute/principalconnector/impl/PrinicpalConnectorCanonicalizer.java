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

package net.shibboleth.idp.saml.attribute.principalconnector.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.resolver.LegacyPrincipalDecoder;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.authn.principal.NameIDPrincipal;
import net.shibboleth.idp.saml.authn.principal.NameIdentifierPrincipal;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.profile.SAML1ObjectSupport;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.profile.SAML2ObjectSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Implements SAML subject canonicalization using a series of {@link PrincipalConnector} instances.
 */
public class PrinicpalConnectorCanonicalizer implements LegacyPrincipalDecoder  {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PrinicpalConnectorCanonicalizer.class);

    /** The connectors. */
    @Nonnull @NonnullElements private final Collection<PrincipalConnector> principalConnectors;

    /**
     * Constructor.
     * 
     * @param connectors the connectors we care about.
     */
    public PrinicpalConnectorCanonicalizer(
            @Nullable @NullableElements final Collection<PrincipalConnector> connectors) {

        if (null != connectors) {
            principalConnectors = ImmutableSet.copyOf(Iterables.filter(connectors, Predicates.notNull()));
        } else {
            principalConnectors = Collections.emptySet();
        }
    }

    /** {@inheritDoc} */
    @Override public boolean hasValidConnectors() {
        return !principalConnectors.isEmpty();
    }

    /**
     * Resolve the principal with respect to the provided context. This is expected to strip out the
     * {@link org.opensaml.saml.saml2.core.NameID} or {@link org.opensaml.saml.saml1.core.NameIdentifier} and match it
     * against the connector definitions configured.
     * 
     * @param c14nContext what to look at.
     * @return the IdP principal, or null if no definitions were applicable. A null will be turned into a
     *         {@link net.shibboleth.idp.authn.AuthnEventIds#INVALID_SUBJECT} event
     * @throws ResolutionException if we recognise the definition but could not decode it (data out of date and so
     *             forth) This will be turned into a {@link net.shibboleth.idp.authn.AuthnEventIds#SUBJECT_C14N_ERROR}
     *             event
     */
    @Override @Nullable public String canonicalize(@Nonnull final SubjectCanonicalizationContext c14nContext)
            throws ResolutionException {
    
        Constraint.isNotNull(c14nContext, "Context cannot be null");
    
        if (c14nContext.getSubject() == null) {
            return null;
        }
    
        final Set<NameIdentifierPrincipal> nameIdentifierPrincipals =
                c14nContext.getSubject().getPrincipals(NameIdentifierPrincipal.class);
        if (nameIdentifierPrincipals != null && !nameIdentifierPrincipals.isEmpty()) {
            if (nameIdentifierPrincipals.size() > 1) {
                log.debug("Legacy Principal Decoder: too many NameIdentifierPrincipals");
            } else {
                return canonicalize(nameIdentifierPrincipals.iterator().next().getNameIdentifier(), c14nContext);
            }
        }
    
        final Set<NameIDPrincipal> nameIDPrincipals = c14nContext.getSubject().getPrincipals(NameIDPrincipal.class);
        if (nameIDPrincipals != null && !nameIDPrincipals.isEmpty()) {
            if (nameIDPrincipals.size() > 1) {
                log.debug("Legacy Principal Decoder: too many NameIDPrincipals");
            } else {
                return canonicalize(nameIDPrincipals.iterator().next().getNameID(), c14nContext);
            }
        }
        
        return null;
    }
    

    /**
     * Canonicalize the provided {@link NameIdentifier} with respect to the provided
     * {@link SubjectCanonicalizationContext}.
     * 
     * <p>We iterate over all the connectors to see whether anything matches.</p>
     * 
     * @param nameIdentifier the {@link NameIdentifier}
     * @param c14nContext the {@link SubjectCanonicalizationContext}
     * 
     * @return the Principal, or null if we could not match
     * @throws ResolutionException if we get a fatal error during decoding.
     */
    @Nullable protected String canonicalize(@Nonnull final NameIdentifier nameIdentifier,
            @Nonnull final SubjectCanonicalizationContext c14nContext) throws ResolutionException {

        for (final PrincipalConnector connector : principalConnectors) {

            log.trace("Legacy Principal Decoder: looking at connector {}", connector.getId());

            if (connector.requesterMatches(c14nContext.getRequesterId())
                    && SAML1ObjectSupport.areNameIdentifierFormatsEquivalent(connector.getFormat(),
                            nameIdentifier.getFormat())) {

                try {
                    final String result = connector.decode(c14nContext, nameIdentifier);
                    if (null != result) {
                        log.trace("Legacy Principal Decoder: decoded to {}", result);
                        return result;
                    }
                    log.trace("Legacy Principal Decoder: decode provided no result");
                } catch (final NameDecoderException e) {
                    throw new ResolutionException(e);
                }
            } else {
                log.trace("Legacy Principal Decoder: format or relying party mismatch");
            }
        }
        
        return null;
    }

    /**
     * Canonicalize the provided {@link NameID} with respect to the provided {@link SubjectCanonicalizationContext}.
     * <br/>
     * We iterate over all the connectors to see whether anything matches.
     * 
     * @param nameID the {@link NameID}
     * @param c14nContext the {@link SubjectCanonicalizationContext}
     * @return the Principal, or null if we could not match
     * @throws ResolutionException if we get a fatal error during decoding.
     */
    @Nullable protected String canonicalize(@Nonnull final NameID nameID,
            @Nonnull final SubjectCanonicalizationContext c14nContext) throws ResolutionException {
        
        for (final PrincipalConnector connector : principalConnectors) {

            log.trace("Legacy Principal Decoder: looking at connector {}", connector.getId());

            if (connector.requesterMatches(c14nContext.getRequesterId()) &&
                    SAML2ObjectSupport.areNameIDFormatsEquivalent(connector.getFormat(), nameID.getFormat())) {

                try {
                    final String result = connector.decode(c14nContext, nameID);
                    if (null != result) {
                        log.trace("Legacy Principal Decoder: decoded to {}", result);
                        return result;
                    }
                    log.trace("Legacy Principal Decoder: decode provided no result");
                } catch (final NameDecoderException e) {
                    throw new ResolutionException(e);
                }
            } else {
                log.trace("Legacy Principal Decoder: format or relying party mismatch");
            }
        }
        
        return null;
    }
    
}