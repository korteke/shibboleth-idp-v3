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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.authn.SubjectCanonicalizationFlowDescriptor;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;

/**
 * A {@link BaseContext} that holds an input {@link Subject} to canonicalize into a
 * principal name.
 */
public class SubjectCanonicalizationContext extends BaseContext {

    /** Subject to canonicalize. */
    @Nonnull private Subject subject;
    
    /** Canonical principal name of subject. */
    @Nullable private String principalName;
    
    /** Requester ID.*/
    @Nullable private String requesterId;
    
    /** Responder ID.*/
    @Nullable private String responderId;

    /** Flows that could potentially be used. */
    @Nonnull @NonnullElements private final Map<String, SubjectCanonicalizationFlowDescriptor> potentialFlows;

    /** Previously attempted flows (could be failures or intermediate results). */
    @Nonnull @NonnullElements private final Map<String, SubjectCanonicalizationFlowDescriptor> intermediateFlows;
    
    /** The last c14 flow attempted. */
    @Nullable private SubjectCanonicalizationFlowDescriptor attemptedFlow;
    
    /** Exception raised by a failed canonicalization. */
    @Nullable private Exception canonicalizationError;

    /** Constructor. */
    public SubjectCanonicalizationContext() {
        potentialFlows = new LinkedHashMap<>();
        intermediateFlows = new HashMap<>();
    }
    
    /**
     * Get the {@link Subject} to canonicalize.
     * 
     * @return Subject to canonicalize
     */
    @Nullable public Subject getSubject() {
        return subject;
    }
    
    /**
     * Set the {@link Subject} to canonicalize.
     * 
     * @param newSubject Subject to canonicalize
     * 
     * @return this context
     */
    @Nonnull public SubjectCanonicalizationContext setSubject(@Nullable final Subject newSubject) {
        subject = newSubject;
        return this;
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
    @Nonnull public SubjectCanonicalizationContext setPrincipalName(@Nullable final String name) {
        principalName = StringSupport.trimOrNull(name);
        return this;
    }
    
    /**
     * Get the exception raised by a failed canonicalization.
     * 
     * @return  exception raised by a failed canonicalization
     */
    @Nullable public Exception getException() {
        return canonicalizationError;
    }
    
    /**
     * Set the exception raised by a failed canonicalization.
     * 
     * @param e  exception raised by a failed canonicalization
     * 
     * @return this context
     */
    @Nonnull public SubjectCanonicalizationContext setException(@Nullable final Exception e) {
        canonicalizationError = e;
        return this;
    }

    /**
     * Get the requester's ID.
     * @return the requester's ID
     */
    @Nullable public String getRequesterId() {
        return requesterId;
    }

    /**
     * Set the requester's ID.
     * 
     * @param id the requester's ID
     * 
     * @return this context
     */
    @Nonnull public SubjectCanonicalizationContext setRequesterId(@Nullable final String id) {
        requesterId = id;
        return this;
    }

    /**
     * Get the responder's ID.
     * 
     * @return the responder's ID
     */
    @Nullable public String getResponderId() {
        return responderId;
    }

    /**
     * Set the responder's ID.
     * 
     * @param id the responder's ID
     * 
     * @return this context
     */
    @Nonnull public SubjectCanonicalizationContext setResponderId(@Nullable final String id) {
        responderId = id;
        return this;
    }
    
    /**
     * Get the set of flows that could potentially be used for subject canonicalization.
     * 
     * @return the potential flows
     */
    @Nonnull @NonnullElements @Live public Map<String, SubjectCanonicalizationFlowDescriptor> getPotentialFlows() {
        return potentialFlows;
    }

    
    /**
     * Get the set of flows that have been executed, successfully or otherwise, without producing a completed result.
     * 
     * @return the intermediately executed flows
     */
    @Nonnull @NonnullElements @Live public Map<String, SubjectCanonicalizationFlowDescriptor> getIntermediateFlows() {
        return intermediateFlows;
    }

    /**
     * Get the last flow that was attempted for subject c14n.
     * 
     * @return last flow that was attempted for subject c14n
     */
    @Nullable public SubjectCanonicalizationFlowDescriptor getAttemptedFlow() {
        return attemptedFlow;
    }

    /**
     * Set the last flow that was attempted for subject c14n.
     * 
     * @param flow last flow that was attempted for subject c14n
     * 
     * @return this context
     */
    @Nonnull public SubjectCanonicalizationContext setAttemptedFlow(
            @Nullable final SubjectCanonicalizationFlowDescriptor flow) {
        attemptedFlow = flow;
        return this;
    }

}