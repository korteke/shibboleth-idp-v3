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

package net.shibboleth.idp.saml.nameid.impl;

import javax.annotation.Nonnull;

import org.opensaml.saml.common.SAMLException;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * Generates and manages persistent identifiers according to specific strategies.
 */
public interface PersistentIdGenerationStrategy {

    /**
     * Get a Persistent ID that corresponds to the inputs.
     * 
     * <p>This may be generated directly from the inputs or retrieved from some other source.</p>
     * 
     * @param assertingPartyId  the asserting party providing the identifier
     * @param relyingPartyId    the relying party for whom we're obtaining the identifier
     * @param principalName     name of the subject
     * @param sourceId          an underlying identifier for the subject
     * 
     * @return  the identifier
     * @throws SAMLException if an error occurs generating the identifier
     */
    @Nonnull @NotEmpty String generate(@Nonnull @NotEmpty final String assertingPartyId,
            @Nonnull @NotEmpty final String relyingPartyId, @Nonnull @NotEmpty final String principalName,
            @Nonnull @NotEmpty final String sourceId) throws SAMLException;

}