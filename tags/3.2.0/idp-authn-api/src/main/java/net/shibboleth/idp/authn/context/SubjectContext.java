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

package net.shibboleth.idp.authn.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

import org.opensaml.messaging.context.BaseContext;

import com.google.common.collect.ImmutableList;

/**
 * A {@link BaseContext} that holds information about the subject of a transaction.
 * 
 * <p>The subject may or may not be authenticated, such as in a back-channel profile, but
 * profiles that operate on subjects can treat the information as "trusted" for their purposes.
 * This context must not be used to carry speculative or unverified subject information.</p>
 */
public class SubjectContext extends BaseContext {

    /** Canonical principal name of subject. */
    @Nullable private String principalName;

    /** The active authentication results for the subject. */
    @Nonnull private final Map<String, AuthenticationResult> authenticationResults;
    
    /** Constructor. */
    public SubjectContext() {
        authenticationResults = new HashMap<>(2);
    }

    /**
     * Get the canonical principal name of the subject.
     * 
     * @return the canonical principal name
     */
    @Nullable public String getPrincipalName() {
        return principalName;
    }

    /**
     * Set the canonical principal name of the subject.
     * 
     * @param name the canonical principal name
     * 
     * @return this context
     */
    @Nonnull public SubjectContext setPrincipalName(@Nullable final String name) {
        principalName = name;
        
        return this;
    }

    /**
     * Get a mutable map of authentication flow IDs to authentication results.
     * 
     * @return  mutable map of authentication flow IDs to authentication results
     */
    @Nonnull @NonnullElements public Map<String, AuthenticationResult> getAuthenticationResults() {
        return authenticationResults;
    }
    
    /**
     * Get an immutable list of Subjects extracted from every AuthenticationResult
     * associated with the context.
     * 
     * @return immutable list of Subjects 
     */
    @Nonnull @Unmodifiable @NonnullElements public List<Subject> getSubjects() {
        List<Subject> composite = new ArrayList<>();
        for (final AuthenticationResult e : getAuthenticationResults().values()) {
            composite.add(e.getSubject());
        }
        return ImmutableList.copyOf(composite);
    }
    
}