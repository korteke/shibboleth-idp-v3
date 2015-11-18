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

package net.shibboleth.idp.saml.audit.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml1.core.AttributeStatement;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.AuthorizationDecisionStatement;
import org.opensaml.saml.saml1.core.SubjectStatement;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;

import com.google.common.base.Function;

import net.shibboleth.utilities.java.support.logic.Constraint;

/** {@link Function} that returns the Name Identifier from a request or response. */
public class NameIDAuditExtractor implements Function<ProfileRequestContext,String> {

    /** Lookup strategy for message to read from. */
    @Nonnull private final Function<ProfileRequestContext,SAMLObject> responseLookupStrategy;
    
    /**
     * Constructor.
     *
     * @param strategy lookup strategy for message
     */
    public NameIDAuditExtractor(@Nonnull final Function<ProfileRequestContext,SAMLObject> strategy) {
        responseLookupStrategy = Constraint.isNotNull(strategy, "Response lookup strategy cannot be null");
    }

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nullable final ProfileRequestContext input) {
        SAMLObject msg = responseLookupStrategy.apply(input);
        if (msg != null) {
            
            // Step down into ArtifactResponses.
            if (msg instanceof ArtifactResponse) {
                msg = ((ArtifactResponse) msg).getMessage();
            }
            
            if (msg instanceof org.opensaml.saml.saml2.core.Response) {
                
                for (final org.opensaml.saml.saml2.core.Assertion assertion
                        : ((org.opensaml.saml.saml2.core.Response) msg).getAssertions()) {
                    final String id = apply(assertion);
                    if (id != null) {
                        return id;
                    }
                }
                
            } else if (msg instanceof LogoutRequest) {
                
                if (((LogoutRequest) msg).getNameID() != null) {
                    return ((LogoutRequest) msg).getNameID().getValue();
                }
                
            } else if (msg instanceof AuthnRequest) {
                if (((AuthnRequest) msg).getSubject() != null &&
                        ((AuthnRequest) msg).getSubject().getNameID() != null) {
                    return ((AuthnRequest) msg).getSubject().getNameID().getValue();
                }
                
            } else if (msg instanceof org.opensaml.saml.saml1.core.Response) {

                for (final org.opensaml.saml.saml1.core.Assertion assertion
                        : ((org.opensaml.saml.saml1.core.Response) msg).getAssertions()) {
                    final String id = apply(assertion);
                    if (id != null) {
                        return id;
                    }
                }
                
            } else if (msg instanceof org.opensaml.saml.saml2.core.Assertion) {
                return apply((org.opensaml.saml.saml2.core.Assertion) msg);
            } else if (msg instanceof org.opensaml.saml.saml1.core.Assertion) {
                return apply((org.opensaml.saml.saml1.core.Assertion) msg);
            }
        }
        
        return null;
    }
// Checkstyle: CyclomaticComplexity ON

    /**
     * Apply function to an assertion.
     * 
     * @param assertion assertion to operate on
     * 
     * @return the identifier, or null
     */
    @Nullable private String apply(@Nonnull final org.opensaml.saml.saml2.core.Assertion assertion) {
        if (assertion.getSubject() != null && assertion.getSubject().getNameID() != null) {
            return assertion.getSubject().getNameID().getValue();
        }
        return null;
    }

// Checkstyle: CyclomaticComplexity OFF
    /**
     * Apply function to an assertion.
     * 
     * @param assertion assertion to operate on
     * 
     * @return the identifier, or null
     */
    @Nullable private String apply(@Nonnull final org.opensaml.saml.saml1.core.Assertion assertion) {

        for (final AuthenticationStatement statement : assertion.getAuthenticationStatements()) {
            if (statement.getSubject() != null && statement.getSubject().getNameIdentifier() != null) {
                return statement.getSubject().getNameIdentifier().getValue();
            }
        }
        for (final AttributeStatement statement : assertion.getAttributeStatements()) {
            if (statement.getSubject() != null && statement.getSubject().getNameIdentifier() != null) {
                return statement.getSubject().getNameIdentifier().getValue();
            }
        }
        for (final AuthorizationDecisionStatement statement
                : assertion.getAuthorizationDecisionStatements()) {
            if (statement.getSubject() != null && statement.getSubject().getNameIdentifier() != null) {
                return statement.getSubject().getNameIdentifier().getValue();
            }
        }
        for (final SubjectStatement statement : assertion.getSubjectStatements()) {
            if (statement.getSubject() != null && statement.getSubject().getNameIdentifier() != null) {
                return statement.getSubject().getNameIdentifier().getValue();
            }
        }
        
        return null;
    }
// Checkstyle: CyclomaticComplexity ON

}