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

package net.shibboleth.idp.consent.flow.impl;

import java.util.Map;

import javax.annotation.Nonnull;

import net.shibboleth.idp.consent.impl.Consent;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Consent action which populates the current consents of a consent context with the output value of a function whose
 * input value is a profile request context.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post See above.
 */
public class PopulateConsentContext extends AbstractConsentAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(PopulateConsentContext.class);

    /** Function which returns the current consents. */
    @Nonnull private Function<ProfileRequestContext, Map<String, Consent>> function;

    /**
     * Constructor.
     *
     * @param currentConsentsFunction function which returns the current consents
     */
    public PopulateConsentContext(
            @Nonnull final Function<ProfileRequestContext, Map<String, Consent>> currentConsentsFunction) {
        function = Constraint.isNotNull(currentConsentsFunction, "Current consents function cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {
        
        final Map<String,Consent> consents = function.apply(profileRequestContext);
        
        log.debug("{} Populating consents: {}", getLogPrefix(), consents.keySet());
        
        if (consents != null) {
            getConsentContext().getCurrentConsents().putAll(consents);
        }
    }
    
}