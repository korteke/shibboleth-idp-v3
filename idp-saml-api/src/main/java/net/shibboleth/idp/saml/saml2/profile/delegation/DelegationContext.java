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

package net.shibboleth.idp.saml.saml2.profile.delegation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.security.credential.Credential;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Context which holds data relevant to the issuance of a delegated {@link org.opensaml.saml.saml2.core.Assertion}.
 */
public class DelegationContext extends BaseContext {
    
    /** Whether issuance of a delegated assertion is active. */
    private boolean issuingDelegatedAssertion;
    
    /** Status of whether the relying party has requested issuance of a delegated assertion token. */
    private DelegationRequest delegationRequested;
    
    /** The relying party credentials which will be included in the assertion's {@link KeyInfoConfirmationDataType}. */
    private List<Credential> subjectConfirmationCredentials;
    
    
    /** Constructor. */
    public DelegationContext() {
        delegationRequested = DelegationRequest.NOT_REQUESTED;
    }

    /**
     * Get whether issuance of a delegated assertion is active.
     * 
     * @return true if issuing a delegated assertion, false otherwise
     */
    public boolean isIssuingDelegatedAssertion() {
        return issuingDelegatedAssertion;
    }

    /**
     * Set whether issuance of a delegated assertion is active.
     * 
     * @param flag true is issuing a delegated assertion false otherwise
     */
    public void setIssuingDelegatedAssertion(boolean flag) {
        issuingDelegatedAssertion = flag;
    }
    
    /**
     * Get the status of whether the relying party has requested issuance of a delegated assertion token.
     * 
     * @return the delegation request status
     */
    @Nonnull public DelegationRequest getDelegationRequested() {
        return delegationRequested;
    }

    /**
     * Set the status of whether the relying party has requested issuance of a delegated assertion token.
     * 
     * @param requested the delegation request status
     */
    public void setDelegationRequested(DelegationRequest requested) {
        delegationRequested = Constraint.isNotNull(requested, "DelegationRequest was null");
    }

    /**
     * Get the relying party credentials which will be included in the assertion's {@link KeyInfoConfirmationDataType}.
     * 
     * @return the confirmation credentials, or null
     */
    @Nullable @NonnullElements public List<Credential> getSubjectConfirmationCredentials() {
        return subjectConfirmationCredentials;
    }

    /**
     * Set the relying party credentials which will be included in the assertion's {@link KeyInfoConfirmationDataType}.
     * 
     * @param credentials the confirmation credentials
     */
    public void setSubjectConfirmationCredentials(
            @Nullable @NonnullElements final List<Credential> credentials) {
        if (credentials == null) {
            subjectConfirmationCredentials = null;
        } else {
            subjectConfirmationCredentials = new ArrayList<>(Collections2.filter(credentials, Predicates.notNull()));
        }
    }

}
