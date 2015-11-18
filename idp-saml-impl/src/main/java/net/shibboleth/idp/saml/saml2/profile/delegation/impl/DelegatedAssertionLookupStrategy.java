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

package net.shibboleth.idp.saml.saml2.profile.delegation.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.saml.saml2.profile.delegation.LibertySSOSContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Lookup function to return the valid delegated assertion token in effect for the Liberty SSOS request.
 */
public class DelegatedAssertionLookupStrategy implements Function<ProfileRequestContext, Assertion> {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(DelegatedAssertionLookupStrategy.class);
    
    /** Function used to resolve the Liberty context to populate. */
    @Nonnull private Function<ProfileRequestContext, LibertySSOSContext> libertyContextLookupStrategy;
    
    /** Constructor. */
    public DelegatedAssertionLookupStrategy() {
        libertyContextLookupStrategy = new ChildContextLookup<>(LibertySSOSContext.class);
    }

    /**
     * Constructor.
     *
     * @param strategy the lookup strategy for {@link LibertySSOSContext}.
     */
    public DelegatedAssertionLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, LibertySSOSContext> strategy) {
        libertyContextLookupStrategy = Constraint.isNotNull(strategy, "Liberty context lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nullable
    public Assertion apply(@Nullable ProfileRequestContext input) {
        if (input == null) {
            return null;
        }
        
        LibertySSOSContext libertyContext = libertyContextLookupStrategy.apply(input);
        if (libertyContext == null || libertyContext.getAttestedToken() == null) {
            log.debug("No attested token available from Liberty context");
            return null;
        }
        return libertyContext.getAttestedToken();
    }

}
