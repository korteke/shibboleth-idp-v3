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

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.saml.saml2.core.Assertion;

/**
 * Context for storing information related to the Liberty SSOS profile and use of an inbound delegated
 * {@link Assertion} token.
 */
public class LibertySSOSContext extends BaseContext {

    /** The SAML 2 Assertion which serves as the authentication token for the AuthnRequest and
     * has been successfully attested by the AuthnRequest presenter. */
    private Assertion attestedToken;
    
    /** Get the confirmation method that was successfully used by the attesting entity. */
    private String attestedSubjectConfirmationMethod;
    
    /**
     * Get the SAML 2 Assertion which serves as the authentication token for the AuthnRequest and
     * which has been successfully attested by the AuthnRequest presenter.
     * 
     * @return Returns the attestedToken.
     */
    public Assertion getAttestedToken() {
        return attestedToken;
    }

    /**
     * Set the SAML 2 Assertion which serves as the authentication token for the AuthnRequest and
     * which has been successfully attested by the AuthnRequest presenter.
     * 
     * @param newAttestedToken The attestedToken to set.
     */
    public void setAttestedToken(Assertion newAttestedToken) {
        attestedToken = newAttestedToken;
    }
    
    /**
     * Get the SAML 2 SubjectConfirmation method which was used by the presenter in
     * the attestation of the authentication token.
     * 
     * @return Returns the attestedSubjectConfirmationMethod.
     */
    public String getAttestedSubjectConfirmationMethod() {
        return attestedSubjectConfirmationMethod;
    }

    /**
     * Set the SAML 2 SubjectConfirmation method which was used by the presenter in
     * the attestation of the authentication token.
     * 
     * @param newAttestedSubjectConfirmationMethod The attestedSubjectConfirmationMethod to set.
     */
    public void setAttestedSubjectConfirmationMethod(String newAttestedSubjectConfirmationMethod) {
        attestedSubjectConfirmationMethod = newAttestedSubjectConfirmationMethod;
    }

}
