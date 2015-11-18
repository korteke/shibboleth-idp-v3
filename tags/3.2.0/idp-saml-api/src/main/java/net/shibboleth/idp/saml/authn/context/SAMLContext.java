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

package net.shibboleth.idp.saml.authn.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.saml.saml2.core.Assertion;

/**
 * Context, usually attached to {@link net.shibboleth.idp.authn.context.AuthenticationContext},
 * that carries a SAML {@link Assertion} to be validated.
 */
public class SAMLContext extends BaseContext {

    /** The assertion to be validated. */
    @Nullable private Assertion assertion;

    /**
     * Gets the assertion to be validated.
     * 
     * @return the assertion to be validated
     */
    @Nullable public Assertion getAssertion() {
        return assertion;
    }

    /**
     * Sets the assertion to be validated.
     * 
     * @param newAssertion assertion to be validated
     * 
     * @return this context
     */
    @Nonnull public SAMLContext setAssertion(@Nullable final Assertion newAssertion) {
        assertion = newAssertion;
        return this;
    }
    
}